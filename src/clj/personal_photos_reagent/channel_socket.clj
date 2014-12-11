(ns personal-photos-reagent.channel-socket
  (:require
    [clojure.core.async :as async]
    [com.stuartsierra.component :as component]
    [taoensso.sente :as sente]))

(defn- stop-channel-socket
  [socket]
  (let [{:keys [ch-recv]} socket]
    (async/close! ch-recv)))

(defrecord ChannelSocket [channel-socket]
  component/Lifecycle
  
  (start [this]
    (if channel-socket
      this
      (do
        (println ";; Starting ChannelSocket")
        (assoc this :channel-socket (sente/make-channel-socket! {:packer :edn})))))
  
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

