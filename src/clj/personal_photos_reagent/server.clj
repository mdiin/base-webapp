(ns personal-photos-reagent.server
  (:require
    [com.stuartsierra.component :as component]

    [compojure.core :as compojure :refer (defroutes GET POST)]
    [compojure.route :as route]
    [ring.middleware.defaults]
    [ring.middleware.anti-forgery :as ring-anti-forgery]
    [org.httpkit.server :as http-kit-server]
    [taoensso.sente :as sente]
    
    [personal-photos-reagent.routes :as routes]
    [personal-photos-reagent.channel-socket :as channel-socket]
    
    [environment]))

;(defn- make-ring-handler
  ;[routes]
  ;(let [ring-defaults-config (assoc-in ring.middleware.defaults/site-defaults
                                       ;[:security :anti-forgery]
                                       ;{:read-token (fn [req] (-> req :params :csrf-token))})]
    ;(ring.middleware.defaults/wrap-defaults routes ring-defaults-config)))

(defn- start-server! [port routes]
  (http-kit-server/run-server (environment/make-ring-handler routes) {:port port}))

(defn- stop-server! [server]
  (when server
    (server :timeout 100)))

(defrecord Server [port ordinary-router channel-socket-router server]
  component/Lifecycle
  
  (start [this]
    (if server
      this
      (do
        (println ";; Starting Server")
        (assoc this :server (start-server! port (:routes ordinary-router))))))
  
  (stop [this]
    (if-not server
      this
      (do
        (println ";; Stopping Server")
        (stop-server! server)
        (dissoc this :server)))))

(defn make-server
  [port]
  (map->Server {:port port}))

