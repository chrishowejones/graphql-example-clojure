(defproject graphql-example-clojure "0.1.0-SNAPSHOT"
  :description "Example project demonstrating GraphQL with Datomic."
  :url "http://github.com/chrishowejones/graphql-example-clojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.datomic/datomic-pro "0.9.5561"]
                 [com.stuartsierra/component "0.3.2"]
                 [com.walmartlabs/lacinia-pedestal "0.6.0"]]
  :profiles {:dev {:source-paths ["env/dev"]}}
  :jvm-opts ["-Xmx1g"])
