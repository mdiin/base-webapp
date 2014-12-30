(ns personal-photos-reagent.comps.state
  (:require
    [reagent.core :as reagent :refer [atom]]))

(defonce storage
  {:local-states {}
   :app-states {:mode (atom :browse)
                :visible-album (atom "No album")
                :current-user (atom nil)
                :albums (atom nil)
                :pictures (atom nil)
                ;:albums (atom {"First" [2]
                               ;"Second" [1]})
                ;:pictures (atom {2 {:full-url "http://placehold.it/300x300"
                                    ;:thumb-url "http://placehold.it/50x50"
                                    ;:timestamp (.now js/Date)
                                    ;:albums #{"First"}
                                    ;:selected #{}
                                    ;:id 2}
                                 ;1 {:full-url "http://placehold.it/300x300"
                                    ;:thumb-url "http://placehold.it/50x50"
                                    ;:timestamp (.now js/Date)
                                    ;:albums #{"Second"}
                                    ;:selected #{}
                                    ;:id 1}})
                }})

(defn insert-local-state
  [component state]
  (assoc-in storage [:local-states component] state))

(defn remove-local-state
  [component state]
  (update-in storage [:local-states] dissoc component))

(defn local-state
  [comp-name]
  (get-in storage [:local-states comp-name]))

(defn app-state
  [state-name]
  (get-in storage [:app-states state-name]))

