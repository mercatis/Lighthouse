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

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * This class provides an interceptor implementing an deferred loader proxy for
 * domain model entities.
 */
public class DomainModelEntityDeferredLoadingInterceptor implements
		MethodInterceptor {
	private EntityLoadHandler entityLoadHandler = null;
	private MethodProxy findMethod = null;
	private Object[] findParameters = null;
	private DomainModelEntityDAO<?> realDAO = null;

	private DomainModelEntity proxiedDomainModelEntity = null;
	private String proxiedCode = null;
	private Long proxiedId = null;

	public int fakedHashCode() {
		final int prime = 31;
		int result = 1;

		result = prime
				* result
				+ ((this.proxiedCode == null) ? 0 : this.proxiedCode.hashCode());

		return result;
	}

	public boolean fakedEquals(Object domainModelEntityProxy, Object obj) {
		if (this == obj)
			return true;

		if (!domainModelEntityProxy.getClass().getSuperclass().isInstance(obj))
			return false;

		DomainModelEntity that = (DomainModelEntity) obj;

		if ((this.proxiedId != null) && (that.getId() != 0))
			return this.proxiedId.equals(that.getId());

		if ((this.proxiedCode != null)
				&& (that instanceof CodedDomainModelEntity))
			return this.proxiedCode.equals(((CodedDomainModelEntity) that)
					.getCode());

		return false;
	}

	public Object intercept(Object domainModelEntityProxy,
			Method theMethodCalled, Object[] theMethodArgs, MethodProxy proxy)
			throws Throwable {

		if ((this.proxiedDomainModelEntity == null)
				&& theMethodCalled.getName().equals("getCode"))
			return this.proxiedCode;

		if ((this.proxiedDomainModelEntity == null)
				&& theMethodCalled.getName().equals("getId"))
			return this.proxiedId;

		if ((this.proxiedDomainModelEntity == null)
				&& theMethodCalled.getName().equals("hashCode"))
			return this.fakedHashCode();

		if ((this.proxiedDomainModelEntity == null)
				&& theMethodCalled.getName().equals("equals"))
			return this.fakedEquals(domainModelEntityProxy, theMethodArgs[0]);

		if ((this.proxiedDomainModelEntity == null)
				&& theMethodCalled.getName().equals("finalize"))
			return null;
		
		if (this.proxiedDomainModelEntity == null)
			this.proxiedDomainModelEntity = this.entityLoadHandler
					.performEntityLoad(new DeferredEntityLoadRequest() {
						public DomainModelEntity doLoad() {
							Object findResult = null;

							try {
								findResult = findMethod.invoke(realDAO,
										findParameters);
							} catch (Throwable e) {
								throw new PersistenceException(
										"Could not load entity deferredly",
										(Exception) e);
							}

							if (findResult == null)
								throw new PersistenceException(
										"Could not find entity to load deferredly "
												+ findParameters, null);

							return (DomainModelEntity) findResult;
						}
					});

		return theMethodCalled.invoke(this.proxiedDomainModelEntity,
				theMethodArgs);
	}

	public DomainModelEntityDeferredLoadingInterceptor(
			DomainModelEntityDAO<?> realDAO,
			EntityLoadHandler entityLoadHandler, MethodProxy findMethod,
			Object[] findParameters) {
		this.realDAO = realDAO;
		this.entityLoadHandler = entityLoadHandler;
		this.findMethod = findMethod;
		this.findParameters = findParameters;

		if (findParameters[0] instanceof Long)
			this.proxiedId = (Long) findParameters[0];
		if (findParameters[0] instanceof String)
			this.proxiedCode = (String) findParameters[0];
	}

}
