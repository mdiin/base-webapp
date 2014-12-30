(ns personal-photos-reagent.comps.main
  (:require
    [reagent.core :as reagent :refer [atom]]

    [personal-photos-reagent.events :as events]
    [personal-photos-reagent.events.types.client :as client-events]
    [personal-photos-reagent.events.types.server :as server-events]
    [personal-photos-reagent.services.server-communication :as server-comm]
    [personal-photos-reagent.comps.mode-changers :refer [mode-select-button mode-add-to-album-button]]
    [personal-photos-reagent.comps.album :refer [album albums new-album remove-from-album deselect-all-button]]
    [personal-photos-reagent.comps.user :refer [user login logout]]
    [personal-photos-reagent.comps.state :refer [app-state]]))

(defn- foo [e]
  (events/publish-client-event :id client-events/read-state))

(defn main []
  (let [current-user @(app-state :current-user)]
    [:div
     [user]
     [:button {:on-click #(server-comm/send-fn [:app/an-event {:data "Foo"}])} "Send event!"]
     [:button {:on-click foo} "Read state"]
     (if current-user
       [:div
        [mode-select-button]
        [mode-add-to-album-button]
        [new-album]
        [remove-from-album]
        [deselect-all-button]
        [albums]
        [album]]
       [:div
        [:h1 "Velkommen!"]
        [:p "Log ind eller opret dig som bruger."]])]))

