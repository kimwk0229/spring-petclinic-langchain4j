package org.springframework.samples.petclinic.chat;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.QueryRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * This filter illustrates how to conditionally skip retrieval. In some cases, retrieval
 * isn’t needed, such as when a user simply says "Hi". Additionally, only the clinic's
 * veterinarians are indexed in the Embedding Store.
 * <p>
 * To implement this, a custom {@link QueryRouter} is the simplest approach. When
 * retrieval is unnecessary, the QueryRouter returns an empty list, indicating that the
 * query won’t be routed to any {@link ContentRetriever}.
 * <p>
 * Decision-making relies on an LLM, which determines whether retrieval is needed based on
 * the user's query.
 * <p>
 *
 * @see <a href=
 * "https://github.com/langchain4j/langchain4j-examples/blob/main/rag-examples/src/main/java/_3_advanced/_06_Advanced_RAG_Skip_Retrieval_Example.java">_06_Advanced_RAG_Skip_Retrieval_Example.java</a>
 */
class VetQueryRouter implements QueryRouter {

	private static final Logger LOGGER = LoggerFactory.getLogger(VetQueryRouter.class);

	private static final PromptTemplate PROMPT_TEMPLATE = PromptTemplate.from("""
			Is the following query related to one or more veterinarians of the pet clinic?
			Answer only 'yes' or 'no'.
			Query: {{it}}
			""");

	private final ContentRetriever vetContentRetriever;

	private final ChatLanguageModel chatLanguageModel;

	public VetQueryRouter(ChatLanguageModel chatLanguageModel, ContentRetriever vetContentRetriever) {
		this.chatLanguageModel = chatLanguageModel;
		this.vetContentRetriever = vetContentRetriever;
	}

	@Override
	public Collection<ContentRetriever> route(Query query) {
		Prompt prompt = PROMPT_TEMPLATE.apply(query.text());

		AiMessage aiMessage = chatLanguageModel.generate(prompt.toUserMessage()).content();
		LOGGER.debug("LLM decided: {}", aiMessage.text());

		if (aiMessage.text().toLowerCase().contains("yes")) {
			return singletonList(vetContentRetriever);
		}
		return emptyList();
	}

}
