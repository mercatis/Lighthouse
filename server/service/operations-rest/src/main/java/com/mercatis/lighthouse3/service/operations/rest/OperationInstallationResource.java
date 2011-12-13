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
package com.mercatis.lighthouse3.service.operations.rest;

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
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry;
import com.mercatis.lighthouse3.persistence.operations.hibernate.OperationInstallationRegistryImplementation;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource;
import com.mercatis.lighthouse3.service.commons.rest.ResourceEventListener;
import com.mercatis.lighthouse3.service.jaas.util.LighthouseAuthorizator;
import com.sun.jersey.api.ConflictException;

/**
 * This resource captures operation installations in a RESTful way. This is used
 * to build up a CRUD web service for operation installations.
 */
@Path("/OperationInstallation")
public class OperationInstallationResource extends DomainModelEntityResource<OperationInstallation> {

	@Override
	public DomainModelEntityDAO<OperationInstallation> getEntityDAO() {
		return (OperationInstallationRegistryImplementation) getServiceContainer().getDAO(OperationInstallationRegistry.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public DomainModelEntityDAO[] getDeserializationDAOs() {
		return new DomainModelEntityDAO[] { getServiceContainer().getDAO(DeploymentRegistry.class) };
	}

	public DeploymentRegistry getDeploymentRegistry() {
		return (DeploymentRegistry) getDeserializationDAOs()[0];
	}

	public OperationInstallationRegistry getOperationInstallationRegistry() {
		return (OperationInstallationRegistry) getEntityDAO();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String createXmlListOfEntityReferences(List operationInstallations) {
		StringWriter result = new StringWriter();
		XmlWriter xml = new XmlEncXmlWriter(result);

		try {
			xml.writeEntity("list");
			xml.writeAttribute("xmlns", XmlMuncher.MERCATIS_NS);

			for (OperationInstallation installation : (List<OperationInstallation>) operationInstallations) {
				xml.writeEntity("entry");
				xml.writeEntityWithText("deployedComponentCode", installation.getInstallationLocation().getDeployedComponent().getCode());
				xml.writeEntityWithText("deploymentLocation", installation.getInstallationLocation().getLocation());
				xml.writeEntityWithText("operationCode", installation.getInstalledOperationCode());
				xml.endEntity();
			}

			xml.endEntity();
		} catch (IOException e) {
			throw new ConflictException("Error creating result list");
		}

		return result.toString();
	}

	@Override
	protected OperationInstallation createEntityTemplateFromQueryParameters(OperationInstallation template, MultivaluedMap<String, String> queryParams) {

		if (queryParams.containsKey("deployedComponentCode") && queryParams.containsKey("deploymentLocation")) {
			String deployedComponentCode = queryParams.getFirst("deployedComponentCode");
			String deploymentLocation = queryParams.getFirst("deploymentLocation");

			Deployment installationLocation = null;

			for (Deployment deployment : getDeploymentRegistry().findAtLocation(deploymentLocation)) {
				if (deployment.getDeployedComponent().getCode().equals(deployedComponentCode)) {
					installationLocation = deployment;
					break;
				}
			}

			if (installationLocation != null)
				template.setInstallationLocation(installationLocation);
			else
				throw new ConstraintViolationException("Could not find deployment of SoftwareComponent " + deployedComponentCode + " at " + deploymentLocation,
						null);
		}

		return super.createEntityTemplateFromQueryParameters(template, queryParams);
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
	public Response update(String payload, UriInfo ui, @Context HttpServletRequest servletRequest) {
		if (log.isDebugEnabled())
			log.debug("Updating OperationInstallation using " + payload);

		String deployedComponentCode = XmlMuncher.readValueFromXml(payload, "//:deployedComponentCode");
		String deploymentLocation = XmlMuncher.readValueFromXml(payload, "//:deploymentLocation");
		String installedOperationCode = XmlMuncher.readValueFromXml(payload, "//:installedOperationCode");

		if ((deployedComponentCode == null) || (deployedComponentCode == null)) {
			log.error("Cannot parse payload: deployedComponentCode and deploymentLocation element missing for OperationInstallation");

			return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).entity("Cannot parse payload").build();
		}

		Deployment installationLocation = null;

		try {
			for (Deployment deployment : getDeploymentRegistry().findAtLocation(deploymentLocation)) {
				if (deployment.getDeployedComponent().getCode().equals(deployedComponentCode)) {
					installationLocation = deployment;
					break;
				}
			}

			if (installationLocation == null) {
				log.error("Could not find Deployment of SoftwareComponent " + deployedComponentCode + " at " + deploymentLocation, null);

				return Response.status(Status.NOT_FOUND)
						.entity("Could not find Deployment of SoftwareComponent " + deployedComponentCode + " at " + deploymentLocation).build();
			}

		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving location of OperationInstallation");

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		OperationInstallation operationInstallationInDb = null;

		try {
			operationInstallationInDb = getOperationInstallationRegistry().findByDeploymentAndOperation(installationLocation, installedOperationCode);

			if (operationInstallationInDb == null) {
				log.error("Could not find installation of Operation " + installedOperationCode + " at Deployment of SoftwareComponent " + deployedComponentCode
						+ " at " + deploymentLocation, null);
				return Response
						.status(Status.NOT_FOUND)
						.entity("Could not find installation of Operation " + installedOperationCode + " at Deployment of SoftwareComponent "
								+ deployedComponentCode + " at " + deploymentLocation).build();
			}
	    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.UPDATE), getEntityContext(operationInstallationInDb)))
	    		return Response.status(Status.UNAUTHORIZED).build();
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving OperationInstallation");

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		try {
			operationInstallationInDb.fromXml(payload, getDeserializationDAOs());
		} catch (XMLSerializationException exception) {
			log.error("XML serialization exception caught while updating OperationInstallation", exception);

			return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).entity(exception.toString()).build();
		}

		try {
			getOperationInstallationRegistry().update(operationInstallationInDb);

			if (log.isDebugEnabled())
				log.debug("OperationInstallation updated.");
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while updating OperationInstallation", exception);

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		notifyResourceEventListeners(ResourceEventListener.ENTITY_UPDATED, operationInstallationInDb);

		return Response.status(Status.OK).build();
	}

	/**
	 * This method implements the HTTP <code>DELETE</code> method of the
	 * operation installation resource. It facilitates the deletion of operation
	 * installations. It expects that the operation installation to be deleted
	 * is addressed via the following URL pattern
	 * <code>/OperationInstallation/{deploymentLocation}/{deployedComponentCode}/{installedOperationCode}</code>
	 * . <code>{deploymentLocation}</code> is the location and
	 * <code>{deployedComponentCode}</code> is the code of the deployed software
	 * component of the deployment serving as the installation location.
	 * <code>{installedOperationCode}</code> is the code of the installed
	 * operation.
	 * 
	 * @param deploymentLocation
	 *            receives the location of the deployment serving as the
	 *            installation location of the operation installation to be
	 *            deleted.
	 * @param deployedComponentCode
	 *            receives the deployed component code of the deployment serving
	 *            as the installation location of the operation installation to
	 *            be deleted.
	 * @param installedOperationCode
	 *            received the code of the operation referred to by the
	 *            operation installation to be deleted.
	 * @return a 500 code in case of an internal problem, a 404 code if the
	 *         operation installation or deployment could not be found, a 200
	 *         code otherwise.
	 */
	@DELETE
	@Path("{deploymentLocation}/{deployedComponentCode}/{installedOperationCode}")
	@Produces("text/plain")
	public Response delete(@PathParam("deploymentLocation") String deploymentLocation, @PathParam("deployedComponentCode") String deployedComponentCode,
			@PathParam("installedOperationCode") String installedOperationCode, @Context HttpServletRequest servletRequest) {

		if (log.isDebugEnabled())
			log.debug("Deleting OperationInstallation of Operation " + installedOperationCode + " at Deployment of component " + deployedComponentCode
					+ " at location " + deploymentLocation);

		Deployment installationLocation = null;

		try {
			for (Deployment deployment : getDeploymentRegistry().findAtLocation(deploymentLocation)) {
				if (deployment.getDeployedComponent().getCode().equals(deployedComponentCode)) {
					installationLocation = deployment;
					break;
				}
			}

			if (installationLocation == null) {
				log.error("Could not find Deployment of SoftwareComponent " + deployedComponentCode + " at " + deploymentLocation, null);
				return Response.status(Status.NOT_FOUND).entity("Could not find Deployment of SoftwareComponent " + deployedComponentCode + " at " + deploymentLocation).build();
			}

		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving location of OperationInstallation");
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		OperationInstallation operationInstallationInDb = null;

		try {
			operationInstallationInDb = getOperationInstallationRegistry().findByDeploymentAndOperation(installationLocation, installedOperationCode);

			if (operationInstallationInDb == null) {
				log.error("Entity not persistent: Could not find installation of Operation " + installedOperationCode + " at Deployment of SoftwareComponent "
						+ deployedComponentCode + " at " + deploymentLocation, null);
				return Response.status(Status.NOT_FOUND).entity("Entity not persistent: Could not find installation of Operation " + installedOperationCode
								+ " at Deployment of SoftwareComponent " + deployedComponentCode + " at " + deploymentLocation).build();
			}
	    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.DELETE), getEntityContext(operationInstallationInDb)))
	    		return Response.status(Status.UNAUTHORIZED).build();
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving OperationInstallation");
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		try {
			getOperationInstallationRegistry().delete(operationInstallationInDb);
			if (log.isDebugEnabled())
				log.debug("OperationInstallation deleted.");
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while deleting OperationInstallation", exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}
		notifyResourceEventListeners(ResourceEventListener.ENTITY_DELETED, operationInstallationInDb);
		return Response.status(Status.OK).build();
	}

	/**
	 * This method implements the HTTP <code>GET</code> method of the operation
	 * installation resource. It facilitates the retrieval of operation
	 * installations. It expects that the operation installation to be retrieved
	 * is addressed via the following URL pattern
	 * <code>/OperationInstallation/{deploymentLocation}/{deployedComponentCode}/{installedOperationCode}</code>
	 * . <code>{deploymentLocation}</code> is the location and
	 * <code>{deployedComponentCode}</code> is the code of the deployed software
	 * component of the deployment serving as the installation location.
	 * <code>{installedOperationCode}</code> is the code of the installed
	 * operation.
	 * 
	 * @param deploymentLocation
	 *            receives the location of the deployment serving as the
	 *            installation location of the operation installation to be
	 *            retrieved.
	 * @param deployedComponentCode
	 *            receives the deployed component code of the deployment serving
	 *            as the installation location of the operation installation to
	 *            be retrieved.
	 * @param installedOperationCode
	 *            received the code of the operation referred to by the
	 *            operation installation to be retrieved.
	 * @return a 500 code in case of an internal problem, a 404 code if the
	 *         operation installation or deployment could not be found, a 200
	 *         code otherwise.
	 */
	@GET
	@Path("{deploymentLocation}/{deployedComponentCode}/{installedOperationCode}")
	@Produces("application/xml")
	public Response findByDeploymentAndLocation(@PathParam("deploymentLocation") String deploymentLocation,
			@PathParam("deployedComponentCode") String deployedComponentCode, @PathParam("installedOperationCode") String installedOperationCode, @Context HttpServletRequest servletRequest) {

		if (log.isDebugEnabled())
			log.debug("Retrieving OperationInstallation of Operation " + installedOperationCode + " at Deployment of component " + deployedComponentCode
					+ " at location " + deploymentLocation);

		Deployment installationLocation = null;

		try {
			for (Deployment deployment : getDeploymentRegistry().findAtLocation(deploymentLocation)) {
				if (deployment.getDeployedComponent().getCode().equals(deployedComponentCode)) {
					installationLocation = deployment;
					break;
				}
			}

			if (installationLocation == null) {
				log.error("Could not find deployment of SoftwareComponent " + deployedComponentCode + " at " + deploymentLocation, null);

				return Response.status(Status.NOT_FOUND)
						.entity("Could not find deployment of SoftwareComponent " + deployedComponentCode + " at " + deploymentLocation).build();
			}

		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving location of OperationInstallation");

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		OperationInstallation operationInstallationInDb = null;

		try {
			operationInstallationInDb = getOperationInstallationRegistry().findByDeploymentAndOperation(installationLocation, installedOperationCode);

			if (operationInstallationInDb == null) {
				log.error("Could not find installation of Operation " + installedOperationCode + " at Deployment of SoftwareComponent " + deployedComponentCode
						+ " at " + deploymentLocation, null);
				return Response
						.status(Status.NOT_FOUND)
						.entity("Could not find installation of Operation " + installedOperationCode + " at Deployment of SoftwareComponent "
								+ deployedComponentCode + " at " + deploymentLocation).build();
			}
	    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), getEntityContext(operationInstallationInDb)))
	    		return Response.status(Status.UNAUTHORIZED).build();

		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving OperationInstallation");

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		return Response.status(Status.OK).entity(operationInstallationInDb.toXml()).build();
	}

	/**
	 * This method implements the HTTP <code>GET</code> method of the operation
	 * installation resource. It facilitates the retrieval of all operation
	 * installations at a given deployment. It expects that the operation
	 * installation to be retrieved is addressed via the following URL pattern
	 * 
	 * <code>/OperationInstallation/{deploymentLocation}/{deployedComponentCode}</code>
	 * . <code>{deploymentLocation}</code> is the location and
	 * <code>{deployedComponentCode}</code> is the code of the deployed software
	 * component of the deployment serving as the installation location.
	 * 
	 * @param deploymentLocation
	 *            receives the location of the deployment serving as the
	 *            installation location of the operation installation to be
	 *            retrieved.
	 * @param deployedComponentCode
	 *            receives the deployed component code of the deployment serving
	 *            as the installation location of the operation installation to
	 *            be retrieved.
	 * @return a 500 code in case of an internal problem, a 404 code if the
	 *         operation installation or deployment could not be found, a 200
	 *         code otherwise.
	 */
	@GET
	@Path("{deploymentLocation}/{deployedComponentCode}")
	@Produces("application/xml")
	public Response findAtDeployment(@PathParam("deploymentLocation") String deploymentLocation,
			@PathParam("deployedComponentCode") String deployedComponentCode, @Context HttpServletRequest servletRequest) {

		if (log.isDebugEnabled())
			log.debug("Retrieving OperationInstallations at Deployment of component " + deployedComponentCode + " location " + deploymentLocation);

		Deployment installationLocation = null;

		try {
			for (Deployment deployment : getDeploymentRegistry().findAtLocation(deploymentLocation)) {
				if (deployment.getDeployedComponent().getCode().equals(deployedComponentCode)) {
					installationLocation = deployment;
					break;
				}
			}

			if (installationLocation == null) {
				log.error("Could not find deployment of SoftwareComponent " + deployedComponentCode + " at " + deploymentLocation, null);

				return Response.status(Status.NOT_FOUND)
						.entity("Could not find deployment of SoftwareComponent " + deployedComponentCode + " at " + deploymentLocation).build();
			}

		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving location of OperationInstallation");

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		List<OperationInstallation> operationInstallationsAtDeployment = new LinkedList<OperationInstallation>();

		try {
			List<OperationInstallation> tmp = getOperationInstallationRegistry().findAtDeployment(installationLocation);
			
			for (OperationInstallation oi : tmp)
		    	if (LighthouseAuthorizator.allowAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), getEntityContext(oi)))
		    		operationInstallationsAtDeployment.add(oi);
			
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving OperationInstallations at Deployment");

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		return Response.status(Status.OK).entity(createXmlListOfEntityReferences(operationInstallationsAtDeployment)).build();
	}

	/**
	 * This method implements the HTTP <code>GET</code> method of the operation
	 * installation resource. It facilitates the retrieval of all operation
	 * installations of an operation with a given code. It expects that the
	 * operation installation to be retrieved is addressed via the following URL
	 * pattern <code>/OperationInstallation/{installedOperationCode}</code> .
	 * <code>{installedOperationCode}</code> is the code of the installed
	 * operation.
	 * 
	 * @param installedOperationCode
	 *            received the code of the operation referred to by the
	 *            operation installation to be retrieved.
	 * @return a 500 code in case of an internal problem, a 404 code if the
	 *         operation installation or deployment could not be found, a 200
	 *         code otherwise.
	 */
	@GET
	@Path("{installedOperationCode}")
	@Produces("application/xml")
	public Response findForOperation(@PathParam("installedOperationCode") String installedOperationCode, @Context HttpServletRequest servletRequest) {
		if (log.isDebugEnabled())
			log.debug("Retrieving OperationInstallation of Operation " + installedOperationCode);

		List<OperationInstallation> installationsOfOperation = new LinkedList<OperationInstallation>();

		try {
			List<OperationInstallation> tmp = getOperationInstallationRegistry().findForOperation(installedOperationCode);
			
			for (OperationInstallation oi : tmp)
		    	if (LighthouseAuthorizator.allowAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), getEntityContext(oi)))
		    		installationsOfOperation.add(oi);

		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving OperationInstallations of Operation");

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		return Response.status(Status.OK).entity(createXmlListOfEntityReferences(installationsOfOperation)).build();
	}

	@Override
	protected String getContextRole(com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource.CtxOp op) {
		return null;
	}

	@Override
	protected StringBuilder getContextString() {
		return new StringBuilder("/");
	}

	@Override
	protected StringBuilder getEntityContext(OperationInstallation e) {
		return getContextString();
	}
}
