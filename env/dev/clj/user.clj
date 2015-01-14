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
  (alter-var-root #'app (constantly (app/system {:database "development"
                                                 :host "localhost"
                                                 :port 5432
                                                 :user "development"
                                                 :pass "development"})))
  (alter-var-root #'app component/start))

(defn stop
  []
  (alter-var-root #'app component/stop))

(defn restart
  []
  (when app
    (stop))
  (repl/refresh :after 'user/start))

