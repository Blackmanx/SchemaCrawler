# SchemaCrawler - Weak Associations Example

## Description
SchemaCrawler allows you to provide information about weak associations, or references from 
a column in one table to another table in a YAML file 
with the `--attributes-file` command-line switch.

## How to Setup
1. Make sure that java is on your PATH
2. Start a command shell in the `_downloader` directory 
3. Run `download.cmd jackson` (or `download.sh jackson` on Unix) to
   install serialization support using Jackson

## How to Run
1. Make sure that java is on your PATH
2. Start the test database server by following instructions in the `_testdb/README.html` file
3. Edit `_schemacrawler/config/schemacrawler.config.properties` and uncomment
   `schemacrawler.format.show_weak_associations` and set it to br true.
4. Start a command shell in the `weak-associations` example directory 
5. Run `weak-associations.cmd weak-associations.yaml` (or `weak-associations.sh weak-associations.yaml` on Unix). 
6. View the image in `weak-associations.png` to see the weak associations that were loaded from the YAML file

## How to Experiment
- Modify `weak-associations.yaml` and return the command
