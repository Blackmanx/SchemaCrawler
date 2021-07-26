package schemacrawler.tools.command.text.schema.options;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import schemacrawler.tools.options.Config;

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
public class BaseSchemaTextOptionsBuilderTest {

  @Test
  public void hideRowCounts() {
    final Config config = SchemaTextOptionsBuilder.builder().hideRowCounts().toConfig();
    final SchemaTextOptions options =
        SchemaTextOptionsBuilder.builder().hideRowCounts().toOptions();

    SchemaTextOptionsBuilder builder;

    // On and off
    builder = SchemaTextOptionsBuilder.builder();
    assertThat(builder.toOptions().isHideTableRowCounts(), is(false));
    builder.hideRowCounts();
    assertThat(builder.toOptions().isHideTableRowCounts(), is(true));
    builder.hideRowCounts(false);
    assertThat(builder.toOptions().isHideTableRowCounts(), is(false));
    builder.hideRowCounts(true);
    assertThat(builder.toOptions().isHideTableRowCounts(), is(true));

    // From config
    builder = SchemaTextOptionsBuilder.builder();
    assertThat(builder.toOptions().isHideTableRowCounts(), is(false));
    builder.fromConfig(config);
    assertThat(builder.toOptions().isHideTableRowCounts(), is(true));
    builder.fromConfig(null);
    assertThat(builder.toOptions().isHideTableRowCounts(), is(true));

    // From options
    builder = SchemaTextOptionsBuilder.builder();
    assertThat(builder.toOptions().isHideTableRowCounts(), is(false));
    builder.fromOptions(options);
    assertThat(builder.toOptions().isHideTableRowCounts(), is(true));
    builder.fromOptions(null);
    assertThat(builder.toOptions().isHideTableRowCounts(), is(true));
  }

  @Test
  public void noAlternateKeyNames() {
    final Config config = SchemaTextOptionsBuilder.builder().noAlternateKeyNames().toConfig();
    final SchemaTextOptions options =
        SchemaTextOptionsBuilder.builder().noAlternateKeyNames().toOptions();

    SchemaTextOptionsBuilder builder;

    // On and off
    builder = SchemaTextOptionsBuilder.builder();
    assertThat(builder.toOptions().isHideAlternateKeyNames(), is(false));
    builder.noAlternateKeyNames();
    assertThat(builder.toOptions().isHideAlternateKeyNames(), is(true));
    builder.noAlternateKeyNames(false);
    assertThat(builder.toOptions().isHideAlternateKeyNames(), is(false));
    builder.noAlternateKeyNames(true);
    assertThat(builder.toOptions().isHideAlternateKeyNames(), is(true));

    // From config
    builder = SchemaTextOptionsBuilder.builder();
    assertThat(builder.toOptions().isHideAlternateKeyNames(), is(false));
    builder.fromConfig(config);
    assertThat(builder.toOptions().isHideAlternateKeyNames(), is(true));
    builder.fromConfig(null);
    assertThat(builder.toOptions().isHideAlternateKeyNames(), is(true));

    // From options
    builder = SchemaTextOptionsBuilder.builder();
    assertThat(builder.toOptions().isHideAlternateKeyNames(), is(false));
    builder.fromOptions(options);
    assertThat(builder.toOptions().isHideAlternateKeyNames(), is(true));
    builder.fromOptions(null);
    assertThat(builder.toOptions().isHideAlternateKeyNames(), is(true));
  }

  @Test
  public void noRoutineSpecificNames() {
    final Config config = SchemaTextOptionsBuilder.builder().noRoutineSpecificNames().toConfig();
    final SchemaTextOptions options =
        SchemaTextOptionsBuilder.builder().noRoutineSpecificNames().toOptions();

    SchemaTextOptionsBuilder builder;

    // On and off
    builder = SchemaTextOptionsBuilder.builder();
    assertThat(builder.toOptions().isHideRoutineSpecificNames(), is(false));
    builder.noRoutineSpecificNames();
    assertThat(builder.toOptions().isHideRoutineSpecificNames(), is(true));
    builder.noRoutineSpecificNames(false);
    assertThat(builder.toOptions().isHideRoutineSpecificNames(), is(false));
    builder.noRoutineSpecificNames(true);
    assertThat(builder.toOptions().isHideRoutineSpecificNames(), is(true));

    // From config
    builder = SchemaTextOptionsBuilder.builder();
    assertThat(builder.toOptions().isHideRoutineSpecificNames(), is(false));
    builder.fromConfig(config);
    assertThat(builder.toOptions().isHideRoutineSpecificNames(), is(true));
    builder.fromConfig(null);
    assertThat(builder.toOptions().isHideRoutineSpecificNames(), is(true));

    // From options
    builder = SchemaTextOptionsBuilder.builder();
    assertThat(builder.toOptions().isHideRoutineSpecificNames(), is(false));
    builder.fromOptions(options);
    assertThat(builder.toOptions().isHideRoutineSpecificNames(), is(true));
    builder.fromOptions(null);
    assertThat(builder.toOptions().isHideRoutineSpecificNames(), is(true));
  }

