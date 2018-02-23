(ns graphql-example-clojure.db
  (:require [datomic.api :as d]
            [com.stuartsierra.component :as component]))

(def uri "datomic:dev://localhost:4334/mbrainz-1968-1973")

(defn artist-by-name
  [db name]
  (d/q '[:find  (pull ?a [*]) .
         :in $ ?artist-name
         :where
         [?a :artist/name ?artist-name]]
       db
       name))

(defn tracks-by-name
  [db name]
  (d/q '[:find  [(pull ?t [*]) ...]
         :in $ ?track-name
         :where
         [?t :track/name ?track-name]]
       db
       name))

(defn release-by-name
  [db name]
  (d/q '[:find  (pull ?r [*]) .
         :in $ ?release-name
         :where
         [?r :release/name ?release-name]]
       db
       name))

(defn releases-by-artist-name
  [db artist-name]
  (d/q '[:find [(pull ?r [*]) ...]
         :in $ ?artist-name
         :where
         [?a :artist/name ?artist-name]
         [?r :release/artists ?a]]
       db
       artist-name))

(defn entity-by-id
  [db id]
  (d/pull db '[*] (:db/id id)))

(defn tracks-by-artist
  [db artist-id]
  (d/q '[:find (pull ?t [*])
         :in $ ?artist-id
         :where
         [?t :track/artists ?artist-id]]
       db
       artist-id))

(defn tracks-by-medium
  [db medium-id]
  (d/q '[:find (pull ?t [*])
         :in $ ?medium-id
         :where
         [?medium-id :medium/tracks ?t]]
       db
       medium-id))

(defn artists-by-release
  [db release-id]
  (:release/artists
   (d/pull db '[{:release/artists [*]}] release-id)))

(defn artists-for-track
  [db track-id]
  (:track/artists
   (d/pull db '[{:track/artists [*]}] track-id )))

(defn release-format
  [db format-id]
  (-> (d/q '[:find ?f .
             :in $ ?format-id
             :where
             [?format-id :db/ident ?f]]
           db
           format-id)
      name))

(defrecord Database
    [db-uri]
  component/Lifecycle
  (start [this]
    (when db-uri
      (assoc this :db-conn (d/connect db-uri))))
  (stop [this]
    (when-let [conn (:db-conn this)]
      (d/release conn))
    (assoc this :db-conn nil)))

(defn new-database []
  {:db (component/using (map->Database {})
                        [:db-uri])})

(comment

  (release-by-name "Hot Rocks 1964-1971")

  (format (-> (release-by-name "Hot Rocks 1964-1971")
              first
              ;; :release/media
              ;; first
              ;; :medium/format
              ;; :db/id
              ))


  (def track1 {:db/id 967570232551699,
               :track/artists [{:db/id 686095255742708}],
               :track/artistCredit "The Rolling Stones",
               :track/position 3,
               :track/name "Play With Fire",
               :track/duration 131933})

  (artists-for-track (:db/id track1))

  (tracks-by-medium (-> (release-by-name "Hot Rocks 1964-1971")
                        :release/media
                        first
                        :db/id))

  (first (releases-by-artist-name "The Rolling Stones"))

  (artists-by-release
   (:db/id (first (releases-by-artist-name "The Rolling Stones"))))


  (tracks-by-name "Prodigal Son")

  (filter #(not= "Official" (:release/status %)) (releases-by-artist-name "The Rolling Stones"))

  (count
   (tracks-by-artist
    (:db/id
     (d/q '[:find (pull ?a [*]) .
            :in $ ?artist-name
            :where
            [?a :artist/name ?artist-name]]
          (db)
          "The Rolling Stones"))))


  )
