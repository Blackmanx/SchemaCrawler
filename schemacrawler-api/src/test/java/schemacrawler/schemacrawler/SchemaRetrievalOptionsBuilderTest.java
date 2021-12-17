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

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import schemacrawler.plugin.EnumDataTypeInfo;
import schemacrawler.plugin.EnumDataTypeInfo.EnumDataTypeTypes;

public class SchemaRetrievalOptionsBuilderTest {

  @Test
  public void dbMetaData() throws SQLException {

    final DatabaseMetaData dbMetaData = mock(DatabaseMetaData.class);
    when(dbMetaData.supportsCatalogsInTableDefinitions()).thenReturn(false);
    when(dbMetaData.supportsSchemasInTableDefinitions()).thenReturn(true);
    when(dbMetaData.getIdentifierQuoteString()).thenReturn("@");

    final Connection connection = mock(Connection.class);
    when(connection.getMetaData()).thenReturn(dbMetaData);

    SchemaRetrievalOptionsBuilder builder;

    builder = SchemaRetrievalOptionsBuilder.builder();
    assertThat(builder.supportsCatalogs, is(true));
    assertThat(builder.supportsSchemas, is(true));
    assertThat(builder.overridesTypeMap, isEmpty());
    assertThat(builder.identifierQuoteString, is(""));
    builder.fromConnnection(connection);
    assertThat(builder.supportsCatalogs, is(false));
    assertThat(builder.supportsSchemas, is(true));
    assertThat(builder.overridesTypeMap, isPresent());
    assertThat(builder.identifierQuoteString, is("@"));
  }

  @Test
  public void dbMetaData_none() throws SQLException {

    SchemaRetrievalOptionsBuilder builder;

    builder = SchemaRetrievalOptionsBuilder.builder();
    assertThat(builder.supportsCatalogs, is(true));
    assertThat(builder.supportsSchemas, is(true));
    assertThat(builder.overridesTypeMap, isEmpty());
    builder.fromConnnection(null);
    assertThat(builder.supportsCatalogs, is(true));
    assertThat(builder.supportsSchemas, is(true));
    assertThat(builder.overridesTypeMap, isEmpty());

    final Connection connection = mock(Connection.class);
    when(connection.getMetaData()).thenThrow(SQLException.class);

    builder = SchemaRetrievalOptionsBuilder.builder();
    assertThat(builder.supportsCatalogs, is(true));
    assertThat(builder.supportsSchemas, is(true));
    assertThat(builder.overridesTypeMap, isEmpty());
    builder.fromConnnection(connection);
    assertThat(builder.supportsCatalogs, is(true));
    assertThat(builder.supportsSchemas, is(true));
    assertThat(builder.overridesTypeMap, isPresent());
  }

  @Test
  public void dbMetaData_overrides() throws SQLException {

    final DatabaseMetaData dbMetaData = mock(DatabaseMetaData.class);
    when(dbMetaData.supportsCatalogsInTableDefinitions()).thenReturn(false);
    when(dbMetaData.supportsSchemasInTableDefinitions()).thenReturn(true);
    when(dbMetaData.getIdentifierQuoteString()).thenReturn("#");

    final Connection connection = mock(Connection.class);
    when(connection.getMetaData()).thenReturn(dbMetaData);

    SchemaRetrievalOptionsBuilder builder;

    builder = SchemaRetrievalOptionsBuilder.builder();
    builder.withSupportsCatalogs();
    builder.withSupportsSchemas();
    builder.withIdentifierQuoteString("@");

    assertThat(builder.supportsCatalogs, is(true));
    assertThat(builder.overridesSupportsCatalogs, isPresentAndIs(true));
    assertThat(builder.supportsSchemas, is(true));
    assertThat(builder.overridesSupportsSchemas, isPresentAndIs(true));
    assertThat(builder.overridesTypeMap, isEmpty());
    assertThat(builder.identifierQuoteString, is("@"));
    builder.fromConnnection(connection);
    assertThat(builder.supportsCatalogs, is(true));
    assertThat(builder.overridesSupportsCatalogs, isPresentAndIs(true));
    assertThat(builder.supportsSchemas, is(true));
    assertThat(builder.overridesSupportsSchemas, isPresentAndIs(true));
    assertThat(builder.overridesTypeMap, isPresent());
    assertThat(builder.identifierQuoteString, is("@"));

    builder = SchemaRetrievalOptionsBuilder.builder();
    builder.withTypeMap(new HashMap<>());
    assertThat(builder.overridesTypeMap, isPresent());
    builder.fromConnnection(connection);
    assertThat(builder.overridesTypeMap, isPresent());

    when(dbMetaData.getIdentifierQuoteString()).thenReturn("\t");
    builder = SchemaRetrievalOptionsBuilder.builder();
    builder.fromConnnection(connection);
    assertThat(builder.identifierQuoteString, is(""));
  }

