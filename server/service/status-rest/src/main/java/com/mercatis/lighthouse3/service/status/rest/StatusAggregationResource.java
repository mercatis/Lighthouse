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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.generationjava.io.xml.XmlEncXmlWriter;
import com.generationjava.io.xml.XmlWriter;
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
import com.mercatis.lighthouse3.domainmodel.status.StatusHistogram;
import com.mercatis.lighthouse3.domainmodel.status.StatusRegistry;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityRestServiceContainer;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityRestServiceContainer.CreatingServiceContainer;
import com.mercatis.lighthouse3.service.jaas.util.LighthouseAuthorizator;

/**
 * This resource represents aggregation functionality on status.
 */
@Path("/Status/Aggregation")
public class StatusAggregationResource {

    /**
     * This property keeps a logger.
     */
    protected Logger log = Logger.getLogger(this.getClass());
    /**
     * Keeps a reference to service container that created the resource.
     */
    @CreatingServiceContainer
    private DomainModelEntityRestServiceContainer serviceContainer = null;

    /**
     * This method implements the HTTP <code>GET</code> method of the status
     * aggregation resource. It facilitates the aggregation of status attached
     * to all entities for the given className. It expects that the className for which the
     * aggregation is to be performed via the following URL pattern
     * <code>/Status/Aggregation/{carrierClass}</code> where
     * <code>{carrierClass}</code> is StatusCarrier.class.getSimpleName()
     *
     * @param carrierClassName
     * @return a 500 code in case of internal errors, a 200 code otherwise. The body of the
     *         response contains the XML representation of a StatusAggregations mapped by the carriers IDs.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@GET
    @Path("/{carrierClass}")
    @Produces("application/xml")
    public Response getAggregatedCurrentStatusForCarrierClass(@PathParam("carrierClass") String carrierClassName, @Context UriInfo ui, @Context HttpServletRequest servletRequest) {
    	if (LighthouseAuthorizator.denyAccess(serviceContainer, servletRequest.getRemoteUser(), null, "/"))
    		return Response.status(Status.UNAUTHORIZED).build();
    	
        if (log.isDebugEnabled()) {
            log.debug("Aggregating for carrierClass " + carrierClassName);
        }
        try {
            Class carrierClass = Class.forName("com.mercatis.lighthouse3.domainmodel.environment." + carrierClassName);
            Map<String, StatusHistogram> result;
            if (ui.getQueryParameters().containsKey("withDeployments")) {
                boolean withDeployments = ui.getQueryParameters().getFirst("withDeployments").equals("true");
                result = this.serviceContainer.getDAO(StatusRegistry.class).getAggregatedCurrentStatusesForCarrierClass(carrierClass, withDeployments);
            } else {
                result = this.serviceContainer.getDAO(StatusRegistry.class).getAggregatedCurrentStatusesForCarrierClass(carrierClass);
            }

            return Response.status(javax.ws.rs.core.Response.Status.OK).entity(getXmlFromMap(carrierClass, result)).build();
        } catch (ClassNotFoundException exception) {
            log.error("ClassNotFound exception caught while retrieving Class " + carrierClassName, exception);
            return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        } catch (Exception exception) {
            log.error("Persistence exception caught while aggregating for " + carrierClassName, exception);
            return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }
    }

    @SuppressWarnings("rawtypes")
	private String getXmlFromMap(Class carrierClass, Map<String, StatusHistogram> map) throws IOException {
        StringWriter result = new StringWriter();
        XmlWriter xml = new XmlEncXmlWriter(result);
        xml.writeEntity("map");
        for (Entry<String, StatusHistogram> entry : map.entrySet()) {
            xml.writeEntity(carrierClass.getSimpleName());
            xml.writeAttribute("code", entry.getKey());
            entry.getValue().toXml(xml);
            xml.endEntity();
        }
        xml.endEntity();
        return result.toString();
    }

    /**
     * This method implements the HTTP <code>GET</code> method of the status
     * aggregation resource. It facilitates the aggregation of status attached
     * to process/tasks. It expects that the process/task for which the
     * aggregation is to be performed via the following URL pattern
     * <code>/Status/Aggregation/ProcessTask/{code}</code> where
     * <code>{code}</code> is the code of the process/task of interest.
     *
     * @param code
     *            receives the code of the process/task to be retrieved.
     * @return a 404 code if the process/task could not be found, a 500 code in
     *         case of internal errors, a 200 code otherwise. The body of the
     *         response contains the XML representation of the aggregated
     *         status.
     */
    @GET
    @Path("/ProcessTask/{code}")
    @Produces("application/xml")
    public Response getAggregatedCurrentStatusForProcessTask(@PathParam("code") String code, @Context UriInfo ui, @Context HttpServletRequest servletRequest) {
    	if (LighthouseAuthorizator.denyAccess(serviceContainer, servletRequest.getRemoteUser(), null, "/"))
    		return Response.status(Status.UNAUTHORIZED).build();
        if (log.isDebugEnabled()) {
            log.debug("Aggregating status for ProcessTask with code " + code);
        }

        ProcessTask processTask = null;
        StatusHistogram aggregatedStatus = null;

        try {
            processTask = this.serviceContainer.getDAO(ProcessTaskRegistry.class).findByCode(code);

            if (processTask == null) {
                log.error("Could not find ProcessTask with code " + code);

                return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity(
                        "Could not find resource with code " + code).build();
            }
        } catch (PersistenceException exception) {
            log.error("Persistence exception caught while retrieving ProcessTask", exception);

            return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }

        try {
            aggregatedStatus = getAggregatedCurrentStatusForStatusCarrier(processTask, ui);
        } catch (PersistenceException exception) {
            log.error("Persistence exception caught while aggregating Status for ProcessTask", exception);

            return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }

        if (log.isDebugEnabled()) {
            log.debug("Status for ProcessTask aggregated");
        }

        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(aggregatedStatus.toXml()).build();
    }

