(ns graphql-example-clojure.server
  (:require [com.stuartsierra.component :as component]
            [com.walmartlabs.lacinia.pedestal :as lp]
            [io.pedestal.http :as http]))

(defrecord Server [schema-provider server]
  component/Lifecycle
  (start [this]
    (assoc this :server (-> schema-provider
                            :schema
                            (lp/service-map {:graphiql true})
                            http/create-server
                            http/start)))
  (stop [this]
    (http/stop server)
    (-> this
        (assoc :server nil)
        (assoc :schema-provider nil))))

(defn new-server
  []
  {:server (component/using (map->Server {})
                            [:schema-provider])})
