# graphql-example-clojure

This is an exemplar project to show the use of GraphQL in a web
service written in Clojure.

This project uses the Walmart Lacinia library and Datomic as the data
store. It uses the mbrainz sample data used in many of the datomic examples.

## Usage

TODO

### Seting up Datomic database


_WIP_

You need to copy the `dev-resources/.credentials` file to `.credentials` in
the top level directory of this project (same dir as this README
file).  Register at datomic.com for a license for Datomic Pro Starter
Edition. Edit the `.credentials` to add the user and datomic download
key (see
[https://my.datomic.com/account](https://my.datomic.com/account)) in
the format `user:download-key`.

Copy the `config/sample-dev-transactor.properties` file to
`config/dev-transactor.properties`. Edit the
`config/dev-transactor.properties` file to add the license key for
your registered datomic pro license (see
[https://my.datomic.com/account](https://my.datomic.com/account)) in
the `license-key=` value.

Download the subset of the mbrainz database covering the period 1968 to
1973 by running this command while in an appropriate parent directory
where you want the backed up data to reside (I use my home directory).

    wget \
    https://s3.amazonaws.com/mbrainz/datomic-mbrainz-1968-1973-backup-2017-07-20.tar \
    -O mbrainz.tar
    tar -xvf mbrainz.tar

Next, build the docker image:

    $ docker build -t graphql-datomic .

Create container called `graphql-datomic` from image `graphql-datomic`
(remember to substitute the path to your mbrainz backup data you
downloaded for the path in the volume i.e `-v` path below).

    $ docker create --name graphql-datomic -p 4334:4334 -p 4335:4335 \
    -p 4336:4336 -v /path/to/mbrainz-1968-1973:/tmp/data \
    graphql-datomic

Start the container with a transactor running but no databases.

    $ docker start graphql-datomic

You can test the datomic instance by running the `get-database-names`
fn in a repl (see `env/dev/user.clj` for example).

Next we need to get into the running docker container and restore the
mbrainz database to the running instance of datomic in the container:

    $ docker exec -it graphql-datomic bash
    ...
    bash-4.3# pwd
    /opt/datomic-pro-0.9.5561

    bash-4.3# ./bin/datomic -Xmx4g -Xms4g restore-db file:///tmp/data/ \
    datomic:dev://localhost:4334/mbrainz-1968-1973

    Copied 0 segments, skipped 0 segments.
    Copied 577 segments, skipped 0 segments.
    Copied 1414 segments, skipped 0 segments.
    :succeeded
    {:event :restore, :db mbrainz-1968-1973, :basis-t 148253, :inst #inst "2017-07-20T16:07:40.880-00:00"}


This should restore mbrainz data backup to running datomic docker
container named `graphql-datomic`

## License

Copyright © 2018 Chris Howe-Jones

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
