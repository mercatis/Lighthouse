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

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * This class provides factory methods to construct a read only cache proxy
 * around a domain model entity DAO. Per default, the cache supports the methods
 * <code>find()</code> and <code>findByCode()</code>. Calls to the methods
 * <code>persist()</code>, <code>delete()</code>, and <code>update()</code>
 * result in the expiration of cache entries.
 * 
 * In case there exists a class implementing cglib's
 * <code>MethodInterceptor</code> interface following the naming convention
 * 
 * <code>daoToCache.getManagedType.getName() + "DAO/Registry" + "CacheInterceptor"</code>
 * this interceptor is used for caching with whatever semantics implemented.
 * This class must have a public constructor receiving the DAO to cache as its
 * sole parameter.
 */
public class DomainModelEntityDAOCache {
	/**
	 * This method creates a caching wrapper around the domain model entity DAO
	 * to cache.
	 * 
	 * @param daoToCache
	 *            the DAO to cache
	 * @return the cached DAO
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static public <D extends DomainModelEntityDAO<? extends DomainModelEntity>> D wrap(final D daoToCache) {
		Set<Class> subClassInterfaces = new HashSet<Class>();
		Class baseClass = daoToCache.getClass();

		subClassInterfaces.addAll(Arrays.asList(baseClass.getInterfaces()));

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(baseClass);
		enhancer.setInterfaces(subClassInterfaces.toArray(new Class[0]));

		MethodInterceptor cachingInterceptor = null;

		try {
			String cacheInterceptorName = daoToCache.getManagedType().getName() + "Registry" + "CacheInterceptor";

			Class cachingInterceptorClass = Class.forName(cacheInterceptorName);
			Constructor cachingInterceptorConstructor = cachingInterceptorClass
					.getConstructor(DomainModelEntityDAO.class);

			cachingInterceptor = (MethodInterceptor) cachingInterceptorConstructor.newInstance(daoToCache);
		} catch (Throwable e) {
			try {
				String cacheInterceptorName = daoToCache.getManagedType().getName() + "DAO" + "CacheInterceptor";

				Class cachingInterceptorClass = Class.forName(cacheInterceptorName);
				Constructor cachingInterceptorConstructor = cachingInterceptorClass
						.getConstructor(DomainModelEntityDAO.class);

				cachingInterceptor = (MethodInterceptor) cachingInterceptorConstructor.newInstance(daoToCache);
			} catch (Throwable t) {
				cachingInterceptor = null;
			}
		}

		if (cachingInterceptor == null)
			cachingInterceptor = new DomainModelEntityDAOCacheInterceptor<D>(daoToCache);

		enhancer.setCallback(cachingInterceptor);

		D result = (D) enhancer.create();
		return result;
	}
}
