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
package schemacrawler.test.commandline.command;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import picocli.CommandLine;
import schemacrawler.tools.commandline.SchemaCrawlerShellCommands;
import schemacrawler.tools.commandline.state.ShellState;
import schemacrawler.tools.commandline.state.StateFactory;

public class SchemaCrawlerShellCommandsTest {

  @Test
  public void noArgs() {
    final String[] args = new String[] {"bad-command"};

    final SchemaCrawlerShellCommands optionsParser = new SchemaCrawlerShellCommands();
    final ShellState state = new ShellState();
    final CommandLine commandLine = new CommandLine(optionsParser, new StateFactory(state));

    assertThrows(CommandLine.UnmatchedArgumentException.class, () -> commandLine.parseArgs(args));
  }
}
