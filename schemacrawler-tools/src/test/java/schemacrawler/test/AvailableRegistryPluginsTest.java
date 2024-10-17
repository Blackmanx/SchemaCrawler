package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.junit.jupiter.api.condition.JRE.JAVA_8;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.hsqldb.jdbc.JDBCDriver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.JRE;
import org.testcontainers.jdbc.ContainerDatabaseDriver;
import schemacrawler.tools.catalogloader.CatalogLoaderRegistry;
import schemacrawler.tools.databaseconnector.DatabaseConnectorRegistry;
import schemacrawler.tools.executable.CommandRegistry;
import schemacrawler.tools.registry.JDBCDriverRegistry;
import schemacrawler.tools.registry.PluginRegistry;
import schemacrawler.tools.registry.ScriptEngineRegistry;
import us.fatehi.test.utility.TestDatabaseDriver;
import us.fatehi.utility.property.PropertyName;

public class AvailableRegistryPluginsTest {

  @Test
  public void availableCatalogLoaders() {
    assertThat(
        getRegisteredPlugins(CatalogLoaderRegistry.getCatalogLoaderRegistry()),
        containsInAnyOrder("testloader", "schemacrawlerloader"));
  }

  @Test
  public void availableCommands() {
    assertThat(
        getRegisteredPlugins(CommandRegistry.getCommandRegistry()),
        containsInAnyOrder("test-command"));
  }

  @Test
  public void availableScriptEngines() throws UnsupportedEncodingException {
    int size = ScriptEngineRegistry.getScriptEngineRegistry().getRegisteredPlugins().size();
    if (JRE.currentVersion() != JAVA_8 && size == 0) {
      // No script engines ship with Java versions later than 8
      return;
    }

    final String scriptEngineName =
        ScriptEngineRegistry.getScriptEngineRegistry().getRegisteredPlugins().stream()
            .findAny()
            .get()
            .getName();

    assertThat(scriptEngineName, containsStringIgnoringCase("Nashorn"));
  }

  @Test
  public void availableJDBCDrivers() throws UnsupportedEncodingException {
    assertThat(
        getRegisteredPlugins(JDBCDriverRegistry.getJDBCDriverRegistry()),
        containsInAnyOrder(
            JDBCDriver.class.getName(),
            TestDatabaseDriver.class.getName(),
            ContainerDatabaseDriver.class.getName()));
  }

  @Test
  public void availableServers() {
    assertThat(
        getRegisteredPlugins(DatabaseConnectorRegistry.getDatabaseConnectorRegistry()),
        containsInAnyOrder("test-db"));
  }

  private List<String> getRegisteredPlugins(final PluginRegistry registry) {
    final List<String> commands = new ArrayList<>();
    final Collection<PropertyName> registeredPlugins = registry.getRegisteredPlugins();
    for (final PropertyName registeredPlugin : registeredPlugins) {
      commands.add(registeredPlugin.getName());
    }
    return commands;
  }
}
