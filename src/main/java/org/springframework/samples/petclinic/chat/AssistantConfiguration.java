package org.springframework.samples.petclinic.chat;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AssistantConfiguration {

	/**
	 * This chat memory will be used by an {@link Assistant}. 이 채팅 메모리는 {@link Assistant}에
	 * 의해 사용됩니다.
	 */
	@Bean
	ChatMemoryProvider chatMemoryProvider() {
		return memoryId -> MessageWindowChatMemory.withMaxMessages(10);
	}

	/**
	 * Creates an in-memory embedding store for text segments. 텍스트 세그먼트를 위한 인메모리 임베딩 저장소를
	 * 생성합니다.
	 */
	@Bean
	InMemoryEmbeddingStore<TextSegment> embeddingStore() {
		return new InMemoryEmbeddingStore<>();
	}

	/**
	 * Provides an instance of the AllMiniLmL6V2EmbeddingModel.
	 * AllMiniLmL6V2EmbeddingModel의 인스턴스를 제공합니다.
	 */
	@Bean
	EmbeddingModel embeddingModel() {
		return new AllMiniLmL6V2EmbeddingModel();
	}

	/**
	 * Creates a content retriever that uses an embedding store and an embedding model.
	 * 임베딩 저장소와 임베딩 모델을 사용하는 콘텐츠 검색기를 생성합니다.
	 */
	@Bean
	EmbeddingStoreContentRetriever contentRetriever(InMemoryEmbeddingStore<TextSegment> embeddingStore,
			EmbeddingModel embeddingModel) {
		return new EmbeddingStoreContentRetriever(embeddingStore, embeddingModel);
	}

	/**
	 * Configures the retrieval augmentor with a query router. 쿼리 라우터를 사용하여 검색 증강기를 구성합니다.
	 */
	@Bean
	RetrievalAugmentor retrievalAugmentor(ChatLanguageModel chatLanguageModel, ContentRetriever vetContentRetriever) {
		return DefaultRetrievalAugmentor.builder()
			.queryRouter(new VetQueryRouter(chatLanguageModel, vetContentRetriever))
			.build();
	}

}
