/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2021, Sualeh Fatehi <sualeh@hotmail.com>.
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
package schemacrawler.crawl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import schemacrawler.BaseProductVersion;
import schemacrawler.inclusionrule.ExcludeAll;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableType;
import schemacrawler.schemacrawler.SchemaInfoLevel;
import schemacrawler.schemacrawler.SchemaReference;
import us.fatehi.utility.graph.DirectedEdge;
import us.fatehi.utility.graph.Vertex;

public class EqualsTest {

  @Test
  public void baseProductVersion() {
    EqualsVerifier.forClass(BaseProductVersion.class).verify();
  }

  @Test
  public void columnReference() {
    EqualsVerifier.forClass(AbstractColumnReference.class).verify();
  }

  @Test
  public void databaseObject() {
    class TestDatabaseObject extends AbstractDatabaseObject {

      /** */
      private static final long serialVersionUID = 6661972079180914099L;

      TestDatabaseObject(final Schema schema, final String name) {
        super(schema, name);
      }
    }

    EqualsVerifier.forClass(TestDatabaseObject.class)
        .suppress(Warning.STRICT_INHERITANCE)
        .withIgnoredFields("key", "attributeMap", "remarks")
        .verify();
  }

  @Test
  public void directedEdge() {
    EqualsVerifier.forClass(DirectedEdge.class).verify();
  }

  @Test
  public void inclusionRules() {
    EqualsVerifier.forClass(IncludeAll.class).verify();
    EqualsVerifier.forClass(ExcludeAll.class).verify();
  }

  @Test
  public void namedObject() {
    EqualsVerifier.forClass(AbstractNamedObject.class)
        .withIgnoredFields("key")
        .suppress(Warning.STRICT_INHERITANCE)
        .verify();
  }

  @Test
  public void namedObjectKey() {
    EqualsVerifier.forClass(NamedObjectKey.class).withNonnullFields("key").verify();
  }

  @Test
  public void namedObjectWithAttributes() {
    EqualsVerifier.forClass(AbstractNamedObjectWithAttributes.class)
        .withIgnoredFields("key", "attributeMap", "remarks")
        .suppress(Warning.STRICT_INHERITANCE)
        .verify();
  }

  @Test
  public void privilege() {
    final Table table1 = new MutableTable(new SchemaReference("catalog", "schema"), "table1");
    final Table table2 = new MutableTable(new SchemaReference("catalog", "schema"), "table2");

    EqualsVerifier.forClass(MutablePrivilege.class)
        .withIgnoredFields("key", "grants", "parent", "attributeMap", "remarks")
        .withPrefabValues(
            DatabaseObjectReference.class, new TableReference(table1), new TableReference(table2))
        .suppress(Warning.STRICT_INHERITANCE)
        .verify();
  }

  @Test
  public void property() {
    EqualsVerifier.forClass(AbstractProperty.class).verify();
  }

  @Test
  public void schemaInfoLevel() {
    EqualsVerifier.forClass(SchemaInfoLevel.class).verify();
  }

  @Test
  public void schemaReference() {
    EqualsVerifier.forClass(SchemaReference.class)
        .withIgnoredFields("key", "attributeMap")
        .verify();
  }

  @Test
  public void tableType() {
    EqualsVerifier.forClass(TableType.class).verify();
  }

  @Test
  public void vertex() {
    EqualsVerifier.forClass(Vertex.class).withIgnoredFields("attributes").verify();
  }

  @Test
  public void weakAssociation() {
    EqualsVerifier.forClass(WeakAssociation.class)
        .withNonnullFields("columnReferences")
        .withOnlyTheseFields("columnReferences")
        .verify();
  }
}
