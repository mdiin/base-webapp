(ns personal-photos-reagent.comps.user
  (:require
    [personal-photos-reagent.comps.state :as state :refer [app-state]]
    [personal-photos-reagent.events.client :as client]))

(def login-local-state (atom {}))

(defn- update-field
  [field]
  (fn [e]
    (let [target (.-target e)
          value (.-value target)]
      (swap! login-local-state
             assoc field value))))

(defn- submit []
  (client/publish-event :id client/sign-in
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

