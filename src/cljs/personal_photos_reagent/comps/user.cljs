(ns personal-photos-reagent.comps.user
  (:require
    [personal-photos-reagent.comps.state :as state :refer [app-state]]
    [personal-photos-reagent.events :as events]
    [personal-photos-reagent.events.types.client :as client-events]
    [personal-photos-reagent.events.types.server :as server-events]))

(def user-local-state (atom {}))

(defn- update-field
  [field]
  (fn [e]
    (let [target (.-target e)
          value (.-value target)]
      (swap! user-local-state
             assoc field value))))

(defn- submit []
  (events/publish-client-event :id client-events/sign-in
                 :payload {:username (get @user-local-state :username)
                           :password (get @user-local-state :password)}))

(defn login []
  (let [current-user @(app-state :current-user)]
    (if current-user
      [:h1 (str "Logged in as " (:name current-user))]
      [:div
       [:input {:type "text" :placeholder "username" :on-change (update-field :username)}]
       [:input {:type "password" :placeholder "password" :on-change (update-field :password)}]
       [:button {:on-click submit}
        "Log in"]])))

(defn- sign-up-submit []
  (events/publish-server-event :id server-events/new-user
                               :payload {:username (get @user-local-state :username)
                                         :password (get @user-local-state :password)
                                         :name (get @user-local-state :name)}
                               :?reply-fn (fn [user]
                                            (println "Got reply: " user)
                                            (events/publish-client-event
                                              :id client-events/user-changed
                                              :payload user))))

(defn sign-up []
  [:div
   [:input {:type "text" :placeholder "name" :on-change (update-field :name)}]
   [:input {:type "text" :placeholder "username" :on-change (update-field :username)}]
   [:input {:type "password" :placeholder "password" :on-change (update-field :password)}]
   [:button {:on-click sign-up-submit}
    "Sign up!"]])

(defn- submit-logout []
  (events/publish-client-event :id client-events/sign-out))

(defn logout []
  (let [current-user @(app-state :current-user)]
    (when current-user
      [:div
       [:button {:on-click submit-logout}
        "Log out"]])))

(defn user []
  (let [current-user @(app-state :current-user)]
    (if current-user
      [logout]
      [:div
       [login]
       [sign-up]])))

