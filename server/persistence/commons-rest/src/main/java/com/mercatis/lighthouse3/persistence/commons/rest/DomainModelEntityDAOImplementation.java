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
package com.mercatis.lighthouse3.persistence.commons.rest;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.httpclient.URI;

import com.mercatis.lighthouse3.commons.commons.HttpRequest;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.commons.commons.HttpRequest.HttpMethod;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAODeferredLoader;
import com.mercatis.lighthouse3.domainmodel.commons.EntityLoadHandler;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.commons.UnitOfWork;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;

/**
 * This class provides an abstract implementation of common methods for domain
 * model entity DAOs. The implementation acts as an HTTP client to a RESTful web
 * service providing the DAO storage functionality.
 */
public abstract class DomainModelEntityDAOImplementation<Entity extends DomainModelEntity> implements DomainModelEntityDAO<Entity> {

	protected String user;
	protected String password;
	
	/**
	 * Generic constructor, necessary for setting the <CODE>entityType</CODE>
	 * property at runtime.
	 */
	@SuppressWarnings("rawtypes")
	public DomainModelEntityDAOImplementation() {
		try {
			ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
			Type[] actualTypeArguments = genericSuperclass.getActualTypeArguments();
			this.entityType = (Class) actualTypeArguments[0];
		} catch (Throwable t) {
			this.entityType = DomainModelEntity.class;
		}
	}

	/**
	 * Keeps a handle to the real class used with the <CODE>Entity</CODE> type
	 * variable.
	 */
	@SuppressWarnings("rawtypes")
	private Class entityType;

	@SuppressWarnings("rawtypes")
	public Class getManagedType() {
		return entityType;
	}

	/**
	 * This method creates a new instance of the managed entity.
	 * 
	 * @return the instance or <code>null</code> if instance creation failed.
	 */
	@SuppressWarnings("unchecked")
	protected Entity newEntity() {
		Entity entity = null;

		try {
			entity = (Entity) this.getManagedType().newInstance();
		} catch (InstantiationException e) {
			entity = null;
		} catch (IllegalAccessException e) {
			entity = null;
		}
		return entity;
	}

	/**
	 * Returns the class name of the entity type.
	 * 
	 * @return The class name.
	 */
	protected String getEntityTypeName() {
		return entityType.getName();
	}

	/**
	 * This property maintains the URL of the RESTful web service implementing
	 * the DAO. This serves as the prefix for the URIs identifying the resources
	 * representing the domain model entities managed by the web service.
	 * 
	 */
	private String serverUrl = null;

	/**
	 * This method returns the URL of the RESTful web service implementing the
	 * DAO functionality. This serves as the prefix for the URIs identifying the
	 * resources representing the domain model entities managed by the web
	 * service.
	 * 
	 * @return the URL
	 */
	public String getServerUrl() {
		return serverUrl;
	}

	/**
	 * This method sets the URL pointing to the RESTful web service providing
	 * the DAO functionality. This serves as the prefix for the URIs identifying
	 * the resources representing the domain model entities managed by the web
	 * service.
	 * 
	 * @param serverUrl
	 *            the service URL to set.
	 */
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	/**
	 * This method should be overridden to return the unique resource path
	 * relative to the server address to the entity given.
	 * 
	 * @param entity
	 *            the entity for which we need the resource path.
	 * @return the resource path
	 */
	protected String urlForEntity(Entity entity) {
		return this.urlForEntityId(entity.getId());
	}

	/**
	 * This method returns the relative resource path to the entity class.
	 * 
	 * @return the path to the class.
	 */
	protected String urlForEntityClass() {
		return "/" + this.getManagedType().getSimpleName();
	}

	/**
	 * Maintains the number of threads to use for web service result resolving.
	 */
	private int resolveWebServiceResultThreadPoolSize = 10;

	/**
	 * Call this method to set the number of threads to use for web service
	 * result resolving.
	 * 
	 * @param size
	 *            the number of threads to use.
	 */
	public void setResolveWebServiceResultThreadPoolSize(int number) {
		this.resolveWebServiceResultThreadPoolSize = number;
	}

	/**
	 * Return this method to set the number of threads to use for web service
	 * result resolving.
	 */
	public int getResolveWebServiceResultThreadPoolSize() {
		return this.resolveWebServiceResultThreadPoolSize;
	}

	/**
	 * This method returns the resource URL for the domain model entity with the
	 * given id.
	 * 
	 * @param id
	 *            the id of the coded domain model entity
	 * @return the URL
	 */
	protected String urlForEntityId(long id) {
		return "/" + this.getManagedType().getSimpleName() + "/" + id;
	}

	private class ByIdFinder implements Callable<Entity> {

		private long id = -1;

		public Entity call() throws Exception {
			return find(this.id);
		}

		public ByIdFinder(long id) {
			this.id = id;
		}
	}

	/**
	 * This method must be implemented by subclasses to resolve result lists
	 * returned from a web service method to a list of corresponding domain
	 * model entities.
	 * 
	 * @param webServiceResultList
	 *            the result list returned from the web service
	 * @return the set of entities
	 */
	protected List<Entity> resolveWebServiceResultList(String webServiceResultList) {
		List<Entity> result = new LinkedList<Entity>();
		List<String> entityIds = XmlMuncher.readValuesFromXml(webServiceResultList, "//:id");

		List<Callable<Entity>> jobs = new ArrayList<Callable<Entity>>();

		for (String entityId : entityIds) {
			jobs.add(new ByIdFinder(Long.parseLong(entityId)));
		}

		ExecutorService pooledExecutor = Executors.newFixedThreadPool(this.getResolveWebServiceResultThreadPoolSize());
		try {
			List<Future<Entity>> jobResults = pooledExecutor.invokeAll(jobs);
			for (Future<Entity> jobResult : jobResults) {
				result.add(jobResult.get());
			}

		} catch (Exception ex) {
			throw new PersistenceException("Encountered problem while resolving entity id result list", ex);
		}

		return result;
	}

