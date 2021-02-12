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

package schemacrawler.tools.command.script;

import static java.util.Objects.requireNonNull;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.logging.Level;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import schemacrawler.SchemaCrawlerLogger;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.tools.command.script.options.ScriptOptions;
import schemacrawler.tools.executable.BaseSchemaCrawlerCommand;
import us.fatehi.utility.ObjectToString;
import us.fatehi.utility.ioresource.InputResource;
import us.fatehi.utility.string.StringFormat;

/**
 * Main executor for the script engine integration.
 *
 * @author Sualeh Fatehi
 */
public final class ScriptCommand extends BaseSchemaCrawlerCommand<ScriptOptions> {

  private static final SchemaCrawlerLogger LOGGER =
      SchemaCrawlerLogger.getLogger(ScriptCommand.class.getName());

  static final String COMMAND = "script";

  private static void logScriptEngineDetails(
      final Level level, final ScriptEngineFactory scriptEngineFactory) {
    if (!LOGGER.isLoggable(level)) {
      return;
    }

    LOGGER.log(
        level,
        String.format(
            "Using script engine%n%s %s (%s %s)%nScript engine names: %s%nSupported file extensions: %s",
            scriptEngineFactory.getEngineName(),
            scriptEngineFactory.getEngineVersion(),
            scriptEngineFactory.getLanguageName(),
            scriptEngineFactory.getLanguageVersion(),
            ObjectToString.toString(scriptEngineFactory.getNames()),
            ObjectToString.toString(scriptEngineFactory.getExtensions())));
  }

  public ScriptCommand() {
    super(COMMAND);
  }

  @Override
  public void checkAvailability() throws Exception {
    getScriptEngine();
    // Check availability of script
    commandOptions
        .getResource()
        .orElseThrow(
            () -> new SchemaCrawlerException("No script found, " + commandOptions.getScript()));
  }

  /** {@inheritDoc} */
  @Override
  public void execute() throws Exception {
    requireNonNull(commandOptions, "No script language provided");
    checkCatalog();

    final Charset inputCharset = outputOptions.getInputCharset();

    final ScriptEngine scriptEngine = getScriptEngine();
    final InputResource inputResource = commandOptions.getResource().get();
    LOGGER.log(Level.CONFIG, new StringFormat("Evaluating script, ", inputResource));
    try (final Reader reader = inputResource.openNewInputReader(inputCharset);
        final Writer writer = outputOptions.openNewOutputWriter()) {

      // Set up the context
      scriptEngine.getContext().setWriter(writer);
      scriptEngine.put("catalog", catalog);
      scriptEngine.put("connection", connection);
      scriptEngine.put("chain", new CommandChain(this));

      // Evaluate the script
      if (scriptEngine instanceof Compilable) {
        final CompiledScript script = ((Compilable) scriptEngine).compile(reader);
        final Object result = script.eval();
        LOGGER.log(Level.INFO, new StringFormat("Script execution result:%n%s", result));
      } else {
        scriptEngine.eval(reader);
      }
    }
  }

  @Override
  public boolean usesConnection() {
    return true;
  }

  private ScriptEngine getScriptEngine() throws SchemaCrawlerException {
    final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    ScriptEngine scriptEngine = null;
    final String scriptingLanguage = commandOptions.getLanguage();
    LOGGER.log(Level.CONFIG, new StringFormat("Using script language <%s>", scriptingLanguage));
    try {
      scriptEngine = scriptEngineManager.getEngineByName(scriptingLanguage);
    } catch (final Exception e) {
      // Ignore exception
    }

    if (scriptEngine == null) {
      scriptEngine = scriptEngineManager.getEngineByExtension(scriptingLanguage);
    }

    if (scriptEngine == null) {
      throw new SchemaCrawlerException("Script engine not found");
    }

    logScriptEngineDetails(Level.CONFIG, scriptEngine.getFactory());

    return scriptEngine;
  }
}
