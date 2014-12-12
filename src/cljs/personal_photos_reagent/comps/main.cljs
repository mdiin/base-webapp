(ns personal-photos-reagent.comps.main
  (:require
    [reagent.core :as reagent :refer [atom]]

    [personal-photos-reagent.services.server-communication :as server-comm]
    [personal-photos-reagent.comps.mode-changers :refer [mode-select-button mode-add-to-album-button]]
    [personal-photos-reagent.comps.album :refer [album albums new-album remove-from-album deselect-all-button]]
    [personal-photos-reagent.comps.state :refer [app-state]]))

(defn main []
  [:div
   [:button {:on-click #(server-comm/send-fn [:app/an-event {:data "Foo"}])} "Send event!"]
   [mode-select-button]
   [mode-add-to-album-button]
   [new-album]
   [remove-from-album]
   [deselect-all-button]
   [albums]
   [album]])