    /**
     * This method implements the HTTP <code>GET</code> method of the status
     * aggregation resource. It facilitates the aggregation of status attached
     * to environments. It expects that the environment for which the
     * aggregation is to be performed via the following URL pattern
     * <code>/Status/Aggregation/Environment/{code}</code> where
     * <code>{code}</code> is the code of the environment of interest.
     *
     * @param code
     *            receives the code of the environment to be retrieved.
     * @return a 404 code if the environment could not be found, a 500 code in
     *         case of internal errors, a 200 code otherwise. The body of the
     *         response contains the XML representation of the aggregated
     *         status.
     */
    @GET
    @Path("/Environment/{code}")
    @Produces("application/xml")
    public Response getAggregatedCurrentStatusForEnvironment(@PathParam("code") String code, @Context UriInfo ui, @Context HttpServletRequest servletRequest) {
    	if (LighthouseAuthorizator.denyAccess(serviceContainer, servletRequest.getRemoteUser(), null, "/"))
    		return Response.status(Status.UNAUTHORIZED).build();
        if (log.isDebugEnabled()) {
            log.debug("Aggregating status for Environment with code " + code);
        }

        Environment environment = null;
        StatusHistogram aggregatedStatus = null;

        try {
            environment = this.serviceContainer.getDAO(EnvironmentRegistry.class).findByCode(code);

            if (environment == null) {
                log.error("Could not find Environment with code " + code);

                return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity(
                        "Could not find resource with code " + code).build();
            }
        } catch (PersistenceException exception) {
            log.error("Persistence exception caught while retrieving Environment", exception);

            return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }

        try {
            aggregatedStatus = getAggregatedCurrentStatusForStatusCarrier(environment, ui);
        } catch (PersistenceException exception) {
            log.error("Persistence exception caught while aggregating Status for Environment", exception);

            return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }

        if (log.isDebugEnabled()) {
            log.debug("Status for Environment aggregated");
        }

        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(aggregatedStatus.toXml()).build();
    }

