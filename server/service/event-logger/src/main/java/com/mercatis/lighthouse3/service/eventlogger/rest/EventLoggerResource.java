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
package com.mercatis.lighthouse3.service.eventlogger.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityRestServiceContainer.CreatingServiceContainer;

/**
 * This class models the REST resource representing the event logger service.
 */
@Path("/EventLogger")
public class EventLoggerResource {
	/**
	 * This property keeps a logger.
	 */
	protected Logger log = Logger.getLogger(this.getClass());

	@CreatingServiceContainer
	protected EventLoggerServiceContainer eventLoggerServiceContainer = null;

	
	/**
	 * This method implements the HTTP <code>POST</code> request handler of the
	 * event logger service. The body of the request contains the XML
	 * representation of the event.
	 * 
	 * @param payload
	 *            receives the XML representation of the entity to be created.
	 * @return a 415 code if the payload could not be parsed, a 500 code if the
	 *         entity could not be created, a 200 code otherwise.
	 */
	@POST
	@Consumes("application/xml")
	@Produces("text/plain")
	public Response logEvent(String payload, @Context HttpServletRequest servletRequest) {
		// NO AUTH ON LOG
//    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), "", "/"))
//    		return Response.status(Status.UNAUTHORIZED).build();
		if (log.isDebugEnabled())
			log.debug("Logging event with XML: " + payload);

		if ("".equals(payload)) {
			log.error("No event XML passed to log");

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Invalid XML format").build();
		}
		try {
			this.eventLoggerServiceContainer.getEventLoggerService().log(payload);
		} catch (XMLSerializationException exception) {
			log.error("XML serialization exception caught while logging events", exception);

			return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).entity(exception.toString()).build();
		} catch (PersistenceException exception) {
			
			log.error("Persistence exception caught while logging events", exception);

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}
		if (log.isDebugEnabled())
			log.debug("Event logged successfully");

		return Response.status(Status.OK).build();
	}
}
