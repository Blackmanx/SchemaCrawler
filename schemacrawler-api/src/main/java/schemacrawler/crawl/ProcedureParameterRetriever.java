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
import static schemacrawler.schema.DataTypeType.user_defined;
import static schemacrawler.schemacrawler.InformationSchemaKey.PROCEDURE_COLUMNS;
import static schemacrawler.schemacrawler.SchemaInfoMetadataRetrievalStrategy.procedureParametersRetrievalStrategy;
import static us.fatehi.utility.Utility.isBlank;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.logging.Level;

import schemacrawler.SchemaCrawlerLogger;
import schemacrawler.filter.InclusionRuleFilter;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.ParameterModeType;
import schemacrawler.schema.ProcedureParameter;
import schemacrawler.schema.RoutineType;
import schemacrawler.schemacrawler.InformationSchemaViews;
import schemacrawler.schemacrawler.Query;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerSQLException;
import us.fatehi.utility.string.StringFormat;

/**
 * A retriever uses database metadata to get the details about the database procedure parameters.
 *
 * @author Sualeh Fatehi
 */
final class ProcedureParameterRetriever extends AbstractRetriever {

  private static final SchemaCrawlerLogger LOGGER =
      SchemaCrawlerLogger.getLogger(ProcedureParameterRetriever.class.getName());

  ProcedureParameterRetriever(
      final RetrieverConnection retrieverConnection,
      final MutableCatalog catalog,
      final SchemaCrawlerOptions options) {
    super(retrieverConnection, catalog, options);
  }

  void retrieveProcedureParameters(
      final NamedObjectList<MutableRoutine> allRoutines, final InclusionRule parameterInclusionRule)
      throws SQLException {
    requireNonNull(allRoutines, "No procedures provided");

    final InclusionRuleFilter<ProcedureParameter> parameterFilter =
        new InclusionRuleFilter<>(parameterInclusionRule, true);
    if (parameterFilter.isExcludeAll()) {
      LOGGER.log(Level.INFO, "Not retrieving procedure parameters, since this was not requested");
      return;
    }

    switch (getRetrieverConnection().get(procedureParametersRetrievalStrategy)) {
      case data_dictionary_all:
        LOGGER.log(
            Level.INFO, "Retrieving procedure parameters, using fast data dictionary retrieval");
        retrieveProcedureParametersFromDataDictionary(allRoutines, parameterFilter);
        break;

      case metadata:
        LOGGER.log(Level.INFO, "Retrieving procedure parameters");
        retrieveProcedureParametersFromMetadata(allRoutines, parameterFilter);
        break;

      default:
        LOGGER.log(Level.INFO, "Not retrieving procedure parameters");
        break;
    }
  }

  private void createProcedureParameter(
      final MetadataResultSet results,
      final NamedObjectList<MutableRoutine> allRoutines,
      final InclusionRuleFilter<ProcedureParameter> parameterFilter) {
    final String columnCatalogName = normalizeCatalogName(results.getString("PROCEDURE_CAT"));
    final String schemaName = normalizeSchemaName(results.getString("PROCEDURE_SCHEM"));
    final String procedureName = results.getString("PROCEDURE_NAME");
    String columnName = results.getString("COLUMN_NAME");
    final String specificName = results.getString("SPECIFIC_NAME");

    final ParameterModeType parameterMode =
        getProcedureParameterMode(
            results.getInt("COLUMN_TYPE", DatabaseMetaData.procedureColumnUnknown));

    LOGGER.log(
        Level.FINE,
        new StringFormat(
            "Retrieving procedure parameter <%s.%s.%s.%s.%s>",
            columnCatalogName, schemaName, procedureName, specificName, columnName));
    if (isBlank(columnName) && parameterMode == ParameterModeType.result) {
      columnName = "<return value>";
    }
    if (isBlank(columnName)) {
      return;
    }

    final Optional<MutableRoutine> optionalRoutine =
        allRoutines.lookup(
            new NamedObjectKey(columnCatalogName, schemaName, procedureName, specificName));
    if (!optionalRoutine.isPresent()) {
      return;
    }

    final MutableRoutine routine = optionalRoutine.get();
    if (routine.getRoutineType() != RoutineType.procedure) {
      return;
    }

    final MutableProcedure procedure = (MutableProcedure) routine;
    final MutableProcedureParameter parameter =
        lookupOrCreateProcedureParameter(procedure, columnName);
    if (parameterFilter.test(parameter)
        && belongsToSchema(procedure, columnCatalogName, schemaName)) {
      final int ordinalPosition = results.getInt("ORDINAL_POSITION", 0);
      final int dataType = results.getInt("DATA_TYPE", 0);
      final String typeName = results.getString("TYPE_NAME");
      final int length = results.getInt("LENGTH", 0);
      final int precision = results.getInt("PRECISION", 0);
      final boolean isNullable =
          results.getShort("NULLABLE", (short) DatabaseMetaData.procedureNullableUnknown)
              == (short) DatabaseMetaData.procedureNullable;
      final String remarks = results.getString("REMARKS");
      parameter.setOrdinalPosition(ordinalPosition);
      parameter.setParameterMode(parameterMode);
      parameter.setColumnDataType(
          lookupOrCreateColumnDataType(user_defined, procedure.getSchema(), dataType, typeName));
      parameter.setSize(length);
      parameter.setPrecision(precision);
      parameter.setNullable(isNullable);
      parameter.setRemarks(remarks);

      parameter.addAttributes(results.getAttributes());

      LOGGER.log(Level.FINER, new StringFormat("Adding parameter to procedure <%s>", parameter));
      procedure.addParameter(parameter);
    }
  }

