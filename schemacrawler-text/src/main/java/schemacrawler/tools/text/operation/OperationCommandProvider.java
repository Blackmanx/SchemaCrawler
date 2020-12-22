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
package schemacrawler.tools.text.operation;

import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.tools.executable.BaseCommandProvider;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.text.base.CommandProviderUtility;

public final class OperationCommandProvider extends BaseCommandProvider {

  public OperationCommandProvider() {
    super(CommandProviderUtility.operationCommands());
  }

  @Override
  public OperationCommand newSchemaCrawlerCommand(final String command, final Config config) {
    final OperationOptions operationOptions =
        OperationOptionsBuilder.builder().withCommand(command).fromConfig(config).toOptions();

    final OperationCommand scCommand = new OperationCommand(command);
    scCommand.setCommandOptions(operationOptions);
    return scCommand;
  }

  @Override
  public boolean supportsOutputFormat(final String command, final OutputOptions outputOptions) {
    return true;
  }

  @Override
  public boolean supportsSchemaCrawlerCommand(
      final String command,
      final SchemaCrawlerOptions schemaCrawlerOptions,
      final Config additionalConfiguration,
      final OutputOptions outputOptions) {
    // Check if the command is an operation
    final boolean isOperation = supportsCommand(command);

    /// Check if the command is a named query
    final boolean isNamedQuery;
    if (additionalConfiguration != null) {
      isNamedQuery = additionalConfiguration.containsKey(command);
    } else {
      isNamedQuery = false;
    }

    // Operation and query output is only in text or HTMl, but nevertheless some operations such as
    // count
    // can be represented on diagrams (since the catalog is annotated with attributes).
    // Also, if a query is part of a comma-separated list of commands, the run should not fail due
    // to a bad output format.
    // So no check is done for output format.
    final boolean supportsSchemaCrawlerCommand = isOperation || isNamedQuery;
    return supportsSchemaCrawlerCommand;
  }
}
