(ns personal-photos-reagent.comps.main
  (:require
    [reagent.core :as reagent :refer [atom]]

    [personal-photos-reagent.events :as events]
    [personal-photos-reagent.services.server-communication :as server-comm]

    [personal-photos-reagent.comps.user :refer [user login logout]]
    [personal-photos-reagent.comps.state :refer [app-state]]))

(defn- foo [e]
  (.log js/console @server-comm/state))

(defn main []
  (let [current-user @(app-state :current-user)]
    [:div
     [user]
     [:button {:on-click #(server-comm/send-fn [:app/an-event {:data "Foo"}])} "Send event!"]
     [:button {:on-click foo} "Read state"]
     (when-not current-user
       [:div
        [:h1 "Velkommen!"]
        [:p "Log ind eller opret dig som bruger."]])]))

