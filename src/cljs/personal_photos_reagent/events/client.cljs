(ns personal-photos-reagent.events.client
  (:require-macros
    [cljs.core.async.macros :as async-macro :refer [go]])
  (:require
    [cljs.core.async :as async]
    [clojure.set :as s :refer [difference]]
    
    [personal-photos-reagent.events :as events :refer [client-event]]
    [personal-photos-reagent.events.types.client :as client-events]
    [personal-photos-reagent.events.types.server :as server-events]
    [personal-photos-reagent.services.server-communication :as server-comm]
    [personal-photos-reagent.comps.state :as state :refer [app-state]]))

;; # State watchers

(defn load-user-event
  [uid]
  (events/publish-server-event
    :id server-events/user
    :payload {:uid uid}
    :?reply-fn (fn [reply]
                 (println "Got reply: " reply)
                 (events/publish-client-event
                   :id client-events/user-changed
                   :payload reply)
                 (when (seq reply)
                   (println "Populating state.")
                   (events/publish-server-event
                     :id server-events/albums
                     :?reply-fn (fn [albums]
                                  (events/publish-client-event
                                    :id client-events/albums-changed
                                    :payload albums)))
                   (events/publish-server-event
                     :id server-events/pictures
                     :?reply-fn (fn [pictures]
                                  (events/publish-client-event
                                    :id client-events/pictures-changed
                                    :payload pictures)))))))

(defn- state-change-handler
  [key state old-state new-state]
  (let [old-uid (:uid old-state)
        new-uid (:uid new-state)]
    (when-not (= old-uid new-uid)
      (load-user-event new-uid))))

(add-watch server-comm/state :uid state-change-handler)

;; ### Default handler

(defmethod client-event :default
  [{:as event :keys [id payload]}]
  (println (str "Unhandled event: " id)))

;; ### Select pictures

(defn- toggle-select-picture
  [pictures-state picture-id album-name]
  (let [existing-selections (get-in pictures-state [picture-id :selected])]
    (if (contains? existing-selections album-name)
      (update-in pictures-state [picture-id :selected] disj album-name)
      (update-in pictures-state [picture-id :selected] conj album-name))))

(defmethod client-event client-events/select-picture
  [{:as event :keys [_ payload]}]
  (let [current-pictures-state @(app-state :pictures)
        picture-id-to-select (get-in payload [:picture :id])
        album-to-select-picture-in (get payload :album)
        new-pictures-state (toggle-select-picture current-pictures-state picture-id-to-select album-to-select-picture-in)]
    (compare-and-set! (app-state :pictures) current-pictures-state new-pictures-state)))

;; ### Deselect pictures

(defn- deselect-picture
  [[pic-id pic]]
  [pic-id (assoc pic :selected #{})])

(defn- deselect-pictures
  [ps]
  (into {}
        (map deselect-picture ps)))

(defmethod client-event client-events/deselect-pictures
  [_]
  (let [current-pictures-state @(app-state :pictures)
        new-pictures-state (deselect-pictures current-pictures-state)]
    (compare-and-set! (app-state :pictures) current-pictures-state new-pictures-state)))

;; ### Pictures changed

(defmethod client-event client-events/pictures-changed
  [_]
  (let [current-picture-state @(app-state :pictures)
        current-album-state @(app-state :albums)
        pictures (vals current-picture-state)
        unsorted-pictures-ids {"No album" (map :id (remove (comp seq :albums) pictures))}
        album-pictures-ids (apply merge-with concat (mapcat #(for [a (:albums %)] 
                                                               {a [(:id %)]})
                                                            pictures))
        new-album-state (merge album-pictures-ids unsorted-pictures-ids)]
    (compare-and-set! (app-state :albums) current-album-state new-album-state)))

;; ### Select album
(defn- add-album-to-picture
  [album picture]
  (update-in picture [:albums] conj album))

(defn- add-pictures-to-album
  [pictures-map album]
  (let [pictures (vals pictures-map)
        selected-pictures (filter (comp seq :selected) pictures)
        with-album (map (partial add-album-to-picture album) selected-pictures)
        with-album-map (into {} (map #(vector (:id %) %) with-album))]
    (merge pictures-map with-album-map)))

(defmethod client-event client-events/select-album
  [{:as event :keys [payload]}]
  (let [album payload
        current-picture-state @(app-state :pictures)
        with-new-albums (add-pictures-to-album current-picture-state album)]
    (when (compare-and-set! (app-state :pictures) current-picture-state with-new-albums)
      (events/publish-client-event :id client-events/pictures-changed))))

;; ### Albums changed

(defmethod client-event client-events/albums-changed
  [{:as event :keys [payload]}]
  (let [albums payload]
    (reset! (app-state :albums) albums)))

;; ### View album

(defmethod client-event client-events/view-album
  [{:as event :keys [payload]}]
  (let [album (get event :payload)
        current-album-state @(app-state :visible-album)]
    (compare-and-set! (app-state :visible-album) current-album-state album)))

;; ### Remove from album
(defn- remove-selected-albums-from-picture
  [picture]
  (-> picture
      (update-in [:albums] difference (:selected picture))
      (assoc :selected #{})))

(defn- remove-selected-pictures-from-album
  [pictures-map]
  (let [pictures (vals pictures-map)
        selected-pictures (filter (comp seq :selected) pictures)
        removed-albums (map remove-selected-albums-from-picture selected-pictures)
        removed-albums-as-map (into {} (map #(vector (:id %) %) removed-albums))]
    (merge pictures-map removed-albums-as-map)))

(defmethod client-event client-events/remove-from-album
  [_]
  (let [current-picture-state @(app-state :pictures)
        removed-albums (remove-selected-pictures-from-album current-picture-state)]
    (when (compare-and-set! (app-state :pictures) current-picture-state removed-albums)
      (events/publish-client-event :id client-events/pictures-changed))))

;; ### Mode change
(defn- new-mode
  [current-mode event-mode]
  (if (= event-mode current-mode)
    nil
    event-mode))

(defmethod client-event client-events/mode-change
  [{:as event :keys [payload]}]
  (let [event-mode (get payload :mode)
        current-mode @(app-state :mode)
        new-mode (new-mode current-mode event-mode)]
    (compare-and-set! (app-state :mode) current-mode new-mode)))

;; ### Sign-in

(defmethod client-event client-events/sign-in
  [{:as event :keys [payload]}]
  (let [username (get-in event [:payload :username])
        password (get-in event [:payload :password])]
    (server-comm/send-ajax "/login"
                           :post
                           {:username username :password password}
                           (fn [response]
                             (do
                               (server-comm/reconnect!))))))

;; ### Sign-out

(defmethod client-event client-events/sign-out
  [_]
  (server-comm/send-ajax "/logout"
                         :get
                         {}
                         (fn [response]
                           (do
                             (server-comm/reconnect!)))))

;; ### User changed

(defmethod client-event client-events/user-changed
  [{:as event :keys [payload]}]
  (reset! (app-state :current-user) payload))

;; ### Read state

(defmethod client-event client-events/read-state
  [_]
  (let [state @server-comm/state]
    (.log js/console state)))

