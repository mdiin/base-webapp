(ns personal-photos-reagent.main
  (:require
    [com.stuartsierra.component :as component]
    [clojure.tools.cli :as cli :refer [parse-opts]]
    
    [personal-photos-reagent.data :as database]
    [personal-photos-reagent.routes :as routes]
    [personal-photos-reagent.server :as server]
    [personal-photos-reagent.channel-socket :as channel-socket]))

(defn system
  [options]
  (let [{:keys [server-port db-host db-port db-name db-user db-pass]} options]
    (component/system-map
      :channel-socket (channel-socket/make-channel-socket)
      :database (database/make-database db-host db-port db-name db-user db-pass)
      :ordinary-router (component/using
                         (routes/make-ordinary-router)
                         [:channel-socket])
      :channel-socket-router (component/using
                               (routes/make-channel-socket-router)
                               [:channel-socket :database])
      :server (component/using
                (server/make-server (or server-port 8091))
                [:ordinary-router :channel-socket-router :database]))))

(def cli-options
  [["-p" "--port PORT" "Server port number"
    :default 8091
    :parse-fn #(Integer/parseInt %)]
   ["-d" "--db-port PORT" "Database port number"
    :default 5432
    :parse-fn #(Integer/parseInt %)]
   ["-o" "--db-host HOST" "Database host name"]
   ["-n" "--db-name NAME" "Database name"]
   ["-u" "--db-user USER" "Database username"]
   ["-s" "--db-pass PASS" "Database password"]
   ["-h" "--help"]])

(defn -main [& args]
  (let [parsed-args (parse-opts args cli-options)]
    (if (seq (:errors parsed-args))
      (doseq [e (:errors parsed-args)] (println e))
      (component/start (system (:options parsed-args))))))

