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
package schemacrawler.tools.command.script;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import schemacrawler.SchemaCrawlerLogger;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.tools.executable.BaseSchemaCrawlerCommand;
import schemacrawler.tools.executable.CommandRegistry;
import schemacrawler.tools.executable.SchemaCrawlerCommand;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.LanguageOptions;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.OutputOptionsBuilder;

/**
 * Allows chaining multiple executables together, that produce different artifacts, such as an image
 * and a HTML file.
 */
public final class CommandChain extends BaseSchemaCrawlerCommand<LanguageOptions> {

  private static final String COMMAND = "chain";

  private static final SchemaCrawlerLogger LOGGER =
      SchemaCrawlerLogger.getLogger(CommandChain.class.getName());

  private final CommandRegistry commandRegistry;
  private final List<SchemaCrawlerCommand<?>> scCommands;
  private final Config additionalConfig;

  /**
   * Copy configuration settings from another command.
   *
   * @param scCommand Other command
   * @throws SchemaCrawlerException On an exception
   */
  public CommandChain(final ScriptCommand scCommand) throws SchemaCrawlerException {
    super(COMMAND);

    requireNonNull(scCommand, "No command provided, for settings");

    commandRegistry = CommandRegistry.getCommandRegistry();
    scCommands = new ArrayList<>();

    // Copy all configuration
    additionalConfig = new Config(scCommand.getCommandOptions().getConfig());
    setSchemaCrawlerOptions(scCommand.getSchemaCrawlerOptions());
    setOutputOptions(scCommand.getOutputOptions());

    setCatalog(scCommand.getCatalog());
    setConnection(scCommand.getConnection());
    setIdentifiers(scCommand.getIdentifiers());
  }

  public final SchemaCrawlerCommand<?> addNext(
      final String command, final String outputFormat, final String outputFileName)
      throws SchemaCrawlerException {
    requireNonNull(command, "No command provided");
    requireNonNull(outputFormat, "No output format provided");
    requireNonNull(outputFileName, "No output file name provided");

    final Path outputFile = Paths.get(outputFileName);
    final OutputOptions outputOptions =
        OutputOptionsBuilder.builder(getOutputOptions())
            .withOutputFormatValue(outputFormat)
            .withOutputFile(outputFile)
            .toOptions();

    return addNextAndConfigureForExecution(command, outputOptions);
  }

  @Override
  public void checkAvailability() throws Exception {
    // Check the availability of the chain, even though there may be no
    // command in the chain until the actual point of execution
    checkAvailabilityChain();
  }

  @Override
  public void execute() throws Exception {
    checkCatalog();

    initializeChain();
    checkAvailabilityChain();
    executeChain();
  }

  @Override
  public boolean usesConnection() {
    return false;
  }

  private SchemaCrawlerCommand<?> addNextAndConfigureForExecution(
      final String command, final OutputOptions outputOptions) throws SchemaCrawlerException {
    try {
      final SchemaCrawlerCommand<?> scCommand =
          commandRegistry.configureNewCommand(
              command, schemaCrawlerOptions, additionalConfig, outputOptions);
      if (scCommand == null) {
        return null;
      }

      scCommand.setCatalog(catalog);
      scCommand.setConnection(connection);
      scCommand.setIdentifiers(identifiers);

      scCommands.add(scCommand);

      return scCommand;
    } catch (final SchemaCrawlerException e) {
      throw e;
    } catch (final Exception e) {
      throw new SchemaCrawlerException(
          String.format("Cannot chain command, unknown command <%s>", command), e);
    }
  }

  private void checkAvailabilityChain() throws Exception {
    if (scCommands.isEmpty()) {
      LOGGER.log(Level.INFO, "No command to execute");
      return;
    }

    for (final SchemaCrawlerCommand<?> scCommand : scCommands) {
      scCommand.checkAvailability();
    }
  }

  private void executeChain() throws Exception {
    if (scCommands.isEmpty()) {
      LOGGER.log(Level.INFO, "No command to execute");
      return;
    }

    for (final SchemaCrawlerCommand<?> scCommand : scCommands) {
      scCommand.execute();
    }
  }

  private void initializeChain() throws Exception {
    if (scCommands.isEmpty()) {
      LOGGER.log(Level.INFO, "No command to initialize");
      return;
    }

    for (final SchemaCrawlerCommand<?> scCommand : scCommands) {
      scCommand.initialize();
    }
  }
}
