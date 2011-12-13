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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * This class provides a wrapper around domain model entity DAO
 * <code>find(long id)</code> and <code>findByCode(String code)</code> methods
 * that returns proxies instead of the real entities. The request to really load
 * the entities will be delegated to a responsible to listener once any method
 * on the proxy is called.
 */
public class DomainModelEntityDAODeferredLoader {

	/**
	 * This method creates a proxying wrapper around a domain model entity DAO's
	 * domain model entity DAO <code>find(long id)</code> and
	 * <code>findByCode(String code)</code> methods.
	 * 
	 * @param daoToProxy
	 *            the DAO to proxy
	 * @param entityLoadHandler
	 *            the delegate responsible for really loading a domain model
	 *            entity DAO proxy returned by the proxying wrapper.
	 * @return the proxied DAO
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public <D extends DomainModelEntityDAO<? extends DomainModelEntity>> D wrap(
			final D daoToProxy, EntityLoadHandler entityLoadHandler) {
		Set<Class> subClassInterfaces = new HashSet<Class>();
		Class baseClass = daoToProxy.getClass();

		subClassInterfaces.addAll(Arrays.asList(baseClass.getInterfaces()));

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(baseClass);
		enhancer.setInterfaces(subClassInterfaces.toArray(new Class[0]));

		MethodInterceptor proxierInterceptor = new DomainModelEntityDAODeferredLoadingInterceptor<D>(
				daoToProxy, entityLoadHandler);

		enhancer.setCallback(proxierInterceptor);

		D result = (D) enhancer.create();
		return result;
	}
}
