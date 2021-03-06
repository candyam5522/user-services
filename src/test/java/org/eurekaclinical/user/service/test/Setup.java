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
package org.eurekaclinical.user.service.test;

import org.eurekaclinical.user.service.entity.LocalUserEntity;
import org.eurekaclinical.user.service.entity.UserEntity;
import org.eurekaclinical.user.service.entity.RoleEntity;
import org.eurekaclinical.user.service.entity.AuthenticationMethodEntity;
import org.eurekaclinical.user.service.entity.LoginTypeEntity;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.eurekaclinical.user.client.comm.authentication.AuthenticationMethod;
import org.eurekaclinical.user.client.comm.authentication.LoginType;

import org.eurekaclinical.user.common.test.TestDataException;
import org.eurekaclinical.user.common.test.TestDataProvider;
import org.eurekaclinical.user.common.util.StringUtil;

import org.eurekaclinical.user.service.dao.AuthenticationMethodDao;
import org.eurekaclinical.user.service.dao.LoginTypeDao;
import org.eurekaclinical.user.service.entity.UserEntityFactory;
/**
 * Sets up the environment for testing, by bootstrapping the data store with
 * certain items to test against.
 *
 * @author miaoai
 */
public class Setup implements TestDataProvider {

	private static final String ORGANIZATION = "Emory University";
	private static final String PASSWORD = "testpassword";
	public static final String TESTING_TIME_UNIT_NAME = "test";
	public static final String TESTING_FREQ_TYPE_NAME = "at least";
	
	private final Provider<EntityManager> managerProvider;
	private RoleEntity researcherRole;
	private RoleEntity adminRole;
	private final UserEntityFactory userEntityFactory;
	private List<LoginTypeEntity> loginTypes;
	private List<AuthenticationMethodEntity> authenticationMethods;


	/**
	 * Create a Bootstrap class with an EntityManager.
	 */
	@Inject
	Setup(Provider<EntityManager> inManagerProvider,
			LoginTypeDao inLoginTypeDao,
			AuthenticationMethodDao inAuthenticationMethodDao) {
		this.managerProvider = inManagerProvider;
		this.userEntityFactory = new UserEntityFactory(inLoginTypeDao, inAuthenticationMethodDao);
	}

	private EntityManager getEntityManager() {
		return this.managerProvider.get();
	}

	@Override
	public void setUp() throws TestDataException {
		this.researcherRole = this.createResearcherRole();
		this.adminRole = this.createAdminRole();
		this.loginTypes = createLoginTypes();
		this.authenticationMethods = createAuthenticationMethods();
		UserEntity researcherUser = this.createResearcherUser();
		UserEntity adminUser = this.createAdminUser();
	}

	@Override
	public void tearDown() throws TestDataException {
		this.remove(UserEntity.class);
		this.remove(RoleEntity.class);
	}

	private <T> void remove(Class<T> className) {
		EntityManager entityManager = this.getEntityManager();
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = builder.createQuery(className);
		criteriaQuery.from(className);
		TypedQuery<T> query = entityManager.createQuery(criteriaQuery);
		List<T> entities = query.getResultList();
		System.out.println("Deleting " + className.getName() + "; count: " +
				entities.size());
		entityManager.getTransaction().begin();
		int i = 1;
		for (T t : entities) {
			System.out.println("on " + i++ + "; " + t);
			entityManager.flush();
			entityManager.remove(t);
		}
		entityManager.getTransaction().commit();
	}
	

	private UserEntity createResearcherUser() throws TestDataException {
		return this.createAndPersistUser("user@emory.edu", "Regular", "User",
				this.researcherRole);
	}

	private UserEntity createAdminUser() throws TestDataException {
		return this.createAndPersistUser("admin.user@emory.edu", "Admin",
				"User", this.researcherRole, this.adminRole);
	}

	private UserEntity createAndPersistUser(String email, String firstName,
	                                  String lastName,
	                                  RoleEntity... roles) throws
			TestDataException {
		EntityManager entityManager = this.getEntityManager();
		LocalUserEntity user = this.userEntityFactory.getLocalUserEntityInstance();
		try {
			user.setActive(true);
			user.setVerified(true);
			user.setEmail(email);
			user.setUsername(email);
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setOrganization(ORGANIZATION);
			user.setPassword(StringUtil.md5(PASSWORD));
			user.setLastLogin(new Date());
			user.setRoles(Arrays.asList(roles));
		} catch (NoSuchAlgorithmException nsaex) {
			throw new TestDataException(nsaex);
		}
		entityManager.getTransaction().begin();
		entityManager.persist(user);
		entityManager.flush();
		entityManager.getTransaction().commit();
		
		entityManager.getTransaction().begin();
		entityManager.flush();
		entityManager.getTransaction().commit();
		return user;
	}

	private RoleEntity createResearcherRole() {
		return this.createAndPersistRole("researcher", Boolean.TRUE);
	}

	private RoleEntity createAdminRole() {
		return this.createAndPersistRole("admin", Boolean.FALSE);
	}

	private RoleEntity createAndPersistRole(String name, Boolean isDefault) {
		EntityManager entityManager = this.getEntityManager();
		RoleEntity role = new RoleEntity();
		role.setName(name);
		role.setDefaultRole(isDefault);
		entityManager.getTransaction().begin();
		entityManager.persist(role);
		entityManager.flush();
		entityManager.getTransaction().commit();
		return role;
	}
	
	private List<LoginTypeEntity> createLoginTypes() {
		EntityManager entityManager = getEntityManager();
		LoginTypeEntity loginType = new LoginTypeEntity();
		loginType.setName(LoginType.INTERNAL);
		loginType.setDescription(LoginType.INTERNAL.name());
		entityManager.getTransaction().begin();
		entityManager.persist(loginType);
		entityManager.getTransaction().commit();
		return Collections.singletonList(loginType);
	}
	
	private List<AuthenticationMethodEntity> createAuthenticationMethods() {
		EntityManager entityManager = getEntityManager();
		AuthenticationMethodEntity authenticationMethod = new AuthenticationMethodEntity();
		authenticationMethod.setName(AuthenticationMethod.LOCAL);
		authenticationMethod.setDescription(AuthenticationMethod.LOCAL.name());
		entityManager.getTransaction().begin();
		entityManager.persist(authenticationMethod);
		entityManager.getTransaction().commit();
		return Collections.singletonList(authenticationMethod);
	}
}
