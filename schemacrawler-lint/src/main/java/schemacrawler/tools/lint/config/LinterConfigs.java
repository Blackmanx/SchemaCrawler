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
package schemacrawler.tools.lint.config;

import static java.util.Objects.requireNonNull;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import schemacrawler.SchemaCrawlerLogger;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.ObjectToString;
import us.fatehi.utility.string.StringFormat;

public class LinterConfigs implements Iterable<LinterConfig> {

  private static final SchemaCrawlerLogger LOGGER =
      SchemaCrawlerLogger.getLogger(LinterConfig.class.getName());

  private final List<LinterConfig> linterConfigs;

  private final Map<String, Object> config;

  public LinterConfigs(final Config config) {
    linterConfigs = new ArrayList<>();
    this.config = requireNonNull(config, "No configuration provided").getSubMap("");
  }

  public void add(final LinterConfig linterConfig) {
    if (linterConfig != null) {
      linterConfig.setContext(config);
      linterConfigs.add(linterConfig);
    }
  }

  /** @return the linterConfigs */
  public List<LinterConfig> getLinterConfigs() {
    return linterConfigs;
  }

  @Override
  public Iterator<LinterConfig> iterator() {
    return linterConfigs.iterator();
  }

  public void parse(final Reader reader) throws SchemaCrawlerException {
    requireNonNull(reader, "No input provided");

    try {
      final YAMLMapper mapper = new YAMLMapper();
      final CollectionType listType =
          mapper.getTypeFactory().constructCollectionType(List.class, LinterConfig.class);

      final List<LinterConfig> linterConfigs = mapper.readValue(reader, listType);
      for (final LinterConfig linterConfig : linterConfigs) {
        add(linterConfig);
      }
    } catch (final Exception e) {
      throw new SchemaCrawlerException("Could not read linter configs", e);
    }
    LOGGER.log(Level.CONFIG, new StringFormat("Read <%d> linter configs", linterConfigs.size()));
  }

  public int size() {
    return linterConfigs.size();
  }

  @Override
  public String toString() {
    return ObjectToString.toString(this);
  }
}
