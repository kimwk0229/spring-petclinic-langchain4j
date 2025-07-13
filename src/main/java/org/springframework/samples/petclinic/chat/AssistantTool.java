package org.springframework.samples.petclinic.chat;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * LLM에 의해 호출되는 함수들은 이 빈을 사용하여 소유자 및 수의사 목록 조회, 소유자에게 애완동물 추가와 같은 정보를 시스템에서 쿼리합니다.
 *
 * @author Oded Shopen
 * @author Antoine Rey
 */
@Component
public class AssistantTool {

	private final OwnerRepository ownerRepository;

	public AssistantTool(OwnerRepository ownerRepository) {
		this.ownerRepository = ownerRepository;
	}

	/**
	 * 이 도구는 {@link Assistant}에서 사용할 수 있습니다.
	 */
	@Tool("current date, today")
	String currentDate() {
		return LocalDate.now().toString();
	}

	/**
	 * 펫 클리닉에 등록된 모든 소유자를 나열합니다.
	 */
	@Tool("List the owners that the pet clinic has: ownerId, name, address, phone number, pets")
	public List<Owner> getAllOwners() {
		var pageable = PageRequest.of(0, 100);
		Page<Owner> ownerPage = ownerRepository.findAll(pageable);

		return ownerPage.getContent();
	}

	/**
	 * 지정된 ownerId를 가진 소유자에게 petTypeId를 가진 애완동물을 추가합니다.
	 */
	@Tool("Add a pet with the specified petTypeId, to an owner identified by the ownerId")
	public Owner addPetToOwner(Pet pet, String petName, Integer ownerId) {
		Owner owner = ownerRepository.findById(ownerId).orElseThrow();
		// https://github.com/langchain4j/langchain4j/issues/2249 해결 대기 중
		pet.setName(petName);
		owner.addPet(pet);

		this.ownerRepository.save(owner);

		return owner;
	}

	/**
	 * 모든 petTypeId와 애완동물 유형 이름 쌍을 나열합니다.
	 */
	@Tool("List all pairs of petTypeId and pet type name")
	public List<PetType> populatePetTypes() {
		return this.ownerRepository.findPetTypes();
	}

	/**
	 * 펫 클리닉에 새로운 애완동물 소유자를 추가합니다.
	 */
	@Tool("""
			Add a new pet owner to the pet clinic. \
			The Owner must include a first name and a last name as two separate words, \
			plus an address and a 10-digit phone number""")
	public Owner addOwnerToPetclinic(Owner owner) {
		return ownerRepository.save(owner);
	}

}
