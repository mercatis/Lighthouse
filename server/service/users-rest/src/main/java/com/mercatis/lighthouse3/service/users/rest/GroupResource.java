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

import java.util.LinkedList;
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
import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.domainmodel.users.GroupRegistry;
import com.mercatis.lighthouse3.domainmodel.users.UserRegistry;
import com.mercatis.lighthouse3.service.commons.rest.CodedDomainModelEntityResource;
import com.mercatis.lighthouse3.service.jaas.util.LighthouseAuthorizator;

/**
 * This class implements the REST resource capturing groups in the
 * user domain model of Lighthouse. As such, CRUD functionality on
 * groups is made available in a RESTful way by the user service.
 */
@Path("/Group")
public class GroupResource extends CodedDomainModelEntityResource<Group> {

    @SuppressWarnings("rawtypes")
	@Override
    public DomainModelEntityDAO[] getDeserializationDAOs() {
        return new DomainModelEntityDAO[]{getEntityDAO(), getServiceContainer().getDAO(UserRegistry.class)};
    }

    @Override
    public GroupRegistry getEntityDAO() {
        return getServiceContainer().getDAO(GroupRegistry.class);
    }

    /**
     * This method implements the HTTP <code>GET</code> method to find groups for a specific user.
     * <br />It expects the method to be addressed via the following URL pattern <code>/Group/User/{code}</code>
     * <br /><code>{code}</code> is the code of an user
     *
     * @return a 500 code in case of an internal problem, a
     *         200 code otherwise. The body of the response contains the XML
     *         representation of the retrieved groups.
     */
    @GET
    @Path("/User/{code}")
    @Produces("application/xml")
    public Response findForUser(@PathParam("code") String code, @Context UriInfo ui, @Context HttpServletRequest servletRequest) {
    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), getContextString()))
    		return Response.status(Status.UNAUTHORIZED).build();
        List<Group> groups = null;
        try {
            groups = getEntityDAO().getGroupsForUserCode(code);

        } catch (PersistenceException exception) {
            log.error("Persistence exception caught while retrieving " + getEntityDAO().getManagedType().getSimpleName(), exception);

            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }
        if (groups == null) {
            groups = new LinkedList<Group>();
        }
        return Response.status(Status.OK).entity(createXmlListOfEntityReferences(groups)).build();
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
	protected StringBuilder getEntityContext(Group e) {
		return getContextString();
	}
}
