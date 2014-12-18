(ns personal-photos-reagent.events.server
  (:require-macros
    [cljs.core.async.macros :as async-macro :refer [go]])
  (:require
    [cljs.core.async :as async]
    
    [personal-photos-reagent.services.server-communication :as server-comm]
    [personal-photos-reagent.comps.state :as state :refer [app-state]]))

(defn publish-event
  [& {:keys [id payload] :as event-map}]
  (server-comm/send-fn [id payload]))

;; ## Handlers

(defmulti server-event :id)

(defn- process-server-events []
  (go
    (while true
      (let [event (async/<! server-comm/ch-recv)]
        (server-event event)))))

(process-server-events)

;; ### Default handler

(defmethod server-event :default
  [event]
  (println (str "Unhandled event " event)))

(defmethod server-event :chsk/state
  [_]
  (println "State changed"))

(defmethod server-event :sign-in-ack
  [event]
  (println "Ack"))

