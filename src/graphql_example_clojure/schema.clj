(ns graphql-example-clojure.schema
  (:require [com.stuartsierra.component :as component]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.util :as util]
            [graphql-example-clojure.db :as db]
            [datomic.api :as d]))

(defn key-factory
  [key]
  (fn [context args value]
    (get value key)))

(defn entity-factory
  [field-key]
  (fn [context args value]
    (db/entity-by-id (get value field-key))))

(defn artist-by-name
  []
  (fn [_ args _]
    (let [{:keys [name]} args]
      (db/artist-by-name name))))

(defn tracks-by-name
  []
  (fn [_ args _]
    (let [{:keys [name]} args]
      (db/tracks-by-name name))))

(defn release-by-name
  [db-conn]
  {:pre [db-conn]}
  (fn [_ args _]
    (let [{:keys [name]} args
          db (d/db db-conn)]
      (db/release-by-name db name))))

(defn tracks-by-artist
  []
  (fn [_ _ artist]
    (let [artist-id (:db/id artist)]
      (flatten (db/tracks-by-artist artist-id)))))

(defn tracks-by-medium
  []
  (fn [_ _ medium]
    (let [medium-id (:db/id medium)]
      (flatten (db/tracks-by-medium medium-id)))))

(defn artists-by-release
  []
  (fn [_ _ release]
    (let [release-id (:db/id release)]
      (db/artists-by-release release-id))))

(defn artists-for-track
  []
  (fn [_ _ track]
    (let [track-id (:db/id track)]
      (db/artists-for-track track-id))))

(defn resolve-format
  []
  (fn [_ _ format]
    (db/release-format (:db/id format))))

(defn resolver-map
  [component]
  {:query/artist-by-name (artist-by-name)
   :query/tracks-by-name (tracks-by-name)
   :query/release-by-name (release-by-name (-> component
                                               :db
                                               :db-conn))
   :Artist/tracks (tracks-by-artist)
   :Track/artists (artists-for-track)
   :Release/artists (artists-by-release)
   :Medium/tracks (tracks-by-medium)
   :Format/format (resolve-format)
   :key key-factory
   :entity entity-factory})

(defn load-schema
  [component]
  (-> component
      :schema
      (util/attach-resolvers (resolver-map component))
      schema/compile))

(defrecord SchemaProvider
    [schema]
    component/Lifecycle
  (start [this]
    (assoc this :schema (load-schema this)))
  (stop [this]
    (assoc this :schema nil)))

(defn new-schema-provider
  []
  {:schema-provider (component/using (map->SchemaProvider {})
                                     [:schema :db])})

(comment

  (def track1 {:db/id 967570232551699,
               :track/artists [{:db/id 686095255742708}],
               :track/artistCredit "The Rolling Stones",
               :track/position 3,
               :track/name "Play With Fire",
               :track/duration 131933})

  ((artists-for-track) nil nil track1)


  )
