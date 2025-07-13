package org.springframework.samples.petclinic.chat;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import java.util.UUID;

@AiService
interface Assistant {

	// 시스템 메시지와 사용자 메시지를 기반으로 채팅 응답을 생성하는 메서드
	// Creates a chat response based on system and user messages.

	@SystemMessage(fromResource = "/prompts/system.st")
	TokenStream chat(@MemoryId UUID memoryId, @UserMessage String userMessage);

}