  @Test
  public void dbServerType() {
    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder();

    assertThat(builder.dbServerType, is(DatabaseServerType.UNKNOWN));

    builder.withDatabaseServerType(new DatabaseServerType("newdb", "New Database"));
    assertThat(builder.dbServerType.getDatabaseSystemIdentifier(), is("newdb"));

    builder.withDatabaseServerType(null);
    assertThat(builder.dbServerType, is(DatabaseServerType.UNKNOWN));
  }

  @Test
  public void enumDataTypeHelper() {
    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder();

    assertThat(
        builder.enumDataTypeHelper.getEnumDataTypeInfo(null, null, null).getType(),
        is(EnumDataTypeInfo.EnumDataTypeTypes.not_enumerated));

    builder.withEnumDataTypeHelper(
        (column, columnDataType, connection) ->
            new EnumDataTypeInfo(EnumDataTypeTypes.enumerated_column, emptyList()));
    assertThat(
        builder.enumDataTypeHelper.getEnumDataTypeInfo(null, null, null).getType(),
        is(EnumDataTypeInfo.EnumDataTypeTypes.enumerated_column));

    builder.withEnumDataTypeHelper(null);
    assertThat(
        builder.enumDataTypeHelper.getEnumDataTypeInfo(null, null, null).getType(),
        is(EnumDataTypeInfo.EnumDataTypeTypes.not_enumerated));
  }

  @Test
  public void fromOptions() {
    final SchemaRetrievalOptions options =
        SchemaRetrievalOptionsBuilder.newSchemaRetrievalOptions();
    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder(options);
    assertThat(builder.supportsCatalogs, is(true));
    assertThat(builder.supportsSchemas, is(true));
    assertThat(builder.overridesTypeMap, isEmpty());
  }

  @Test
  public void fromOptions_null() {
    final SchemaRetrievalOptionsBuilder builder =
        SchemaRetrievalOptionsBuilder.builder().fromOptions(null);
    assertThat(builder.supportsCatalogs, is(true));
    assertThat(builder.supportsSchemas, is(true));
    assertThat(builder.overridesTypeMap, isEmpty());
  }

  @Test
  public void identifierQuoteString() {
    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder();

    assertThat(builder.identifierQuoteString, is(""));

    builder.withIdentifierQuoteString("@");
    assertThat(builder.identifierQuoteString, is("@"));

    builder.withoutIdentifierQuoteString();
    assertThat(builder.identifierQuoteString, is(""));

    builder.withIdentifierQuoteString(null);
    assertThat(builder.identifierQuoteString, is(""));

    builder.withIdentifierQuoteString("\t");
    assertThat(builder.identifierQuoteString, is(""));
  }

  @Test
  public void informationSchemaViews() {

    final InformationSchemaViews informationSchemaViews =
        InformationSchemaViewsBuilder.builder()
            .withSql(InformationSchemaKey.ADDITIONAL_COLUMN_ATTRIBUTES, "SELECT * FROM DUAL")
            .toOptions();

    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder();

    assertThat(builder.getInformationSchemaViews().isEmpty(), is(true));

    builder.withInformationSchemaViews(informationSchemaViews);
    assertThat(builder.getInformationSchemaViews().isEmpty(), is(false));

    builder.withInformationSchemaViews(null);
    assertThat(builder.getInformationSchemaViews().isEmpty(), is(true));
  }

