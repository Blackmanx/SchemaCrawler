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

package schemacrawler.crawl;

import static java.lang.System.lineSeparator;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;
import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.UUID;

import schemacrawler.BaseProductVersion;
import schemacrawler.JvmSystemInfo;
import schemacrawler.OperatingSystemInfo;
import schemacrawler.ProductVersion;
import schemacrawler.Version;
import schemacrawler.schema.ConnectionInfo;
import schemacrawler.schema.CrawlInfo;

/** SchemaCrawler crawl information. */
final class MutableCrawlInfo implements CrawlInfo {

  private static final long serialVersionUID = 5982990326485881993L;

  public static final DateTimeFormatter DATE_TIME_FORMATTER;

  static {
    DATE_TIME_FORMATTER =
        new DateTimeFormatterBuilder()
            .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendLiteral('-')
            .appendValue(MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(DAY_OF_MONTH, 2)
            .appendLiteral(' ')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .toFormatter();
  }

  private final Instant crawlTimestamp;
  private final ProductVersion jvmVersion;
  private final ProductVersion operatingSystemVersion;
  private final UUID runId;
  private final ProductVersion schemaCrawlerVersion;
  private final ProductVersion databaseVersion;
  private final ProductVersion jdbcDriverVersion;

  MutableCrawlInfo(final ConnectionInfo connectionInfo) {
    requireNonNull(connectionInfo, "No connection information provided");

    schemaCrawlerVersion = Version.version();
    operatingSystemVersion = OperatingSystemInfo.operatingSystemInfo();
    jvmVersion = JvmSystemInfo.jvmSystemInfo();

    this.jdbcDriverVersion =
        new BaseProductVersion(connectionInfo.getDriverName(), connectionInfo.getDriverVersion());
    this.databaseVersion =
        new BaseProductVersion(
            connectionInfo.getDatabaseProductName(), connectionInfo.getDatabaseProductVersion());

    crawlTimestamp = Instant.now();
    runId = UUID.randomUUID();
  }

  @Override
  public String getCrawlTimestamp() {
    final ZonedDateTime dateTime = getCrawlTimestampUTC();
    return DATE_TIME_FORMATTER.format(dateTime);
  }

  @Override
  public Instant getCrawlTimestampInstant() {
    return crawlTimestamp;
  }

  /** {@inheritDoc} */
  @Override
  public ProductVersion getDatabaseVersion() {
    return databaseVersion;
  }

  /** {@inheritDoc} */
  @Override
  public ProductVersion getJdbcDriverVersion() {
    return jdbcDriverVersion;
  }

  /** {@inheritDoc} */
  @Override
  public ProductVersion getJvmVersion() {
    return jvmVersion;
  }

  /** {@inheritDoc} */
  @Override
  public ProductVersion getOperatingSystemVersion() {
    return operatingSystemVersion;
  }

  /** {@inheritDoc} */
  @Override
  public String getRunId() {
    return runId.toString();
  }

  /** {@inheritDoc} */
  @Override
  public ProductVersion getSchemaCrawlerVersion() {
    return schemaCrawlerVersion;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    final String crawlTimestampString = ISO_LOCAL_DATE_TIME.format(getCrawlTimestampUTC());

    final StringBuilder info = new StringBuilder(1024);
    info.append("-- generated by: ").append(schemaCrawlerVersion).append(lineSeparator());
    info.append("-- generated on: ").append(crawlTimestampString).append(lineSeparator());
    info.append("-- database: ").append(databaseVersion).append(lineSeparator());
    info.append("-- driver: ").append(jdbcDriverVersion).append(lineSeparator());
    info.append("-- operating system: ").append(operatingSystemVersion).append(lineSeparator());
    info.append("-- JVM system: ").append(jvmVersion).append(lineSeparator());

    return info.toString();
  }

  private ZonedDateTime getCrawlTimestampUTC() {
    return ZonedDateTime.ofInstant(crawlTimestamp, UTC);
  }
}