	/**
	 * This method performs an HTTP request against the RESTful web service.
	 * 
	 * @param resourcePath
	 *            the path to the resource relative to the server url.
	 * @param method
	 *            the HTTP method to execute
	 * @param body
	 *            the body of a POST or PUT request, can be <code>null</code>
	 * @param queryParams
	 *            a Hash with the query parameter, can be <code>null</code>
	 * @return the data returned by the web service
	 * @throws PersistenceException
	 *             in case a communication error occurred.
	 */
	protected String executeHttpMethod(String resourcePath, HttpMethod method, String body, Map<String, String> queryParams) {

		String url = null;

		try {
			url = new URI(this.getServerUrl() + resourcePath, true).toString();
		} catch (Exception e) {
			throw new PersistenceException("Invalid resource path given", e);
		}

		try {
			return new HttpRequest(user, password).execute(url, method, body, queryParams);
		} catch (Exception anyProblem) {
			String message = anyProblem.getCause() != null ? anyProblem.getCause().getMessage() : anyProblem.getMessage();
			throw new PersistenceException(message, anyProblem);
		}
	}

	/**
	 * This method has to be overridden to return the entity resolvers required
	 * to deserialize the domain model entities managed by the present DAO.
	 * 
	 * @return the resolvers.
	 */
	@SuppressWarnings("rawtypes")
	protected abstract DomainModelEntityDAO[] getRealEntityResolvers();

	/**
	 * The entity load handler to use to proxify the entity resolvers for
	 * deferred entity loading.
	 */
	private EntityLoadHandler entityLoadHandler = null;

	/**
	 * The entity load handler to when a proxying mode for resolving entity
	 * references is desired in order to avoid bulk loading of entity
	 * hierarchies.
	 * 
	 * @param entityLoadHandler
	 *            the handler to use
	 */
	public void setEntityLoadHandler(EntityLoadHandler entityLoadHandler) {
		this.entityLoadHandler = entityLoadHandler;
	}

	/**
	 * This method returns the entity resolvers required to deserialize the
	 * domain model entities managed by the present DAO.
	 * 
	 * Those are wrapped by a domain model entity proxier if an entity load
	 * handler has been given.
	 * 
	 * @return the potentially wrapped resolvers.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected DomainModelEntityDAO[] getEntityResolvers() {
		DomainModelEntityDAO[] realResolvers = this.getRealEntityResolvers();
		if (this.entityLoadHandler == null) {
			return realResolvers;
		}

		DomainModelEntityDAO[] wrappedResolvers = new DomainModelEntityDAO[realResolvers.length];

		for (int r = 0; r < realResolvers.length; r++) {
			wrappedResolvers[r] = DomainModelEntityDAODeferredLoader.wrap(realResolvers[r], this.entityLoadHandler);
		}

		return wrappedResolvers;
	}

	public UnitOfWork getUnitOfWork() {
		return null;
	}

	/**
	 * This method should be overridden appropriately to check whether an entity
	 * is already persisted.
	 * 
	 * @param entity
	 * @return <code>true</code> iff the entity is already persistent.
	 */
	public boolean alreadyPersisted(Entity entity) {
		return this.find(entity.getId()) != null;
	}

	public List<Entity> findByTemplate(Entity template) {
		Map<String, String> queryParameters = template.toQueryParameters();

		String result = this.executeHttpMethod(this.urlForEntityClass(), HttpMethod.GET, null, queryParameters);

		return this.resolveWebServiceResultList(result);
	}

	/**
	 * This method returns the lighthouse domain of the server.
	 * 
	 * @return the lighthouse domain.
	 */
	public String getLighthouseDomain() {
		try {
			return this.executeHttpMethod("/Version/LighthouseDomain", HttpMethod.GET, null, null);
		} catch (PersistenceException ex) {
			return null;
		}
	}

	public List<Entity> findAll() {
		List<Entity> result = new ArrayList<Entity>();
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("detailed", "true");
		String response = this.executeHttpMethod(urlForEntityClass(), HttpMethod.GET, null, queryParams);
		XmlMuncher muncher = new XmlMuncher(response);

		List<XmlMuncher> subMunchers = muncher.getSubMunchersForContext("/:list/:" + getManagedType().getSimpleName());
		Iterator<XmlMuncher> it = subMunchers.iterator();
		while (it.hasNext()) {
			XmlMuncher xm = it.next();
			try {
				Entity entity = newEntity();
				entity.fromXml(xm, getEntityResolvers());
				result.add(entity);
			} catch (XMLSerializationException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	public Entity find(long id) {
		try {
			String result = this.executeHttpMethod(this.urlForEntityId(id), HttpMethod.GET, null, null);

			Entity entity = newEntity();
			entity.fromXml(result, this.getEntityResolvers());

			return entity;
		} catch (PersistenceException ex) {
			return null;
		}
	}
	
	public void delete(Entity entityToDelete) {
		this.executeHttpMethod(this.urlForEntity(entityToDelete), HttpMethod.DELETE, null, null);
	}

	public void persist(Entity entityToPersist) {
		if (this.alreadyPersisted(entityToPersist)) {
			throw new PersistenceException("Entity already persistent", null);
		}
		this.executeHttpMethod(this.urlForEntityClass(), HttpMethod.POST, entityToPersist.toXml(), null);
	}

	public void update(Entity entityToUpdate) {
		this.executeHttpMethod(this.urlForEntityClass(), HttpMethod.PUT, entityToUpdate.toXml(), null);
	}
}
