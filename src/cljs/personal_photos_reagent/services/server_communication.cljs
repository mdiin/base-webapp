(ns personal-photos-reagent.services.server-communication
  (:require
    [taoensso.sente :as sente]))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk" {:type :auto})]
  (def chsk chsk)
  (def ch-recv ch-recv)
  (def send-fn send-fn)
  (def state state))

