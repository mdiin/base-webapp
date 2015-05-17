(ns personal-photos-reagent.data
  (:require
    [jdbc.pool.c3p0 :as pool]
    [yesql.core :refer [defqueries defquery]]

    [com.stuartsierra.component :as component]
    [cemerick.friend.credentials :as credentials]))

;; # Component

(defrecord Database [host port db-name user-name password dbspec]
  component/Lifecycle
  
  (start
    [this]
    (if dbspec
      this
      (do
        (println ";; Starting database")
        (assoc this :dbspec (pool/make-datasource-spec
                                  {:vendor "postgresql"
                                   :name db-name
                                   :host host
                                   :port port
                                   :user user-name
                                   :password password})))))
  
  (stop
    [this]
    (if dbspec
      (do
        (println ";; Stopping database")
        (dissoc this :dbspec))
      this)))

(defn make-database
  [host port db-name user-name password]
  (map->Database {:host host
                  :port port
                  :db-name db-name
                  :user-name user-name
                  :password password}))

;; # Queries

(defqueries "personal_photos_reagent/database/user.sql")

;; # Functions

(defn credential-user-fn
  [dbspec uid]
  (first (user-by-username dbspec uid)))

(defn user
  [dbspec uid]
  (dissoc (first (user-by-username dbspec uid)) :password))

