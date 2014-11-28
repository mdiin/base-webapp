(defproject personal-photos-reagent "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :min-lein-version "2.3.4"

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [reagent "0.4.3"]
                 ]
  
  :plugins [[lein-cljsbuild "1.0.3"]]

  :hooks [leiningen.cljsbuild]

  :cljsbuild
  {:builds {:dev
            {:compiler {:preamble ["reagent/react.js"]
                        :output-to "dev-resources/public/js/personal_photos_reagent.js"
                        :optimizations :none
                        :pretty-print true}}

            :personal-photos-reagent
            {:source-paths ["src/cljs"]
             :compiler {:preamble ["reagent/react.js"]
                        :output-to "dev-resources/public/js/personal_photos_reagent.js"
                        :optimizations :advanced
                        :pretty-print false}}}})
