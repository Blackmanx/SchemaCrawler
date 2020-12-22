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
package schemacrawler.tools.integration.diagram;

import static java.nio.file.Files.copy;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.requireNonNull;
import static schemacrawler.tools.integration.diagram.GraphvizUtility.isGraphvizAvailable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

import schemacrawler.SchemaCrawlerLogger;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import us.fatehi.utility.ProcessExecutor;
import us.fatehi.utility.string.FileContents;
import us.fatehi.utility.string.StringFormat;

final class GraphvizProcessExecutor extends AbstractGraphProcessExecutor {

  private static final SchemaCrawlerLogger LOGGER =
      SchemaCrawlerLogger.getLogger(GraphvizProcessExecutor.class.getName());

  private final List<String> graphvizOpts;

  GraphvizProcessExecutor(
      final Path dotFile,
      final Path outputFile,
      final DiagramOutputFormat diagramOutputFormat,
      final List<String> graphvizOpts)
      throws SchemaCrawlerException {
    super(dotFile, outputFile, diagramOutputFormat);

    this.graphvizOpts = requireNonNull(graphvizOpts, "No Graphviz options provided");
  }

  @Override
  public Boolean call() {

    final List<String> command = createDiagramCommand();
    LOGGER.log(
        Level.INFO, new StringFormat("Generating diagram using Graphviz:\n%s", command.toString()));

    final ProcessExecutor processExecutor = new ProcessExecutor();
    processExecutor.setCommandLine(command);

    Integer exitCode;
    try {
      exitCode = processExecutor.call();
    } catch (final Exception e) {
      LOGGER.log(
          Level.INFO,
          String.format("Could not generate diagram using Graphviz:%n%s", command.toString()),
          e);
      exitCode = Integer.MIN_VALUE;
    }
    final boolean successful = exitCode != null && exitCode == 0;

    LOGGER.log(
        Level.FINE,
        new StringFormat(
            "Graphviz stdout:%n%s", new FileContents(processExecutor.getProcessOutput())));
    if (!successful) {
      final Supplier<String> processError = new FileContents(processExecutor.getProcessError());
      System.err.println(processError);
      LOGGER.log(
          Level.SEVERE,
          new StringFormat(
              "Graphviz returned exit code <%d>%nGraphviz stderr:%n%s", exitCode, processError));
      retainDotFile(processExecutor.getCommand());
    } else {
      LOGGER.log(
          Level.FINE,
          new StringFormat(
              "Graphviz stderr:%n%s", new FileContents(processExecutor.getProcessError())));
      LOGGER.log(Level.INFO, new StringFormat("Generated diagram <%s>", outputFile));
    }

    return successful;
  }

  @Override
  public boolean canGenerate() {
    return isGraphvizAvailable();
  }

  private List<String> createDiagramCommand() {
    final List<String> command = new ArrayList<>();
    command.add("dot");

    command.addAll(graphvizOpts);

    command.add("-T");
    command.add(diagramOutputFormat.getFormat());
    command.add("-o");
    command.add(outputFile.toString());
    command.add(dotFile.toString());

    return command;
  }

  private void retainDotFile(final List<String> command) {
    try {
      // Find name of DOT file in local directory
      final Path parentPath = outputFile.normalize().getParent();
      if (parentPath == null) {
        throw new UnsupportedOperationException("Cannot use output file path");
      }
      final Path movedDotFile = parentPath.resolve(outputFile.getFileName() + ".dot");
      // Copy DOT file
      copy(dotFile, movedDotFile, REPLACE_EXISTING);

      // Print command to run
      command.remove(command.size() - 1);
      command.remove(command.size() - 1);
      command.add(outputFile.toString());
      command.add(movedDotFile.toString());

      LOGGER.log(
          Level.SEVERE,
          String.format(
              "Error generating diagram%nGenerate your diagram manually, using:%n%s",
              String.join(" ", command)));
    } catch (final IOException e) {
      LOGGER.log(Level.WARNING, "Could not retain generated DOT file", e);
    }
  }
}
