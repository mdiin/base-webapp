(ns personal-photos-reagent.comps.album
  (:require
    [reagent.core :as reagent :refer [atom]]

    [personal-photos-reagent.events :as events]
    [personal-photos-reagent.events.types.client :as client-events]
    [personal-photos-reagent.comps.state :as state :refer [local-state app-state]]))

(defn- select-picture
  [pic album]
  (fn [e]
    (events/publish-client-event :id client-events/select-picture :payload {:picture pic :album album})))

(defn picture [& {:keys [pic mode album]}]
  (if (= mode :select)
    [:div {:on-click (select-picture pic album)}
     [:img {:src (:dataURL pic)}]
     [:p (str (:created pic))]]
    [:div
     [:img {:src (:dataURL pic)}]
     [:p (str (:created pic))]]))

(defn album []
  (let [mode @(app-state :mode)
        pictures @(app-state :pictures)
        visible-album @(app-state :visible-album)
        album-pictures (filter (fn [{:keys [albums]}]
                                 (if (= visible-album "No album")
                                   (not (seq albums))
                                   (albums visible-album)))
                               pictures)]
    [:div
     [:h1 visible-album]
     (for [p album-pictures]
       ^{:key (:id p)} [picture :pic p :mode mode :album visible-album])]))

(defn- select-album
  [album]
  (fn [e]
    (events/publish-client-event :id client-events/select-album :payload album)))

(defn- view-album
  [album]
  (fn [e]
    (events/publish-client-event :id client-events/view-album :payload album)))

(defn albums []
  (let [mode @(app-state :mode)
        albums @(app-state :albums)]
    [:div
     [:h1 "Albums"]
     [:ol
      (for [a (keys albums)]
        ^{:key a} [:li {:on-click (if (= mode :add-to-album)
                                    (select-album a)
                                    (view-album a))}
                   a])]]))

(defonce new-album-local-state (atom ""))
(defn new-album []
  (let [mode @(app-state :mode)]
    (letfn [(update-local-state
              [e]
              (reset! new-album-local-state (-> e .-target .-value)))]
      (when (= mode :add-to-album)
        [:div
         [:label
          "New album:"
          [:input {:type "text"
                   :value @new-album-local-state
                   :on-change update-local-state}]]
         [:button {:on-click (select-album @new-album-local-state)}
          "Add"]]))))

(defn remove-from-album []
  (let [pictures @(app-state :pictures)
        selected-pictures (filter (comp seq :selected) pictures)]
    (when (seq selected-pictures)
      [:button {:on-click #(events/publish-client-event :id client-events/remove-from-album :payload selected-pictures)}
       "Remove from album"])))

(defn deselect-all-button []
  [:button {:on-click #(events/publish-client-event :id client-events/deselect-pictures)}
   "Deselect all"])

