(ns personal-photos-reagent.server
  (:require
    [com.stuartsierra.component :as component]

    [compojure.core :as compojure :refer (defroutes GET POST)]
    [compojure.route :as route]

    [ring.util.response :as ring-response]
    [ring.middleware.defaults]
    [ring.middleware.session :as ring-session]

    [org.httpkit.server :as http-kit-server]

    [taoensso.sente :as sente]

    [cemerick.friend :as friend]
    [cemerick.friend.workflows :as workflows]
    [cemerick.friend.credentials :as credentials]
    
    [personal-photos-reagent.data :as data]
    [personal-photos-reagent.routes :as routes]
    [personal-photos-reagent.channel-socket :as channel-socket]
    
    [environment]))

(defn- generate-response
  [body status]
  (-> (ring-response/response body)
      (ring-response/status status)))

(def users
  {"mdiin" {:username "mdiin" :password (credentials/hash-bcrypt "mdiin")}})

(def authentication-map
  {:allow-anon? true
   :redirect-on-auth? false
   :login-failure-handler (fn [e] (generate-response {:error "Wrong credentials"} 401))
   :unauthenticated-handler #(generate-response "unauthenticated" 401)
   :login-uri "/login"
   :default-landing-uri "/"
   :unauthorized-handler #(generate-response "unauthorized" 403)
   :credential-fn (fn [c] 
                    (credentials/bcrypt-credential-fn data/credential-user-fn c))
   :workflows [(workflows/interactive-form :redirect-on-auth? false)]
   })

(defn- make-ring-handler
  [base-handler]
  (-> base-handler
      (environment/make-ring-handler)))

(defn- start-server! [port routes]
  (let [base-handler (friend/authenticate routes authentication-map)]
    (http-kit-server/run-server (make-ring-handler base-handler) {:port port})))

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

