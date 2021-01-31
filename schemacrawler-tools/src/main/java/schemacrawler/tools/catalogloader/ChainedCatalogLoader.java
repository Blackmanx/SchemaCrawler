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

package schemacrawler.tools.catalogloader;

import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.tools.options.Config;

public class ChainedCatalogLoader extends BaseCatalogLoader implements Iterable<CatalogLoader> {

  private final List<CatalogLoader> chainedCatalogLoaders;

  public ChainedCatalogLoader(final List<CatalogLoader> chainedCatalogLoaders) {
    super(Integer.MIN_VALUE);
    requireNonNull(chainedCatalogLoaders);
    this.chainedCatalogLoaders = new ArrayList<>(chainedCatalogLoaders);
  }

  @Override
  public String getDescription() {
    return "Chain of all catalog loaders, called in turn by priority";
  }

  @Override
  public String getName() {
    return "chainloader";
  }

  @Override
  public Iterator<CatalogLoader> iterator() {
    return chainedCatalogLoaders.iterator();
  }

  @Override
  public void loadCatalog() throws SchemaCrawlerException {
    Catalog catalog = null;
    final Connection connection = getConnection();
    final SchemaCrawlerOptions schemaCrawlerOptions = getSchemaCrawlerOptions();
    final SchemaRetrievalOptions schemaRetrievalOptions = getSchemaRetrievalOptions();
    final Config additionalConfig = getAdditionalConfiguration();
    for (final CatalogLoader nextCatalogLoader : chainedCatalogLoaders) {
      nextCatalogLoader.setCatalog(catalog);
      nextCatalogLoader.setConnection(connection);
      nextCatalogLoader.setSchemaCrawlerOptions(schemaCrawlerOptions);
      nextCatalogLoader.setSchemaRetrievalOptions(schemaRetrievalOptions);
      nextCatalogLoader.setAdditionalConfiguration(additionalConfig);

      nextCatalogLoader.loadCatalog();

      catalog = nextCatalogLoader.getCatalog();
    }
    setCatalog(catalog);
  }

  @Override
  public String toString() {
    return "CatalogLoader [" + chainedCatalogLoaders + "]";
  }
}