  @Test
  public void metadataRetrievalStrategy() {
    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder();

    MetadataRetrievalStrategy metadataRetrievalStrategy;

    metadataRetrievalStrategy =
        builder.get(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy);
    assertThat(metadataRetrievalStrategy, is(MetadataRetrievalStrategy.metadata));

    builder.with(
        SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy,
        MetadataRetrievalStrategy.data_dictionary_all);
    metadataRetrievalStrategy =
        builder.get(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy);
    assertThat(metadataRetrievalStrategy, is(MetadataRetrievalStrategy.data_dictionary_all));

    assertThat(builder.get(null), is(nullValue()));

    // -- Set with variations of null arguments

    // 1.
    // Setup
    builder.with(
        SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy,
        MetadataRetrievalStrategy.data_dictionary_all);
    metadataRetrievalStrategy =
        builder.get(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy);
    assertThat(metadataRetrievalStrategy, is(MetadataRetrievalStrategy.data_dictionary_all));
    // Test
    builder.with(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy, null);
    metadataRetrievalStrategy =
        builder.get(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy);
    assertThat(metadataRetrievalStrategy, is(MetadataRetrievalStrategy.metadata));

    // 2.
    // Setup
    builder.with(
        SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy,
        MetadataRetrievalStrategy.data_dictionary_all);
    metadataRetrievalStrategy =
        builder.get(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy);
    assertThat(metadataRetrievalStrategy, is(MetadataRetrievalStrategy.data_dictionary_all));
    // Test
    builder.with(null, MetadataRetrievalStrategy.metadata);
    metadataRetrievalStrategy =
        builder.get(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy);
    assertThat(metadataRetrievalStrategy, is(MetadataRetrievalStrategy.data_dictionary_all));

    // 3.
    // Setup
    builder.with(
        SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy,
        MetadataRetrievalStrategy.data_dictionary_all);
    metadataRetrievalStrategy =
        builder.get(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy);
    assertThat(metadataRetrievalStrategy, is(MetadataRetrievalStrategy.data_dictionary_all));
    // Test
    builder.with(null, null);
    metadataRetrievalStrategy =
        builder.get(SchemaInfoMetadataRetrievalStrategy.foreignKeysRetrievalStrategy);
    assertThat(metadataRetrievalStrategy, is(MetadataRetrievalStrategy.data_dictionary_all));
  }

  @Test
  public void override_catalog_schema() {
    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder();

    assertThat(builder.overridesSupportsCatalogs, isEmpty());
    builder.withSupportsCatalogs();
    assertThat(builder.overridesSupportsCatalogs, isPresentAndIs(true));
    builder.withoutSupportsCatalogs();
    assertThat(builder.overridesSupportsCatalogs, isEmpty());
    builder.withDoesNotSupportCatalogs();
    assertThat(builder.overridesSupportsCatalogs, isPresentAndIs(false));

    assertThat(builder.overridesSupportsSchemas, isEmpty());
    builder.withSupportsSchemas();
    assertThat(builder.overridesSupportsSchemas, isPresentAndIs(true));
    builder.withoutSupportsSchemas();
    assertThat(builder.overridesSupportsSchemas, isEmpty());
    builder.withDoesNotSupportSchemas();
    assertThat(builder.overridesSupportsSchemas, isPresentAndIs(false));
  }

  @Test
  public void typeMap() {
    final SchemaRetrievalOptionsBuilder builder = SchemaRetrievalOptionsBuilder.builder();

    assertThat(builder.overridesTypeMap, isEmpty());

    builder.withTypeMap(new HashMap<>());
    assertThat(builder.overridesTypeMap, isPresent());
    assertThat(builder.overridesTypeMap.get().size(), is(0));

    builder.withTypeMap(null);
    assertThat(builder.overridesTypeMap, isEmpty());
  }
}
