(ns personal-photos-reagent.events
  (:require-macros
    [cljs.core.async.macros :as async-macro :refer [go]])
  (:require
    [cljs.core.async :as async]
    [clojure.set :as s :refer [difference]]

    [personal-photos-reagent.comps.state :as state :refer [app-state]]))

;; # Main entry point to NS
(defonce events (async/chan))
(defonce event-publisher (async/pub events :event))

(defn publish-event
  [& {:keys [event payload] :as event-map}]
  (async/put! events event-map))

;; # Picture events
(defonce select-picture-chan (async/chan))

(async/sub event-publisher :select-picture select-picture-chan)

(defn- toggle-select-picture
  [pictures-state picture-id album-name]
  (let [existing-selections (get-in pictures-state [picture-id :selected])]
    (if (contains? existing-selections album-name)
      (update-in pictures-state [picture-id :selected] disj album-name)
      (update-in pictures-state [picture-id :selected] conj album-name))))

(defn- select-picture-event
  [event]
  (let [current-pictures-state @(app-state :pictures)
        picture-id-to-select (get-in event [:payload :picture :id])
        album-to-select-picture-in (get-in event [:payload :album])
        new-pictures-state (toggle-select-picture current-pictures-state picture-id-to-select album-to-select-picture-in)]
    (compare-and-set! (app-state :pictures) current-pictures-state new-pictures-state)))

(defn- process-select-picture-events []
  (go
    (while true
      (let [event (async/<! select-picture-chan)]
        (select-picture-event event)))))

(process-select-picture-events)

;; ## Deselect pictures events

(defonce deselect-pictures-chan (async/chan))
(async/sub event-publisher :deselect-pictures deselect-pictures-chan)

(defn- deselect-picture
  [[pic-id pic]]
  [pic-id (assoc pic :selected #{})])

(defn- deselect-pictures
  [ps]
  (into {}
        (map deselect-picture ps)))

(defn- deselect-pictures-event
  [event]
  (let [current-pictures-state @(app-state :pictures)
        new-pictures-state (deselect-pictures current-pictures-state)]
    (compare-and-set! (app-state :pictures) current-pictures-state new-pictures-state)))

(defn- process-deselect-pictures-events []
  (go
    (while true
      (let [event (async/<! deselect-pictures-chan)]
        (deselect-pictures-event event)))))

(process-deselect-pictures-events)

;; ## Pictures changed events
(defonce pictures-changed-chan (async/chan))
(async/sub event-publisher :pictures-changed pictures-changed-chan)

(defn- pictures-changed-event
  [event]
  (let [current-picture-state @(app-state :pictures)
        current-album-state @(app-state :albums)
        pictures (vals current-picture-state)
        unsorted-pictures-ids {"No album" (map :id (remove (comp seq :albums) pictures))}
        album-pictures-ids (apply merge-with concat (mapcat #(for [a (:albums %)] 
                                                               {a [(:id %)]})
                                                            pictures))
        new-album-state (merge album-pictures-ids unsorted-pictures-ids)]
    (compare-and-set! (app-state :albums) current-album-state new-album-state)))

(defn- process-pictures-changed-events []
  (go
    (while true
      (let [event (async/<! pictures-changed-chan)]
        (pictures-changed-event event)))))

(process-pictures-changed-events)

;; ## Select album events
(defonce select-album-chan (async/chan))
(async/sub event-publisher :select-album select-album-chan)

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

(defn- select-album-event
  [event]
  (let [album (get event :payload)
        current-picture-state @(app-state :pictures)
        with-new-albums (add-pictures-to-album current-picture-state album)]
    (when (compare-and-set! (app-state :pictures) current-picture-state with-new-albums)
      (publish-event :event :pictures-changed))))

(defn- process-select-album-events []
  (go
    (while true
      (let [event (async/<! select-album-chan)]
        (select-album-event event)))))

(process-select-album-events)

;; ## Remove from album events

(defonce remove-from-album-chan (async/chan))
(async/sub event-publisher :remove-from-album remove-from-album-chan)

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

(defn- remove-from-album-event
  [event]
  (let [current-picture-state @(app-state :pictures)
        removed-albums (remove-selected-pictures-from-album current-picture-state)]
    (when (compare-and-set! (app-state :pictures) current-picture-state removed-albums)
      (publish-event :event :pictures-changed :payload (vals @(app-state :pictures))))))

(defn- process-remove-from-album-events []
  (go
    (while true
      (let [event (async/<! remove-from-album-chan)]
        (remove-from-album-event event)))))
(process-remove-from-album-events)


;; # Mode change events
(defonce mode-change-chan (async/chan))

(async/sub event-publisher :mode-change mode-change-chan)

(defn- new-mode
  [current-mode event-mode]
  (if (= event-mode current-mode)
    nil
    event-mode))

(defn- mode-change-event
  [event]
  (let [event-mode (get-in event [:payload :mode])
        current-mode @(app-state :mode)
        new-mode (new-mode current-mode event-mode)]
    (compare-and-set! (app-state :mode) current-mode new-mode)))

(defn- process-mode-change-events []
  (go
    (while true
      (let [event (async/<! mode-change-chan)]
        (mode-change-event event)))))
(process-mode-change-events)

