(ns personal-photos-reagent.handlers.base)

(defn logf [fmt & xs] (println (apply format fmt xs)))

(defmulti event-msg-handler :id)

