FROM pointslope/datomic-pro-starter:0.9.5561
LABEL maintainer="chris.howe-jones@devcycle.co.uk"
CMD ["config/dev-transactor.properties"]
