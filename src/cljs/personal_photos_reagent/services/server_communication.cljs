(ns personal-photos-reagent.services.server-communication
  (:require-macros
    [cljs.core.async.macros :as async-macro :refer [go]])
  (:require
    [taoensso.sente :as sente]
    
    [cljs.core.async :as async]))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk" {:type :auto})]
  (def chsk chsk)
  (def ch-recv ch-recv)
  (def send-fn send-fn)
  (def state state))

(defn send-ajax
  [url method params callback]
  (let [state @state]
    (sente/ajax-call url
                     {:method method
                      :params params}
                     callback)))

(defn reconnect! []
  (sente/chsk-reconnect! chsk))

