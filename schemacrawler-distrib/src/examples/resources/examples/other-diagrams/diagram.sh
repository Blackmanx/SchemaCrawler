#!/usr/bin/env bash
../../_schemacrawler/schemacrawler.sh --server=hsqldb --database=schemacrawler --user=sa --password= --info-level=standard -c script --sort-tables=false --script "$1"
