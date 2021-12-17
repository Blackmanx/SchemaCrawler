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

package schemacrawler.tools.offline.jdbc;

import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;

public class OfflineConnectionUtility {

  private static class OfflineConnectionInvocationHandler implements InvocationHandler {

    private final Path offlineDatabasePath;

    public OfflineConnectionInvocationHandler(final Path offlineDatabasePath) {
      this.offlineDatabasePath = offlineDatabasePath;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
        throws SQLFeatureNotSupportedException {
      final String methodName = method.getName();
      switch (methodName) {
        case "close":
        case "setAutoCommit":
          // Do nothing
          return null;
        case "getOfflineDatabasePath":
          return offlineDatabasePath;
        case "isWrapperFor":
        case "isClosed":
          return false;
        case "isValid":
          return true;
        case "getTypeMap":
          return Collections.emptyMap();
        case "toString":
          return String.format(
              "schemacrawler.tools.offline.jdbc.OfflineConnection@%s",
              offlineDatabasePath.hashCode());
        default:
          throw new SQLFeatureNotSupportedException(
              String.format(
                  "Offline catalog snapshot connection does not support method <%s>", methodName),
              "HYC00");
      }
    }
  }

  public static OfflineConnection newOfflineConnection(final Path offlineDatabasePath) {
    requireNonNull(offlineDatabasePath, "No offline catalog snapshot path provided");

    final Path absoluteOfflineDatabasePath = offlineDatabasePath.toAbsolutePath();
    return (OfflineConnection)
        newProxyInstance(
            OfflineConnectionUtility.class.getClassLoader(),
            new Class[] {OfflineConnection.class},
            new OfflineConnectionInvocationHandler(absoluteOfflineDatabasePath));
  }

  private OfflineConnectionUtility() {
    // Prevent instantiation
  }
}
