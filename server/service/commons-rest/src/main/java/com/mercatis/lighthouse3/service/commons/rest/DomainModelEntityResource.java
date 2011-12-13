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
package com.mercatis.lighthouse3.service.commons.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.generationjava.io.xml.XmlEncXmlWriter;
import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.ConstraintViolationException;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityRestServiceContainer.CreatingServiceContainer;
import com.mercatis.lighthouse3.service.jaas.util.LighthouseAuthorizator;
import com.sun.jersey.api.ConflictException;

/**
 * This is an abstract base class wrapping a domain model entities as a REST web
 * service resource.
 */
public abstract class DomainModelEntityResource<Entity extends DomainModelEntity> {
	protected enum CtxOp { FIND, PERSIST, UPDATE, DELETE };
	
	protected Logger log = Logger.getLogger(getClass());

	protected abstract String getContextRole(CtxOp op);
	
	protected abstract StringBuilder getContextString();
	protected abstract StringBuilder getEntityContext(Entity e);
	
	protected StringBuilder formatContext(String arg0, String arg1) {
		return formatContext(getContextString(), arg0, arg1);
	}
	
	protected StringBuilder formatContext(StringBuilder builder, String arg0, String arg1) {
		if (arg0==null)
			return builder;
		builder.append('(').append(arg0);
		if (arg1!=null)
			builder.append(',').append(arg1);
		return builder.append(')');
	}
	
	
	/**
	 * The listeners for resource events.
	 */
	protected List<ResourceEventListener> resourceEventListeners = new ArrayList<ResourceEventListener>();

	/**
	 * A state flag indicating whether initial resource event listeners have
	 * already been added to the resource.
	 */
	private boolean initialListenersAdded = false;

	/**
	 * This method can be overridden by sub classes to add initial resource
	 * event listeners to the present resource. The method is lazily called once
	 * before any modifications or notifications to the listeners take place.
	 */
	protected List<ResourceEventListener> returnInitialResourceEventListeners() {
		List<ResourceEventListener> initialListeners = new ArrayList<ResourceEventListener>();
		return initialListeners;
	}

	protected void addInitialResourceEventListeners() {
		if (!initialListenersAdded) {
			resourceEventListeners.addAll(returnInitialResourceEventListeners());
			initialListenersAdded = true;
		}
	}

	/**
	 * This method adds a listener to resource events, if not already
	 * registered.
	 * 
	 * @param listener
	 *            the listener to register
	 */
	synchronized public void addResourceEventListener(ResourceEventListener listener) {
		addInitialResourceEventListeners();

		if (!resourceEventListeners.contains(listener))
			resourceEventListeners.add(listener);
	}

	/**
	 * The method removes a listener for resource events.s
	 * 
	 * @param listener
	 *            the listener to remove.
	 */
	synchronized public void removeResourceEventListener(ResourceEventListener listener) {
		addInitialResourceEventListeners();

		resourceEventListeners.remove(listener);
	}

	/**
	 * Calling this method defuses all resource event listeners.
	 */
	synchronized public void removeAllResourceEventListeners() {
		addInitialResourceEventListeners();

		resourceEventListeners.clear();
	}

	/**
	 * Calling this method notifies all resource event listeners about a given
	 * event
	 * 
	 * @param notificationType
	 *            the type of notification, either <code>ENTITY_CREATED</code>,
	 *            <code>ENTITY_UPDATED</code>,<code>ENTITY_DELETED</code>,
	 * @param entity
	 *            the entity the notification is about.
	 */
	synchronized public void notifyResourceEventListeners(int notificationType, Entity entity) {
		addInitialResourceEventListeners();

		if (log.isDebugEnabled()) {
			log.debug("Notifying ResourceEventListeners about change on '" + entity.getRootElementName() + "'.");
		}

		for (ResourceEventListener listener : resourceEventListeners)
			if (notificationType == ResourceEventListener.ENTITY_CREATED)
				listener.entityCreated(this, "" + entity.getId());
			else if (notificationType == ResourceEventListener.ENTITY_UPDATED)
				listener.entityUpdated(this, "" + entity.getId());
			else if (notificationType == ResourceEventListener.ENTITY_DELETED)
				listener.entityDeleted(this, "" + entity.getId());
	}

