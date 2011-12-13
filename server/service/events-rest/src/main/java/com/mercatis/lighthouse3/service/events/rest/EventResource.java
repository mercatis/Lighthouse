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
package com.mercatis.lighthouse3.service.events.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.mercatis.lighthouse3.commons.commons.Ranger;
import com.mercatis.lighthouse3.domainmodel.commons.ConstraintViolationException;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventBuilder;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource;
import com.mercatis.lighthouse3.service.jaas.util.LighthouseAuthorizator;

/**
 * This class implements the REST resource capturing events in a RESTful way by
 * means of the events service.
 */
@Path("/Event")
public class EventResource extends DomainModelEntityResource<Event> {

    @Override
    public DomainModelEntityDAO<Event> getEntityDAO() {
        return getServiceContainer().getDAO(EventRegistry.class);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public DomainModelEntityDAO[] getDeserializationDAOs() {
        return new DomainModelEntityDAO[]{getServiceContainer().getDAO(DeploymentRegistry.class),
                    getServiceContainer().getDAO(SoftwareComponentRegistry.class)};
    }

    public DeploymentRegistry getDeploymentRegistry() {
        return (DeploymentRegistry) getDeserializationDAOs()[0];
    }

    public SoftwareComponentRegistry getSoftwareComponentRegistry() {
        return (SoftwareComponentRegistry) getDeserializationDAOs()[1];
    }

    @Override
    protected Event newEmptyEntityTemplate() {
        return EventBuilder.template().done();
    }

    @Override
    protected Event createEntityTemplateFromQueryParameters(Event template, MultivaluedMap<String, String> queryParams) {

        Map<String, String> cgiParameters = new HashMap<String, String>();
        for (String key : queryParams.keySet()) {
            cgiParameters.put(key, queryParams.getFirst(key));
        }

        template.fromQueryParameters(cgiParameters, getSoftwareComponentRegistry(), getDeploymentRegistry());
        return template;
    }

    @Override
    public Response findByTemplate(UriInfo ui, HttpServletRequest servletRequest) {
		// bypassed authorization by passing 'null' role to LighthouseAuthorizator, due to missing 'viewEvents' role. VF
    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), null, "/"))
    		return Response.status(Status.UNAUTHORIZED).build();
        if (log.isDebugEnabled()) {
            log.debug("Retrieving Events by template - blocking invalid templates");
        }

        if (log.isDebugEnabled()) {
            log.debug("Retrieving " + getEntityDAO().getManagedType().getSimpleName() + " by template "
                    + ui.getRequestUri());
        }

        MultivaluedMap<String, String> queryParams = ui.getQueryParameters(true);
        Event template = null;
        
        try {
            template = createEntityTemplateFromQueryParameters(newEmptyEntityTemplate(), queryParams);
            
            int paramValue = 100;
            String param = queryParams.getFirst("limit");
            if (param!=null) {
    	        try {
    	        	paramValue = Integer.parseInt(param);
    	        } catch (NumberFormatException e) {
    	        	log.warn("Error parsing 'limit' parameter, using default of 100: "+e.getMessage());
    	        }
            }
            template.setUdf("eventRESTResourceLimitRestriction", Math.min(paramValue, 500));
            
            param = queryParams.getFirst("timespan");
            if (param!=null) {
    	        try {
    	        	paramValue = Integer.parseInt(param);
    	        	long now = System.currentTimeMillis() / 60000; // round down to whole minutes
    	        	now *= 60000;
    	        	template.setDateOfOccurrence(Ranger.interval(new Date(now-(paramValue*60000)), new Date(now)));
    	        } catch (NumberFormatException e) {
    	        	log.warn("Error parsing 'timespan' parameter, ignoring value of: "+param);
    	        }
            }
            
        } catch (ConstraintViolationException exception) {
            log.error("Constraint violation by " + getEntityDAO().getManagedType().getSimpleName() + " template",
                    exception);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }

        List<Event> matches = new ArrayList<Event>();

        try {
        	List<Event> all = getEntityDAO().findByTemplate(template);
        	for (Event e : all)
        		if (LighthouseAuthorizator.allowAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), getEntityContext(e)))
       				matches.add(e);
        	
            if (log.isDebugEnabled()) {
                log.debug("Matching " + getEntityDAO().getManagedType().getSimpleName() + "s found for template");
            }
        } catch (PersistenceException exception) {
            log.error("Persistence exception caught while retrieving "
                    + getEntityDAO().getManagedType().getSimpleName(), exception);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }
        if (matches.isEmpty()) {
        	Event e = new Event();
        	e.setLevel("DETAIL");
        	e.setCode("NothingFound");
        	e.setDateOfOccurrence(new Date());
        	e.setId(template.getId());
        	e.setContext(template.getContext());
        	e.setLighthouseDomain(template.getLighthouseDomain());
        	e.setMachineOfOrigin("EventServer");
        	e.setMessage("Your query did not return any results.");
        	matches.add(e);
        }
        String xml;

        boolean detailed = queryParams.containsKey("detailed") ? Boolean.parseBoolean(queryParams.get("detailed").iterator().next()) : false;
        if (detailed)
        	xml = createDetailedXmlListOfEntities(matches);
        else
        	xml = createXmlListOfEntityReferences(matches);
        
        return Response.status(Status.OK).entity(xml).build();
    }

	@Override
	protected String getContextRole(com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource.CtxOp op) {
		switch (op) {
			case FIND:
			case PERSIST:
			case UPDATE:
			case DELETE:
				return null;
			default:
				throw new PersistenceException("unsupported operation in role lookup", null);
		}
	}

	@Override
	protected StringBuilder getContextString() {
		return new StringBuilder("/");
	}

	@Override
	protected StringBuilder getEntityContext(Event e) {
		return formatContext(e.getMachineOfOrigin(), e.getCode());
	}
}
