(ns graphql-example-clojure.schema
  (:require [com.stuartsierra.component :as component]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.util :as util]
            [graphql-example-clojure.db :as db]
            [datomic.api :as d]
            [clojure.spec.alpha :as spec]))

(defn key-factory
  [key]
  (fn [context args value]
    (get value key)))

(defn entity-factory
  [db-conn field-key]
  {:pre [db-conn]}
  (fn [context args value]
    (let [db (d/db db-conn)]
     (db/entity-by-id db (get value field-key)))))

(defn artist-by-name
  [db-conn]
  {:pre [db-conn]}
  (fn [_ args _]
    (let [{:keys [name]} args
          db (d/db db-conn)]
      (db/artist-by-name db name))))

(defn tracks-by-name
  [db-conn]
  {:pre [db-conn]}
  (fn [_ args _]
    (let [{:keys [name]} args
          db (d/db db-conn)]
      (db/tracks-by-name db name))))

(defn release-by-name
  [db-conn]
  {:pre [db-conn]}
  (fn [_ args _]
    (let [{:keys [name]} args
          db (d/db db-conn)]
      (db/release-by-name db name))))

(defn tracks-by-artist
  [db-conn]
  {:pre [db-conn]}
  (fn [_ _ artist]
    (let [artist-id (:db/id artist)
          db (d/db db-conn)]
      (flatten (db/tracks-by-artist db artist-id)))))

(defn tracks-by-medium
  [db-conn]
  {:pre [db-conn]}
  (fn [_ _ medium]
    (let [medium-id (:db/id medium)
          db (d/db db-conn)]
      (flatten (db/tracks-by-medium db medium-id)))))

(defn artists-by-release
  [db-conn]
  {:pre [db-conn]}
  (fn [_ _ release]
    (let [release-id (:db/id release)
          db (d/db db-conn)]
      (db/artists-by-release db release-id))))

(defn artists-for-track
  [db-conn]
  {:pre [db-conn]}
  (fn [_ _ track]
    (let [track-id (:db/id track)
          db (d/db db-conn)]
      (db/artists-for-track db track-id))))

(defn resolve-format
  [db-conn]
  {:pre [db-conn]}
  (fn [_ _ format]
    (let [db (d/db db-conn)]
      (db/release-format db (:db/id format)))))

(defn add-artist
  [db-conn]
  (fn [_ artist _]
    (db/add-artist db-conn artist)))

(defn resolver-map
  [component]
  (let [db-conn (-> component :db :db-conn)]
    {:query/artist-by-name (artist-by-name db-conn)
     :query/tracks-by-name (tracks-by-name db-conn)
     :query/release-by-name (release-by-name db-conn)
     :Artist/tracks (tracks-by-artist db-conn)
     :Track/artists (artists-for-track db-conn)
     :Release/artists (artists-by-release db-conn)
     :Medium/tracks (tracks-by-medium db-conn)
     :Format/format (resolve-format db-conn)
     :mutation/add-artist (add-artist db-conn)
     :key key-factory
     :entity (partial entity-factory db-conn)}))

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
    (-> this
        (assoc :schema nil)
        (assoc :db nil))))

(defn new-schema-provider
  []
  {:schema-provider (component/using (map->SchemaProvider {})
                                     [:schema :db])})

(comment



  )
