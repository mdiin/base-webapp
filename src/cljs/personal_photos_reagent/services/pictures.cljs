(ns personal-photos-reagent.services.pictures
  (:require
    [personal-photos-reagent.comps.state :as state :refer [app-state]]
    [personal-photos-reagent.events :as events :refer [publish-event]]))

(defn add-pictures
  [ps]
  (doseq [p ps]
    (swap! (app-state :pictures) assoc (:id p) p))
  (publish-event :event :pictures-changed :payload (vals @(app-state :pictures))))

