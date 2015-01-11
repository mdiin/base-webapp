(ns personal-photos-reagent.comps.picture
  (:require
    [reagent.core :as reagent :refer [atom]]

    [cljs.core.async :as async]
    
    [personal-photos-reagent.events :as events]
    [personal-photos-reagent.comps.state :as state :refer [app-state]]
    [personal-photos-reagent.services.server-communication :as server-comm]
    [personal-photos-reagent.events.types.server :as server-events])
  (:require-macros
    [cljs.core.async.macros :as async-macros :refer [go]])
  (:import goog.crypt))

(defonce upload-local-state (atom #{}))

(defn file-list-to-array [js-col]
  (-> (clj->js []) 
      (.-slice)
      (.call js-col)
      (js->clj)))

(defn- handle-file-select
  [e]
  (when-let [files (.. e -target -files)]
    (let [files-map (map #(hash-map :file %) (file-list-to-array files))]
      (swap! upload-local-state into files-map))))

(defn- handle-file-dragover
  [e]
  (println "Over drop zone..."))

(def worker-chan (async/chan 1))
(def worker (js/Worker. "/js/worker_jpeg_analyze.js"))
(defn- add-worker-listener [w]
  (letfn [(cb [d]
            (async/put! worker-chan (js->clj (.-data d))))]
    (set! (.-onmessage w) cb)))
(add-worker-listener worker)

(defn- upload-files [_]
  (println "Uploading " @upload-local-state)
  (go
    (doseq [file @upload-local-state]
      (.postMessage worker (:file file))
      (events/publish-server-event
          :id server-events/upload-url
          :payload (async/<! worker-chan)
          :?reply-fn (fn [url]
                       (println "POST URL: " url)))
      (swap! upload-local-state disj file))))

(defn file-preview
  [{:keys [file progress]}]
  [:p (.-name file)])

(defn upload []
  (let [upload-state @upload-local-state]
    [:div
     [:div {:id "drop_zone"
            :on-dragover handle-file-dragover
            :on-drop handle-file-select}
      (for [f upload-state]
        (file-preview f))]
     [:input {:type "file"
              :multiple true
              :on-change handle-file-select}]
     [:button {:on-click upload-files} "Upload!"]]))

