(ns personal-photos-reagent.comps.user
  (:require
    [personal-photos-reagent.comps.state :as state :refer [app-state]]
    [personal-photos-reagent.events :as events :refer [publish-event]]))

(def login-local-state (atom {}))

(defn- update-field
  [field]
  (fn [e]
    (let [target (.-target e)
          value (.-value target)]
      (println @login-local-state)
      (swap! login-local-state
             assoc field value))))

(defn- submit []
  (publish-event :event :sign-in
                 :payload {:username (get @login-local-state :username)
                           :password (get @login-local-state :password)}))

(defn login []
  (let [current-user @(app-state :current-user)]
    (if current-user
      [:h1 "You are already signed in."]
      [:div
       [:input {:type "text" :placeholder "username" :on-change (update-field :username)}]
       [:input {:type "password" :placeholder "password" :on-change (update-field :password)}]
       [:button {:on-click submit}
        "Log in"]])))

