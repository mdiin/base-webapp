(ns personal-photos-reagent.handlers.all
  (:require
    [personal-photos-reagent.data :as data]
    [personal-photos-reagent.handlers.base :refer (logf event-msg-handler)]))

(defmethod event-msg-handler :default ; Fallback
  [{:as ev-msg :keys  [event id ?data ring-req ?reply-fn send-fn]}]
  (let  [session  (:session ring-req)
         uid  (:uid session)]
    (logf  "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn  {:umatched-event-as-echoed-from-from-server event}))))

(defmethod event-msg-handler :app/an-event
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    (logf "app/an-event received: %s. %s" event ring-req)))

(defmethod event-msg-handler :server/user
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [uid (:uid ?data)]
    (when ?reply-fn
      (?reply-fn
        (data/user uid)))))

