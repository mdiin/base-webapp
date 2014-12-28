(ns personal-photos-reagent.routes
  (:require
    [com.stuartsierra.component :as component]
    
    [compojure.core :as compojure :refer (defroutes ANY GET POST)]
    [compojure.route :as route]

    [ring.util.response :as response]

    [taoensso.sente :as sente]
    [cemerick.friend :as friend]
    
    [personal-photos-reagent.handlers :as handlers]
    [personal-photos-reagent.channel-socket :as channel-socket]
    ))

(defn- make-routes
  [channel-socket]
  (let [{:keys [ajax-get-or-ws-handshake-fn ajax-post-fn]} channel-socket]
    (compojure/routes
      (GET "/" req "<h1>LANDING</h1>")
      (POST "/login" req (response/response {:status 200}))

      (GET "/chsk" req (ajax-get-or-ws-handshake-fn req))
      (POST "/chsk" req (ajax-post-fn req))

      (friend/logout (ANY "/logout" req (response/response {:status 200})))

      (route/resources "/") ; Static files, notably public/main.js (our cljs target)
      (route/not-found "<h1>Page not found</h1>"))))

(defrecord OrdinaryRouter [channel-socket routes]
  component/Lifecycle
  
  (start [this]
    (if routes
      this
      (do
        (println ";; Starting Router")
        (assoc this :routes (make-routes (:channel-socket channel-socket))))))
  
  (stop [this]
    (if-not routes
      this
      (do
        (println ";; Stopping Router")
        (dissoc this :routes)))))

(defn make-ordinary-router
  []
  (map->OrdinaryRouter {}))

;; # ChannelSocketRouter

(defn- start-channel-socket-router [ch-recv dbspec]
  (sente/start-chsk-router! ch-recv (partial handlers/event-msg-handler* dbspec)))

(defn- stop-channel-socket-router [router]
  (router))

(defrecord ChannelSocketRouter [channel-socket database router]
  component/Lifecycle
  
  (start [this]
    (if router
      this
      (do
        (println ";; Starting ChannelSocketRouter")
        (assoc this :router (start-channel-socket-router (get-in channel-socket [:channel-socket :ch-recv]) (:dbspec database))))))
  
  (stop [this]
    (if-not router
      this
      (do
        (println ";; Stopping ChannelSocketRouter")
        (stop-channel-socket-router router)
        (dissoc this :router)))))

(defn make-channel-socket-router
  []
  (map->ChannelSocketRouter {}))

