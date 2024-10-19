package org.springframework.samples.petclinic.chat;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AssistantController {

	private final Assistant assistant;

	AssistantController(Assistant assistant) {
		this.assistant = assistant;
	}

	@PostMapping("/chat")
	public String chat(@RequestBody String query) {
		return assistant.chat(query);
	}

}
