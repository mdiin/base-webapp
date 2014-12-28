(ns personal-photos-reagent.core
  (:require
    [reagent.core :as reagent :refer [atom]]

    [personal-photos-reagent.events.client]
    [personal-photos-reagent.events.server]
    [personal-photos-reagent.events.types.client]
    [personal-photos-reagent.events.types.server]
    [personal-photos-reagent.services.server-communication :as server-comm]
    [personal-photos-reagent.comps.main :as main :refer [main]]))

(defn init []
  (reagent/render-component [main] (.getElementById js/document "app")))

