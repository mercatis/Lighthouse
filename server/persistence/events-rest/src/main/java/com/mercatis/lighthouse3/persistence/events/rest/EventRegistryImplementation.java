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
package com.mercatis.lighthouse3.persistence.events.rest;

import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.mercatis.lighthouse3.commons.commons.HttpRequest.HttpMethod;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventBuilder;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.domainmodel.events.aggregation.Aggregation;
import com.mercatis.lighthouse3.domainmodel.events.aggregation.AggregationCommand;
import com.mercatis.lighthouse3.persistence.commons.rest.DomainModelEntityDAOImplementation;

/**
 * This class provides an event registry implementation. The implementation acts
 * as an HTTP client to a RESTful web service providing the DAO storage
 * functionality.
 */
public class EventRegistryImplementation extends DomainModelEntityDAOImplementation<Event> implements EventRegistry {

	/**
	 * This property maintains a reference to a suitable deployment registry to
	 * use for reference resolving.
	 */
	private DeploymentRegistry deploymentRegistry = null;
	
	/**
	 * This property maintains a reference to a suitable software component registry to
	 * use for reference resolving.
	 */
	private SoftwareComponentRegistry softwareComponentRegistry;

	/**
	 * This method returns a suitable deployment registry for reference
	 * resolving purposes.
	 * 
	 * @return the deployment registry
	 */
	protected DeploymentRegistry getDeploymentRegistry() {
		return deploymentRegistry;
	}

	/**
	 * This method sets the deployment registry to use for reference resolving
	 * purposes.
	 * 
	 * @param deploymentRegistry
	 *            the deployment registry to use
	 */
	public void setDeploymentRegistry(DeploymentRegistry deploymentRegistry) {
		this.deploymentRegistry = deploymentRegistry;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected DomainModelEntityDAO[] getRealEntityResolvers() {
		return new DomainModelEntityDAO[] { this.getDeploymentRegistry(), this.softwareComponentRegistry };
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected List<Event> resolveWebServiceResultList(String webServiceResultList) {
		List<Event> result = new LinkedList<Event>();
		List<String> eventIds = XmlMuncher.readValuesFromXml(webServiceResultList, "//:id");

		DomainModelEntityDAO[] entityResolvers = this.getEntityResolvers();

		for (String eventId : eventIds) {
			try {
				String eventXml = this.executeHttpMethod(this.urlForEntityId(new Long(eventId)), HttpMethod.GET, null,
						null);

				Event event = EventBuilder.template().done();
				event.fromXml(eventXml, entityResolvers);

				result.add(event);

			} catch (PersistenceException ex) {
			}
		}

		return result;
	}

	public void log(Event event) {
		this.persist(event);
	}

	public void log(Deployment context, Event event) {
		event.setContext(context);
		this.log(event);
	}

	public void log(DeploymentRegistry deploymentRegistry, SoftwareComponentRegistry softwareComponentRegistry,
			String location, String componentCode, Event event) {
		SoftwareComponent softwareComponent = softwareComponentRegistry.findByCode(componentCode);
		if (softwareComponent == null)
			throw new PersistenceException("Could not find event issuing software component with code.", null);

		Deployment deployment = deploymentRegistry.findByComponentAndLocation(softwareComponent, location);
		if (deployment == null)
			throw new PersistenceException(
					"Could not find deployment of event issuing software component at location.", null);

		this.log(deployment, event);
	}

	public EventRegistryImplementation() {
		super();
	}

	public EventRegistryImplementation(String serverUrl, DeploymentRegistry deploymentRegistry, SoftwareComponentRegistry softwareComponentRegistry) {
		super();
		this.setServerUrl(serverUrl);
		this.deploymentRegistry = deploymentRegistry;
		this.softwareComponentRegistry = softwareComponentRegistry;
	}
	
	public EventRegistryImplementation(String serverUrl, DeploymentRegistry deploymentRegistry, SoftwareComponentRegistry softwareComponentRegistry, String user, String password) {
	    this(serverUrl, deploymentRegistry, softwareComponentRegistry);
	    this.user = user;
	    this.password = password;
	}

	public Aggregation aggregate(AggregationCommand command) {
		Map<String, String> queryParameters = command.toQueryParameters();

		String resultXML = this.executeHttpMethod("/Event/Aggregation", HttpMethod.GET, null, queryParameters);

		Aggregation result = new Aggregation();
		result.fromXML(resultXML);

		return result;
	}

	@SuppressWarnings("rawtypes")
	public Deployment[] getAllDeployments() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("type", "deployments");
		
		String resultXML = this.executeHttpMethod("/Event/Information", HttpMethod.GET, null, queryParameters);
		
		String[] deployments = stringArrayFromXml(resultXML);
		
		Deployment[] result = new Deployment[deployments.length];
		
		DomainModelEntityDAO[] references = new DomainModelEntityDAO[] { this.softwareComponentRegistry, this.getDeploymentRegistry() };
		
		for(int x = 0; x < deployments.length; x++) {
			result[x] = new Deployment();
			String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			result[x].fromXml(header + deployments[x], references);
		}
		
		return result;
	}

	public String[] getAllUdfNames() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("type", "udfNames");
		
		String resultXML = this.executeHttpMethod("/Event/Information", HttpMethod.GET, null, queryParameters);
		
		String[] result = stringArrayFromXml(resultXML);
		
		return result;
	}
	
	/**
	 * Parses an XML representation of a String array.
	 * 
	 * @param xml
	 * 			the XML String to parse
	 * @return the String array
	 */
	public static String[] stringArrayFromXml(String xml) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(xml));
	        Document doc = db.parse(is);
	        
	        NodeList nodes = doc.getElementsByTagName("value");
	        
	        String[] result = new String[nodes.getLength()];
	        
	        for(int x = 0; x < nodes.getLength(); x++) {
	        	result[x] = nodes.item(x).getTextContent();
	        }
	        
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse XML to String array.", e);
		}
	}
}
