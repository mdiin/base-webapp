(ns personal-photos-reagent.data
  (:require
    [jdbc.pool.c3p0 :as pool]
    [yesql.core :refer [defqueries defquery]]

    [com.stuartsierra.component :as component]
    [cemerick.friend.credentials :as credentials]))

(def users
  {"mdiin" {:username "mdiin"
            :password (credentials/hash-bcrypt "mdiin")
            :name "Matthias Diehn Ingesman"
            :photo "http://placehold.it/50x50"}})

(def picture-data (atom {}))

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
(defqueries "personal_photos_reagent/database/albums.sql")
(defqueries "personal_photos_reagent/database/pictures.sql")

;; # Functions

(defn credential-user-fn
  [dbspec uid]
  (first (user-by-username dbspec uid)))

(defn user
  [dbspec uid]
  (dissoc (first (user-by-username dbspec uid)) :password))

;; # Development fns

(defn pictures*
  [dbspec uid]
  (let [ps-meta (pictures dbspec uid)]
    (println ps-meta)
    (map (fn [{:as p :keys [id]}]
           (into p {:dataURL (get @picture-data id)
                    :albums (into #{} (albums-for-picture dbspec id))}))
         ps-meta)))

(defn new-picture*
  [dbspec uid data-url date]
  (try
    (let [{:keys [id]} (new-picture<! dbspec uid date)]
      (swap! picture-data assoc id data-url))
    (catch Exception e
      (println "Exception happened..." (.getMessage e)))))

