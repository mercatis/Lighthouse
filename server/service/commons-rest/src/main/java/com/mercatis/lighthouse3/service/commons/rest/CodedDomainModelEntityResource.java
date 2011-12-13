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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.generationjava.io.xml.XmlEncXmlWriter;
import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.sun.jersey.api.ConflictException;

/**
 * This is an abstract base class wrapping a coded domain model entities as a
 * REST web service resource. The main difference to normal entity resources is
 * that coded domain model entity resources are addressed via their code and not
 * their id
 */
public abstract class CodedDomainModelEntityResource<Entity extends CodedDomainModelEntity> extends DomainModelEntityResource<Entity> {
	@Override
	synchronized public void notifyResourceEventListeners(int notificationType, Entity entity) {
		addInitialResourceEventListeners();

		if (log.isDebugEnabled()) {
			log.debug("Notifying ResourceEventListeners about change on '" + entity.getRootElementName() + "'.");
		}

		for (ResourceEventListener listener : resourceEventListeners)
			if (notificationType == ResourceEventListener.ENTITY_CREATED)
				listener.entityCreated(this, "" + entity.getCode());
			else if (notificationType == ResourceEventListener.ENTITY_UPDATED)
				listener.entityUpdated(this, "" + entity.getCode());
			else if (notificationType == ResourceEventListener.ENTITY_DELETED)
				listener.entityDeleted(this, "" + entity.getCode());
	}

	/**
	 * Given a set of entities, this method creates an XML list of references to
	 * those entities.
	 * 
	 * The form is <blockquote> <list
	 * xmlns="http://www.mercatis.com/lighthouse3"> <code>AAA</code>
	 * <code>BBB</code> </list> </blockquote>
	 * 
	 * @param entities
	 *            the set of entities or entity ids for which to create the list
	 * @return the XML list of references
	 */
	@SuppressWarnings("rawtypes")
	@Override
	protected String createXmlListOfEntityReferences(List codesOrDomainModelEntities) {
		StringWriter result = new StringWriter();
		XmlWriter xml = new XmlEncXmlWriter(result);

		try {
			xml.writeEntity("list");
			xml.writeAttribute("xmlns", XmlMuncher.MERCATIS_NS);

			for (Object codeOrEntity : codesOrDomainModelEntities) {
				if (codeOrEntity instanceof DomainModelEntity) {
					xml.writeEntityWithText("code", ((CodedDomainModelEntity) codeOrEntity).getCode());
				} else {
					xml.writeEntityWithText("code", codeOrEntity.toString());
				}
			}
			xml.endEntity();
		} catch (IOException e) {
			throw new ConflictException("Error creating result list");
		}

		return result.toString();
	}

	@Override
	public abstract CodedDomainModelEntityDAO<Entity> getEntityDAO();

	/**
	 * This method implements the HTTP <code>GET</code> method of the present
	 * coded domain model entity resource. It facilitates retrieval by code of
	 * the entities represented by the resource. It expects that the entity to
	 * be retrieved is addressed via the following URL pattern
	 * <code>/{Entity}/{code}</code> where <code>{Entity}</code> is the class
	 * name of entity and <code>{code}</code> is the code of the entity.
	 * 
	 * @param code
	 *            receives the code of the entity to be retrieved.
	 * @return a 404 code if the entity could not be found, 500 code in case of
	 *         internal errors, a 200 code otherwise. The body of the response
	 *         contains the XML representation of the retrieved entity.
	 */
	@GET
	@Path("{code}")
	@Produces("application/xml")
	public Response find(@PathParam("code") String code, @Context UriInfo ui, @Context HttpServletRequest servletRequest) {
		CodedDomainModelEntityDAO<Entity> dao = getEntityDAO();
		if (log.isDebugEnabled())
			log.debug("Retrieving " + dao.getManagedType().getSimpleName() + " with code '" + code + "'.");

		String xmlString = null;

		try {
			Entity entity = getEntityDAO().findByCode(code);
			if (entity != null) {
		    	if (overridableDenyAccess(servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), entity))
		    		return Response.status(Status.UNAUTHORIZED).build();
				
				xmlString = entity.toXml();

				if (log.isDebugEnabled())
					log.debug("Found  " + getEntityDAO().getManagedType().getSimpleName() + ": " + xmlString);
			}
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		if (xmlString == null) {
			if (log.isDebugEnabled())
				log.debug("Could not find " + getEntityDAO().getManagedType().getSimpleName() + " with code '" + code + "'.");
			return Response.status(Status.NOT_FOUND).entity("Could not find resource with code " + code).build();
		}
		return Response.status(Status.OK).entity(xmlString).build();
	}
	
