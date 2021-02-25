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
package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import schemacrawler.tools.executable.CommandDescription;

public class CommandDescriptionTest {

  @Test
  public void commandDescription() {
    EqualsVerifier.forClass(CommandDescription.class).withIgnoredFields("description").verify();
  }

  @Test
  public void compare() {
    final CommandDescription commandDescription1 = new CommandDescription("hello1", "world");
    final CommandDescription commandDescription2 = new CommandDescription("hello", "  ");
    assertThat(commandDescription1.compareTo(commandDescription2), is(equalTo(1)));

    assertThat(commandDescription1.compareTo(null), is(equalTo(-1)));
  }

  @Test
  public void testString() {
    final CommandDescription commandDescription1 = new CommandDescription("hello", "world");
    assertThat(commandDescription1.getName(), is(equalTo("hello")));
    assertThat(commandDescription1.getDescription(), is(equalTo("world")));
    assertThat(commandDescription1.toString(), is(equalTo("hello - world")));

    final CommandDescription commandDescription2 = new CommandDescription("hello", "  ");
    assertThat(commandDescription2.getName(), is(equalTo("hello")));
    assertThat(commandDescription2.getDescription(), is(equalTo("")));
    assertThat(commandDescription2.toString(), is(equalTo("hello")));
  }
}
