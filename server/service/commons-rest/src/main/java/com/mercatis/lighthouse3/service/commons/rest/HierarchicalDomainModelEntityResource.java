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

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.service.jaas.util.LighthouseAuthorizator;

/**
 * This is an abstract base class wrapping a hierarchical domain model entities
 * as a REST web service resource.
 */
public abstract class HierarchicalDomainModelEntityResource<Entity extends HierarchicalDomainModelEntity<Entity>> extends
		CodedDomainModelEntityResource<Entity> {

	@Override
	public abstract HierarchicalDomainModelEntityDAO<Entity> getEntityDAO();

	@Override
	@SuppressWarnings("rawtypes")
	public DomainModelEntityDAO[] getDeserializationDAOs() {
		return new DomainModelEntityDAO[] { getEntityDAO() };
	}

	/**
	 * This method implements the HTTP <code>GET</code> method of the present
	 * hierarchical domain model entity resource. It facilitates the retrieval
	 * of all toplevel codes of the entities represented by the resource. It
	 * expects the method to be addressed via the following URL pattern
	 * <code>/{Entity}/Code/toplevel</code> where <code>{Entity}</code> is the
	 * class name of entity.
	 * 
	 * @return a 500 code if the entity codes could not be retrieved, a 200 code
	 *         otherwise. The body of the response contains the XML
	 *         representation of the retrieved entity codes.
	 * 
	 */
	@GET
	@Path("/Code/toplevel")
	@Produces("application/xml")
	public Response findAllTopLevelCodes(@Context HttpServletRequest servletRequest) {
		if (log.isDebugEnabled())
			log.debug("Retrieving all toplevel codes for " + getEntityDAO().getManagedType().getSimpleName() + "s");
		String xmlString = null;

		try {
			List<String> toplevelCodes = getEntityDAO().findAllTopLevelComponentCodes();
			List<String> allowed = new LinkedList<String>();
			for (String code : toplevelCodes) {
		    	if (LighthouseAuthorizator.allowAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), formatContext(code, null)))
		    		allowed.add(code);
			}

			if (log.isDebugEnabled())
				log.debug("Toplevel codes for " + getEntityDAO().getManagedType().getSimpleName() + "s");

			xmlString = createXmlListOfEntityReferences(allowed);
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving toplevel codes for " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}
		return Response.status(Status.OK).entity(xmlString).build();
	}

	@Override
	public Response findByTemplate(UriInfo ui, HttpServletRequest servletRequest) {
		if (log.isDebugEnabled())
			log.debug("Retrieving " + getEntityDAO().getManagedType().getSimpleName() + " by template " + ui.getRequestUri());
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters(true);

		Entity template = newEmptyEntityTemplate();

		for (String attributeName : queryParams.keySet()) {
			String value = queryParams.getFirst(attributeName);

			if (attributeName.equals("parentEntity")) {
				Entity parentEntity = getEntityDAO().findByCode(value);
				if (parentEntity != null)
					template.setParentEntity(parentEntity);
			} else {
				template.setAttributeByName(attributeName, value);
			}
		}

		String xmlString = null;

		try {
			List<Entity> matches = getEntityDAO().findByTemplate(template);
			List<Entity> allowed = new LinkedList<Entity>();
			for (Entity e : matches) {
		    	if (!overridableDenyAccess(servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), e))
		    		allowed.add(e);
			}
			if (log.isDebugEnabled())
				log.debug("Matching " + getEntityDAO().getManagedType().getSimpleName() + "s found for template");
			boolean detailed = queryParams.containsKey("detailed") ? Boolean.parseBoolean(queryParams.get("detailed").iterator().next()) : false;
			if (detailed)
				xmlString = createDetailedXmlListOfEntities(allowed);
			else
				xmlString = createXmlListOfEntityReferences(allowed);
			return Response.status(Status.OK).entity(xmlString).build();
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}
	}

	@Override
	protected void makeEntityWritable(Entity e) {
		for (Entity sub : e.getSubEntities())
			makeEntityWritable(sub);
		super.makeEntityWritable(e);
	}
	
	// this will grant access if the user is allowed to access a parent entity
	@Override
	protected boolean overridableDenyAccess(String remoteUser, String role, Entity entity) {
		DomainModelEntityRestServiceContainer dmersc = getServiceContainer(); 
		Entity e = entity;
		while (e != null) {
			if (LighthouseAuthorizator.allowAccess(dmersc, remoteUser, role, getEntityContext(e)))
				return false;
			e = e.getParentEntity();
		}
		return true;
	}
}
