(ns personal-photos-reagent.comps.main
  (:require
    [reagent.core :as reagent :refer [atom]]

    [personal-photos-reagent.events :as events]
    [personal-photos-reagent.services.server-communication :as server-comm]
    [personal-photos-reagent.comps.mode-changers :refer [mode-select-button mode-add-to-album-button]]
    [personal-photos-reagent.comps.album :refer [album albums new-album remove-from-album deselect-all-button]]
    [personal-photos-reagent.comps.user :refer [login]]
    [personal-photos-reagent.comps.state :refer [app-state]]))

(defn- foo [e]
  (events/publish-event :event :read-state))

(defn main []
  [:div
   [:button {:on-click #(server-comm/send-fn [:app/an-event {:data "Foo"}])} "Send event!"]
   [:button {:on-click foo} "Read state"]
   [login]
   [mode-select-button]
   [mode-add-to-album-button]
   [new-album]
   [remove-from-album]
   [deselect-all-button]
   [albums]
   [album]])

