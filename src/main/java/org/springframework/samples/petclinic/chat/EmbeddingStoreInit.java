package org.springframework.samples.petclinic.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Loads the veterinarians data into an Embedding Store for the purpose of RAG
 * functionality.
 *
 * RAG(Retrieval Augmented Generation) 기능을 위해 수의사 데이터를 임베딩 스토어에 로드합니다.
 *
 * @author Oded Shopen
 * @author Antoine Rey
 */
@Component
public class EmbeddingStoreInit {

	private final Logger logger = LoggerFactory.getLogger(EmbeddingStoreInit.class);

	private final InMemoryEmbeddingStore<TextSegment> embeddingStore;

	private final EmbeddingModel embeddingModel;

	private final VetRepository vetRepository;

	public EmbeddingStoreInit(InMemoryEmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel,
			VetRepository vetRepository) {
		this.embeddingStore = embeddingStore;
		this.embeddingModel = embeddingModel;
		this.vetRepository = vetRepository;
	}

	@EventListener
	public void loadVetDataToEmbeddingStoreOnStartup(ApplicationStartedEvent event) {
		// 애플리케이션 시작 시 수의사 데이터를 임베딩 스토어에 로드합니다.
		// Loads veterinarian data into the embedding store on application startup.

		var pageable = PageRequest.of(0, Integer.MAX_VALUE);
		Page<Vet> vetsPage = vetRepository.findAll(pageable);

		var vetsAsJson = convertListToJson(vetsPage.getContent());

		var ingestor = EmbeddingStoreIngestor.builder()
			.documentSplitter(new DocumentByLineSplitter(1000, 200))
			.embeddingModel(embeddingModel)
			.embeddingStore(embeddingStore)
			.build();

		// JSON 형식의 수의사 데이터를 임베딩 스토어에 ingest 합니다.
		// Ingests veterinarian data in JSON format into the embedding store.
		ingestor.ingest(Document.from(vetsAsJson));

		// 인메모리 임베딩 스토어는 파일로 직렬화 및 역직렬화할 수 있습니다.
		// String filePath = "embedding.store";
		// embeddingStore.serializeToFile(filePath);
		// 인메모리 임베딩 스토어는 파일로 직렬화 및 역직렬화할 수 있습니다.
	}

	public String convertListToJson(List<Vet> vets) {
		var objectMapper = new ObjectMapper();

		try {
			// Convert List<Vet> to JSON string
			// List<Vet>를 JSON 문자열로 변환합니다.
			var jsonArray = new StringBuilder();

			for (Vet vet : vets) {
				// 각 Vet 객체를 JSON 문자열로 변환하고 줄바꿈 문자로 구분합니다.
				// Converts each Vet object to a JSON string and separates them with a
				// newline character.
				var jsonElement = objectMapper.writeValueAsString(vet);
				jsonArray.append(jsonElement).append("\n"); // For use of the
															// DocumentByLineSplitter
			}

			return jsonArray.toString();
		}
		catch (JsonProcessingException e) {
			// 수의사 목록에서 JSON을 생성하는 중 문제가 발생하면 오류를 로깅합니다.
			// Logs an error if problems are encountered when generating JSON from the
			// vets list.
			logger.error("Problems encountered when generating JSON from the vets list", e);
			return null;
		}
	}

}
