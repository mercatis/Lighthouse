/*
 * Copyright 2011 mercatis Technologies AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mercatis.lighthouse3.domainmodel.commons;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * This class an interceptor for domain model entity DAO that intercepts
 * <code>find(long id)</code> and <code>findByCode(String code)</code> methods
 * to return proxies for deferred loading of the resulting domain model entities
 * instead of the entities directly.
 */
public class DomainModelEntityDAODeferredLoadingInterceptor<D extends DomainModelEntityDAO<? extends DomainModelEntity>>
		implements MethodInterceptor {

	/**
	 * The load handler to use for loading domain model entities.
	 */
	private EntityLoadHandler entityLoadHandler = null;

	/**
	 * The wrapped domain model entity DAO.
	 */
	private D daoToProxy = null;

	public DomainModelEntityDAODeferredLoadingInterceptor(D daoToProxy,
			EntityLoadHandler entityLoadHandler) {
		this.daoToProxy = daoToProxy;
		this.entityLoadHandler = entityLoadHandler;
	}

	public Object intercept(Object proxy, Method method, Object[] arguments,
			MethodProxy methodProxy) throws Throwable {
		if ("find".equals(method.getName())
				|| "findByCode".equals(method.getName()))
			return this.createDeferredLoadDomainModelEntityProxy(
					this.daoToProxy, this.entityLoadHandler, methodProxy,
					arguments);
		else
			return methodProxy.invoke(this.daoToProxy, arguments);
	}

	@SuppressWarnings("rawtypes")
	private DomainModelEntity createDeferredLoadDomainModelEntityProxy(
			D realDAO, EntityLoadHandler entityLoadHandler,
			MethodProxy findMethod, Object[] findParameters) {

		Set<Class> subClassInterfaces = new HashSet<Class>();
		Class baseClass = realDAO.getManagedType();

		subClassInterfaces.addAll(Arrays.asList(baseClass.getInterfaces()));

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(baseClass);
		enhancer.setInterfaces(subClassInterfaces.toArray(new Class[0]));

		enhancer.setCallback(new DomainModelEntityDeferredLoadingInterceptor(realDAO,
				entityLoadHandler, findMethod, findParameters));

		DomainModelEntity result = (DomainModelEntity) enhancer.create();
		return result;

	}
}
