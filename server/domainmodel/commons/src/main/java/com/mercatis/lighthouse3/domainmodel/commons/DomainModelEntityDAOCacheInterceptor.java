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
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * This class provides a basic method interceptor for caching domain model
 * entity DAOs.
 */
public class DomainModelEntityDAOCacheInterceptor<D extends DomainModelEntityDAO<? extends DomainModelEntity>>
		implements MethodInterceptor {
	protected Map<Long, DomainModelEntity> idCache = new HashMap<Long, DomainModelEntity>();
	protected Map<String, CodedDomainModelEntity> codeCache = new HashMap<String, CodedDomainModelEntity>();

	public DomainModelEntity getEntityFromCache(Object key) {
		if (key instanceof Long)
			return this.idCache.get(key);
		else if (key instanceof String)
			return this.codeCache.get(key);
		else
			return null;
	}

	public void cacheEntity(DomainModelEntity entity) {
		this.idCache.put(entity.getId(), entity);
		if (entity instanceof CodedDomainModelEntity)
			this.codeCache.put(((CodedDomainModelEntity) entity).getCode(), (CodedDomainModelEntity) entity);
	}

	public void decacheEntity(DomainModelEntity entity) {
		this.idCache.remove(entity.getId());
		if (entity instanceof CodedDomainModelEntity)
			this.codeCache.remove(((CodedDomainModelEntity) entity).getCode());
	}

	public Object intercept(Object proxy, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {
		if ("find".equals(method.getName()) || "findByCode".equals(method.getName()))
			return this.cachedFind(daoToCache, methodProxy, arguments[0]);
		else if ("persist".equals(method.getName()) || "update".equals(method.getName())
				|| "delete".equals(method.getName()))
			return this.decacheMethodCall(daoToCache, methodProxy, (DomainModelEntity) arguments[0]);
		else
			return methodProxy.invoke(daoToCache, arguments);
	}

	protected DomainModelEntity cachedFind(final D daoToCache, MethodProxy findMethod, Object key) throws Throwable {

		DomainModelEntity findResult = null;

		synchronized (this) {
			findResult = this.getEntityFromCache(key);
		}

		if (findResult != null) {
			return findResult;
		}

		findResult = (DomainModelEntity) findMethod.invoke(daoToCache, new Object[] { key });

		if (findResult != null)
			synchronized (this) {
				this.cacheEntity(findResult);
			}

		return findResult;
	}

	protected Object decacheMethodCall(final D daoToCache, MethodProxy methodAfterDecache,
			DomainModelEntity entityToDecache) throws Throwable {

		synchronized (this) {
			DomainModelEntity cachedEntity = this.getEntityFromCache(entityToDecache.getId());
			if (cachedEntity != null) {
				this.decacheEntity(entityToDecache);
			}
		}

		return methodAfterDecache.invoke(daoToCache, new Object[] { entityToDecache });
	}

	protected D daoToCache = null;

	public DomainModelEntityDAOCacheInterceptor(D daoToCache) {
		this.daoToCache = daoToCache;
	}
}
