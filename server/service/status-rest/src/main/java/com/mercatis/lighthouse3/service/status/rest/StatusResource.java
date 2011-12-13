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
package com.mercatis.lighthouse3.service.status.rest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.ConstraintViolationException;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusChangeNotificationChannel;
import com.mercatis.lighthouse3.domainmodel.status.StatusRegistry;
import com.mercatis.lighthouse3.service.commons.rest.CodedDomainModelEntityResource;
import com.mercatis.lighthouse3.service.commons.rest.ResourceEventListener;
import com.mercatis.lighthouse3.service.commons.rest.ResourceEventTopicPublisher;
import com.mercatis.lighthouse3.service.jaas.util.LighthouseAuthorizator;

/**
 * This class implements the REST resource capturing status in a RESTful way by
 * means of the status service.
 * 
 * The service publishes updates to status on a JMS topic. Thus, the resource
 * reacts to the following servlet init parameters:
 * 
 * <ul>
 * <li>
 * <code>com.mercatis.lighthouse3.service.status.jms.JmsProvider</code> for
 * passing the class name of the implementation of the <code>JmsProvider</code>
 * interface for the JMS broker to use.
 * <li>
 * <code>com.mercatis.lighthouse3.service.status.jms.JmsConfigResource</code>
 * for passing the resource name of the configuration file with the JMS
 * settings.
 * <li>
 * <code>com.mercatis.lighthouse3.service.status.jms.JmsConfigFileLocation</code>
 * for passing the path to the configuration file with the JMS settings.
 * </ul>
 */
@Path("/Status")
public class StatusResource extends CodedDomainModelEntityResource<Status> {

	/**
	 * This property refers to the singleton publisher for status events.
	 */
	static private ResourceEventTopicPublisher statusEventPublisher = null;

	@Override
	protected List<ResourceEventListener> returnInitialResourceEventListeners() {
		if (statusEventPublisher == null)
			statusEventPublisher = new ResourceEventTopicPublisher(Status.class, StatusRegistry.class, getServiceContainer());

		List<ResourceEventListener> initialListeners = super.returnInitialResourceEventListeners();
		initialListeners.add(statusEventPublisher);

		return initialListeners;
	}

	@Override
	public CodedDomainModelEntityDAO<Status> getEntityDAO() {
		return getServiceContainer().getDAO(StatusRegistry.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public DomainModelEntityDAO[] getDeserializationDAOs() {
		return new DomainModelEntityDAO[] { getServiceContainer().getDAO(EnvironmentRegistry.class),
				getServiceContainer().getDAO(ProcessTaskRegistry.class), getServiceContainer().getDAO(DeploymentRegistry.class),
				getServiceContainer().getDAO(SoftwareComponentRegistry.class), getServiceContainer().getDAO(EventRegistry.class) };
	}

	private EnvironmentRegistry getEnvironmentRegistry() {
		return (EnvironmentRegistry) getDeserializationDAOs()[0];
	}

	private ProcessTaskRegistry getProcessTaskRegistry() {
		return (ProcessTaskRegistry) getDeserializationDAOs()[1];
	}

	private SoftwareComponentRegistry getSoftwareComponentRegistry() {
		return (SoftwareComponentRegistry) getDeserializationDAOs()[3];
	}

	private DeploymentRegistry getDeploymentRegistry() {
		return (DeploymentRegistry) getDeserializationDAOs()[2];
	}

	private StatusRegistry getStatusRegistry() {
		return (StatusRegistry) getEntityDAO();
	}

	/**
	 * This method implements the HTTP <code>GET</code> method of the status
	 * resource. It facilitates retrieval by deployment. It expects that the
	 * deployment for which the attached status are to be retrieved is addressed
	 * via the following URL pattern
	 * <code>/Status/Deployment/{location}/{code}</code> where
	 * <code>{location}</code> is the deployment location <code>{code}</code> is
	 * the code of the deployed software component.
	 * 
	 * @param location
	 *            receives the location of the deployment to be retrieved.
	 * @param code
	 *            receives the code of the deployed software component of the
	 *            deployment to be retrieved.
	 * @return a 404 code if the deployment or deployed software component could
	 *         not be found, 500 code in case of internal errors, a 200 code
	 *         otherwise. The body of the response contains the XML
	 *         representation of the retrieved status.
	 */
	@GET
	@Path("/Deployment/{location}/{code}")
	@Produces("application/xml")
	public Response findForDeployment(@PathParam("location") String location, @PathParam("code") String code, @Context HttpServletRequest servletRequest) {
    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), formatContext(new StringBuilder("/Deployment/Deployment"), location, code)))
    		return Response.status(javax.ws.rs.core.Response.Status.UNAUTHORIZED).build();
		if (log.isDebugEnabled())
			log.debug("Retrieving attached Status for Deployment of Component " + code + " at " + location);

