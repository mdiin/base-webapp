(ns personal-photos-reagent.data
  (:require
    [cemerick.friend.credentials :as credentials]))

(def users
  {"mdiin" {:username "mdiin"
            :password (credentials/hash-bcrypt "mdiin")
            :name "Matthias Diehn Ingesman"
            :photo "http://placehold.it/50x50"}})

(defn credential-user-fn
  [uid]
  (get users uid))

(defn user
  [uid]
  (dissoc (get users uid) :password))

