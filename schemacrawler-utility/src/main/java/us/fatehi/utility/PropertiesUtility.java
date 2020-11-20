/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2020, Sualeh Fatehi <sualeh@hotmail.com>.
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
package us.fatehi.utility;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.isBlank;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import us.fatehi.utility.ioresource.InputResource;
import us.fatehi.utility.string.StringFormat;

public class PropertiesUtility {

  private static final Logger LOGGER = Logger.getLogger(PropertiesUtility.class.getName());

  public static String getSystemConfigurationProperty(final String key, final String defaultValue) {
    final String systemPropertyValue = System.getProperty(key);
    if (!isBlank(systemPropertyValue)) {
      LOGGER.log(
          Level.CONFIG,
          new StringFormat("Using value from system property <%s=%s>", key, systemPropertyValue));
      return systemPropertyValue;
    }

    final String envVariableValue = System.getenv(key);
    if (!isBlank(envVariableValue)) {
      LOGGER.log(
          Level.CONFIG,
          new StringFormat(
              "Using value from enivronmental variable <%s=%s>", key, envVariableValue));
      return envVariableValue;
    }

    return defaultValue;
  }

  /**
   * Loads a properties file.
   *
   * @param inputResource Properties resource.
   * @return Properties
   */
  public static Properties loadProperties(final InputResource inputResource) {
    requireNonNull(inputResource, "No input resource provided");
    LOGGER.log(Level.INFO, new StringFormat("Loading properties from <%s>", inputResource));

    try (final Reader reader = inputResource.openNewInputReader(UTF_8); ) {
      final Properties properties = new Properties();
      properties.load(reader);
      return properties;
    } catch (final IOException e) {
      LOGGER.log(
          Level.WARNING, String.format("Cannot load properties from <%s>", inputResource), e);
      return new Properties();
    }
  }

  /**
   * Copies properties into a map.
   *
   * @param properties Properties to copy
   * @return Map of properties and values
   */
  public static Map<String, String> propertiesMap(final Properties properties) {
    final Map<String, String> propertiesMap = new HashMap<>();
    if (properties != null) {
      final Set<Entry<Object, Object>> entries = properties.entrySet();
      for (final Entry<Object, Object> entry : entries) {
        propertiesMap.put((String) entry.getKey(), (String) entry.getValue());
      }
    }
    return propertiesMap;
  }

  private PropertiesUtility() {
    // Prevent instantiation
  }
}
