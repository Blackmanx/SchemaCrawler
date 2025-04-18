/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2025, Sualeh Fatehi <sualeh@hotmail.com>.
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

package us.fatehi.utility.scheduler;

import static us.fatehi.utility.PropertiesUtility.getSystemConfigurationProperty;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskRunners {

  private static final Logger LOGGER = Logger.getLogger(TaskRunners.class.getName());

  public static TaskRunner getTaskRunner(final String id, final int maxThreadsSuggested) {
    final boolean isSingleThreaded =
        Boolean.valueOf(
            getSystemConfigurationProperty("SC_SINGLE_THREADED", Boolean.FALSE.toString()));
    if (isSingleThreaded) {
      LOGGER.log(Level.CONFIG, "Loading database schema in the main thread");
      return new MainThreadTaskRunner(id);
    } else {
      LOGGER.log(Level.CONFIG, "Loading database schema using multiple threads");
      return new MultiThreadedTaskRunner(id, maxThreadsSuggested);
    }
  }
}
