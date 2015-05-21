(defproject base-webapp "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :min-lein-version "2.3.4"

  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :resource-paths ["resources"]

  :dependencies [;; Components
                 [com.stuartsierra/component "0.2.3"]

                 ;; Server base
                 [http-kit "2.1.19"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.5"]
                 [compojure "1.3.4"]
                 [hiccup "1.0.5"]

                 ;; Server security
                 [com.cemerick/friend "0.2.1"]

                 ;; Time
                 [clj-time "0.9.0"]

                 ;; SQL
                 [yesql "0.4.1"]
                 [org.postgresql/postgresql "9.3-1102-jdbc41"]
                 [clojure.jdbc/clojure.jdbc-c3p0  "0.3.2"]

                 ;; Clojure official tools
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/clojure "1.7.0-RC1"]
                 [org.clojure/clojurescript "0.0-3269"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]

                 ;; Client UI
                 [reagent "0.5.0"]

                 ;; Client -> server connection
                 [com.taoensso/sente "1.4.1" :exclusions [com.taoensso/encore]]
                 [com.taoensso/encore "1.30.0"]
                 
                 ;; Client development
                 [figwheel "0.3.2"]]

  :dev-dependencies [[com.cemerick/piggieback "0.1.3"]]
  
  :main personal-photos-reagent.main
  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-figwheel "0.2.5"]
            ]

  :hooks [leiningen.cljsbuild]

  :cljsbuild
  {:builds {:app
            {:source-paths ["src/cljs"]
             :compiler {:externs ["react/externs/react.js"]
                        :output-to "resources/public/js/app.js"
                        :output-dir "resources/public/js/out"
                        :source-map "resources/public/js/out.js.map"
                        }}}}

  :profiles
  {:dev
   {:source-paths ["env/dev/clj"]
    :resource-paths ["env/dev/resources"]
    :dependencies [[org.clojure/tools.nrepl "0.2.10"]
                   [spyscope "0.1.5"]]
    :cljsbuild
    {:builds {:app
              {:source-paths ["env/dev/cljs"]
               :compiler {:optimizations :none
                          :sourse-map true
                          :pretty-print true}}}}}
   
   :production
   {:source-paths ["env/prod/clj"]
    :resource-paths ["env/prod/resources"]
    :cljsbuild
    {:builds {:app
              {:source-paths ["env/prod/cljs"]
               :compiler {:optimizations :advanced
                          :pretty-print false}}}}}}
  
  :figwheel
  {:http-server-root "public"
   :css-dirs ["resources/public/css"]})
