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
 * <p>
 * 이 필터는 검색을 조건부로 건너뛰는 방법을 보여줍니다. 예를 들어, 사용자가 단순히 "안녕하세요"라고 말하는 경우처럼 검색이 필요하지 않은 경우가 있습니다.
 * 또한, 클리닉의 수의사들만 임베딩 스토어에 색인되어 있습니다. 이를 구현하는 가장 간단한 방법은 사용자 정의 {@link QueryRouter}를 사용하는
 * 것입니다. 검색이 불필요할 때, QueryRouter는 빈 리스트를 반환하여 쿼리가 어떤 {@link ContentRetriever}로도 라우팅되지 않음을
 * 나타냅니다.
 * <p>
 * 의사 결정은 LLM에 의존하며, LLM은 사용자 쿼리를 기반으로 검색이 필요한지 여부를 결정합니다.
 * <p>
 *
 * @see <a href=
 * "https://github.com/langchain4j/langchain4j-examples/blob/main/rag-examples/src/main/java/_3_advanced/_06_Advanced_RAG_Skip_Retrieval_Example.java">_06_Advanced_RAG_Skip_Retrieval_Example.java</a>
 */
class VetQueryRouter implements QueryRouter {

	private static final Logger LOGGER = LoggerFactory.getLogger(VetQueryRouter.class);

	// 다음 쿼리가 펫 클리닉의 한 명 이상의 수의사와 관련이 있습니까?
	// '예' 또는 '아니오'로만 답변하세요.
	// 쿼리: {{it}}
	private static final PromptTemplate PROMPT_TEMPLATE = PromptTemplate.from("""
			Is the following query related to one or more veterinarians of the pet clinic?
			Answer only 'yes' or 'no'.
			Query: {{it}}
			""");

	// 수의사 콘텐츠 검색기
	private final ContentRetriever vetContentRetriever;

	private final ChatLanguageModel chatLanguageModel;

	public VetQueryRouter(ChatLanguageModel chatLanguageModel, ContentRetriever vetContentRetriever) {
		this.chatLanguageModel = chatLanguageModel;
		this.vetContentRetriever = vetContentRetriever;
	}

	@Override
	public Collection<ContentRetriever> route(Query query) {
		Prompt prompt = PROMPT_TEMPLATE.apply(query.text()); // 쿼리 텍스트를 프롬프트 템플릿에 적용

		AiMessage aiMessage = chatLanguageModel.chat(prompt.toUserMessage()).aiMessage(); // LLM을
																							// 사용하여
																							// 채팅하고
																							// AI
																							// 메시지
																							// 가져오기
		LOGGER.debug("LLM decided: {}", aiMessage.text()); // LLM의 결정 로깅

		if (aiMessage.text().toLowerCase().contains("yes")) {
			return singletonList(vetContentRetriever);
		}
		return emptyList();
	}

}
