(ns personal-photos-reagent.comps.album
  (:require
    [reagent.core :as reagent :refer [atom]]

    [personal-photos-reagent.events :as events :refer [publish-event]]
    [personal-photos-reagent.comps.state :as state :refer [local-state app-state]]))

(defn- select-picture
  [pic album]
  (fn [e]
    (publish-event :event :select-picture :payload {:picture pic :album album})))

(defn picture [& {:keys [pic mode album]}]
  (if (= mode :select)
    [:div {:on-click (select-picture pic album)}
     [:img {:src (:full-url pic)}]
     [:p (js/Date (:timestamp pic))]]
    [:div
     [:img {:src (:full-url pic)}]
     [:p (js/Date (:timestamp pic))]]))

(defn album [album-name picture-ids]
  (let [mode @(app-state :mode)
        pictures @(app-state :pictures)

        album-pictures (map #(get pictures %) picture-ids)]
    [:div
     [:h1 album-name]
     (for [p album-pictures]
       ^{:key (:id p)} [picture :pic p :mode mode :album album-name])]))

(defn- select-album
  [album]
  (fn [e]
    (publish-event :event :select-album :payload album)))

(defn albums [{:keys [pictures?]}]
  (let [mode @(app-state :mode)
        albums @(app-state :albums)]
    (if pictures?
      [:div
       (for [[album-name picture-ids] albums]
         ^{:key album-name} [album album-name picture-ids])]
      [:div
       [:ol
        (for [a (keys albums)]
          ^{:key a} [:li {:on-click (when (= mode :add-to-album)
                                      (select-album a))}
                     a])]])))

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
  (let [pictures (vals @(app-state :pictures))
        selected-pictures (filter (comp seq :selected) pictures)]
    (when (seq selected-pictures)
      [:button {:on-click #(publish-event :event :remove-from-album :payload selected-pictures)}
       "Remove from album"])))

(defn deselect-all-button []
  [:button {:on-click #(publish-event :event :deselect-pictures)}
   "Deselect all"])

