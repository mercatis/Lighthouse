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

import com.generationjava.io.xml.XmlEncXmlWriter;
import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.service.commons.rest.HierarchicalDomainModelEntityResource;
import com.mercatis.lighthouse3.service.jaas.util.LighthouseAuthorizator;
import com.sun.jersey.api.ConflictException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;

/**
 * This is an abstract base class wrapping a deployment carrying domain model
 * entities as a REST web service resource.
 */
public abstract class DeploymentCarryingDomainModelEntityResource<Entity extends DeploymentCarryingDomainModelEntity<Entity>>
		 extends HierarchicalDomainModelEntityResource<Entity> {

    @Override
    @SuppressWarnings("rawtypes")
    public DomainModelEntityDAO[] getDeserializationDAOs() {
        return new DomainModelEntityDAO[]{getEntityDAO(),
                    getServiceContainer().getDAO(DeploymentRegistry.class)};
    }

    @Override
    public abstract DeploymentCarryingDomainModelEntityDAO<Entity> getEntityDAO();

    /**
     * This method implements the HTTP <code>GET</code> method of the present
     * deployment carrying entity resource. It facilitates the retrieval of all
     * deployments either attached to the present entity or one of its subentities.
     * It expects the method to be addressed via the
     * following URL pattern <code>/{Entity}/Deployment/all/{code}</code>.
     * <code>{Entity}</code> is the name of the deployment carrying entity
     * class, <code>{code}</code> is the code of the entity.
     *
     * @return a 500 code in case of an internal problem, a 404 code if the
     *         deployment carrying entity resource could not be found, a
     *         200 code otherwise. The body of the response contains the XML
     *         representation of the retrieved entity codes.
     *
     *         The form is <blockquote> <list
     *         xmlns="http://www.mercatis.com/lighthouse3"> <code>AAA</code>
     *         <location>BBB</location> </list> </blockquote>
     */
    @GET
    @Path("Deployment/all/{code}")
    @Produces("application/xml")
    public Response findAllDeployments(@PathParam("code") String code, @Context HttpServletRequest servletRequest) {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving all deployments at " + getEntityDAO().getManagedType().getSimpleName());
        }

        List<Deployment> matches = new LinkedList<Deployment>();

        try {
            Entity entity = getEntityDAO().findByCode(code);
			if (entity == null) {
				log.error("Could not find " + getEntityDAO().getManagedType().getSimpleName() + " with code" + code);
				return Response.status(Status.NOT_FOUND).entity("Could not find resource with code " + code).build();
			}
	    	if (overridableDenyAccess(servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), entity))
	    		return Response.status(Status.UNAUTHORIZED).build();

	    	for (Deployment d : entity.getAllDeployments()) {
	            StringBuilder context = formatContext(new StringBuilder("/Deployment/Deployment"), d.getLocation(), d.getDeployedComponent().getCode());
	            if (LighthouseAuthorizator.allowAccess(getServiceContainer(), servletRequest.getRemoteUser(), "viewDeployment", context))
	            	matches.add(d);
	    	}
        } catch (PersistenceException exception) {
            log.error("Persistence exception caught while retrieving deployments for "
                    + getEntityDAO().getManagedType().getSimpleName(), exception);

            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }

        return Response.status(Status.OK).entity(createXmlListOfDeploymentReferences(matches)).build();
    }

	/**
	 * Given a list of deployments, this method creates an XML list of references to
	 * those deployments.
	 *
	 * The form is <blockquote> <list
	 * xmlns="http://www.mercatis.com/lighthouse3"> <code>AAA</code>
	 * <location>BBB</location> </list> </blockquote>
	 *
	 * @param entities
	 *            the list of deployments for which to create the list
	 * @return the XML list of references
	 */
	protected String createXmlListOfDeploymentReferences(List<Deployment> deployments) {
		StringWriter result = new StringWriter();
		XmlWriter xml = new XmlEncXmlWriter(result);

		try {
			xml.writeEntity("list");
			xml.writeAttribute("xmlns", XmlMuncher.MERCATIS_NS);

			for (Deployment deployment : deployments) {
                            xml.writeEntity("entry");
                            xml.writeEntityWithText("code", deployment.getDeployedComponent().getCode());
                            xml.writeEntityWithText("location", deployment.getLocation());
                            xml.endEntity();
			}

			xml.endEntity();
		} catch (IOException e) {
			throw new ConflictException("Error creating result list");
		}

		return result.toString();
	}

    /**
     * This method implements the HTTP <code>GET</code> method of the present
     * deployment carrying entity resource. It facilitates the retrieval of all
     * codes of the entities represented by the resource that are associated to
     * certain deployments. It expects the method to be addressed via the
     * following URL pattern <code>/{Entity}/Deployment/{location}/{code}</code>
     * . <code>{Entity}</code> is the name of the deployment carrying entity
     * class, <code>{code}</code> is the code of the deployed software
     * component, and <code>{location}</code> deployment location.
     *
     * @return a 500 code in case of an internal problem, a 404 code if the
     *         deployment or deployed software component could not be found, a
     *         200 code otherwise. The body of the response contains the XML
     *         representation of the retrieved entity codes.
     *
     *         The form is <blockquote> <list
     *         xmlns="http://www.mercatis.com/lighthouse3"> <code>AAA</code>
     *         <code>BBB</code> </list> </blockquote>
     */
    @GET
    @Path("Deployment/{location}/{code}")
    @Produces("application/xml")
    public Response findForDeployment(@PathParam("location") String location, @PathParam("code") String code, @Context HttpServletRequest servletRequest) {
        StringBuilder context = formatContext(new StringBuilder("/Deployment/Deployment"), location, code);
    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), "viewDeployment", context))
    		return Response.status(Status.UNAUTHORIZED).build();

    	if (log.isDebugEnabled()) {
            log.debug("Retrieving all " + getEntityDAO().getManagedType().getSimpleName() + "s for deployment of "+ code + " at " + location);
        }

        List<Entity> allowed = null;

        try {
            SoftwareComponent softwareComponent = getServiceContainer().getDAO(SoftwareComponentRegistry.class).findByCode(code);
            if (softwareComponent == null) {
                log.error("Could not find SoftwareComponent with code" + code);

                return Response.status(Status.NOT_FOUND).entity("Could not find resource with code " + code).build();
            }
            Deployment deployment = ((DeploymentRegistry) getDeserializationDAOs()[1]).findByComponentAndLocation(
                    softwareComponent, location);
            
            if (deployment == null) {
                log.error("Could not find Deployment with code" + code + " at " + location);

                return Response.status(Status.NOT_FOUND).entity(
                        "Could not find resource with code " + code + " and location " + location).build();
            }
            
            
            List<Entity> matches = getEntityDAO().findForDeployment(deployment);
            allowed = new LinkedList<Entity>();
            for (Entity e : matches)
            	if (!overridableDenyAccess(servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), e))
            		allowed.add(e);

            if (log.isDebugEnabled()) {
                log.debug("Matching " + getEntityDAO().getManagedType().getSimpleName()
                        + "s found for Deployment");
            }

        } catch (PersistenceException exception) {
            log.error("Persistence exception caught while retrieving "
                    + getEntityDAO().getManagedType().getSimpleName(), exception);

            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }

        return Response.status(Status.OK).entity(createXmlListOfEntityReferences(allowed)).build();
    }
}
