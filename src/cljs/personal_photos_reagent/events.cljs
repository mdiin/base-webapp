(ns personal-photos-reagent.events
  (:require-macros
    [cljs.core.async.macros :as async-macro :refer [go]])
  (:require
    [cljs.core.async :as async]

    [personal-photos-reagent.services.server-communication :as server-comm]))

;; # Client-side

(defonce events (async/chan))

(defn publish-client-event
  [& {:keys [id payload] :as event-map}]
  (async/put! events event-map))

(defmulti client-event :id)
(defn- client-event*
  [event]
  (println (str "Event: " event))
  (client-event event))

(defn- process-client-events []
  (go
    (while true
      (let [event (async/<! events)]
        (client-event* event)))))

(process-client-events)

;; # Server-side

(defn publish-server-event
  [& {:keys [id payload ?timeout ?reply-fn] :as event-map}]
  (if ?reply-fn
    (server-comm/send-fn [id payload] (or ?timeout 10000) ?reply-fn)
    (server-comm/send-fn [id payload])))

(defmulti server-event :id)
(defn- server-event*
  [event]
  (println (str "Event: " event))
  (server-event event))

(defn- process-server-events []
  (go
    (while true
      (let [event (async/<! server-comm/ch-recv)]
        (server-event* event)))))

(process-server-events)

