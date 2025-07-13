/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.model.Person;
import org.springframework.samples.petclinic.vet.Vet;

public class PetClinicRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		hints.resources().registerPattern("db/*"); // 리소스 힌트 등록: 'db/' 경로의 모든 리소스
		hints.resources().registerPattern("messages/*"); // 리소스 힌트 등록: 'messages/' 경로의 모든
															// 리소스
		hints.resources().registerPattern("mysql-default-conf"); // 리소스 힌트 등록:
																	// 'mysql-default-conf'
																	// 파일

		hints.serialization().registerType(BaseEntity.class); // 직렬화 힌트 등록: BaseEntity 클래스
		hints.serialization().registerType(Person.class); // 직렬화 힌트 등록: Person 클래스
		hints.serialization().registerType(Vet.class); // 직렬화 힌트 등록: Vet 클래스
	}

}
