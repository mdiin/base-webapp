(defproject personal-photos-reagent "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :min-lein-version "2.3.4"

  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources"]

  :dependencies [[com.stuartsierra/component "0.2.2"]
                 [com.taoensso/sente "1.2.0"]
                 [http-kit "2.1.19"]
                 [compojure "1.3.1"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.2"]
                 [clj-time "0.8.0"]

                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2411"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [reagent "0.4.3"]
                 
                 [figwheel "0.1.7-SNAPSHOT"]]

  :dev-dependencies [[com.cemerick/piggieback "0.1.3"]]
  
  :main personal-photos-reagent.main
  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-figwheel "0.1.7-SNAPSHOT"]]

  :hooks [leiningen.cljsbuild]

  :cljsbuild
  {:builds {:app
            {:source-paths ["src/cljs"]
             :compiler {:preamble ["reagent/react.js"]
                        :externs ["react/externs/react.js"]
                        :output-to "resources/public/js/app.js"
                        :output-dir "resources/public/js/out"
                        :source-map "resources/public/js/out.js.map"
                        }}}}

  :profiles
  {:dev
   {:source-paths ["env/dev/clj"]
    :resource-paths ["env/dev/resources"]
    :cljsbuild
    {:builds {:app
              {:source-paths ["env/dev/cljs"]
               :compiler {:optimizations :none
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
