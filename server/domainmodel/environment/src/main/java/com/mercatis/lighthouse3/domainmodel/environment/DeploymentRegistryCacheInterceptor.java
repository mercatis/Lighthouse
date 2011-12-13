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
package com.mercatis.lighthouse3.domainmodel.environment;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.mercatis.lighthouse3.commons.commons.Tuple;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;

/**
 * This class provides a basic method interceptor for caching deployment
 * registries.
 */
public class DeploymentRegistryCacheInterceptor implements MethodInterceptor {

	protected Map<Long, Deployment> idCache = new HashMap<Long, Deployment>();
	protected Map<Tuple<String, String>, Deployment> codeLocationCache = new HashMap<Tuple<String, String>, Deployment>();
	protected Map<String, List<Deployment>> locationCache = new HashMap<String, List<Deployment>>();

	public void cacheDeployment(Deployment deployment) {
		this.idCache.put(deployment.getId(), deployment);

		this.codeLocationCache.put(new Tuple<String, String>(deployment.getLocation(), deployment
				.getDeployedComponent().getCode()), deployment);

		if (this.locationCache.get(deployment.getLocation()) == null)
			this.locationCache.put(deployment.getLocation(), new LinkedList<Deployment>());

		this.locationCache.get(deployment.getLocation()).add(deployment);
	}

	public void decacheDeployment(Deployment deployment) {
		this.idCache.remove(deployment.getId());

		this.codeLocationCache.remove(new Tuple<String, String>(deployment.getLocation(), deployment
				.getDeployedComponent().getCode()));

		this.locationCache.remove(deployment.getLocation());
	}

	public Object intercept(Object proxy, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {
		if ("find".equals(method.getName()))
			return this.cachedFindByKey(registryToCache, methodProxy, arguments[0]);
		else if ("findByComponentAndLocation".equals(method.getName()))
			return this.cachedFindByLocationAndCode(registryToCache, methodProxy, (SoftwareComponent) arguments[0],
					(String) arguments[1]);
		else if ("findAtLocation".equals(method.getName()))
			return this.cachedFindAtLocation(registryToCache, methodProxy, (String) arguments[0]);
		else if ("persist".equals(method.getName()) || "update".equals(method.getName())
				|| "delete".equals(method.getName()))
			return this.decacheMethodCall(registryToCache, methodProxy, (Deployment) arguments[0]);
		else
			return methodProxy.invoke(registryToCache, arguments);
	}

	@SuppressWarnings("unchecked")
	protected List<Deployment> cachedFindAtLocation(DeploymentRegistry registryToCache,
			MethodProxy findAtLocationMethod, String location) throws Throwable {
		List<Deployment> findResult = null;

		synchronized (this) {
			findResult = this.locationCache.get(location);
			if (findResult != null)
				findResult = new LinkedList<Deployment>(findResult);
		}

		if (findResult != null) {
			return findResult;
		}

		findResult = (List<Deployment>) findAtLocationMethod.invoke(registryToCache, new Object[] { location });

		if ((findResult != null) && !findResult.isEmpty())
			synchronized (this) {
				for (Deployment result : findResult)
					this.cacheDeployment(result);
			}

		return findResult;
	}

	protected Deployment cachedFindByLocationAndCode(DeploymentRegistry registryToCache,
			MethodProxy findByComponentAndLocationMethod, SoftwareComponent component, String location)
			throws Throwable {
		Deployment findResult = null;

		synchronized (this) {
			findResult = this.codeLocationCache.get(new Tuple<String, String>(location, component.getCode()));
		}
		if (findResult != null) {
			return findResult;
		}

		findResult = (Deployment) findByComponentAndLocationMethod.invoke(registryToCache, new Object[] { component,
				location });

		if (findResult != null)
			synchronized (this) {
				this.cacheDeployment(findResult);
			}

		return findResult;
	}

	protected Deployment cachedFindByKey(final DeploymentRegistry registryToCache, MethodProxy findMethod, Object key)
			throws Throwable {

		Deployment findResult = null;

		synchronized (this) {
			findResult = this.idCache.get(key);
		}

		if (findResult != null) {
			return findResult;
		}

		findResult = (Deployment) findMethod.invoke(registryToCache, new Object[] { key });

		if (findResult != null)
			synchronized (this) {
				this.cacheDeployment(findResult);
			}

		return findResult;
	}

	protected Object decacheMethodCall(final DeploymentRegistry registryToCache, MethodProxy methodAfterDecache,
			Deployment deploymentToDecache) throws Throwable {

		synchronized (this) {
			this.decacheDeployment(deploymentToDecache);
		}

		return methodAfterDecache.invoke(registryToCache, new Object[] { deploymentToDecache });
	}

	protected DeploymentRegistry registryToCache = null;

	@SuppressWarnings("rawtypes")
	public DeploymentRegistryCacheInterceptor(DomainModelEntityDAO registryToCache) {
		this.registryToCache = (DeploymentRegistry) registryToCache;
	}
}
