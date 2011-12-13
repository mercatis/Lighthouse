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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.domainmodel.users.UserRegistry;
import com.mercatis.lighthouse3.service.commons.rest.CodedDomainModelEntityResource;

/**
 * This class implements the REST resource capturing users in the
 * user domain model of Lighthouse. As such, CRUD functionality on
 * users is made available in a RESTful way by the user service.
 */
@Path("/User")
public class UserResource extends CodedDomainModelEntityResource<User> {

    @Override
    public CodedDomainModelEntityDAO<User> getEntityDAO() {
        return this.getServiceContainer().getDAO(UserRegistry.class);
    }

    /**
     * Authenticate an user with given password.
     * <br />Usage <code>/User/Authenticate</code>.
     * User code and password are expected as
     * query parameters <code>code, password</code>.
     *
     * @return a 500 code in case of an internal problem, a
     *         200 code otherwise. The body of the response contains the XML
     *         representation of the authenticated user, on authentication failure a 401 code.
     */
    @POST
    @Path("/Authenticate")
    @Produces("application/xml")
    public Response findForUser(String payload, @Context UriInfo ui, @Context HttpServletRequest servletRequest) {
    	String authenticatedUser = servletRequest.getRemoteUser();
    	User user = null;
        try {
        	if (payload == null) {
        		throw new PersistenceException("Empty post body for user authentication.", null);
        	}
        	String splittedPayload[] = payload.split("\n");
        	
        	if (splittedPayload == null || splittedPayload.length < 1 || splittedPayload[0] == null) {
        		throw new PersistenceException("No code for user given.", null);
        	}
        	String userCode = splittedPayload[0];
        	// only the authenticated user may use this method
        	if (authenticatedUser!=null && !userCode.equals(authenticatedUser)) {
           		return Response.status(Status.UNAUTHORIZED).build();
        	}
        	
        	if (splittedPayload.length < 2 || splittedPayload[1] == null) {
        		throw new PersistenceException("No password for user given.", null);
        	}
        	String password = splittedPayload[1];
        	
            user = ((UserRegistry)this.getEntityDAO()).authenticate(userCode, password);
        } catch (PersistenceException exception) {
            log.error("Persistence exception caught while authenticating " + this.getEntityDAO().getManagedType().getSimpleName(), exception);

            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }
        if (user == null) {
            return Response.status(Status.UNAUTHORIZED).entity("Authentication failure").build();
        }
        return Response.status(Status.OK).entity(user.toXml()).build();
    }

	@Override
	protected String getContextRole(com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource.CtxOp op) {
		return "modifyPermissions";
	}

	@Override
	protected StringBuilder getContextString() {
		return new StringBuilder("/");
	}
	
	@Override
	protected StringBuilder getEntityContext(User e) {
		return getContextString();
	}
}
