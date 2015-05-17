(ns user
  (:require
    [personal-photos-reagent.main :as app]

    [com.stuartsierra.component :as component]
    [clojure.tools.namespace.repl :as repl]
    [spyscope.repl :as spy-repl]))

(def trace-query spy-repl/trace-query)
(def trace-next spy-repl/trace-next)

(def app nil)
(def server nil)

(declare stop)
(defn start
  []
  (alter-var-root #'app (constantly (app/system {:db-name "development"
                                                 :db-host "localhost"
                                                 :db-port 5432
                                                 :db-user "development"
                                                 :db-pass "development"
                                                 :s3-bucket "mdiin-pictures"
                                                 :s3-key "AKIAIEYQQXE6VORYPFOQ"
                                                 :s3-secret "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"})))
  (alter-var-root #'app component/start))

(defn stop
  []
  (alter-var-root #'app component/stop))

(defn restart
  []
  (when app
    (stop))
  (repl/refresh :after 'user/start))