	@Override
	public Response update(String payload, UriInfo ui, HttpServletRequest servletRequest) {
		if (log.isDebugEnabled())
			log.debug("Updating " + getEntityDAO().getManagedType().getSimpleName() + " using " + payload);

		String code = XmlMuncher.readValueFromXml(payload, "//:code");

		if (code == null) {
			log.error("Cannot parse payload: code element missing for " + getEntityDAO().getManagedType().getSimpleName());
			return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).entity("Cannot parse payload").build();
		}

		Entity entityInDb = null;

		try {
			entityInDb = getEntityDAO().findByCode(code);
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while updating " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		if (entityInDb == null) {
			log.error("Updating non-persistent " + getEntityDAO().getManagedType().getSimpleName());
			return Response.status(Status.NOT_FOUND).entity("Entity not persistent. Could not find resource with code " + code).build();
		}

		try {
	    	if (overridableDenyAccess(servletRequest.getRemoteUser(), getContextRole(CtxOp.UPDATE), entityInDb))
	    		return Response.status(Status.UNAUTHORIZED).build();
			
			makeEntityWritable(entityInDb);
			entityInDb.fromXml(payload, getDeserializationDAOs());
		} catch (XMLSerializationException exception) {
			log.error("XML serialization exception caught while updating " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).entity(exception.toString()).build();
		}

		try {
			getEntityDAO().update(entityInDb);
			getEntityDAO().getUnitOfWork().commit();
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while updating " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		try {
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
	 * coded domain model entity resource. It facilitates the deletion of the
	 * entities represented by the resource. It expects that the entity to be
	 * deleted is addressed via the following URL pattern
	 * <code>/{Entity}/{code}</code>. <code>Entity</code> is the entity class
	 * name, <code>{code}</code> is the code of the entity.
	 * 
	 * @param code
	 *            receives the code of the entity to be deleted.
	 * @return a 500 code in case of an internal problem, a 404 code if the
	 *         entity could not be found, a 200 code otherwise.
	 */
	@DELETE
	@Path("{code}")
	@Produces("text/plain")
	public Response delete(@PathParam("code") String code, @Context HttpServletRequest servletRequest) {
		if (log.isDebugEnabled())
			log.debug("Deleting " + getEntityDAO().getManagedType().getSimpleName() + " with code " + code);

		Entity entity = null;

		try {
			entity = getEntityDAO().findByCode(code);
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while deleting " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		if (entity == null) {
			log.error("Entity " + getEntityDAO().getManagedType().getSimpleName() + " not found in database: " + code);
			return Response.status(Status.NOT_FOUND).entity("Entity not persistent. Could not find entity with code " + code).build();
		}

		try {
	    	if (overridableDenyAccess(servletRequest.getRemoteUser(), getContextRole(CtxOp.DELETE), entity))
	    		return Response.status(Status.UNAUTHORIZED).build();

			if (log.isDebugEnabled()) {
				log.debug("Entity to be deleted: " + entity.toXml());
			}
			getEntityDAO().delete(entity);
			notifyResourceEventListeners(ResourceEventListener.ENTITY_DELETED, entity);

			if (log.isDebugEnabled())
				log.debug(getEntityDAO().getManagedType().getSimpleName() + " with code " + code + " deleted");
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while deleting " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}
		return Response.status(Status.OK).build();
	}

	@Override
	protected StringBuilder getEntityContext(Entity e) {
		return formatContext(e.getCode(), null);
	}
}
