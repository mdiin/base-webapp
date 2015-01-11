(ns personal-photos-reagent.events.client
  (:require-macros
    [cljs.core.async.macros :as async-macro :refer [go]])
  (:require
    [cljs.core.async :as async]
    [clojure.set :as s :refer [difference]]
    
    [personal-photos-reagent.events :as events :refer [client-event]]
    [personal-photos-reagent.events.types.client :as client-events]
    [personal-photos-reagent.events.types.server :as server-events]
    [personal-photos-reagent.services.server-communication :as server-comm]
    [personal-photos-reagent.comps.state :as state :refer [app-state]]))

;; # State watchers

(defn load-user-event
  [uid]
  (when (:open? @server-comm/state)
    (events/publish-server-event
      :id server-events/user
      :payload {:uid uid}
      :?reply-fn (fn [user]
                   (println "Got reply: " user)
                   (reset! (app-state :current-user) user)

                   (when (seq user)
                     (println "Populating state.")
                     (events/publish-server-event
                       :id server-events/albums
                       :?reply-fn (fn [albums]
                                    (println "Albums: " albums)
                                    (reset! (app-state :albums) albums)))
                     (events/publish-server-event
                       :id server-events/pictures
                       :?reply-fn (fn [pictures]
                                    (println "Pictures: " pictures)
                                    (reset! (app-state :pictures) pictures))))))))

(defn- state-change-handler
  [key state old-state new-state]
  (let [old-uid (:uid old-state)
        new-uid (:uid new-state)]
    (when-not (= old-uid new-uid)
      (if new-uid
        (load-user-event new-uid)
        (reset! (app-state :current-user) nil)))))

(add-watch server-comm/state :uid state-change-handler)

;; ### Default handler

(defmethod client-event :default
  [{:as event :keys [id payload]}]
  (println (str "Unhandled event: " id)))