		List<Status> results = null;
		Deployment deployment = null;

		try {
			SoftwareComponent softwareComponent = getSoftwareComponentRegistry().findByCode(code);
			if (softwareComponent == null) {
				log.error("Could not find SoftwareComponent with code" + code);
				return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity("Could not find resource with code " + code).build();
			}

			deployment = getDeploymentRegistry().findByComponentAndLocation(softwareComponent, location);

			if (deployment == null) {
				log.error("Could not find Deployment with code" + code + " at " + location);
				return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND)
						.entity("Could not find resource with code " + code + " and location " + location).build();
			}
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving Deployment", exception);
			return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		try {
			List<Status> tmp = getStatusRegistry().getStatusForCarrier(deployment);
			results = new LinkedList<Status>();
			for (Status s : tmp) {
				if (LighthouseAuthorizator.allowAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), getEntityContext(s)))
		    		results.add(s);
			}

		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving Status for Deployment", exception);
			return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		if (log.isDebugEnabled())
			log.debug("Attached Status for Deployment retrieved");

		String xml = createXmlListOfEntityReferences(results);
		return Response.status(javax.ws.rs.core.Response.Status.OK).entity(xml).build();
	}

	/**
	 * This method implements the HTTP <code>GET</code> method of the status
	 * resource. It facilitates retrieval by environment. It expects that the
	 * environment for which the attached status are to be retrieved is
	 * addressed via the following URL pattern
	 * <code>/Status/Environment/{code}</code> where <code>{code}</code> is the
	 * code of the environment of interest.
	 * 
	 * @param code
	 *            receives the code of the environment to be retrieved.
	 * @return a 404 code if the environment could not be found,500 code in case
	 *         of internal errors, a 200 code otherwise. The body of the
	 *         response contains the XML representation of the retrieved status.
	 */
	@GET
	@Path("/Environment/{code}")
	@Produces("application/xml")
	public Response findForEnvironment(@PathParam("code") String code, @Context HttpServletRequest servletRequest) {
    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), formatContext(new StringBuilder("/Environment/Environment"), code, null)))
    		return Response.status(javax.ws.rs.core.Response.Status.UNAUTHORIZED).build();
		if (log.isDebugEnabled())
			log.debug("Retrieving attached Status for Environment with code " + code);

		List<Status> results = null;
		Environment environment = null;

		try {
			environment = getEnvironmentRegistry().findByCode(code);

			if (environment == null) {
				log.error("Could not find Environment with code " + code);

				return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity("Could not find resource with code " + code).build();
			}
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving Environment", exception);

			return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		try {
			List<Status> tmp = getStatusRegistry().getStatusForCarrier(environment);
			results = new LinkedList<Status>();
			for (Status s : tmp) {
				if (LighthouseAuthorizator.allowAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), getEntityContext(s)))
		    		results.add(s);
			}
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving Status for Environment", exception);

			return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		if (log.isDebugEnabled())
			log.debug("Attached Status for Environment retrieved");

		return Response.status(javax.ws.rs.core.Response.Status.OK).entity(createXmlListOfEntityReferences(results)).build();
	}

	/**
	 * This method implements the HTTP <code>GET</code> method of the status
	 * resource. It facilitates retrieval by process/tasks. It expects that the
	 * process/task for which the attached status are to be retrieved is
	 * addressed via the following URL pattern
	 * <code>/Status/ProcessTask/{code}</code> where <code>{code}</code> is the
	 * code of the process/task of interest.
	 * 
	 * @param code
	 *            receives the code of the process/task to be retrieved.
	 * @return a 404 code if the process/task could not be found, a 500 code in
	 *         case of internal errors, a 200 code otherwise. The body of the
	 *         response contains the XML representation of the retrieved status.
	 */
	@GET
	@Path("/ProcessTask/{code}")
	@Produces("application/xml")
	public Response findForProcessTask(@PathParam("code") String code, @Context HttpServletRequest servletRequest) {
    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), formatContext(new StringBuilder("/ProcessTask"), code, null)))
    		return Response.status(javax.ws.rs.core.Response.Status.UNAUTHORIZED).build();
		if (log.isDebugEnabled())
			log.debug("Retrieving attached Status for ProcessTask with code " + code);

		List<Status> results = null;
		ProcessTask processTask = null;

		try {
			processTask = getProcessTaskRegistry().findByCode(code);

			if (processTask == null) {
				log.error("Could not find ProcessTask with code " + code);
				return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity("Could not find resource with code " + code).build();
			}
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving ProcessTask", exception);
			return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		try {
			List<Status> tmp = getStatusRegistry().getStatusForCarrier(processTask);
			results = new LinkedList<Status>();
			for (Status s : tmp) {
				if (LighthouseAuthorizator.allowAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), getEntityContext(s)))
		    		results.add(s);
			}

		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving Status for ProcessTask", exception);
			return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		if (log.isDebugEnabled())
			log.debug("Attached Status for ProcessTask retrieved");
		return Response.status(javax.ws.rs.core.Response.Status.OK).entity(createXmlListOfEntityReferences(results)).build();
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
		int pageSize = -1;
		int pageNo = -1;

		if (ui.getQueryParameters().containsKey("pageSize"))
			pageSize = Integer.parseInt(ui.getQueryParameters(true).get("pageSize").get(0));

		if (ui.getQueryParameters().containsKey("pageNo"))
			pageNo = Integer.parseInt(ui.getQueryParameters(true).get("pageNo").get(0));

		boolean detailed = ui.getQueryParameters(true).containsKey("detailed") ? Boolean.parseBoolean(ui.getQueryParameters(true).get("detailed").iterator()
				.next()) : false;

		if (log.isDebugEnabled()) {
			log.debug("Retrieving " + getEntityDAO().getManagedType().getSimpleName() + " by template " + ui.getRequestUri()
					+ " and history pagination, size" + pageSize + " page " + pageNo);
		} else {
			log.debug("Retrieving " + getEntityDAO().getManagedType().getSimpleName() + " by template " + ui.getRequestUri());
		}

		MultivaluedMap<String, String> queryParams = ui.getQueryParameters(true);
		queryParams.remove("detailed");
		queryParams.remove("pageSize");
		queryParams.remove("pageNo");
		Status template = null;

		try {
			template = createEntityTemplateFromQueryParameters(new Status(), queryParams);
		} catch (ConstraintViolationException exception) {
			log.error("Constraint violation by " + getEntityDAO().getManagedType().getSimpleName() + " template", exception);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		List<Status> matches = null;

		try {
			matches = getEntityDAO().findByTemplate(template);
			if (pageSize != -1 && pageNo != -1) {
				List<Status> tmp = new ArrayList<Status>(matches.size());
				for (Status match : matches) {
					tmp.add(match.createPaginatedStatus(pageSize, pageNo));
				}
				matches = new LinkedList<Status>();
				for (Status match : tmp) {
					if (LighthouseAuthorizator.allowAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), getEntityContext(match)))
						matches.add(match);
				}
			}

			if (log.isDebugEnabled())
				log.debug("Matching " + getEntityDAO().getManagedType().getSimpleName() + "s found for template");
		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving " + getEntityDAO().getManagedType().getSimpleName(), exception);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		String xml;
		if (detailed)
			xml = createDetailedXmlListOfEntities(matches);
		else
			xml = createXmlListOfEntityReferences(matches);

		return Response.status(Response.Status.OK).entity(xml).build();
	}

	/**
	 * This method implements the HTTP <code>GET</code> method of the present
	 * status resource. It facilitates retrieval by code of the entities
	 * represented by the resource. It expects that the status to be retrieved
	 * is addressed via the following URL pattern <code>/Status/{code}</code>
	 * where <code>{code}</code> is the code of the entity.
	 * 
	 * It is possible to paginate the status change history of the retrieved
	 * status by passing both <code>pageNo</code> and <code>pageSize</code>
	 * query parameters.
	 * 
	 * @param code
	 *            receives the code of the entity to be retrieved.
	 * @param pageSize
	 *            the granularity of pagination
	 * @param pageNo
	 *            the page to return, counting from 1
	 * @return a 404 code if the entity could not be found, 500 code in case of
	 *         internal errors, a 200 code otherwise. The body of the response
	 *         contains the XML representation of the retrieved entity.
	 */
	@Override
	public Response find(String code, UriInfo ui, HttpServletRequest servletRequest) {
		int pageSize = -1;
		int pageNo = -1;

		if (ui.getQueryParameters().containsKey("pageSize"))
			pageSize = Integer.parseInt(ui.getQueryParameters(true).get("pageSize").get(0));

		if (ui.getQueryParameters().containsKey("pageNo"))
			pageNo = Integer.parseInt(ui.getQueryParameters(true).get("pageNo").get(0));

		if (log.isDebugEnabled())
			if ((pageNo != -1) && (pageSize != -1))
				log.debug("Retrieving Status with code " + code + " and history pagination, size" + pageSize + " page " + pageNo);
			else
				log.debug("Retrieving Status with code " + code);

		Status status = null;

		try {
			if ((pageNo != -1) && (pageSize != -1))
				status = getStatusRegistry().findByCode(code, pageSize, pageNo);
			else
				status = getStatusRegistry().findByCode(code);

			if (status != null) {
				if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), getEntityContext(status)))
		    		return Response.status(javax.ws.rs.core.Response.Status.UNAUTHORIZED).build();
				
				if (log.isDebugEnabled())
					log.debug("Found  " + getEntityDAO().getManagedType().getSimpleName() + ": " + status.toXml());
			}
			

		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving Status", exception);

			return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		if (status == null) {
			if (log.isDebugEnabled())
				log.debug("Could not find " + getEntityDAO().getManagedType().getSimpleName() + " with code " + code);

			return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity("Could not find resource with code " + code).build();
		}

		String xml = status.toXml();
		return Response.status(javax.ws.rs.core.Response.Status.OK).entity(xml).build();
	}

	/**
	 * This method implements the HTTP <code>PUT</code> method of the present
	 * Status resource. It facilitates updates of the entities represented by
	 * the resource. It expects that the XML representation of the entity to be
	 * created is passed as the post request payload to the following URL
	 * pattern <code>/{Entity}</code> where <code>{Entity}</code> is the class
	 * name of entity.
	 * 
	 * Note that additional query parameters <code>clearer</code> and
	 * <code>reason</code> may be passed in order to additionally perform a
	 * manual status clearance.
	 * 
	 * @param payload
	 *            receives the XML representation of the entity to be updated.
	 * @return a 415 code if the payload could not be parsed, a 500 code if the
	 *         entity could not be updated, a 404 code if the entity to be
	 *         updated could not be found, a 200 code otherwise.
	 */
	@Override
	public Response update(String payload, UriInfo ui, HttpServletRequest servletRequest) {
		String code = XmlMuncher.readValueFromXml(payload, "/*/:code");

		Response response = null;
		String clearer = null;
		String reason = null;

		if (ui.getQueryParameters(true).containsKey("clearer"))
			clearer = ui.getQueryParameters(true).get("clearer").get(0);

		if (ui.getQueryParameters(true).containsKey("reason"))
			reason = ui.getQueryParameters(true).get("reason").get(0);

		if ((code != null) && ((clearer != null) || (reason != null))) {
			Status s = getStatusRegistry().findByCode(code);
			if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.UPDATE), getEntityContext(s)))
	    		return Response.status(javax.ws.rs.core.Response.Status.UNAUTHORIZED).build();
			
			getStatusRegistry().clearStatusManually(code, clearer, reason);
			getEntityDAO().getUnitOfWork().commit();
		}
		
		try {
			response = super.update(payload, ui, servletRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}
	
	protected void makeEntityWritable(Status e) {
		super.makeEntityWritable(e);
		
		Set<StatusChangeNotificationChannel<?, ?>> channels = e.getChangeNotificationChannels();
		for (StatusChangeNotificationChannel<?, ?> channel : channels) {
			getEntityDAO().getUnitOfWork().setReadOnly(channel, false);
		}
	}

	@Override
	protected String getContextRole(com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource.CtxOp op) {
		switch (op) {
			case FIND:
				return "viewStatus";
			case PERSIST:
				return "createStatus";
			case UPDATE:
				return "modifyStatus";
			case DELETE:
				return "deleteStatus";
			default:
				throw new PersistenceException("unsupported operation in role lookup", null);
		}
	}

	@Override
	protected StringBuilder getEntityContext(Status s) {
		StatusCarrier sc = s.getContext();
		if (sc instanceof Deployment) {
			Deployment d = (Deployment) sc;
			return formatContext(new StringBuilder("/Deployment/Deployment"), d.getLocation(), d.getDeployedComponent().getCode());
		}
		if (sc instanceof Environment) {
			Environment e = (Environment) sc;
			return formatContext(new StringBuilder("/Environment/Environment"), e.getCode(), null);
		}
		if (sc instanceof ProcessTask) {
			ProcessTask pt = (ProcessTask) sc;
			return formatContext(new StringBuilder("/ProcessTask/ProcessTask"), pt.getCode(), null);
		}
		
		return new StringBuilder("/");
	}
	
	@Override
	protected StringBuilder getContextString() {
		throw new PersistenceException("internal error, getContextString should not get used in this context", null);
	}
}
