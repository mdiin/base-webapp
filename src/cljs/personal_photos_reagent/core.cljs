(ns personal-photos-reagent.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    
    [personal-photos-reagent.comps.main :as main :refer [main]]))

(enable-console-print!)

(reagent/render-component [main] (.getElementById js/document "app"))

