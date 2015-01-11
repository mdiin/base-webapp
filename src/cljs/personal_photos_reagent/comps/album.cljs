(ns personal-photos-reagent.comps.album
  (:require
    [reagent.core :as reagent :refer [atom]]

    [personal-photos-reagent.events :as events]
    [personal-photos-reagent.events.types.client :as client-events]
    [personal-photos-reagent.comps.state :as state :refer [local-state app-state]]))

(defn- toggle-select-state
  [pictures picture album]
  (let [picture-id (:id picture)
        existing-selections (get-in pictures [picture-id :selected])]
    (if (contains? existing-selections album)
      (update-in pictures [picture-id :selected] disj album)
      (update-in pictures [picture-id :selected] conj album))))

(defn- select-picture
  [pic album]
  (fn [e]
    (swap! (app-state :pictures) toggle-select-state pic album)))

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

(defn- add-album
  ^{:pre [(sequential? pictures) (string? album)]
    :post [(sequential? %)]}
  [album pictures]
  (map (fn [picture]
         (update-in picture [:albums] conj album))
       pictures))

(defn- to-picture-map
  ^{:pre [(sequential? pictures)]
    :post [(map? %)]}
  [pictures]
  (into {} (map #(vector (:id %) %) pictures)))

(defn- add-album-to-selected
  ^{:pre [(map? pictures) (string? album)]
    :post [(map? %)]}
  [pictures album]
  (let [selected (filter (comp seq :selected) (vals pictures))]
    (->> selected
         (add-album album)
         (to-picture-map)
         (merge pictures))))

(defn- add-selection-to-album
  [album]
  (fn [e]
    (swap! (app-state :pictures) add-album-to-selected album)))

(defn- view-album
  [album]
  (fn [e]
    (reset! (app-state :visible-album) album)))

(defn albums []
  (let [mode @(app-state :mode)
        albums @(app-state :albums)]
    [:div
     [:h1 "Albums"]
     [:ol
      (for [a (keys albums)]
        ^{:key a} [:li {:on-click (if (= mode :add-to-album)
                                    (add-selection-to-album a)
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

(defn- remove-from-albums
  ^{:pre [(sequential? pictures)]
    :post [(sequential? %)]}
  [pictures]
  (map (fn [picture]
         (-> picture
             (update-in [:albums] difference (:selected picture))
             (assoc :selected #{})))
       pictures))

(defn- remove-selected-from-albums
  ^{:pre [(map? pictures)]
    :post [(map? %)]}
  [pictures]
  (let [selected (filter (comp seq :selected) (vals pictures))]
    (->> selected
         (remove-from-albums)
         (to-picture-map)
         (merge pictures))))

(defn remove-from-album []
  (let [pictures @(app-state :pictures)
        selected-pictures (filter (comp seq :selected) pictures)]
    (when (seq selected-pictures)
      [:button {:on-click #(swap! (app-state :pictures) remove-selected-from-albums)}
       "Remove from album"])))

(defn- deselect
  ^{:pre [(sequential? pictures)]
    :post [(sequential? %)]}
  [pictures]
  (map #(assoc % :selected #{})))

(defn- deselect-selected
  ^{:pre [(map? pictures)]
    :post [(map? %)]}
  [pictures]
  (let [selected (filter (comp seq :selected) (vals pictures))]
    (->> selected
         (deselect)
         (to-picture-map)
         (merge pictures))))

(defn deselect-all-button []
  [:button {:on-click #(swap! (app-state :pictures) deselect-selected)}
   "Deselect all"])

