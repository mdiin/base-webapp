(ns personal-photos-reagent.core
  (:require
    [reagent.core :as reagent :refer [atom]]

    [personal-photos-reagent.events :as events]
    [personal-photos-reagent.events.client :refer [load-user-event]]
    [personal-photos-reagent.events.server]
    [personal-photos-reagent.events.types.client :as client-events]
    [personal-photos-reagent.events.types.server :as server-events]
    [personal-photos-reagent.services.server-communication :as server-comm]
    [personal-photos-reagent.comps.main :as main :refer [main]]))

;; # State initializers

(defn- init-user-state! []
  (load-user-event (server-comm/current-uid)))

;; # Initialization proper

(defn init []
  (init-user-state!)
  (reagent/render-component [main] (.getElementById js/document "app")))

