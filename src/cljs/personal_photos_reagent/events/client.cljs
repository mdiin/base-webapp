(ns personal-photos-reagent.events.client
  (:require-macros
    [cljs.core.async.macros :as async-macro :refer [go]])
  (:require
    [cljs.core.async :as async]
    [clojure.set :as s :refer [difference]]
    
    [personal-photos-reagent.services.server-communication :as server-comm]
    [personal-photos-reagent.comps.state :as state :refer [app-state]]))

(defonce events (async/chan))

(defn publish-event
  [& {:keys [id payload] :as event-map}]
  (async/put! events event-map))

;; ## Handlers

(defmulti client-event :id)

(defn- process-client-events []
  (go
    (while true
      (let [event (async/<! events)]
        (client-event event)))))

(process-client-events)

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

(def select-picture :client/select-picture)

(defmethod client-event select-picture
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

(def deselect-pictures :client/deselect-pictures)

(defmethod client-event deselect-pictures
  [_]
  (let [current-pictures-state @(app-state :pictures)
        new-pictures-state (deselect-pictures current-pictures-state)]
    (compare-and-set! (app-state :pictures) current-pictures-state new-pictures-state)))

;; ### Pictures changed

(def pictures-changed :client/pictures-changed)

(defmethod client-event pictures-changed
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

(def select-album :client/select-album)

(defmethod client-event select-album
  [{:as event :keys [payload]}]
  (let [album payload
        current-picture-state @(app-state :pictures)
        with-new-albums (add-pictures-to-album current-picture-state album)]
    (when (compare-and-set! (app-state :pictures) current-picture-state with-new-albums)
      (publish-event :id pictures-changed))))

;; ### View album

(def view-album :client/view-album)

(defmethod client-event view-album
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

(def remove-from-album :client/remove-from-album)

(defmethod client-event remove-from-album
  [_]
  (let [current-picture-state @(app-state :pictures)
        removed-albums (remove-selected-pictures-from-album current-picture-state)]
    (when (compare-and-set! (app-state :pictures) current-picture-state removed-albums)
      (publish-event :id pictures-changed))))

;; ### Mode change
(defn- new-mode
  [current-mode event-mode]
  (if (= event-mode current-mode)
    nil
    event-mode))

(def mode-change :client/mode-change)

(defmethod client-event mode-change
  [{:as event :keys [payload]}]
  (let [event-mode (get payload :mode)
        current-mode @(app-state :mode)
        new-mode (new-mode current-mode event-mode)]
    (compare-and-set! (app-state :mode) current-mode new-mode)))

;; ### Sign-in

(def sign-in :client/sign-in)

(defmethod client-event sign-in
  [{:as event :keys [payload]}]
  (let [username (get-in event [:payload :username])
        password (get-in event [:payload :password])]
    (server-comm/send-ajax "/login"
                           :post
                           {:username username :password password}
                           (fn [response]
                             (do
                               (server-comm/reconnect!))))))

;; ### User changed

(def user-changed :client/user-changed)

(defmethod client-event user-changed
  [{:as event :keys [payload]}]
  (let [new-user (get payload :uid)]
    (reset! app-state :current-user new-user)))

;; ### Read state

(def read-state :client/read-state)

(defmethod client-event read-state
  [_]
  (let [state @server-comm/state]
    (.log js/console state)))

