(ns personal-photos-reagent.core
  (:require
    [reagent.core :as reagent :refer [atom]]

    [personal-photos-reagent.events.client :refer [load-user-event]]
    [personal-photos-reagent.events.server]
    [personal-photos-reagent.events.types.client]
    [personal-photos-reagent.events.types.server]
    [personal-photos-reagent.services.server-communication :as server-comm]
    [personal-photos-reagent.comps.main :as main :refer [main]]))

(defn init []
  (load-user-event (server-comm/current-uid))
  (reagent/render-component [main] (.getElementById js/document "app")))

