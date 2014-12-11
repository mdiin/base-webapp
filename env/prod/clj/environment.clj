(ns environment
  (:require
    [ring.middleware.defaults]
    [ring.middleware.anti-forgery :as ring-anti-forgery]))

(defn make-ring-handler
  [routes]
  (let [ring-defaults-config (assoc-in ring.middleware.defaults/site-defaults
                                       [:security :anti-forgery]
                                       {:read-token (fn [req] (-> req :params :csrf-token))})]
    (ring.middleware.defaults/wrap-defaults routes ring-defaults-config)))

