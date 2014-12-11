(ns personal-photos-reagent.routes
  (:require
    [com.stuartsierra.component :as component]
    
    [compojure.core :as compojure :refer (defroutes GET POST)]
    [compojure.route :as route]

    [taoensso.sente :as sente]
    
    [personal-photos-reagent.channel-socket :as channel-socket]
    ))

(defn- make-routes
  [channel-socket]
  (let [{:keys [ajax-get-or-ws-handshake-fn ajax-post-fn]} channel-socket]
    (compojure/routes
      (GET "/" req "<h1>LANDING</h1")

      (GET "/chsk" req (ajax-get-or-ws-handshake-fn req))
      (POST "/chsk" req (ajax-post-fn req))
      (POST "/login" req "<h1>LOGIN</h1>")

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

(defn- logf [fmt & xs] (println (apply format fmt xs)))

(defmulti event-msg-handler :id)
(defn event-msg-handler* [{:as ev-msg :keys [id ?data event]}]
  (logf "Event: %s" event)
  (event-msg-handler ev-msg))

(defmethod event-msg-handler :default ; Fallback
  [{:as ev-msg :keys  [event id ?data ring-req ?reply-fn send-fn]}]
  (let  [session  (:session ring-req)
         uid  (:uid session)]
    (logf  "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn  {:umatched-event-as-echoed-from-from-server event}))))


(defn- start-channel-socket-router [ch-recv]
  (sente/start-chsk-router! ch-recv event-msg-handler*))

(defn- stop-channel-socket-router [router]
  (router))

(defrecord ChannelSocketRouter [channel-socket router]
  component/Lifecycle
  
  (start [this]
    (if router
      this
      (do
        (println ";; Starting ChannelSocketRouter")
        (assoc this :router (start-channel-socket-router (get-in channel-socket [:channel-socket :ch-recv]))))))
  
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

