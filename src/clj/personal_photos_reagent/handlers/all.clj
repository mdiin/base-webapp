(ns personal-photos-reagent.handlers.all
  (:require
    [cemerick.friend :as friend]
    [cemerick.friend.credentials :as credentials]

    [clj-time.format :as f]
    [clj-time.coerce :as c]

    [personal-photos-reagent.data :as data]
    [personal-photos-reagent.handlers.base :refer (logf event-msg-handler)]))

(defmethod event-msg-handler :default ; Fallback
  [dbspec {:as ev-msg :keys  [event id ?data ring-req ?reply-fn send-fn]}]
  (let  [session  (:session ring-req)
         uid  (:uid session)]
    (logf  "Unhandled event: %s" id)
    (when ?reply-fn
      (?reply-fn  {:umatched-event-as-echoed-from-from-server event}))))

(defmethod event-msg-handler :app/an-event
  [dbspec {:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    (logf "app/an-event received: %s. %s" event ring-req)))

(defmethod event-msg-handler :server/user
  [dbspec {:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [uid (:uid ?data)]
    (when ?reply-fn
      (?reply-fn
        (data/user dbspec uid)))))

(defmethod event-msg-handler :server/new-user
  [dbspec {:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [uid (:username ?data)
        password (:password ?data)
        name (:name ?data)]
    (if (and uid password name)
      (if-let [user (data/new-user<! dbspec name uid (credentials/hash-bcrypt password))]
        (when ?reply-fn
          (println (str "User " user))
          (?reply-fn user))
        (when ?reply-fn
          (println (str "FAIL"))
          (?reply-fn "Could not create user.")))
      (when ?reply-fn
        (println (str "WRONG"))
        (?reply-fn "Could not validate username, password, or name.")))))

(defmethod event-msg-handler :server/albums
  [dbspec {:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (if-let [uid (:identity (friend/current-authentication ring-req))]
    (when ?reply-fn
      (?reply-fn (data/albums-for-user dbspec uid)))
    (when ?reply-fn
      (?reply-fn "Not allowed"))))

(defmethod event-msg-handler :server/pictures
  [dbspec {:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (if-let [uid (:identity (friend/current-authentication ring-req))]
    (when ?reply-fn
      (?reply-fn (data/pictures* dbspec uid)))
    (when ?reply-fn
      (?reply-fn "Not allowed"))))

(def custom-formatter (f/formatter "yyyy:MM:dd HH:mm:ss"))

(defmethod event-msg-handler :server/upload-picture
  [dbspec {:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (data/new-picture*
    dbspec
    (:identity (friend/current-authentication ring-req))
    (second (second ?data))
    (c/to-sql-date (f/parse custom-formatter (second (first ?data))))))

