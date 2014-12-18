(ns personal-photos-reagent.channel-socket
  (:require
    [clojure.core.async :as async]
    [com.stuartsierra.component :as component]
    [taoensso.sente :as sente]
    [cemerick.friend :as friend]))

(defn- stop-channel-socket
  [socket]
  (let [{:keys [ch-recv]} socket]
    (async/close! ch-recv)))

(defn- start-channel-socket []
  (sente/make-channel-socket!
    {:packer :edn
     :user-id-fn (fn [ring-req]
                   (:identity (friend/current-authentication ring-req)))}))

(defrecord ChannelSocket [channel-socket]
  component/Lifecycle
  
  (start [this]
    (if channel-socket
      this
      (do
        (println ";; Starting ChannelSocket")
        (assoc this :channel-socket (start-channel-socket)))))
  
  (stop [this]
    (if-not channel-socket
      this
      (do
        (println ";; Stopping ChannelSocket")
        (stop-channel-socket channel-socket)
        (dissoc this :channel-socket)))))

(defn make-channel-socket
  []
  (map->ChannelSocket {}))

