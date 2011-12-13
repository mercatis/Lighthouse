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
package com.mercatis.lighthouse3.service.environment.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.generationjava.io.xml.XmlEncXmlWriter;
import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.ConstraintViolationException;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource;
import com.mercatis.lighthouse3.service.commons.rest.ResourceEventListener;
import com.mercatis.lighthouse3.service.jaas.util.LighthouseAuthorizator;
import com.sun.jersey.api.ConflictException;

/**
 * This class implements the REST resource capturing software components in the
 * environment domain model of Lighthouse. As such, CRUD functionality on
 * software components is made available in a RESTful way by the environment
 * service.
 */
@Path("/Deployment")
public class DeploymentResource extends DomainModelEntityResource<Deployment> {

	@SuppressWarnings("rawtypes")
	@Override
	public DomainModelEntityDAO[] getDeserializationDAOs() {
		return new DomainModelEntityDAO[] { getServiceContainer().getDAO(SoftwareComponentRegistry.class) };
	}

	@Override
	public DomainModelEntityDAO<Deployment> getEntityDAO() {
		return getServiceContainer().getDAO(DeploymentRegistry.class);
	}

	public DeploymentRegistry getDeploymentRegistry() {
		return (DeploymentRegistry) getEntityDAO();
	}

	public SoftwareComponentRegistry getSoftwareComponentRegistry() {
		return (SoftwareComponentRegistry) getDeserializationDAOs()[0];
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public String createXmlListOfEntityReferences(List locationsOrDeployments) {
		StringWriter result = new StringWriter();
		XmlWriter xml = new XmlEncXmlWriter(result);

		try {
			xml.writeEntity("list");
			xml.writeAttribute("xmlns", XmlMuncher.MERCATIS_NS);

			for (Object locationOrDeployment : locationsOrDeployments) {
				if (locationOrDeployment instanceof Deployment) {
					xml.writeEntity("entry");
					xml.writeEntityWithText("code", ((Deployment) locationOrDeployment).getDeployedComponent()
							.getCode());
					xml.writeEntityWithText("location", ((Deployment) locationOrDeployment).getLocation());
					xml.endEntity();
				} else {
					xml.writeEntityWithText("location", locationOrDeployment.toString());
				}
			}

			xml.endEntity();
		} catch (IOException e) {
			throw new ConflictException("Error creating result list");
		}

		return result.toString();
	}

	@Override
	protected Deployment createEntityTemplateFromQueryParameters(Deployment template,
			MultivaluedMap<String, String> queryParams) {

		if (queryParams.containsKey("code")) {
			String codeValue = queryParams.getFirst("code");

			try {
				SoftwareComponent softwareComponent = getSoftwareComponentRegistry().findByCode(codeValue);
				if (softwareComponent == null)
					throw new ConstraintViolationException("Could not find resource with code " + codeValue, null);
				else
					template.setDeployedComponent(softwareComponent);
			} catch (PersistenceException exception) {
				throw new ConstraintViolationException("Error while looking up resource with code " + codeValue,
						exception);
			}
		}

		return super.createEntityTemplateFromQueryParameters(template, queryParams);
	}

	@Override
	public Response update(String payload, UriInfo ui, HttpServletRequest servletRequest) {
		String code = XmlMuncher.readValueFromXml(payload, "//:code");
		String location = XmlMuncher.readValueFromXml(payload, "//:location");

		if ((code == null) || (location == null)) {
			log.error("Cannot parse payload: code and location element missing for Deployment");

			return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).entity("Cannot parse payload").build();
		}
		
		if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.UPDATE), formatContext(location, code)))
    		return Response.status(Status.UNAUTHORIZED).build();
		if (log.isDebugEnabled())
			log.debug("Updating Deployment using " + payload);

		Deployment deploymentInDb = null;

		try {
			SoftwareComponent softwareComponent = getSoftwareComponentRegistry().findByCode(code);
			if (softwareComponent == null) {
				log.error("Could not find SoftwareComponent with code " + code);
				return Response.status(Status.NOT_FOUND).entity("Could not find resource with code " + code).build();
			}
			deploymentInDb = getDeploymentRegistry().findByComponentAndLocation(softwareComponent, location);
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving Deployment");
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		if (deploymentInDb == null) {
			log.error("Could not find Deployment with code" + code + " at " + location);
			return Response.status(Status.NOT_FOUND).entity(
					"Entity not persistent. Could not find resource with code " + code + " and location " + location)
					.build();
		}

		try {
			deploymentInDb.fromXml(payload, getDeserializationDAOs());
		} catch (XMLSerializationException exception) {
			log.error("XML serialization exception caught while updating Deployment", exception);
			return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).entity(exception.toString()).build();
		}

		try {
			getDeploymentRegistry().update(deploymentInDb);

			if (log.isDebugEnabled())
				log.debug("Deployment updated.");
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while updating Deployment", exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		notifyResourceEventListeners(ResourceEventListener.ENTITY_UPDATED, deploymentInDb);
		return Response.status(Status.OK).build();
	}

	/**
	 * This method implements the HTTP <code>DELETE</code> method of the
	 * deployment entity resource. It facilitates the deletion of deployments.
	 * It expects that the deployment to be deleted is addressed via the
	 * following URL pattern <code>/Deployment/{location}/{code}</code>.
	 * <code>{code}</code> is the code of the deployed software component and
	 * <code>{location}</code> deployment location.
	 * 
	 * @param location
	 *            receives the location of the deployment to be deleted.
	 * @param code
	 *            receives the code of the deployment to be deleted.
	 * @return a 500 code in case of an internal problem, a 404 code if the
	 *         deployment or deployed software component could not be found, a
	 *         200 code otherwise.
	 */
	@DELETE
	@Path("{location}/{code}")
	@Produces("text/plain")
	public Response delete(@PathParam("location") String location, @PathParam("code") String code, @Context HttpServletRequest servletRequest) {
    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.DELETE), formatContext(location, code)))
    		return Response.status(Status.UNAUTHORIZED).build();
		if (log.isDebugEnabled())
			log.debug("Deleting Deployment of component " + code + " at " + location);

		Deployment deployment = null;

		try {
			SoftwareComponent softwareComponent = getSoftwareComponentRegistry().findByCode(code);
			if (softwareComponent == null) {
				log.error("Could not find SoftwareComponent with code " + code);

				return Response.status(Status.NOT_FOUND).entity("Could not find resource with code " + code).build();
			}

			deployment = getDeploymentRegistry().findByComponentAndLocation(softwareComponent, location);
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while deleting Deployment", exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		if (deployment == null) {
			log.error("Could not find Deployment with code" + code + " at " + location);
			return Response.status(Status.NOT_FOUND).entity(
					"Entity not persistent. Could not find resource with code " + code + " and location " + location)
					.build();
		}
		try {
			getDeploymentRegistry().delete(deployment);

			if (log.isDebugEnabled())
				log.debug("Deployment deleted.");
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while deleting Deployment", exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		notifyResourceEventListeners(ResourceEventListener.ENTITY_DELETED, deployment);
		return Response.status(Status.OK).build();
	}

	/**
	 * This method implements the HTTP <code>GET</code> method of the deployment
	 * resource. It facilitates retrieval by code and location of deployments.
	 * It expects that the deployment to be retrieved is addressed via the
	 * following URL pattern <code>/Deployment/{location}/{code}</code> where
	 * <code>{location}</code> is the deployment location <code>{code}</code> is
	 * the code of the deployed software component.
	 * 
	 * @param code
	 *            receives the code of the entity to be retrieved.
	 * @return a 404 code if the deployment or deployed software component could
	 *         not be found, , 500 code in case of internal errors, a 200 code
	 *         otherwise. The body of the response contains the XML
	 *         representation of the retrieved deployment.
	 */
	@GET
	@Path("{location}/{code}")
	@Produces("application/xml")
	public Response findByLocationAndCode(@PathParam("location") String location, @PathParam("code") String code, @Context HttpServletRequest servletRequest) {
    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), formatContext(location, code)))
    		return Response.status(Status.UNAUTHORIZED).build();
		if (log.isDebugEnabled())
			log.debug("Retrieving Deployment of component " + code + " at " + location);

		Deployment deployment = null;

		try {
			SoftwareComponent softwareComponent = getSoftwareComponentRegistry().findByCode(code);
			if (softwareComponent == null) {
				log.error("Could not find SoftwareComponent with code " + code);
				return Response.status(Status.NOT_FOUND).entity("Could not find resource with code " + code).build();
			}
			deployment = getDeploymentRegistry().findByComponentAndLocation(softwareComponent, location);
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving Deployment", exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		if (deployment == null) {
			log.error("Could not find Deployment with code " + code + " at " + location);
			return Response.status(Status.NOT_FOUND).entity(
					"Could not find resource with code " + code + " and location " + location).build();
		}

		if (log.isDebugEnabled())
			log.debug("Deployment retrieved: " + deployment.toXml());
		String s = deployment.toXml();
		return Response.status(Status.OK).entity(s).build();
	}

	/**
	 * This method implements the HTTP <code>GET</code> method of the deployment
	 * resource. It facilitates the retrieval of all locations where deployments
	 * exist. It expects the method to be addressed via the following URL
	 * pattern <code>/Deployment/Location/all</code>.
	 * 
	 * @return a 500 code if the entity codes could not be retrieved, a 200 code
	 *         otherwise. The body of the response contains the XML
	 *         representation of the retrieved entity codes.
	 * 
	 *         The form is <blockquote> <list
	 *         xmlns="http://www.mercatis.com/lighthouse3">
	 *         <location>AAA</location> <location>BBB</location> </list>
	 *         </blockquote>
	 */
	@GET
	@Path("/Location/all")
	@Produces("application/xml")
	public Response findAllLocations(@Context HttpServletRequest servletRequest) {
		if (log.isDebugEnabled())
			log.debug("Retrieving all locations with Deployments");

		List<String> locations = null;

		try {
			locations = getDeploymentRegistry().findAllLocations();

			if (log.isDebugEnabled())
				log.debug("Locations with Deployments found.");

		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving all Deployment locations", exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}
		String s = createXmlListOfEntityReferences(locations);
		return Response.status(Status.OK).entity(s).build();
	}

	/**
	 * This method implements the HTTP <code>GET</code> method of the deployment
	 * resource. It facilitates the retrieval of the component codes and
	 * locations of all deployments at a given location. It expects the method
	 * to be addressed via the following URL pattern
	 * <code>/Deployment/{location}</code> where <code>{location}</code> is the
	 * location of interest.
	 * 
	 * @return a 500 code if the entity codes could not be retrieved, a 200 code
	 *         otherwise. The body of the response contains the XML
	 *         representation of the retrieved deployments.
	 * 
	 *         The form is <blockquote> <list
	 *         xmlns="http://www.mercatis.com/lighthouse3"> <entry>
	 *         <code>AAA</code> <location>BBB</location></entry>... </list>
	 *         </blockquote>
	 */
	@GET
	@Path("{location}")
	@Produces("application/xml")
	public Response findAtLocation(@PathParam("location") String location, @Context HttpServletRequest servletRequest) {
		if (log.isDebugEnabled())
			log.debug("Retrieving all Deployments at locations " + location);

		List<Deployment> deployments = null;

		try {
			deployments = getDeploymentRegistry().findAtLocation(location);
			List<Deployment> allowed = new LinkedList<Deployment>();
			for (Deployment d : deployments) {
				if (LighthouseAuthorizator.allowAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), formatContext(location, d.getDeployedComponent().getCode())))
					allowed.add(d);
			}
			deployments = allowed;

			if (log.isDebugEnabled())
				log.debug("Deployments found at location " + location);
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving all Deployments at location", exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}
		String s = createXmlListOfEntityReferences(deployments);
		return Response.status(Status.OK).entity(s).build();
	}

	/**
	 * This method implements the HTTP <code>GET</code> method of the deployment
	 * resource. It facilitates the retrieval of the component codes and
	 * locations of all deployments of a given software component. It expects
	 * the method to be addressed via the following URL pattern
	 * <code>/Deployment/SoftwareComponent/{code}</code> where
	 * <code>{code}</code> is the code of the software component of interest.
	 * 
	 * @return a 500 code if the entity codes could not be retrieved, a 404 code
	 *         if the deployment or deployed software component could not be
	 *         found, a 200 code otherwise. The body of the response contains
	 *         the XML representation of the retrieved deployments.
	 * 
	 *         The form is <blockquote> <list
	 *         xmlns="http://www.mercatis.com/lighthouse3"> <entry>
	 *         <code>AAA</code> <location>BBB</location></entry>... </list>
	 *         </blockquote>
	 */
	@GET
	@Path("SoftwareComponent/{code}")
	@Produces("application/xml")
	public Response findByComponent(@PathParam("code") String code, @Context HttpServletRequest servletRequest) {
		if (log.isDebugEnabled())
			log.debug("Retrieving all Deployments for SoftwareComponent with code " + code);

		List<Deployment> deployments = null;

		try {
			SoftwareComponent softwareComponent = getSoftwareComponentRegistry().findByCode(code);
			if (softwareComponent == null) {
				log.error("Could not find SoftwareComponent with code " + code);
				return Response.status(Status.NOT_FOUND).entity("Could not find resource with code " + code).build();
			}

			deployments = getDeploymentRegistry().findByComponent(softwareComponent);
			List<Deployment> allowed = new LinkedList<Deployment>();
			for (Deployment d : deployments) {
				if (LighthouseAuthorizator.allowAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), formatContext(d.getLocation(), code)))
					allowed.add(d);
			}
			deployments = allowed;

			if (log.isDebugEnabled())
				log.debug("Deployments found for SoftwareComponent with code " + code);
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving all Deployments for SoftwareComponent", exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}
		String s = createXmlListOfEntityReferences(deployments);
		return Response.status(Status.OK).entity(s).build();
	}

	@Override
	@GET
	@Path("/Overdubbed/{id}")
	@Produces("application/xml")
	public Response find(@PathParam("id") String id, @Context UriInfo ui, @Context HttpServletRequest servletRequest) {
		return Response.status(Status.MOVED_PERMANENTLY).build();
	}

	@Override
	@DELETE
	@Path("/Overdubbed/{id}")
	@Produces("text/plain")
	public Response delete(@PathParam("id") String ids, @Context HttpServletRequest servletRequest) {
		return Response.status(Status.MOVED_PERMANENTLY).build();
	}

	@Override
	protected String getContextRole(CtxOp op) {
		switch (op) {
			case FIND:
				return "viewDeployment";
			case PERSIST:
				return "deploySoftwareComponent";
			case UPDATE:
				return "modifyDeployment";
			case DELETE:
				return "deleteDeployment";
			default:
				throw new PersistenceException("unsupported operation in role lookup", null);
		}
	}

	@Override
	protected StringBuilder getContextString() {
		return new StringBuilder("/Deployment/Deployment");
	}

	@Override
	protected StringBuilder getEntityContext(Deployment e) {
		return formatContext(e.getLocation(), e.getDeployedComponent().getCode());
	}
}