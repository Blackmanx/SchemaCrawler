/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2024, Sualeh Fatehi <sualeh@hotmail.com>.
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

package schemacrawler.tools.command.chatgpt.embeddings;

import static com.theokanning.openai.completion.chat.ChatMessageRole.SYSTEM;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import static java.util.Objects.requireNonNull;
import schemacrawler.schema.Table;
import us.fatehi.utility.string.ObjectToStringFormat;
import us.fatehi.utility.string.StringFormat;

public final class QueryService {

  private static final Logger LOGGER = Logger.getLogger(QueryService.class.getCanonicalName());

  private static final int TOP_K = 16;

  private final TableEmbeddingService tableEmbeddingService;
  private final TableSimilarityService tableSimilarityService;

  public QueryService(final OpenAiService service) {
    requireNonNull(service, "No Open AI service provided");
    final EmbeddingService embeddingService = new EmbeddingService(service);
    tableEmbeddingService = new TableEmbeddingService(embeddingService);
    tableSimilarityService = new TableSimilarityService(embeddingService);
  }

  public void addTables(final Collection<Table> tables) {
    requireNonNull(tables, "No tables provided");

    LOGGER.log(Level.INFO, "Embedding all tables in the catalog");
    for (final Table table : tables) {
      final EmbeddedTable embeddingTable = tableEmbeddingService.embedTable(table);
      tableSimilarityService.addTable(embeddingTable);
    }
  }

  public Collection<ChatMessage> query(final String prompt) {
    LOGGER.log(Level.INFO, new StringFormat("Searching for tables matching prompt:%n%s", prompt));

    final Collection<ChatMessage> messages = new ArrayList<>();

    messages.add(
        new ChatMessage(
            SYSTEM.value(),
            "You are a helpful assistant, conversing with a user about "
                + "the database schema contained in a set of JSON documents. "
                + "Use the information from the JSON to provide accurate answers. "
                + "Use only columns or tables in joins that you do have information about. "
                + "If you do not have enough information, please state what you will need to complete the request. "));

    final Collection<EmbeddedTable> matchedTables = tableSimilarityService.query(prompt, TOP_K);
    LOGGER.log(Level.CONFIG, new ObjectToStringFormat("Tables matching prompt", matchedTables));

    for (final EmbeddedTable embeddedTable : matchedTables) {
      final ChatMessage chatMessage = new ChatMessage(SYSTEM.value(), embeddedTable.toJson());
      messages.add(chatMessage);
    }
    return messages;
  }
}
