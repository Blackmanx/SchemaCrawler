# SchemaCrawler - Offline Snapshot Example

SchemaCrawler allows you to save off your database metadata into an 
offline snapshot, for future use. Later, you can connect to this offline 
snapshot as you would to a regular database.

## How to Setup
1. Make sure that java is on your PATH

## How to Run
1. Follow the instructions in the [commandline](../commandline/commandline-readme.html) example. 
2. To create an offline snapshot, run 
   `schemacrawler.cmd --server=hsqldb --database=schemacrawler --user=sa --password= --info-level=maximum --command=serialize --output-format=ser -o=offline.ser.gz` 
   (use `schemacrawler.sh` instead of `schemacrawler.cmd` on Unix)
3. To use the offline snapshot, run 
   `schemacrawler.cmd --server=offline --database=offline.ser.gz --info-level=standard --command=schema` 
   (use `schemacrawler.sh` instead of `schemacrawler.cmd` on Unix)
## How to Experiment
1. Try other SchemaCrawler commands with the offline snapshot.