  @Test
  public void noTriggerNames() {
    final Config config = SchemaTextOptionsBuilder.builder().noTriggerNames().toConfig();
    final SchemaTextOptions options =
        SchemaTextOptionsBuilder.builder().noTriggerNames().toOptions();

    SchemaTextOptionsBuilder builder;

    // On and off
    builder = SchemaTextOptionsBuilder.builder();
    assertThat(builder.toOptions().isHideTriggerNames(), is(false));
    builder.noTriggerNames();
    assertThat(builder.toOptions().isHideTriggerNames(), is(true));
    builder.noTriggerNames(false);
    assertThat(builder.toOptions().isHideTriggerNames(), is(false));
    builder.noTriggerNames(true);
    assertThat(builder.toOptions().isHideTriggerNames(), is(true));

    // From config
    builder = SchemaTextOptionsBuilder.builder();
    assertThat(builder.toOptions().isHideTriggerNames(), is(false));
    builder.fromConfig(config);
    assertThat(builder.toOptions().isHideTriggerNames(), is(true));
    builder.fromConfig(null);
    assertThat(builder.toOptions().isHideTriggerNames(), is(true));

    // From options
    builder = SchemaTextOptionsBuilder.builder();
    assertThat(builder.toOptions().isHideTriggerNames(), is(false));
    builder.fromOptions(options);
    assertThat(builder.toOptions().isHideTriggerNames(), is(true));
    builder.fromOptions(null);
    assertThat(builder.toOptions().isHideTriggerNames(), is(true));
  }

  @Test
  public void sorts() {
    final Config config =
        SchemaTextOptionsBuilder.builder()
            .sortTableColumns()
            .sortIndexes()
            .sortForeignKeys()
            .toConfig();
    final SchemaTextOptions options =
        SchemaTextOptionsBuilder.builder()
            .sortTableColumns()
            .sortIndexes()
            .sortForeignKeys()
            .toOptions();

    SchemaTextOptionsBuilder builder;

    // On and off
    builder = SchemaTextOptionsBuilder.builder();
    assertThat(builder.toOptions().isAlphabeticalSortForTableColumns(), is(false));
    assertThat(builder.toOptions().isAlphabeticalSortForIndexes(), is(false));
    assertThat(builder.toOptions().isAlphabeticalSortForForeignKeys(), is(false));
    builder.sortTableColumns().sortIndexes().sortForeignKeys();
    assertThat(builder.toOptions().isAlphabeticalSortForTableColumns(), is(true));
    assertThat(builder.toOptions().isAlphabeticalSortForIndexes(), is(true));
    assertThat(builder.toOptions().isAlphabeticalSortForForeignKeys(), is(true));
    builder.sortTableColumns(false).sortIndexes(false).sortForeignKeys(false);
    assertThat(builder.toOptions().isAlphabeticalSortForTableColumns(), is(false));
    assertThat(builder.toOptions().isAlphabeticalSortForIndexes(), is(false));
    assertThat(builder.toOptions().isAlphabeticalSortForForeignKeys(), is(false));
    builder.sortTableColumns(true).sortIndexes(true).sortForeignKeys(true);
    assertThat(builder.toOptions().isAlphabeticalSortForTableColumns(), is(true));
    assertThat(builder.toOptions().isAlphabeticalSortForIndexes(), is(true));
    assertThat(builder.toOptions().isAlphabeticalSortForForeignKeys(), is(true));

    // From config
    builder = SchemaTextOptionsBuilder.builder();
    assertThat(builder.toOptions().isAlphabeticalSortForTableColumns(), is(false));
    assertThat(builder.toOptions().isAlphabeticalSortForIndexes(), is(false));
    assertThat(builder.toOptions().isAlphabeticalSortForForeignKeys(), is(false));
    builder.fromConfig(config);
    assertThat(builder.toOptions().isAlphabeticalSortForTableColumns(), is(true));
    assertThat(builder.toOptions().isAlphabeticalSortForIndexes(), is(true));
    assertThat(builder.toOptions().isAlphabeticalSortForForeignKeys(), is(true));
    builder.fromConfig(null);
    assertThat(builder.toOptions().isAlphabeticalSortForTableColumns(), is(true));
    assertThat(builder.toOptions().isAlphabeticalSortForIndexes(), is(true));
    assertThat(builder.toOptions().isAlphabeticalSortForForeignKeys(), is(true));

    // From options
    builder = SchemaTextOptionsBuilder.builder();
    assertThat(builder.toOptions().isAlphabeticalSortForTableColumns(), is(false));
    assertThat(builder.toOptions().isAlphabeticalSortForIndexes(), is(false));
    assertThat(builder.toOptions().isAlphabeticalSortForForeignKeys(), is(false));
    builder.fromOptions(options);
    assertThat(builder.toOptions().isAlphabeticalSortForTableColumns(), is(true));
    assertThat(builder.toOptions().isAlphabeticalSortForIndexes(), is(true));
    assertThat(builder.toOptions().isAlphabeticalSortForForeignKeys(), is(true));
    builder.fromOptions(null);
    assertThat(builder.toOptions().isAlphabeticalSortForTableColumns(), is(true));
    assertThat(builder.toOptions().isAlphabeticalSortForIndexes(), is(true));
    assertThat(builder.toOptions().isAlphabeticalSortForForeignKeys(), is(true));
  }
}