    /**
     * Aggregates the given {@link StatusCarrier} to a {@link StatusHistogram}. The aggregation of status in
     * attached deployments is optional and triggered via UriInfo query parameters.
     * <br />When the query parameters contains the key <code>withDeployments</code> and value <code>true</code> the deployments
     * attached to the given StatusCarrier are respected during aggregation.
     *
     * @param statusCarrier {@link StatusCarrier} to aggregate
     * @param ui The {@link UriInfo} received in the service call
     * @return The aggregated status as {@link StatusHistogram}
     * @throws PersistenceException
     */
    private StatusHistogram getAggregatedCurrentStatusForStatusCarrier(StatusCarrier statusCarrier, UriInfo ui) throws PersistenceException {
        if (ui.getQueryParameters().containsKey("withDeployments")) {
            return this.serviceContainer.getDAO(StatusRegistry.class).getAggregatedCurrentStatusForCarrier(statusCarrier,
                    ui.getQueryParameters().getFirst("withDeployments").equals("true"));
        } else {
            return this.serviceContainer.getDAO(StatusRegistry.class).getAggregatedCurrentStatusForCarrier(statusCarrier);
        }
    }

    /**
     * This method implements the HTTP <code>GET</code> method of the status
     * aggregation resource. It facilitates the aggregation of status attached
     * to deployments. It expects that the deployment for which the aggregation
     * is to be performed via the following URL pattern
     * <code>/Status/Aggregation/Deployment/{location}/{code}</code> where
     * <code>{location}</code> is the location of the deployment of interest and
     * <code>{code}</code> is the code of the deployed software component.
     *
     * @param location
     *            receives the location of the deployment to be retrieved.
     * @param code
     *            receives the code of the deployed software component of the
     *            deployment to be retrieved.
     * @return a 404 code if the deployment could not be found, a 500 code in
     *         case of internal errors, a 200 code otherwise. The body of the
     *         response contains the XML representation of the aggregated
     *         status.
     */
    @GET
    @Path("/Deployment/{location}/{code}")
    @Produces("application/xml")
    public Response findForDeployment(@PathParam("location") String location, @PathParam("code") String code, @Context HttpServletRequest servletRequest) {
    	if (LighthouseAuthorizator.denyAccess(serviceContainer, servletRequest.getRemoteUser(), null, "/"))
    		return Response.status(Status.UNAUTHORIZED).build();
        if (log.isDebugEnabled()) {
            log.debug("Aggregating Status for Deployment of Component " + code + " at " + location);
        }

        StatusHistogram aggregatedStatus = null;
        Deployment deployment = null;

        SoftwareComponentRegistry softwareComponentRegistry = serviceContainer.getDAO(SoftwareComponentRegistry.class);
        DeploymentRegistry deploymentRegistry = serviceContainer.getDAO(DeploymentRegistry.class);
        
        try {
            SoftwareComponent softwareComponent = softwareComponentRegistry.findByCode(code);
            if (softwareComponent == null) {
                log.error("Could not find SoftwareComponent with code" + code);

                return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity(
                        "Could not find resource with code " + code).build();
            }

            deployment = deploymentRegistry.findByComponentAndLocation(softwareComponent, location);

            if (deployment == null) {
                log.error("Could not find Deployment with code" + code + " at " + location);
                return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity(
                        "Could not find resource with code " + code + " and location " + location).build();
            }
        } catch (PersistenceException exception) {
            log.error("Persistence exception caught while retrieving Deployment", exception);
            return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }

        StatusRegistry statusRegistry = serviceContainer.getDAO(StatusRegistry.class);
        
        try {
            aggregatedStatus = statusRegistry.getAggregatedCurrentStatusForCarrier(deployment);
        } catch (PersistenceException exception) {
            log.error("Persistence exception caught while aggregating Status for Deployment", exception);
            return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }

        if (log.isDebugEnabled()) {
            log.debug("Status for Deployment aggregated");
        }
        
        String xml = aggregatedStatus.toXml();
        
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(xml).build();
    }
}
