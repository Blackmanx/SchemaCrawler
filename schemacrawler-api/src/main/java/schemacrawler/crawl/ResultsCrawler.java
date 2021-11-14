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

package schemacrawler.crawl;

import static java.util.Objects.requireNonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import schemacrawler.schema.ResultsColumns;
import schemacrawler.schemacrawler.exceptions.SchemaCrawlerSQLException;
import us.fatehi.utility.StopWatch;

/** SchemaCrawler uses database meta-data to get the details about the schema. */
public final class ResultsCrawler {

  private static final Logger LOGGER = Logger.getLogger(ResultsCrawler.class.getName());

  private final ResultSet results;

  /**
   * Constructs a SchemaCrawler object, from a result-set.
   *
   * @param results Result-set of data.
   */
  public ResultsCrawler(final ResultSet results) {
    // NOTE: Do not check if the result set is closed, since some JDBC
    // drivers like SQLite may not work
    this.results = requireNonNull(results, "No result-set specified");
  }

  /**
   * Crawls the database, to obtain result set metadata.
   *
   * @return Result set metadata
   */
  public ResultsColumns crawl() throws SQLException {

    final StopWatch stopWatch = new StopWatch("crawlResultSet");

    LOGGER.log(Level.FINE, "Crawling result set");

    try {
      final ResultsRetriever resultsRetriever = new ResultsRetriever(results);
      final ResultsColumns resultsColumns =
          stopWatch.time("retrieveResults", resultsRetriever::retrieveResults);

      LOGGER.log(Level.FINE, stopWatch.stringify());

      return resultsColumns;
    } catch (final SQLException e) {
      throw e;
    } catch (final Exception e) {
      throw new SchemaCrawlerSQLException("Could not retrieve result-set metadata", e);
    }
  }
}
