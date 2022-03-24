<!-- markdownlint-disable MD024 -->
# SchemaCrawler Tests with Docker Compose

## Pre-requisites

- Run a full SchemaCrawler build with `mvn -Ddistrib clean package`
- Or, for an incremental build, run `mvn -Ddistrib clean package` for the "schemacrawler-distrib", "schemacrawler-docker" and "schemacrawler-docker-compose" submodules in order



## PostgreSQL

### Setup

- To start SchemaCrawler with PostgreSQL, run
  `docker-compose -f schemacrawler.yml -f postgresql.yml up -d`
- Create a test PostgreSQL database schema, run
  `docker exec -it schemacrawler ./testdb/createtestschema.sh --url "jdbc:postgresql://postgresql:5432/schemacrawler?ApplicationName=SchemaCrawler;loggerLevel=DEBUG" --user schemacrawler --password schemacrawler --debug`

### Testing

- Start SchemaCrawler bash with
  `docker exec -it schemacrawler /bin/bash`
- Run SchemaCrawler from Docker container bash
  `schemacrawler --server postgresql --host postgresql --database schemacrawler --user schemacrawler --password schemacrawler --info-level minimum -c list`
- Output can be created with `--output-file share/out.txt`

### Tear Down

- To stop SchemaCrawler with PostgreSQL, run
  `docker-compose -f schemacrawler.yml -f postgresql.yml down -t0`



## Oracle

### Setup

#### Oracle 18

- To start SchemaCrawler with Oracle, run
  `docker-compose -f schemacrawler.yml -f oracle.yml up -d`
- Create a test Oracle database schema, run
  `docker exec -it schemacrawler ./testdb/createtestschema.sh --url "jdbc:oracle:thin:@//oracle:1521/xepdb1" --user "SYS AS SYSDBA" --password test --scripts-resource /oracle.scripts.txt --debug`

#### Oracle 11

- To start SchemaCrawler with Oracle, run
  `docker-compose -f schemacrawler.yml -f oracle-11g.yml up -d`
- Create a test Oracle database schema, run
  `docker exec -it schemacrawler ./testdb/createtestschema.sh --url "jdbc:oracle:thin:@oracle:1521:xe" --user "SYS AS SYSDBA" --password test --scripts-resource /oracle-11g.scripts.txt --debug`

### Testing

#### Oracle 18

- Start SchemaCrawler bash with
  `docker exec -it schemacrawler /bin/bash`
- Run SchemaCrawler from Docker container bash
  `schemacrawler --server oracle --host oracle --database xepdb1 --user "SYS AS SYSDBA" --password test --schemas BOOKS --info-level minimum -c list`
- Output can be created with `--output-file share/out.txt`

#### Oracle 11

- Start SchemaCrawler bash with
  `docker exec -it schemacrawler /bin/bash`
- Run SchemaCrawler from Docker container bash
  `schemacrawler --url "jdbc:oracle:thin:@oracle:1521:xe" --user "SYS AS SYSDBA" --password test --schemas BOOKS --info-level minimum -c list`
- Output can be created with `--output-file share/out.txt`

### Tear Down

- To stop SchemaCrawler with Oracle, run
  `docker-compose -f schemacrawler.yml -f oracle.yml down -t0`



## Microsoft SQL Server

### Setup

- To start SchemaCrawler with Microsoft SQL Server, run
  `docker-compose -f schemacrawler.yml -f sqlserver.yml up -d`
- Create a test Microsoft SQL Server database schema, run
  `docker exec -it schemacrawler ./testdb/createtestschema.sh --url "jdbc:sqlserver://sqlserver:1433;databaseName=master;encrypt=false" --user SA --password Schem#Crawl3r --debug`

### Testing

- Start SchemaCrawler bash with
  `docker exec -it schemacrawler /bin/bash`
- Run SchemaCrawler from Docker container bash
  `schemacrawler --server sqlserver --host sqlserver --database BOOKS --schemas BOOKS\.dbo --user SA --password Schem#Crawl3r --info-level minimum -c list`
- Output can be created with `--output-file share/out.txt`

### Tear Down

- To stop SchemaCrawler with Microsoft SQL Server, run
  `docker-compose -f schemacrawler.yml -f sqlserver.yml down -t0`



## MySQL

### Setup

- To start SchemaCrawler with MySQL, run
  `docker-compose -f schemacrawler.yml -f mysql.yml up -d`
- Create a test MySQL database schema, run
  `docker exec -it schemacrawler ./testdb/createtestschema.sh --url "jdbc:mysql://mysql:3306/books?disableMariaDbDriver&useInformationSchema=true" --user root --password schemacrawler --debug`

### Testing

- Start SchemaCrawler bash with
  `docker exec -it schemacrawler /bin/bash`
- Run SchemaCrawler from Docker container bash
  `schemacrawler --server mysql --host mysql --database books --user schemacrawler --password schemacrawler --info-level minimum -c list`
- Output can be created with `--output-file share/out.txt`

### Tear Down

- To stop SchemaCrawler with MySQL, run
  `docker-compose -f schemacrawler.yml -f mysql.yml down -t0`



## IBM DB2


### Setup

- To start SchemaCrawler with IBM DB2, run
  `docker-compose -f schemacrawler.yml -f db2.yml up -d`
- Create a test IBM DB2 database schema, run
  `docker exec -it schemacrawler ./testdb/createtestschema.sh --url "jdbc:db2://db2:50000/schcrwlr:retrieveMessagesFromServerOnGetMessage=true;" --user schcrwlr --password schemacrawler --debug`

### Testing

- Start SchemaCrawler bash with
  `docker exec -it schemacrawler /bin/bash`
- Run SchemaCrawler from Docker container bash
  `schemacrawler --server db2 --host db2 --database schcrwlr --schemas SCHCRWLR --user schcrwlr --password schemacrawler --info-level minimum -c list`
- Output can be created with `--output-file share/out.txt`

Connect to the IBM DB2 container if needed, run
`docker exec -it db2 /bin/bash`

### Tear Down

- To stop SchemaCrawler with IBM DB2, run
  `docker-compose -f schemacrawler.yml -f db2.yml down -t0`
