(ns graphql-example-clojure.system
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [graphql-example-clojure.schema :as schema]
            [graphql-example-clojure.server :as server]
            [graphql-example-clojure.db :as db])
  (:gen-class))

(defn load-schema-file
  []
  (with-open [rdr (io/reader (io/resource "mbrainz-schema.edn"))]
    (edn/read-string (slurp rdr))))

(defn new-system
  []
  (merge (component/system-map)
         {:schema (load-schema-file)}
         {:db-uri db/uri}
         (db/new-database)
         (server/new-server)
         (schema/new-schema-provider)))

(defn -main []
  (component/start-system (new-system))
  (println :started))
