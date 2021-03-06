(ns personal-photos-reagent.events.server
  (:require-macros
    [cljs.core.async.macros :as async-macro :refer [go]])
  (:require
    [cljs.core.async :as async]
    
    [personal-photos-reagent.events :as events :refer [server-event]]
    [personal-photos-reagent.events.types :as event-types]
    [personal-photos-reagent.services.server-communication :as server-comm]
    [personal-photos-reagent.comps.state :as state :refer [app-state]]))

;; ### Default handler

(defmethod server-event :default
  [event]
  (println (str "Unhandled event " (:id event))))

(defmethod server-event event-types/state-change
  [_]
  (println "State changed"))

(defmethod server-event event-types/user
  [event]
  (println "Ack"))

