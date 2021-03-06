(ns personal-photos-reagent.comps.user
  (:require
    [personal-photos-reagent.events.types :as event-types]
    [personal-photos-reagent.comps.state :as state :refer [app-state]]
    [personal-photos-reagent.events :as events]
    [personal-photos-reagent.services.server-communication :as server-comm]))

(def user-local-state (atom {}))

(defn- update-field
  [field]
  (fn [e]
    (let [target (.-target e)
          value (.-value target)]
      (swap! user-local-state
             assoc field value))))

(defn- submit []
  (server-comm/send-ajax
    "/login"
    :post
    {:username (get @user-local-state :username)
     :password (get @user-local-state :password)}
    (fn [response]
      (server-comm/reconnect!))))

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
  (events/publish-server-event :id event-types/new-user
                               :payload {:username (get @user-local-state :username)
                                         :password (get @user-local-state :password)
                                         :name (get @user-local-state :name)}
                               :?reply-fn (fn [user]
                                            (println "Got reply: " user)
                                            (reset! (app-state :current-user) user))))

(defn sign-up []
  [:div
   [:input {:type "text" :placeholder "name" :on-change (update-field :name)}]
   [:input {:type "text" :placeholder "username" :on-change (update-field :username)}]
   [:input {:type "password" :placeholder "password" :on-change (update-field :password)}]
   [:button {:on-click sign-up-submit}
    "Sign up!"]])

(defn- submit-logout []
  (server-comm/send-ajax
    "/logout"
    :get
    {}
    (fn [_]
      (reset! (app-state :current-user) nil)
      (server-comm/reconnect!))))

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

