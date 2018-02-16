(ns dev.user
  (:require [clojure.pprint :refer (pprint)]
            [datomic.api :as d]))

;; test empty datomic docker container once it's started using get-database-names
;; will return nil initially when empty
;; using this call after db-restore will return "mbrainz-1968-1973"

(comment

  (d/get-database-names "datomic:dev://localhost:4334/*")

  )

;; Useful test commands once database is restored using db-restore.
;; (see README.md for how to restore db from mbrainz dataset.)
(comment

 (def uri "datomic:dev://localhost:4334/mbrainz-1968-1973")

 (def conn (d/connect uri))
 (def db (d/db conn))

 (set! *print-length* 250)

 (d/q '[:find [?title ...]
        :in $ ?artist-name
        :where
        [?a :artist/name ?artist-name]
        [?t :track/artists ?a]
        [?t :track/name ?title]]
      db
      "The Rolling Stones")

 (filter
  #(re-find #"Rolling Stones" %)
  (d/q '[:find [?artist-name ...]
          :where
          [?a :artist/name ?artist-name]]
        db))

 )