  private ParameterModeType getProcedureParameterMode(final int columnType) {
    switch (columnType) {
      case DatabaseMetaData.procedureColumnIn:
        return ParameterModeType.in;
      case DatabaseMetaData.procedureColumnInOut:
        return ParameterModeType.inOut;
      case DatabaseMetaData.procedureColumnOut:
        return ParameterModeType.out;
      case DatabaseMetaData.procedureColumnResult:
        return ParameterModeType.result;
      case DatabaseMetaData.procedureColumnReturn:
        return ParameterModeType.returnValue;
      default:
        return ParameterModeType.unknown;
    }
  }

  private MutableProcedureParameter lookupOrCreateProcedureParameter(
      final MutableProcedure procedure, final String columnName) {
    final Optional<MutableProcedureParameter> parameterOptional =
        procedure.lookupParameter(columnName);
    final MutableProcedureParameter parameter =
        parameterOptional.orElseGet(() -> new MutableProcedureParameter(procedure, columnName));
    return parameter;
  }

  private void retrieveProcedureParametersFromDataDictionary(
      final NamedObjectList<MutableRoutine> allRoutines,
      final InclusionRuleFilter<ProcedureParameter> parameterFilter)
      throws SQLException {
    final InformationSchemaViews informationSchemaViews =
        getRetrieverConnection().getInformationSchemaViews();
    if (!informationSchemaViews.hasQuery(PROCEDURE_COLUMNS)) {
      throw new SchemaCrawlerSQLException("No procedure parameters SQL provided", null);
    }
    final Query procedureColumnsSql = informationSchemaViews.getQuery(PROCEDURE_COLUMNS);
    final Connection connection = getDatabaseConnection();
    try (final Statement statement = connection.createStatement();
        final MetadataResultSet results =
            new MetadataResultSet(procedureColumnsSql, statement, getSchemaInclusionRule())) {
      results.setDescription("retrieveProcedureParametersFromDataDictionary");
      while (results.next()) {
        createProcedureParameter(results, allRoutines, parameterFilter);
      }
    }
  }

  private void retrieveProcedureParametersFromMetadata(
      final NamedObjectList<MutableRoutine> allRoutines,
      final InclusionRuleFilter<ProcedureParameter> parameterFilter)
      throws SchemaCrawlerSQLException {
    for (final MutableRoutine routine : allRoutines) {
      if (routine.getRoutineType() != RoutineType.procedure) {
        continue;
      }

      final MutableProcedure procedure = (MutableProcedure) routine;
      LOGGER.log(Level.FINE, "Retrieving procedure parameters for " + procedure);
      try (final MetadataResultSet results =
          new MetadataResultSet(
              getMetaData()
                  .getProcedureColumns(
                      procedure.getSchema().getCatalogName(),
                      procedure.getSchema().getName(),
                      procedure.getName(),
                      null))) {
        while (results.next()) {
          createProcedureParameter(results, allRoutines, parameterFilter);
        }
      } catch (final SQLException e) {
        throw new SchemaCrawlerSQLException(
            String.format("Could not retrieve procedure parameters for procedure <%s>", procedure),
            e);
      }
    }
  }
}
