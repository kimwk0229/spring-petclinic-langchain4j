package org.springframework.samples.petclinic.chat;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.spring.AiService;

@AiService
interface Assistant {

	@SystemMessage(fromResource = "/prompts/system.st")
	TokenStream chat(String userMessage);

}
