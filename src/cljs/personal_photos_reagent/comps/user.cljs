(ns personal-photos-reagent.comps.user
  (:require
    [personal-photos-reagent.comps.state :as state :refer [app-state]]
    [personal-photos-reagent.events :as events]
    [personal-photos-reagent.events.types.client :as client-events]))

(def login-local-state (atom {}))

(defn- update-field
  [field]
  (fn [e]
    (let [target (.-target e)
          value (.-value target)]
      (swap! login-local-state
             assoc field value))))

(defn- submit []
  (events/publish-client-event :id client-events/sign-in
                 :payload {:username (get @login-local-state :username)
                           :password (get @login-local-state :password)}))

(defn login []
  (let [current-user @(app-state :current-user)]
    (if current-user
      [:h1 (str "Logged in as " (:name current-user))]
      [:div
       [:input {:type "text" :placeholder "username" :on-change (update-field :username)}]
       [:input {:type "password" :placeholder "password" :on-change (update-field :password)}]
       [:button {:on-click submit}
        "Log in"]])))

(defn- submit-logout []
  (events/publish-client-event :id client-events/sign-out))

(defn logout []
  (let [current-user @(app-state :current-user)]
    (when current-user
      [:div
       [:button {:on-click submit-logout}
        "Log out"]])))

