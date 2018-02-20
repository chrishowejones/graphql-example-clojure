(ns graphql-example-clojure.schema
  (:require [com.stuartsierra.component :as component]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.util :as util]
            [graphql-example-clojure.db :as db]))

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
  []
  (fn [_ args _]
    (let [{:keys [name]} args]
      (db/release-by-name name))))

(defn track-by-artist
  []
  (fn [_ _ artist]
    (let [artist-id (:db/id artist)]
      (flatten (db/tracks-by-artist artist-id)))))

(defn artists-by-release
  []
  (fn [_ _ release]
    (let [release-id (:db/id release)]
      (println "Release id = " release-id)
      (db/artists-by-release release-id))))

(defn resolver-map
  [component]
  {:query/artist-by-name (artist-by-name)
   :query/tracks-by-name (tracks-by-name)
   :query/release-by-name (release-by-name)
   :Artist/tracks (track-by-artist)
   :Release/artists (artists-by-release)
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
                                     [:schema])})
