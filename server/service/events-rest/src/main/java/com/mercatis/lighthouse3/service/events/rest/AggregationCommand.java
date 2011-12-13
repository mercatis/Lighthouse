/**
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.domainmodel.events.aggregation.Aggregation;
import com.mercatis.lighthouse3.domainmodel.events.aggregation.AggregationIntervalResult;
import com.mercatis.lighthouse3.domainmodel.events.aggregation.Aggregator;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityRestServiceContainer;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityRestServiceContainer.CreatingServiceContainer;
import com.mercatis.lighthouse3.service.jaas.util.LighthouseAuthorizator;

/**
 * This webservice provides aggregation functionality over a list of events
 */
@Path("/Event/Aggregation")
public class AggregationCommand {

	/**
	 * This property keeps a logger.
	 */
	protected Logger log = Logger.getLogger(this.getClass());

	@CreatingServiceContainer
	protected DomainModelEntityRestServiceContainer domainModelEntityRestServiceContainer = null;

	/**
	 * This method returns the service container which created this resource
	 * 
	 * @return the service container
	 */
	protected DomainModelEntityRestServiceContainer getServiceContainer() {
		return this.domainModelEntityRestServiceContainer;
	}

	/**
	 * This method receives the aggregation command from a client, uses the
	 * local database to retrieve the events, aggregates them and sends back the
	 * result as XML.
	 * 
	 * @return the aggregation's result formatted in XML
	 */
	@GET
	@Produces("application/xml")
	public Response aggregate(@Context UriInfo ui, @Context HttpServletRequest servletRequest) {
    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), null, "/"))
    		return Response.status(Status.UNAUTHORIZED).build();
		if (log.isDebugEnabled())
			log.debug("Aggregating event using parameters " + ui.getRequestUri());

		Map<String, String> queryParams = null;
		try {
			queryParams = flattenMap(ui.getQueryParameters(true));
		} catch (Exception e) {
			log.error("Exception caught while parsing aggregation parameters", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
		}

		EventRegistry registry = null;
		try {
			registry = this.getServiceContainer().getDAO(EventRegistry.class);
		} catch (Exception e) {
			log.error("Exception caught while accessing event registry", e);

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
		}

		// create AggregationCommand
		com.mercatis.lighthouse3.domainmodel.events.aggregation.AggregationCommand command = new com.mercatis.lighthouse3.domainmodel.events.aggregation.AggregationCommand(
				registry, null, null, null, null, null);
		try {
			command.fromQueryParameters(queryParams);
		} catch (Exception e) {
			log.error("Exception caught while parsing event aggregation parameters", e);

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
		}

		Aggregation result = new Aggregation();
		Aggregator aggregator = null;
		try {
			aggregator = command.getAggregator();

		} catch (Exception e) {
			log.error("Exception caught while aggregating events", e);

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
		}

		for (AggregationIntervalResult intervalResult : aggregator) {

			result.add(intervalResult);
		}

		if (log.isDebugEnabled())
			log.debug("Events aggregated into time intervals");

		try {
			return Response.status(Status.OK).entity(result.toXML()).build();
		} catch (IOException e) {
			log.error("Exception caught while converting result to XML", e);

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
		}
	}

	/**
	 * Turns a MultivaluedMap into a Map that uses only on dimension.
	 * 
	 * @param queryParams
	 *            the MultivaluedMap
	 * @return a usual map
	 */
	private Map<String, String> flattenMap(MultivaluedMap<String, String> queryParams) {
		Map<String, String> params = new HashMap<String, String>();
		for (String key : queryParams.keySet())
			params.put(key, queryParams.getFirst(key));
		return params;
	}

}
