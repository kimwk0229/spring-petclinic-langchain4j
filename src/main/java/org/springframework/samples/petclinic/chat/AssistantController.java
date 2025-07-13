package org.springframework.samples.petclinic.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
class AssistantController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AssistantController.class);

	private final Assistant assistant;

	private final ExecutorService nonBlockingService = Executors.newCachedThreadPool();

	AssistantController(Assistant assistant) {
		this.assistant = assistant;
	}

	// Using the POST method due to chat memory capabilities (채팅 메모리 기능으로 인해 POST 메서드 사용)
	@PostMapping(value = "/chat/{user}")
	public SseEmitter chat(@PathVariable UUID user, @RequestBody String query) {
		var emitter = new SseEmitter();

		// 비동기 서비스에서 어시스턴트의 채팅 메서드를 실행
		nonBlockingService.execute(() -> assistant.chat(user, query).onPartialResponse(message -> {
			try {
				// 부분 응답 메시지를 클라이언트에 전송
				sendMessage(emitter, message);
			}
			catch (IOException e) {
				// 다음 토큰 작성 중 오류 발생 시 로깅 및 에미터 완료
				LOGGER.error("Error while writing next token", e);
				emitter.completeWithError(e);
			}
		}).onCompleteResponse(token -> emitter.complete()).onError(error -> {
			// 예상치 못한 채팅 오류 발생 시 로깅
			LOGGER.error("Unexpected chat error", error);
			try {
				// 오류 메시지를 클라이언트에 전송
				sendMessage(emitter, error.getMessage());
			}
			catch (IOException e) {
				// 다음 토큰 작성 중 오류 발생 시 로깅
				LOGGER.error("Error while writing next token", e);
			}
			// 오류와 함께 에미터 완료
			emitter.completeWithError(error);
		}).start());

		return emitter;
	}

	private static void sendMessage(SseEmitter emitter, String message) throws IOException {
		// 메시지에서 줄 바꿈을 <br>로 바꾸고 JSON 따옴표를 이스케이프 처리
		var token = message
			// SSE(Server-Sent Events) 사용 시 줄 바꿈 문제 해결
			.replace("\n", "<br>")
			// SSE 사용 시 줄 바꿈 문제 해결
			// Escape JSON quotes
			.replace("\"", "\\\"");

		emitter.send("{\"t\": \"" + token + "\"}");
	}

}
