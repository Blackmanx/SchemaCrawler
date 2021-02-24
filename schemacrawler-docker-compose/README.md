<!-- markdownlint-disable MD024 -->
# SchemaCrawler Tests with Docker Compose

## Pre-requisites

- Start a Linux command shell in the `docker-compose` directory
- Run `./schemacrawler-testdb-local.sh`



## PostgreSQL

### Setup

- To start SchemaCrawler with PostgreSQL, run
  `docker-compose -f schemacrawler.yml -f postgresql.yml up -d`
- Create a test PostgreSQL database schema, run
  `docker exec -it docker-compose_schemacrawler_1 ./_testdb/createtestschema.sh --url "jdbc:postgresql://postgresql:5432/schemacrawler?ApplicationName=SchemaCrawler;loggerLevel=DEBUG" --user schemacrawler --password schemacrawler --debug`

### Testing

- Start SchemaCrawler bash with
  `docker exec -it docker-compose_schemacrawler_1 /bin/bash`
- Run SchemaCrawler from Docker container bash
  `schemacrawler --server postgresql --host postgresql --database schemacrawler --user schemacrawler --password schemacrawler --info-level minimum -c list`

### Tear Down

- To stop SchemaCrawler with PostgreSQL, run
  `docker-compose -f schemacrawler.yml -f postgresql.yml down -t0`



## Oracle

### Setup

- To start SchemaCrawler with Oracle, run
  `docker-compose -f schemacrawler.yml -f oracle.yml up -d`
- Create a test Oracle database schema, run
  `docker exec -it docker-compose_schemacrawler_1 ./_testdb/createtestschema.sh --url "jdbc:oracle:thin:@//oracle:1521/xe" --user system --password oracle --scripts-resource /oracle-11g.scripts.txt --debug`

### Testing

- Start SchemaCrawler bash with
  `docker exec -it docker-compose_schemacrawler_1 /bin/bash`
- Run SchemaCrawler from Docker container bash
  `schemacrawler --server oracle --host oracle --database xe --user system --password oracle --info-level minimum -c list`

### Tear Down

- To stop SchemaCrawler with Oracle, run
  `docker-compose -f schemacrawler.yml -f oracle.yml down -t0`



## Microsoft SQL Server

### Setup

- To start SchemaCrawler with Microsoft SQL Server, run
  `docker-compose -f schemacrawler.yml -f sqlserver.yml up -d`
- Create a test Microsoft SQL Server database schema, run
  `docker exec -it docker-compose_schemacrawler_1 ./_testdb/createtestschema.sh --url "jdbc:sqlserver://sqlserver:1433;databaseName=master" --user SA --password Schem#Crawl3r --debug`

### Testing

- Start SchemaCrawler bash with
  `docker exec -it docker-compose_schemacrawler_1 /bin/bash`
- Run SchemaCrawler from Docker container bash
  `schemacrawler --server sqlserver --host sqlserver --database BOOKS --schemas BOOKS\.dbo --user SA --password Schem#Crawl3r --info-level minimum -c list`

### Tear Down

- To stop SchemaCrawler with Microsoft SQL Server, run
  `docker-compose -f schemacrawler.yml -f sqlserver.yml down -t0`



## MySQL

### Setup

- To start SchemaCrawler with MySQL, run
  `docker-compose -f schemacrawler.yml -f mysql.yml up -d`
- Create a test MySQL database schema, run
  `docker exec -it docker-compose_schemacrawler_1 ./_testdb/createtestschema.sh --url "jdbc:mysql://mysql:3306/books?disableMariaDbDriver&useInformationSchema=true" --user root --password schemacrawler --debug`

### Testing

- Start SchemaCrawler bash with
  `docker exec -it docker-compose_schemacrawler_1 /bin/bash`
- Run SchemaCrawler from Docker container bash
  `schemacrawler --server mysql --host mysql --database books --user schemacrawler --password schemacrawler --info-level minimum -c list`

### Tear Down

- To stop SchemaCrawler with MySQL, run
  `docker-compose -f schemacrawler.yml -f mysql.yml down -t0`



## IBM DB2


### Setup

- To start SchemaCrawler with IBM DB2, run
  `docker-compose -f schemacrawler.yml -f db2.yml up -d`
- Create a test IBM DB2 database schema, run
  `docker exec -it docker-compose_schemacrawler_1 ./_testdb/createtestschema.sh --url "jdbc:db2://db2:50000/schcrwlr:retrieveMessagesFromServerOnGetMessage=true;" --user schcrwlr --password schemacrawler --debug`

### Testing

- Start SchemaCrawler bash with
  `docker exec -it docker-compose_schemacrawler_1 /bin/bash`
- Run SchemaCrawler from Docker container bash
  `schemacrawler --server db2 --host db2 --database schcrwlr --user schcrwlr --password schemacrawler --info-level minimum -c list`

Connect to the IBM DB2 container if needed, run
`docker exec -it docker-compose_db2_1 /bin/bash`

### Tear Down

- To stop SchemaCrawler with IBM DB2, run
  `docker-compose -f schemacrawler.yml -f db2.yml down -t0`