	/**
	 * Generic constructor, necessary for setting the <CODE>entityType</CODE>
	 * property at runtime.
	 */
	@SuppressWarnings("unchecked")
	public DomainModelEntityResource() {
		entityType = (Class<Entity>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	/**
	 * Keeps a handle to the real class used with the <CODE>Entity</CODE> type
	 * variable.
	 */
	private Class<Entity> entityType;

	/**
	 * 
	 * Returns the class of the entity type.
	 * 
	 * @return The class.
	 */
	protected Class<Entity> getEntityType() {
		return entityType;
	}

	@CreatingServiceContainer
	protected DomainModelEntityRestServiceContainer creatingServiceContainer = null;

	/**
	 * This method returns the service container which created this resource
	 * 
	 * @return the service container
	 */
	protected DomainModelEntityRestServiceContainer getServiceContainer() {
		return creatingServiceContainer;
	}

	/**
	 * This method returns an empty template of the given entity type. An empty
	 * template is one that matches all entities.
	 * 
	 * @return an empty template
	 */
	protected Entity newEmptyEntityTemplate() {
		Entity template = null;
		try {
			template = getEntityType().newInstance();
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		}
		return template;
	}

	/**
	 * This method transforms CGI query parameter passed to the
	 * <code>findByTemplate</code> method into an entity template. By default,
	 * it is attempted to set the attribute with the same name as the query
	 * parameter to the value of the query parameter. If more sophisticated
	 * treatment of query attributes is required, this method should be
	 * overridden.
	 * 
	 * @param emptyTemplate
	 *            the template for which to set the attribute values
	 * @param queryParams
	 *            the map of query parameters
	 * @return the entity template to use for the query.
	 * @throws ConstraintViolationException
	 *             in case that a template could not be built
	 */
	protected Entity createEntityTemplateFromQueryParameters(Entity emptyTemplate, MultivaluedMap<String, String> queryParams) {
		Map<String, String> cgiParameters = new HashMap<String, String>();
		for (String key : queryParams.keySet())
			cgiParameters.put(key, queryParams.getFirst(key));

		emptyTemplate.fromQueryParameters(cgiParameters);

		return emptyTemplate;
	}

	/**
	 * Given a set of entities, this method creates an XML list of references to
	 * those entities.
	 * 
	 * The form is <blockquote> <list
	 * xmlns="http://www.mercatis.com/lighthouse3"> <id>AAA</id> <id>BBB</id>
	 * </list> </blockquote>
	 * 
	 * @param entities
	 *            the set of entities or entity ids for which to create the list
	 * @return the XML list of references
	 */
	@SuppressWarnings("rawtypes")
	protected String createXmlListOfEntityReferences(List idsOrDomainModelEntities) {
		StringWriter result = new StringWriter();
		XmlWriter xml = new XmlEncXmlWriter(result);

		try {
			xml.writeEntity("list");
			xml.writeAttribute("xmlns", XmlMuncher.MERCATIS_NS);

			for (Object idOrEntity : idsOrDomainModelEntities) {
				if (idOrEntity instanceof DomainModelEntity) {
					xml.writeEntityWithText("id", ((DomainModelEntity) idOrEntity).getId());
				} else {
					xml.writeEntityWithText("id", idOrEntity.toString());
				}
			}

			xml.endEntity();
		} catch (IOException e) {
			throw new ConflictException("Error creating result list");
		}

		return result.toString();
	}

	@SuppressWarnings("rawtypes")
	protected String createDetailedXmlListOfEntities(List domainModelEntities) {
		StringWriter result = new StringWriter();
		XmlWriter xml = new XmlEncXmlWriter(result);

		try {
			xml.writeEntity("list");
			xml.writeAttribute("xmlns", XmlMuncher.MERCATIS_NS);

			for (Object entity : domainModelEntities) {
				((DomainModelEntity) entity).toXml(xml);
			}

			xml.endEntity();
		} catch (IOException e) {
			throw new ConflictException("Error creating result list");
		}

		return result.toString();
	}

	/**
	 * This method has to be overridden to return the appropriate DAOs for the
	 * entity represented by the given resource.
	 * 
	 * @return the entity registry.
	 */
	public abstract DomainModelEntityDAO<Entity> getEntityDAO();

	/**
	 * This method returns the array of DAOs required to execute the
	 * <code>fromXml()</code> method of the entities represented by the current
	 * resource.
	 * 
	 * @return the array of DAOs required for entity deserialization. As a
	 *         default, an empty array is returned.
	 */
	@SuppressWarnings("rawtypes")
	public DomainModelEntityDAO[] getDeserializationDAOs() {
		return new DomainModelEntityDAO[] {};
	}

	/**
	 * This method implements the HTTP <code>GET</code> method of the present
	 * domain model entity resource. It facilitates the retrieval of the
	 * lighthouse domain of the present server.
	 * 
	 * @return a500 code in case of internal errors, a 200 code otherwise. The
	 *         body of the response contains the lighthouse domain name
	 */
	@GET
	@Path("/LighthouseDomain")
	@Produces("text/ascii")
	public Response getLighthouseDomain(@Context HttpServletRequest servletRequest) {
		if (log.isDebugEnabled())
			log.debug("Returning lighthouse domain");

		String lighthouseDomain = null;

		Entity entity = null;
		try {
			entity = getEntityType().newInstance();
		} catch (Exception e) {
			log.error("Persistence exception caught while retrieving lighthouse domain", e);

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
		}

		lighthouseDomain = entity.getLighthouseDomain();

		if (log.isDebugEnabled())
			log.debug("Returned lighthouse domain " + lighthouseDomain);

		return Response.status(Status.OK).entity(lighthouseDomain).build();
	}

	/**
	 * This method implements the HTTP <code>GET</code> method of the present
	 * domain model entity resource. It facilitates retrieval by id of the
	 * entities represented by the resource. It expects that the entity to be
	 * retrieved is addressed via the following URL pattern
	 * <code>/{Entity}/{id}</code> where <code>{Entity}</code> is the class name
	 * of entity and <code>{id}</code> is the database id of the entity.
	 * 
	 * @param id
	 *            receives the id of the entity to be retrieved.
	 * @return a 404 code if the entity could not be found, 500 code in case of
	 *         internal errors, a 200 code otherwise. The body of the response
	 *         contains the XML representation of the retrieved entity.
	 */
	@GET
	@Path("{id}")
	@Produces("application/xml;charset=utf-8")
	public Response find(@PathParam("id") String id, @Context UriInfo ui, @Context HttpServletRequest servletRequest) {
		if (log.isDebugEnabled())
			log.debug("Retrieving " + getEntityDAO().getManagedType().getSimpleName() + " with id " + id);

		String xmlString = null;
		try {
			Entity entity = getEntityDAO().find(Long.parseLong(id));
			if (entity != null) {
				if (overridableDenyAccess(servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), entity))
					return Response.status(Status.UNAUTHORIZED).build();
				
				xmlString = entity.toXml();

				if (log.isDebugEnabled())
					log.debug("Found  " + getEntityDAO().getManagedType().getSimpleName() + ": " + entity.toXml());
			}
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		if (xmlString == null) {
			if (log.isDebugEnabled())
				log.debug("Could not find " + getEntityDAO().getManagedType().getSimpleName() + " with id " + id);
			return Response.status(Status.NOT_FOUND).entity("Could not find resource with id " + id).build();
		}
		return Response.status(Status.OK).entity(xmlString).build();
	}

	/**
	 * This method implements the HTTP <code>GET</code> method of the present
	 * domain model entity resource. It facilitates the retrieval of all ids of
	 * those entities that match a given pattern. It expects the method to be
	 * addressed via the following URL pattern
	 * <code>/{Entity}?{attribute1}={value1}&...&{attributen}={valuen}</code> .
	 * code>{Entity}</code> is the class name of entity.
	 * <code>{attribute1}={value1}</code> upto
	 * <code>{attributen}={valuen}</code> encode the values of attributes the
	 * matching components should have.
	 * 
	 * @return a 500 code if the entity ids not be retrieved, a 200 code
	 *         otherwise. The body of the response contains the XML
	 *         representation of the retrieved entity codes.
	 */
	@GET
	@Produces("application/xml")
	public Response findByTemplate(@Context UriInfo ui, @Context HttpServletRequest servletRequest) {
		if (log.isDebugEnabled())
			log.debug("Retrieving " + getEntityDAO().getManagedType().getSimpleName() + " by template " + ui.getRequestUri());

		MultivaluedMap<String, String> queryParams = ui.getQueryParameters(true);
		Entity template = null;

		try {
			template = createEntityTemplateFromQueryParameters(newEmptyEntityTemplate(), queryParams);
		} catch (ConstraintViolationException exception) {
			log.error("Constraint violation by " + getEntityDAO().getManagedType().getSimpleName() + " template", exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		String xmlString = null;
		try {
			List<Entity> matches = getEntityDAO().findByTemplate(template);
			List<Entity> allowed = new LinkedList<Entity>();
			for (Entity e : matches) {
		    	if (!overridableDenyAccess(servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), e))
		    		allowed.add(e);
			}
			boolean detailed = queryParams.containsKey("detailed") ? Boolean.parseBoolean(queryParams.get("detailed").iterator().next()) : false;
			if (detailed)
				xmlString = createDetailedXmlListOfEntities(allowed);
			else
				xmlString = createXmlListOfEntityReferences(allowed);

			if (log.isDebugEnabled())
				log.debug("Matching " + getEntityDAO().getManagedType().getSimpleName() + "s found for template");
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}
		return Response.status(Status.OK).entity(xmlString).build();
	}

	/**
	 * This method implements the HTTP <code>POST</code> method of the present
	 * domain model entity resource. It facilitates creation of the entities
	 * represented by the resource. It expects that the XML representation of
	 * the entity to be created is passed as the post request payload to the
	 * following URL pattern <code>/{Entity}</code> where <code>{Entity}</code>
	 * is the class name of entity.
	 * 
	 * @param payload
	 *            receives the XML representation of the entity to be created.
	 * @return a 415 code if the payload could not be parsed, a 500 code if the
	 *         entity could not be created, a 200 code otherwise.
	 */
	@POST
	@Consumes("application/xml")
	@Produces("text/plain")
	public Response persist(String payload, @Context HttpServletRequest servletRequest) {
		if (log.isDebugEnabled())
			log.debug("Persisting " + getEntityDAO().getManagedType().getSimpleName() + " using " + payload);

		Entity entity = newEmptyEntityTemplate();

		try {
			entity.fromXml(payload, getDeserializationDAOs());
	    	if (overridableDenyAccess(servletRequest.getRemoteUser(), getContextRole(CtxOp.PERSIST), entity))
	    		return Response.status(Status.UNAUTHORIZED).build();
		} catch (XMLSerializationException exception) {
			log.error("XML serialization exception caught while persisting " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).entity(exception.toString()).build();
		}
		try {
			getEntityDAO().persist(entity);
			getEntityDAO().getUnitOfWork().commit();
			if (log.isDebugEnabled())
				log.debug(getEntityDAO().getManagedType().getSimpleName() + " persisted.");
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while persisting " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}
		notifyResourceEventListeners(ResourceEventListener.ENTITY_CREATED, entity);
		return Response.status(Status.OK).build();
	}

	protected void makeEntityWritable(Entity e) {
		getEntityDAO().getUnitOfWork().setReadOnly(e, false);
	}

	/**
	 * This method implements the HTTP <code>PUT</code> method of the present
	 * domain model entity resource. It facilitates updates of the entities
	 * represented by the resource. It expects that the XML representation of
	 * the entity to be created is passed as the post request payload to the
	 * following URL pattern <code>/{Entity}</code> where <code>{Entity}</code>
	 * is the class name of entity.
	 * 
	 * @param payload
	 *            receives the XML representation of the entity to be updated.
	 * @return a 415 code if the payload could not be parsed, a 500 code if the
	 *         entity could not be updated, a 404 code if the entity to be
	 *         updated could not be found, a 200 code otherwise.
	 */
	@PUT
	@Consumes("application/xml")
	@Produces("text/plain")
	public Response update(String payload, @Context UriInfo ui, @Context HttpServletRequest servletRequest) {
		if (log.isDebugEnabled())
			log.debug("Updating " + getEntityDAO().getManagedType().getSimpleName() + " using " + payload);

		String id = XmlMuncher.readValueFromXml(payload, "//:id");

		if (id == null) {
			log.error("Cannot parse payload: id element missing" + getEntityDAO().getManagedType().getSimpleName());
			return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).entity("Cannot parse payload").build();
		}
		Entity entityInDb = null;

		try {
			entityInDb = getEntityDAO().find(Long.parseLong(id));

		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while updating " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		if (entityInDb == null) {
			log.error("Updating non-persistent " + getEntityDAO().getManagedType().getSimpleName());
			return Response.status(Status.NOT_FOUND).entity("Entity not persistent. Could not find resource with id " + id).build();
		}

    	if (overridableDenyAccess(servletRequest.getRemoteUser(), getContextRole(CtxOp.UPDATE), entityInDb))
    		return Response.status(Status.UNAUTHORIZED).build();
		
		try {
			entityInDb.fromXml(payload, getDeserializationDAOs());
		} catch (XMLSerializationException exception) {
			log.error("XML serialization exception caught while updating " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).entity(exception.toString()).build();
		}

		try {
			getEntityDAO().update(entityInDb);
			notifyResourceEventListeners(ResourceEventListener.ENTITY_UPDATED, entityInDb);

			if (log.isDebugEnabled())
				log.debug(getEntityDAO().getManagedType().getSimpleName() + " updated.");
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while updating " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		return Response.status(Status.OK).build();
	}

	/**
	 * This method implements the HTTP <code>DELETE</code> method of the present
	 * domain model entity resource. It facilitates the deletion of the entities
	 * represented by the resource. It expects that the entity to be deleted is
	 * addressed via the following URL pattern <code>/{Entity}/{id}</code>.
	 * <code>Entity</code> is the entity class name, <code>{id}</code> is the id
	 * of the entity.
	 * 
	 * @param id
	 *            receives the id of the entity to be deleted.
	 * @return a 500 code in case of an internal problem, a 404 code if the
	 *         entity could not be found, a 200 code otherwise.
	 */
	@DELETE
	@Path("{id}")
	@Produces("text/plain")
	public Response delete(@PathParam("id") String id, @Context HttpServletRequest servletRequest) {
		Entity entity = null;

		try {
			entity = getEntityDAO().find(Long.parseLong(id));
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while deleting " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		if (log.isDebugEnabled())
			log.debug("Deleting " + getEntityDAO().getManagedType().getSimpleName() + " with id " + id);
		
		if (entity == null) {
			log.error("Deleting non-persistent " + getEntityDAO().getManagedType().getSimpleName());
			return Response.status(Status.NOT_FOUND).entity("Entity not persistent. Could not find entity with id " + id).build();
		}
		
    	if (overridableDenyAccess(servletRequest.getRemoteUser(), getContextRole(CtxOp.DELETE), entity))
    		return Response.status(Status.UNAUTHORIZED).build();
		
		try {
			getEntityDAO().delete(entity);
			notifyResourceEventListeners(ResourceEventListener.ENTITY_DELETED, entity);

			if (log.isDebugEnabled())
				log.debug(getEntityDAO().getManagedType().getSimpleName() + " with id " + id + " deleted");
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while deleting " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}
		return Response.status(Status.OK).build();
	}

	// override in hierachicaldomainmodelentity
	protected boolean overridableDenyAccess(String remoteUser, String role, Entity entity) {
		return LighthouseAuthorizator.denyAccess(getServiceContainer(), remoteUser, role, getEntityContext(entity));
	}

}