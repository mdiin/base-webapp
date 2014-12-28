(ns personal-photos-reagent.handlers.base)

(defn logf [fmt & xs] (println (apply format fmt xs)))

(defmulti event-msg-handler (fn [dbspec event] (:id event)))

