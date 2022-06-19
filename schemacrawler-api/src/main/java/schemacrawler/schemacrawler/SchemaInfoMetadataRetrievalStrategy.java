/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2022, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/

package schemacrawler.schemacrawler;

public enum SchemaInfoMetadataRetrievalStrategy {
  foreignKeysRetrievalStrategy("foreignkeys"),
  functionParametersRetrievalStrategy("functionparameters"),
  functionsRetrievalStrategy("functions"),
  indexesRetrievalStrategy("indexes"),
  primaryKeysRetrievalStrategy("primarykeys"),
  proceduresRetrievalStrategy("procedures"),
  procedureParametersRetrievalStrategy("procedureparameters"),
  tableColumnPrivilegesRetrievalStrategy("tablecolumnprivileges"),
  tableColumnsRetrievalStrategy("tablecolumns"),
  tablePrivilegesRetrievalStrategy("tableprivileges"),
  tablesRetrievalStrategy("tables"),
  typeInfoRetrievalStrategy("typeinfo"),
  ;

  private final String key;

  SchemaInfoMetadataRetrievalStrategy(final String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
