(ns personal-photos-reagent.events.client
  (:require-macros
    [cljs.core.async.macros :as async-macro :refer [go]])
  (:require
    [cljs.core.async :as async]
    [clojure.set :as s :refer [difference]]
    
    [personal-photos-reagent.events :as events :refer [client-event]]
    [personal-photos-reagent.events.types :as event-types]
    [personal-photos-reagent.services.server-communication :as server-comm]
    [personal-photos-reagent.comps.state :as state :refer [app-state]]))

;; # State watchers

(defn load-user-event
  [uid]
  (when (:open? @server-comm/state)
    (events/publish-server-event
      :id event-types/user
      :payload {:uid uid}
      :?reply-fn (fn [user]
                   (println "Got reply: " user)
                   (reset! (app-state :current-user) user)))))

(defn- state-change-handler
  [key state old-state new-state]
  (let [old-uid (:uid old-state)
        new-uid (:uid new-state)]
    (println new-uid)
    (when-not (or
                (= old-uid new-uid)
                (= new-uid :taoensso.sente/nil-uid))
      (if new-uid
        (load-user-event new-uid)
        (reset! (app-state :current-user) nil)))))

(add-watch server-comm/state :uid state-change-handler)

;; ### Default handler

(defmethod client-event :default
  [{:as event :keys [id payload]}]
  (println (str "Unhandled event: " id)))

