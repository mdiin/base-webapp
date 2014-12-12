(ns personal-photos-reagent.handlers
  (:require
    [personal-photos-reagent.handlers.base :as base-handler]
    [personal-photos-reagent.handlers.all]))

(defn event-msg-handler* [{:as ev-msg :keys [id ?data event]}]
  (base-handler/logf "Event: %s" event)
  (base-handler/event-msg-handler ev-msg))

