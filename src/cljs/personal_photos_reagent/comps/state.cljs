(ns personal-photos-reagent.comps.state
  (:require
    [reagent.core :as reagent :refer [atom]]))

(defonce storage
  {:local-states {}
   :app-states {:current-user (atom nil)}})

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

