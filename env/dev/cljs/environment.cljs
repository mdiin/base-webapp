(ns environment
  (:require
    [figwheel.client :as fw]
    
    [personal-photos-reagent.core :as app-core]))

(enable-console-print!)

(println ";; Figwheel started")

(fw/start {:on-jsload #(print "reloaded")
           :websocket-url "ws://localhost:3449/figwheel-ws"})

(app-core/init)

