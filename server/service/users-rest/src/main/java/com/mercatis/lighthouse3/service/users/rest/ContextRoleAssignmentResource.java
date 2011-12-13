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
package com.mercatis.lighthouse3.service.users.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignment;
import com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignmentRegistry;
import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource;
import com.mercatis.lighthouse3.service.jaas.util.LighthouseAuthorizator;

/**
 * This class implements the REST resource capturing context role assignments in the
 * user domain model of Lighthouse. As such, CRUD functionality on
 * context role assignments is made available in a RESTful way by the user service.
 */
@Path("/ContextRoleAssignment")
public class ContextRoleAssignmentResource extends DomainModelEntityResource<ContextRoleAssignment> {

    @Override
    public DomainModelEntityDAO<ContextRoleAssignment> getEntityDAO() {
        return this.getServiceContainer().getDAO(ContextRoleAssignmentRegistry.class);
    }

    /**
     * Find the context role assignments for a specific user.
     * <br />Usage <code>ContextRoleAssignment/User/{code}</code> where <code>{code}</code> is the users code.
     * <br />Optional you can provide the query parameter <code>context</code> to filter with this context.
     *
     * @return a 500 code in case of an internal problem, a
     *         200 code otherwise. The body of the response contains the XML
     *         representation of the retrieved context role assignments.
     */
    @GET
    @Path("/User/{code}")
    @Produces("application/xml")
    public Response findForUser(@PathParam("code") String code, @Context UriInfo ui, @Context HttpServletRequest servletRequest) {
    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), "/"))
    		return Response.status(Status.UNAUTHORIZED).build();
        List<ContextRoleAssignment> contextRoleAssignments = null;
        User user = new User();
        user.setCode(code);
        try {
            String context = ui.getQueryParameters().getFirst("context");
            if (context == null) {
                contextRoleAssignments = ((ContextRoleAssignmentRegistry) this.getEntityDAO()).findFor(user);
            } else {
                contextRoleAssignments = ((ContextRoleAssignmentRegistry) this.getEntityDAO()).findFor(context, user);
            }
        } catch (PersistenceException exception) {
            log.error("Persistence exception caught while retrieving " + this.getEntityDAO().getManagedType().getSimpleName(), exception);

            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }
        return Response.status(Status.OK).entity(this.createXmlListOfEntityReferences(contextRoleAssignments)).build();
    }

    /**
     * Find the context role assignments for a specific user considering his assignments at groups.
     * <br />Usage <code>ContextRoleAssignment/All/User/{code}</code> where <code>{code}</code> is the users code.
     * <br />Optional you can provide the query parameter <code>context</code> to filter with this context.
     *
     * @return a 500 code in case of an internal problem, a
     *         200 code otherwise. The body of the response contains the XML
     *         representation of the retrieved context role assignments.
     */
    @GET
    @Path("/All/User/{code}")
    @Produces("application/xml")
    public Response findAllForUser(@PathParam("code") String code, @Context UriInfo ui, @Context HttpServletRequest servletRequest) {
    	String authenticatedUser = servletRequest.getRemoteUser();
    	// the authenticated user may use this method without special authorizations
    	if (authenticatedUser!=null && !code.equals(authenticatedUser) && LighthouseAuthorizator.denyAccess(getServiceContainer(), authenticatedUser, getContextRole(CtxOp.FIND), "/")) {
        		return Response.status(Status.UNAUTHORIZED).build();
    	}
        List<ContextRoleAssignment> contextRoleAssignments = null;
        User user = new User();
        user.setCode(code);
        try {
            String context = ui.getQueryParameters().getFirst("context");
            if (context == null) {
                contextRoleAssignments = ((ContextRoleAssignmentRegistry) this.getEntityDAO()).findAllFor(user);
            } else {
                contextRoleAssignments = ((ContextRoleAssignmentRegistry) this.getEntityDAO()).findAllFor(context, user);
            }
        } catch (PersistenceException exception) {
            log.error("Persistence exception caught while retrieving " + this.getEntityDAO().getManagedType().getSimpleName(), exception);

            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }
        return Response.status(Status.OK).entity(this.createXmlListOfEntityReferences(contextRoleAssignments)).build();
    }

    /**
     * Find the context role assignments for a specific group.
     * <br />Usage <code>ContextRoleAssignment/Group/{code}</code> where <code>{code}</code> is the groups code.
     * <br />Optional you can provide the query parameter <code>context</code> to filter with this context.
     *
     * @return a 500 code in case of an internal problem, a
     *         200 code otherwise. The body of the response contains the XML
     *         representation of the retrieved context role assignments.
     */
    @GET
    @Path("/Group/{code}")
    @Produces("application/xml")
    public Response findForGroup(@PathParam("code") String code, @Context UriInfo ui, @Context HttpServletRequest servletRequest) {
    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), "/"))
    		return Response.status(Status.UNAUTHORIZED).build();
        List<ContextRoleAssignment> contextRoleAssignments = null;
        Group group = new Group();
        group.setCode(code);
        try {
            String context = ui.getQueryParameters().getFirst("context");
            if (context == null) {
                contextRoleAssignments = ((ContextRoleAssignmentRegistry) this.getEntityDAO()).findFor(group);
            } else {
                contextRoleAssignments = ((ContextRoleAssignmentRegistry) this.getEntityDAO()).findFor(context, group);
            }
        } catch (PersistenceException exception) {
            log.error("Persistence exception caught while retrieving " + this.getEntityDAO().getManagedType().getSimpleName(), exception);

            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }
        return Response.status(Status.OK).entity(this.createXmlListOfEntityReferences(contextRoleAssignments)).build();
    }

    /**
     * Find the context role assignments for a specific context.
     * <br />Usage <code>ContextRoleAssignment/Context</code>.
     * <br />In this case, the query parameter <code>context</code> is mandatory.
     *
     * @return a 500 code in case of an internal problem, a
     *         200 code otherwise. The body of the response contains the XML
     *         representation of the retrieved context role assignments.
     */
    @GET
    @Path("/Context")
    @Produces("application/xml")
    public Response findForContext(@Context UriInfo ui, @Context HttpServletRequest servletRequest) {
    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), "/"))
    		return Response.status(Status.UNAUTHORIZED).build();
        List<ContextRoleAssignment> contextRoleAssignments = null;
        try {
            String context = ui.getQueryParameters().getFirst("context");
            if (context == null) {
                throw new PersistenceException("No query parameter for context given", null);
            } else {
                contextRoleAssignments = ((ContextRoleAssignmentRegistry) this.getEntityDAO()).findFor(context);
            }
        } catch (PersistenceException exception) {
            log.error("Persistence exception caught while retrieving " + this.getEntityDAO().getManagedType().getSimpleName(), exception);

            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }
        return Response.status(Status.OK).entity(this.createXmlListOfEntityReferences(contextRoleAssignments)).build();
    }
    
	@Override
	protected String getContextRole(com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource.CtxOp op) {
		switch (op) {
			case FIND:
				return null;
			case PERSIST:
			case UPDATE:
			case DELETE:
				return "modifyPermissions";
			default:
				throw new PersistenceException("unsupported operation in role lookup", null);
		}
	}

	@Override
	protected StringBuilder getContextString() {
		return new StringBuilder("/");
	}
	
	@Override
	protected StringBuilder getEntityContext(ContextRoleAssignment e) {
		return getContextString();
	}
}
