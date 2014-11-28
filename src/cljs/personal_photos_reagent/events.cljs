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
(defn- process-select-picture-events []
  (go
    (while true
      (let [event (async/<! select-picture-chan)
            pic-id (get-in event [:payload :picture :id])
            album-name (get-in event [:payload :album])
            existing-selections (get-in @(app-state :pictures) [pic-id :selected])]
        (if (contains? existing-selections album-name)
          (swap! (app-state :pictures) update-in [pic-id :selected] disj album-name)
          (swap! (app-state :pictures) update-in [pic-id :selected] conj album-name))))))
(process-select-picture-events)

(defonce deselect-pictures-chan (async/chan))
(async/sub event-publisher :deselect-pictures deselect-pictures-chan)
(defn- deselect-pictures-event
  [e]
  (letfn [(deselect-picture
            [[pic-id pic]]
            [pic-id (assoc pic :selected #{})])
          (deselect-pictures
            [ps]
            (into {}
                  (map deselect-picture ps)))]
    (swap! (app-state :pictures) deselect-pictures)))

(defn- process-deselect-pictures-events []
  (go
    (while true
      (let [event (async/<! deselect-pictures-chan)]
        (deselect-pictures-event event)))))
(process-deselect-pictures-events)

(defonce pictures-changed-chan (async/chan))
(async/sub event-publisher :pictures-changed pictures-changed-chan)
(defn- process-pictures-changed-events []
  (go
    (while true
      (let [event (async/<! pictures-changed-chan)
            pictures (get event :payload)
            unsorted-pictures {"No album"
                               (map :id 
                                    (remove (comp seq :albums)
                                            pictures))}
            pictures-albums (apply merge-with concat (mapcat #(for [a (:albums %)] 
                                                                {a [(:id %)]})
                                                             pictures))
            albums (merge pictures-albums unsorted-pictures)]
        (reset! (app-state :albums) albums)))))
(process-pictures-changed-events)

;; ## Select album events
(defonce select-album-chan (async/chan))
(async/sub event-publisher :select-album select-album-chan)

(defn- add-pictures-to-album
  [pictures album]
  ())

(defn- select-album-event
  [e]
  (let [album (get e :payload)
        pictures @(app-state :pictures)
        ps (vals pictures)
        selected-pictures (filter (comp seq :selected) ps)]
    (doseq [p selected-pictures]
      (swap! (app-state :pictures) update-in [(:id p) :albums] conj album))
    (publish-event :event :pictures-changed :payload (vals @(app-state :pictures)))))

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

