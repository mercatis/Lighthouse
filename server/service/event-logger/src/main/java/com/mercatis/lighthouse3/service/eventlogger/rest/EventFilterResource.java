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
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventBuilder;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityRestServiceContainer.CreatingServiceContainer;
import com.mercatis.lighthouse3.service.eventlogger.EventFilter;
import com.mercatis.lighthouse3.service.eventlogger.EventLoggerService;
import com.mercatis.lighthouse3.service.jaas.util.LighthouseAuthorizator;

/**
 * This resource captures the event filters registered by clients with the event
 * logger service. Thus, it provides a RESTful event filter registration
 * service.
 */
@Path("/EventLogger/EventFilter")
public class EventFilterResource {
	/**
	 * This property keeps a logger.
	 */
	protected Logger log = Logger.getLogger(this.getClass());

	@CreatingServiceContainer
	protected EventLoggerServiceContainer eventLoggerServiceContainer = null;

	/**
	 * This method implements the HTTP <code>POST</code> request handler of the
	 * event filter RESTful registration web service. The result of this call is
	 * the registration of an event filter with the event logger service.
	 * 
	 * The request must be directed against the path
	 * <code>/EventLogger/EventFilter</code>.
	 * 
	 * The request expects the client ID with which the caller has registered
	 * with the JMS topic configured by means of the
	 * <code>Topic.EventsNotification</code> parameter in the JMS configuration
	 * file/resource as the request payload content in the form
	 * 
	 * <blockquote> <EventFilter xmlns="http://www.mercatis.com/lighthouse3">
	 * <clientId>{clientId}</clientId> <eventTemplate>...</eventTemplate>
	 * </EventFilter> </blockquote>.
	 * 
	 * It returns the UUID of the event filter registration in its response as
	 * follows: <code><EventFilter><uuid>{uuid}</uuid></EventFilter></code>
	 * 
	 * The event template to use is passed in the usual event XML encoding in
	 * the <code><eventTemplate>...</eventTemplate></code> part above.
	 * 
	 * @param payload
	 *            the XML encoding of the JMS client ID of the caller.
	 * @return a 415 code if the payload could not be parsed, a 500 code if the
	 *         filter could not be registered, a 200 code otherwise.
	 */
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
	public Response register(String payload, @QueryParam("limit") Integer limit, @Context HttpServletRequest servletRequest) {
		if (LighthouseAuthorizator.denyAccess(eventLoggerServiceContainer, servletRequest.getRemoteUser(), null, "/"))
			return Response.status(Status.UNAUTHORIZED).build();
		if (log.isDebugEnabled())
			log.debug("Registering event filter: " + payload);

		String clientId = XmlMuncher.readValueFromXml(payload, "/*//:clientId");
		if (clientId == null) {
			log.error("Could not parse event filter: " + payload);
			return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).entity("Expected <clientId> element within <EventFilter> element in body").build();
		}

		Event templateToRegister = null;
		EventFilter eventFilter = null;

		try {
			String[] payloadParts = payload.replace("<eventTemplate>", "<split/><Event xmlns=\"" + XmlMuncher.MERCATIS_NS + "\">")
					.replace("</eventTemplate>", "</Event><split/>").split("<split/>");

			if (payloadParts.length < 2)
				return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).entity("Expected <eventTemplate> element within <EventFilter> element in body").build();

			templateToRegister = EventBuilder.template().done();
			templateToRegister.fromXml(payloadParts[1], this.getDeploymentRegistry(), this.getSoftwareComponentRegistry());

			eventFilter = new EventFilter(clientId, templateToRegister);

			int i = limit == null ? 200 : limit;

			this.getEventLoggerService().registerEventFilter(eventFilter, i);
		} catch (Exception anything) {
			log.error("Exception caught while registering event filter", anything);

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(anything.toString()).build();
		}

		String response = "<EventFilter xmlns=\"" + XmlMuncher.MERCATIS_NS + "\"><uuid>" + eventFilter.getUuid() + "</uuid></EventFilter>";

		if (log.isDebugEnabled())
			log.debug("Event filter successfully registered. UUID: " + response);

		return Response.status(Status.OK).entity(response).build();
	}

	/**
	 * This method implements the HTTP <code>PUT</code> request handler of the
	 * event filter RESTful registration web service. The result of this call is
	 * that a previously register event filter is refreshed, avoiding its
	 * expiry.
	 * 
	 * The request must be directed against the path
	 * <code>/EventLogger/EventFilter/{uuid}</code>, where uuid is the uuid of
	 * the filter to refresh.
	 * 
	 * @return a 500 code if the filter could not be refreshed, a 200 code
	 *         otherwise.
	 */
	@PUT
	@Path("{uuid}")
	@Produces("application/xml")
	public Response refresh(@PathParam("uuid") String uuid, @Context HttpServletRequest servletRequest) {
		if (LighthouseAuthorizator.denyAccess(eventLoggerServiceContainer, servletRequest.getRemoteUser(), null, "/"))
			return Response.status(Status.UNAUTHORIZED).build();
		if (log.isDebugEnabled())
			log.debug("Refreshing event filter with UUID: " + uuid);

		try {
			this.getEventLoggerService().refreshEventFilter(uuid);
		} catch (Exception anything) {
			log.error("Exception caught while refreshing event filter", anything);

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(anything.toString()).build();
		}

		if (log.isDebugEnabled())
			log.debug("Event filter successfully registered. UUID: " + uuid);

		return Response.status(Status.OK).build();
	}

	/**
	 * This method implements the HTTP <code>DELETE</code> request handler of
	 * the event filter RESTful registration web service. The result of this
	 * call is that a previously register event filter is removed from the event
	 * logger.
	 * 
	 * The request must be directed against the path
	 * <code>/EventLogger/EventFilter/{uuid}</code>, where uuid is the uuid of
	 * the filter to remove.
	 * 
	 * @return a 500 code if the filter could not be refreshed, a 200 code
	 */
	@DELETE
	@Path("{uuid}")
	@Produces("application/xml")
	public Response deregister(@PathParam("uuid") String uuid, @Context HttpServletRequest servletRequest) {
		if (LighthouseAuthorizator.denyAccess(eventLoggerServiceContainer, servletRequest.getRemoteUser(), null, "/"))
			return Response.status(Status.UNAUTHORIZED).build();
		if (log.isDebugEnabled())
			log.debug("Deregistering event filter with UUID: " + uuid);

		try {
			this.getEventLoggerService().deregisterEventFilter(uuid);
		} catch (Exception anything) {
			log.error("Exception caught while deregistering event filter", anything);

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(anything.toString()).build();
		}

		if (log.isDebugEnabled())
			log.debug("Event filter successfully deregistered. UUID: " + uuid);

		return Response.status(Status.OK).build();
	}

	private EventLoggerService getEventLoggerService() {
		return this.eventLoggerServiceContainer.getEventLoggerService();
	}

	private SoftwareComponentRegistry getSoftwareComponentRegistry() {
		return this.eventLoggerServiceContainer.getDAO(SoftwareComponentRegistry.class);
	}

	private DeploymentRegistry getDeploymentRegistry() {
		return this.eventLoggerServiceContainer.getDAO(DeploymentRegistry.class);
	}

}
