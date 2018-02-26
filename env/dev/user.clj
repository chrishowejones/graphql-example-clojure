(ns user
  (:require [clojure.java.browse :refer [browse-url]]
            [com.stuartsierra.component :as component]
            [datomic.api :as d]
            [graphql-example-clojure.system :as sys]))

(defonce system nil)

(defn go
  []
  (let [system (alter-var-root #'system (constantly
                                         (component/start-system (graphql-example-clojure.system/new-system))))]
    (browse-url "http://localhost:8888/")
    system))

(defn stop
  []
  (alter-var-root #'system component/stop-system))

(defn reset
  []
  (stop)
  (go))

(comment

  (reset)

  (-> system
      :schema-provider
      :schema
      :QueryRoot
      :fields
      :artists_by_name)

  (-> system
      :schema-provider
      :schema
      :ArtistConnection)

  (go)

  (stop)

  )
