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
import java.io.StringWriter;
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

import com.generationjava.io.xml.XmlEncXmlWriter;
import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityRestServiceContainer;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityRestServiceContainer.CreatingServiceContainer;
import com.mercatis.lighthouse3.service.jaas.util.LighthouseAuthorizator;

/**
 * This webservice provides general information about all events,
 * such as all the different deployments or all different UDF names.
 */
@Path("/Event/Information")
public class GeneralEventInformation {
	
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
	 * This method sends back the requested information
	 * using XML
	 * HTTP Parameter 'type' must be set and needs to be
	 * one of the following:
	 * 'deployments'  (gets all different deployments existing)
	 * 'udfNames'  (gets all different udfNames existing)
	 * 
	 * @return the requested information formatted in XML
	 */
	@GET
	@Produces("application/xml")
	public Response getInformation(@Context UriInfo ui, @Context HttpServletRequest servletRequest) {
    	if (LighthouseAuthorizator.denyAccess(getServiceContainer(), servletRequest.getRemoteUser(), null, "/"))
    		return Response.status(Status.UNAUTHORIZED).build();
		if (log.isDebugEnabled())
			log.debug("Retrieving Information about " + ui.getRequestUri());
		
		EventRegistry registry = null;
		try {
			registry = this.getServiceContainer().getDAO(EventRegistry.class);
		} catch (Exception e) {
			log.error("Exception caught while accessing event registry", e);

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
		}
		
		Map<String, String> queryParams = null;
		try {
			queryParams = flattenMap(ui.getQueryParameters(true));
		} catch (Exception e) {
			log.error("Exception caught while parsing information parameters", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
		}
		
		if(queryParams.get("type").equals("deployments")) {
			Deployment[] result = null;
			try {
				result = registry.getAllDeployments();
			} catch (Exception e) {
				log.error("Exception caught while retrieving all deployments", e);
	
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
			}
			
			try {
				return Response.status(Status.OK).entity(deploymentArrayToXml(result)).build();
			} catch (Exception e) {
				log.error("Exception caught while converting result to XML", e);

				return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
			}
			
		} else if(queryParams.get("type").equals("udfNames")) {
			String[] result = null;
			try {
				result = registry.getAllUdfNames();
			} catch (Exception e) {
				log.error("Exception caught while retrieving all UDF names", e);
	
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
			}
			
			try {
				return Response.status(Status.OK).entity(stringArrayToXml(result)).build();
			} catch (Exception e) {
				log.error("Exception caught while converting result to XML", e);

				return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
			}
			
		} else {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Parameter " + queryParams.get("type") + " unknown.").build();
		}
	}
	
	/**
	 * Turns an array of Deployments into its XML representation,
	 * using the XML representation of a String array to handle
	 * multiple deployments.
	 * 
	 * @param array
	 * 			the array of Deployments
	 * @return the XML representation
	 * 
	 * @throws IOException
	 */
	public static String deploymentArrayToXml(Deployment[] array) throws IOException {
		String result = "";
		
		String[] stringArray = new String[array.length];
		
		for(int x = 0; x < array.length; x++) {
			stringArray[x] = array[x].toXml();
		}
		
		result = stringArrayToXml(stringArray);
		
		return result;
	}
	
	/**
	 * Turns a String array into its XML representation
	 * 
	 * @param array
	 * 			the String array
	 * @return the XML representation
	 * 
	 * @throws IOException
	 */
	public static String stringArrayToXml(String[] array) throws IOException {
		
		StringWriter result = new StringWriter();
		XmlWriter xml = new XmlEncXmlWriter(result);
		
		xml.writeEntity("stringArray");
		
		for(int x = 0; x < array.length; x++) {
			xml.writeEntityWithText("value", array[x]);
		}
		
		xml.endEntity();
		
		String xmlResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + result.toString();
		
		return xmlResult;
	}
	
	/**
	 * Turns a MultivaluedMap into a Map that uses only one dimension.
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
