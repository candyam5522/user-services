/*-
 * #%L
 * Eureka! Clinical User Services
 * %%
 * Copyright (C) 2016 Emory University
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.eurekaclinical.user.service.dao;

import org.eurekaclinical.standardapis.dao.Dao;

import org.eurekaclinical.user.client.comm.authentication.LoginType;

import org.eurekaclinical.user.service.entity.LoginTypeEntity;
/**
 * @author miaoai
 */
public interface LoginTypeDao extends Dao<LoginTypeEntity, Long> {
	/**
	 * Gets a value definition match operator based on the name attribute.
	 * @param inName the name to search for in the database
	 * @return a {@link LoginTypeEntity} with the given name if
	 * found, null otherwise
	 */
	public LoginTypeEntity getByName(LoginType inName);
}
