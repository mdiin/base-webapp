(ns personal-photos-reagent.comps.mode-changers
  (:require
    [reagent.core :as reagent :refer [atom]]

    [personal-photos-reagent.events :as events]
    [personal-photos-reagent.events.types.client :as client-events]
    [personal-photos-reagent.events.types.server :as server-events]
    [personal-photos-reagent.comps.state :as state :refer [app-state]]))

(defn- mode-change-event
  [mode]
  (reset! (app-state :mode) mode))

(defn- toggle-select
  []
  (mode-change-event :select))

(defn mode-select-button []
  (let [mode @(app-state :mode)]
    (if (= mode :select)
      [:button {:on-click toggle-select} "Select"]
      [:button {:on-click toggle-select} "Select"])))

(defn- toggle-add-to-album
  []
  (mode-change-event :add-to-album))

(defn mode-add-to-album-button []
  (let [pictures @(app-state :pictures)
        selected-pictures (filter (comp seq :selected) pictures)]
    (when (seq selected-pictures)
      [:button {:on-click toggle-add-to-album} "Add to album"])))

