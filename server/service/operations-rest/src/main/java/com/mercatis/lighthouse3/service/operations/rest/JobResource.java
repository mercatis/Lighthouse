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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.domainmodel.operations.JobRegistry;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry;
import com.mercatis.lighthouse3.service.commons.rest.CodedDomainModelEntityResource;
import com.mercatis.lighthouse3.service.commons.rest.ResourceEventListener;
import com.mercatis.lighthouse3.service.commons.rest.ResourceEventTopicPublisher;
import com.mercatis.lighthouse3.service.jaas.util.LighthouseAuthorizator;

/**
 * This resource captures jobs in a RESTful way. This is used to build up a CRUD
 * web service for jobs.
 */
@Path("/Job")
public class JobResource extends CodedDomainModelEntityResource<Job> {

	/**
	 * This property refers to the singleton publisher for job events.
	 */
	static private ResourceEventTopicPublisher jobEventPublisher = null;

	@Override
	protected List<ResourceEventListener> returnInitialResourceEventListeners() {
		if (jobEventPublisher == null)
			jobEventPublisher = new ResourceEventTopicPublisher(Job.class, JobRegistry.class, getServiceContainer());

		List<ResourceEventListener> initialListeners = super.returnInitialResourceEventListeners();
		initialListeners.add(jobEventPublisher);

		return initialListeners;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public DomainModelEntityDAO[] getDeserializationDAOs() {
		return new DomainModelEntityDAO[] { getServiceContainer().getDAO(SoftwareComponentRegistry.class),
				getServiceContainer().getDAO(DeploymentRegistry.class), getServiceContainer().getDAO(OperationInstallationRegistry.class) };
	}

	private SoftwareComponentRegistry getSoftwareComponentRegistry() {
		return (SoftwareComponentRegistry) getDeserializationDAOs()[0];
	}

	private DeploymentRegistry getDeploymentRegistry() {
		return (DeploymentRegistry) getDeserializationDAOs()[1];
	}

	private JobRegistry getJobRegistry() {
		return getServiceContainer().getDAO(JobRegistry.class);
	}

	@Override
	public CodedDomainModelEntityDAO<Job> getEntityDAO() {
		return getJobRegistry();
	}

	@GET
	@Path("/Deployment/{location}/{code}")
	@Produces("application/xml")
	public Response findAtDeployment(@PathParam("location") String location, @PathParam("code") String code, @Context HttpServletRequest servletRequest) {
		if (log.isDebugEnabled())
			log.debug("Retrieving Jobs for Deployment of component " + code + " at " + location);

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
			log.error("Could not find Deployment with code" + code + " at " + location);

			return Response.status(Status.NOT_FOUND).entity("Could not find resource with code " + code + " and location " + location).build();
		}
		
		if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), "viewDeployment", formatContext(new StringBuilder("/Deployment/Deployment"), location, code)))
			return Response.status(Status.UNAUTHORIZED).build();

		if (log.isDebugEnabled())
			log.debug("Deployment retrieved: " + deployment.toXml());

		List<Job> jobsAtDeployment = null;

		try {
			jobsAtDeployment = getJobRegistry().findAtDeployment(deployment);
		} catch (PersistenceException persistenceException) {
			log.error("Persistence exception caught while retrieving Jobs for Deployment", persistenceException);

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(persistenceException.toString()).build();
		}

		return Response.status(Status.OK).entity(createXmlListOfEntityReferences(jobsAtDeployment)).build();
	}

	@Override
	protected String getContextRole(com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource.CtxOp op) {
		switch (op) {
			case FIND:
				return null;
			case UPDATE:
				return "modifyJob";
			case DELETE:
				return "uninstallJob";
			case PERSIST:
				return "installJob";
			default:
				throw new PersistenceException("unsupported operation in role lookup", null);
		}
	}

	@Override
	protected StringBuilder getEntityContext(Job e) {
		return getContextString();
	}
	
	@Override
	protected StringBuilder getContextString() {
		return new StringBuilder("/Job/Job");
	}

}
