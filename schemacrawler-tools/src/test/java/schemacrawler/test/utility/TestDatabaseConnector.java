package schemacrawler.test.utility;

import schemacrawler.schemacrawler.DatabaseServerType;
import schemacrawler.tools.databaseconnector.DatabaseConnectionUrlBuilder;
import schemacrawler.tools.databaseconnector.DatabaseConnector;
import schemacrawler.tools.executable.commandline.PluginCommand;

/**
 * SchemaCrawler database support plug-in.
 *
 * <p>Plug-in to support a hypothetical RMDBS, "Test Database".
 *
 * @see <a href="https://www.schemacrawler.com">SchemaCrawler</a>
 */
public final class TestDatabaseConnector extends DatabaseConnector {

  public TestDatabaseConnector() throws Exception {
    super(
        new DatabaseServerType("test-db", "Test Database"),
        url -> url != null && url.startsWith("jdbc:test-db:"),
        (informationSchemaViewsBuilder, connection) ->
            informationSchemaViewsBuilder.fromResourceFolder("/test-db.information_schema"),
        (schemaRetrievalOptionsBuilder, connection) -> {},
        (limitOptionsBuilder) -> {},
        () -> DatabaseConnectionUrlBuilder.builder("jdbc:test-db:${database}"));

    Class.forName("schemacrawler.test.utility.TestDatabaseDriver");
  }

  @Override
  public PluginCommand getHelpCommand() {
    final PluginCommand pluginCommand = super.getHelpCommand();
    pluginCommand.addOption(
        "server",
        String.class,
        "--server=test-db%n" + "Loads SchemaCrawler plug-in for Test Database");
    return pluginCommand;
  }
}
