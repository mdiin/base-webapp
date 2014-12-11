(ns personal-photos-reagent.main
  (:require
    [com.stuartsierra.component :as component]
    
    [personal-photos-reagent.routes :as routes]
    [personal-photos-reagent.server :as server]
    [personal-photos-reagent.channel-socket :as channel-socket]))

(defn system
  [options]
  (let [{:keys [server-port]} options]
    (component/system-map
      :channel-socket (channel-socket/make-channel-socket)
      :ordinary-router (component/using
                         (routes/make-ordinary-router)
                         [:channel-socket])
      :channel-socket-router (component/using
                               (routes/make-channel-socket-router)
                               [:channel-socket])
      :server (component/using
                (server/make-server (or server-port 8091))
                [:ordinary-router :channel-socket-router]))))

(defn -main [& args]
  (component/start (system args)))

