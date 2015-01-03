(ns personal-photos-reagent.comps.picture
  (:require
    [reagent.core :as reagent :refer [atom]]

    [cljs.core.async :as async]
    
    [personal-photos-reagent.events :as events]
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

(defn- file-event-handler
  [file picture-reader]
  (fn [e]
    (let [image-data (.. e -target -result)]
      (events/publish-server-event
        :id server-events/picture-info
        :payload (js->clj (goog.crypt.stringToByteArray image-data))
        :?reply-fn (fn [picture-data]
                     (set! (.-onloadend picture-reader) (fn [e]
                                                          (let [picture (.. e -target -result)
                                                                final-picture (merge picture-data {:picture picture})]
                                                            (events/publish-server-event
                                                              :id server-events/upload-picture
                                                              :payload picture-data)))))))))

(defn- upload-files-2 [_]
  (println "Uploading " @upload-local-state)
  (let [picture-reader (js/FileReader.)
        picture-info-reader (js/FileReader.)]
    (doseq [file @upload-local-state]
      (let [exif-part (.slice (:file file) 0 131072)]
        (set! (.-onloadend picture-info-reader) (file-event-handler file picture-reader))
        (.readAsBinaryString picture-info-reader exif-part)))))

(def worker-chan (async/chan 1))
(def worker (js/Worker. "/js/worker_jpeg_analyze.js"))
(defn- add-worker-listener [w]
  (letfn [(cb [d]
            (async/put! worker-chan (js->clj (.-data d))))]
    (set! (.-onmessage w) cb)))
(add-worker-listener worker)

(defn- upload-files [_]
  (println "Uploading " @upload-local-state)
  (doseq [file @upload-local-state]
    (.postMessage worker (:file file))
    (go
      (events/publish-server-event
        :id server-events/upload-picture
        :payload (async/<! worker-chan)))))

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

