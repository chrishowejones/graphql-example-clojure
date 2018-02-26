(ns graphql-example-clojure.db
  (:require [clojure.string :as s]
            [com.stuartsierra.component :as component]
            [datomic.api :as d]))

(def uri "datomic:dev://localhost:4334/mbrainz-1968-1973")

(defn get-schema-attributes
  [db]
  (d/q '[:find [?ident ...]
         :where
         [?a :db/ident ?ident]
         [_ :db.install/attribute ?a]
         (not [?a :db/valueType :db.type/ref])]
       db))

(defn- build-artist-name-query
  [cursor]
  (let [q '[:find  (pull ?a [*]) (sort ?a)
            :in $ ?artist-name
            :where
            [?a :artist/name ?artist-name]]]
    (if cursor
      (conj q [(list '< cursor '?a)])
      q)))

(defn artist-by-name
  [db name first cursor]
  (let [query (build-artist-name-query cursor)
        artists (flatten (d/q query db name))]
    {:artists (if first (take first artists) artists)
     :hasNextPage (if first (< first (count artists)) false)}))

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

(defn- add-ns
  [entity-map ns]
  (->  entity-map
       keys
       (->> (map #(->> % name (str ns "/") keyword)))
       (zipmap (vals entity-map))))

(defn- build-valid-artist
  [artist-attrs artist-with-ns]
  (reduce (fn [m [k v]] (if (contains? artist-attrs k) (assoc m k v) m))
          {}
          artist-with-ns))

(defn add-artist
  [conn artist]
  (let [artist-with-ns (add-ns artist "artist")
        db-schema-attrs (get-schema-attributes (d/db conn))
        artist-attrs (set (filter #(= "artist" (namespace %)) db-schema-attrs))
        valid-artist (build-valid-artist artist-attrs artist-with-ns)
        id-map {:db/id #db/id [:db.part/user]}
        tx-data (merge valid-artist id-map)
        txn @(d/transact conn [tx-data]) ;; transact attrs
        db-after (:db-after txn)
        id (-> txn :tempids vals first)]
    (println "txn = " txn)
    (d/pull db-after '[*] id) ;; pull transacted entity from db after txn
    ))

(defrecord Database
    [db-uri]
  component/Lifecycle
  (start [this]
    (when db-uri
      (let [conn (d/connect db-uri)]
        (-> this
            (assoc :db-conn conn)
            (assoc :db-schema-attrs (get-schema-attributes (d/db conn)))))))
  (stop [this]
    (when-let [conn (:db-conn this)]
      (d/release conn))
    (-> this
        (assoc :db-conn nil)
        (assoc :db-schema-attrs nil))))

(defn new-database []
  {:db (component/using (map->Database {})
                        [:db-uri])})

(comment

  (d/q '[:find ?e :where [?e :artist/name "Chris"]] (d/db (d/connect uri)))

  (artist-by-name (d/db (d/connect uri)) "Chris" 3 nil)

  @(d/transact (d/connect uri) [{ :artist/name "Chris", :db/id { :part :db.part/user, :idx -1000003 } }])

  (add-artist (d/connect uri) {:name "Chris" :startDay 2 :startMonth 4 :startYear 1980})

  (let [id-map {:db/id #db/id [:db.part/user]}
        artist (merge {:artist/name "Chris"} id-map)]
    (println artist)
    @(d/transact (d/connect uri) [artist])
    )

  (let [artist {:artist/name "Chris" :artist/country 1234}
        valid-attrs #{:artist/name :artist/age}]
    (reduce (fn [m [k v]] (if (contains? valid-attrs k) (assoc m k v) m))
            {}
            artist))

  (add-artist (d/connect uri)
              {:name "Chris"})

  (d/q '[:find (pull ?e [*]) :where [?e :country/name "United Kingdom"]] (d/db (d/connect uri)))

  (d/q '[:find ?e . :where [?e :db/ident :country/GB]] (d/db (d/connect uri)))

  (d/transact (d/connect uri) (map
                               #(cons :db.fn/retractEntity %)
                               (d/q '[:find ?e
                                      :in $
                                      :where
                                      [?e :artist/name "Chris"]]
                                    (d/db (d/connect uri)))))

  (map
   #(cons :db.fn/retractEntity %)
   (d/q '[:find ?e
          :in $
          :where
          [?e :artist/name "Chris"]]
        (d/db (d/connect uri))))

  (artist-by-name (d/db (d/connect uri)) "Chris" nil nil)

  (d/q '[:find  (pull ?a [*])
         :in $ ?artist-name
         :where
         [?a :artist/name ?artist-name]]
       (d/db (d/connect uri))
       "Chris")

  (build-artist-name-query nil)


)
