(ns environment
  (:require
    [ring.middleware.defaults]
    [ring.middleware.anti-forgery :as ring-anti-forgery]
    [ring.middleware.reload :as reload]))

(defn make-ring-handler
  [routes]
  (let [ring-defaults-config (assoc-in ring.middleware.defaults/site-defaults
                                       [:security :anti-forgery]
                                       {:read-token (fn [req] (-> req :params :csrf-token))})]
    (-> routes
        (ring.middleware.defaults/wrap-defaults ring-defaults-config)
        (reload/wrap-reload))))

