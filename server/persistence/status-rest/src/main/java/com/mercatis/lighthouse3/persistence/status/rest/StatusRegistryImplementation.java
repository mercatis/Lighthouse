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
package com.mercatis.lighthouse3.persistence.status.rest;

import static com.mercatis.lighthouse3.commons.commons.HttpRequest.appendPathElementToUrl;

import java.io.CharArrayReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.mercatis.lighthouse3.commons.commons.HttpRequest.HttpMethod;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusHistogram;
import com.mercatis.lighthouse3.domainmodel.status.StatusRegistry;
import com.mercatis.lighthouse3.persistence.commons.rest.CodedDomainModelEntityDAOImplementation;

/**
 * This class provides a status registry implementation. The implementation acts
 * as an HTTP client to a RESTful web service providing the DAO storage
 * functionality.
 */
public class StatusRegistryImplementation extends CodedDomainModelEntityDAOImplementation<Status> implements
        StatusRegistry {

    /**
     * This property keeps a reference to the RESTful environment registry
     * client implementation to use for resolving status context.
     */
    private EnvironmentRegistry environmentRegistry = null;
    /**
     * This property keeps a reference to the RESTful process/task registry
     * client implementation to use for resolving status context.
     */
    private ProcessTaskRegistry processTaskRegistry = null;
    /**
     * This property keeps a reference to the RESTful deployment registry client
     * implementation to use for resolving status context.
     */
    private DeploymentRegistry deploymentRegistry = null;
    
    private EventRegistry eventRegistry = null;
    
    /**
     * This property keeps a reference to the RESTful software component registry client
     * implementation to use for resolving status context.
     */
    private SoftwareComponentRegistry softwareComponentRegistry = null;

    @SuppressWarnings("rawtypes")
	@Override
    protected DomainModelEntityDAO[] getRealEntityResolvers() {
	      return new DomainModelEntityDAO[]{this.environmentRegistry,
		      this.processTaskRegistry,
		      this.deploymentRegistry,
		      this.softwareComponentRegistry,
		      this.eventRegistry
	      };
    }

    protected List<Status> resolveWebServiceResultList(String webServiceResultList, int pageSize, int pageNo) {
        List<Status> result = new LinkedList<Status>();
        List<String> entityCodes = XmlMuncher.readValuesFromXml(webServiceResultList, "//:code");

        for (String entityCode : entityCodes) {
            result.add(this.findByCode(entityCode, pageSize, pageNo));
        }

        return result;
    }

    private String getRequestPathForCarrier(StatusCarrier carrier) {
        String requestPath = null;

        if (carrier instanceof Deployment) {
            requestPath = appendPathElementToUrl(appendPathElementToUrl("/Status/Deployment", ((Deployment) carrier).getLocation()), ((Deployment) carrier).getDeployedComponent().getCode());
        } else if (carrier instanceof Environment) {
            requestPath = appendPathElementToUrl("/Status/Environment", ((Environment) carrier).getCode());

        } else {
            requestPath = appendPathElementToUrl("/Status/ProcessTask", ((ProcessTask) carrier).getCode());
        }

        return requestPath;
    }

    public List<Status> getStatusForCarrier(StatusCarrier carrier) {
        try {
            String httpResponse = this.executeHttpMethod(this.getRequestPathForCarrier(carrier), HttpMethod.GET, null, null);

            return this.resolveWebServiceResultList(httpResponse);
        } catch (Exception e) {
            throw new PersistenceException("Failed to get status for carrier", e);
        }
    }

    public List<Status> getStatusForCarrier(StatusCarrier carrier, int pageSize, int pageNo) {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("pageNo", "" + pageNo);
        queryParams.put("pageSize", "" + pageSize);

        try {
            String httpResponse = this.executeHttpMethod(this.getRequestPathForCarrier(carrier), HttpMethod.GET, null, null);

            return this.resolveWebServiceResultList(httpResponse, pageSize, pageNo);
        } catch (Exception e) {
            throw new PersistenceException("Failed to get status for carrier", e);
        }
    }

    public StatusHistogram getAggregatedCurrentStatusForCarrier(StatusCarrier carrier) {
        return getAggregatedCurrentStatusForCarrier(carrier, false);
    }

    public StatusHistogram getAggregatedCurrentStatusForCarrier(StatusCarrier carrier, boolean withDeployments) {
        StatusHistogram result = new StatusHistogram();
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("withDeployments", Boolean.toString(withDeployments));

        try {
            String requestPath = null;

            if (carrier instanceof Deployment) {
                requestPath = appendPathElementToUrl(appendPathElementToUrl("/Status/Aggregation/Deployment", ((Deployment) carrier).getLocation()), ((Deployment) carrier).getDeployedComponent().getCode());
            } else if (carrier instanceof Environment) {
                requestPath = appendPathElementToUrl("/Status/Aggregation/Environment", ((Environment) carrier).getCode());

            } else {
                requestPath = appendPathElementToUrl("/Status/Aggregation/ProcessTask", ((ProcessTask) carrier).getCode());
            }

            String httpResponse = this.executeHttpMethod(requestPath, HttpMethod.GET, null, queryParams);

            result.fromXml(httpResponse);
        } catch (Exception e) {
            throw new PersistenceException("Failed to get status for carrier", e);
        }

        return result;
    }

    public void clearStatusManually(String code, String clearer, String reason) {
        Map<String, String> queryParams = new HashMap<String, String>();
        if (clearer != null) {
            queryParams.put("clearer", clearer);
        }
        if (reason != null) {
            queryParams.put("reason", reason);
        }

        Status statusToClear = this.findByCode(code, 1, 1);
        
        this.executeHttpMethod(this.urlForEntityClass(), HttpMethod.PUT, statusToClear.toXml(), queryParams);
    }

    public Status findByCode(String code, int pageSize, int pageNo) {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("pageNo", "" + pageNo);
        queryParams.put("pageSize", "" + pageSize);

        try {
            String result = this.executeHttpMethod(this.urlForEntityCode(code), HttpMethod.GET, null, queryParams);

            Status status = new Status();
            status.fromXml(result, this.getEntityResolvers());

            return status;
        } catch (PersistenceException ex) {
            return null;
        }
    }

    /**
     * The public constructor for the RESTful client implementation of the
     * status registry interface.
     *
     * @param serverUrl
     *            the URL of the RESTful status web service.
     * @param environmentRegistry
     *            the RESTful client registry implementation to the environment
     *            web service
     * @param processTaskRegistry
     *            the RESTful client registry implementation to the process task
     *            web service
     * @param deploymentRegistry
     *            the RESTful client registry implementation to the deployment
     *            web service
     */
    public StatusRegistryImplementation(String serverUrl, EnvironmentRegistry environmentRegistry,
            ProcessTaskRegistry processTaskRegistry, DeploymentRegistry deploymentRegistry, SoftwareComponentRegistry softwareComponentRegistry, EventRegistry eventRegistry) {

        this.setServerUrl(serverUrl);
        this.environmentRegistry = environmentRegistry;
        this.processTaskRegistry = processTaskRegistry;
        this.deploymentRegistry = deploymentRegistry;
        this.softwareComponentRegistry = softwareComponentRegistry;
        this.eventRegistry = eventRegistry;
    }
    
    public StatusRegistryImplementation(String serverUrl, EnvironmentRegistry environmentRegistry,
            ProcessTaskRegistry processTaskRegistry, DeploymentRegistry deploymentRegistry, SoftwareComponentRegistry softwareComponentRegistry, EventRegistry eventRegistry, String user, String password) {
	this(serverUrl, environmentRegistry, processTaskRegistry, deploymentRegistry, softwareComponentRegistry, eventRegistry);
	this.user = user;
	this.password = password;
    }

    @SuppressWarnings("rawtypes")
	public Map<String, StatusHistogram> getAggregatedCurrentStatusesForCarrierClass(Class carrierClass) {
        return getAggregatedCurrentStatusesForCarrierClass(carrierClass, false);
    }

    @SuppressWarnings("rawtypes")
	public Map<String, StatusHistogram> getAggregatedCurrentStatusesForCarrierClass(Class carrierClass, boolean withDeployments) {
        Map<String, StatusHistogram> result = new HashMap<String, StatusHistogram>();
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("withDeployments", Boolean.toString(withDeployments));

        try {
            String requestPath = appendPathElementToUrl("/Status/Aggregation", carrierClass.getSimpleName());
            String httpResponse = this.executeHttpMethod(requestPath, HttpMethod.GET, null, queryParams);

            result = mapFromXml(httpResponse, carrierClass);
        } catch (Exception e) {
            throw new PersistenceException("Failed to aggregate for " + carrierClass.getSimpleName(), e);
        }

        return result;
    }
    
    public List<Status> findAll(int pageSize, int pageNo) {
    	List<Status> result = new ArrayList<Status>();
    	try {
    		Map<String,String> queryParams = new HashMap<String,String>();
    		queryParams.put("detailed", "true");
    		queryParams.put("pageSize", "" + pageSize);
    		queryParams.put("pageNo", "" + pageNo);
    		String response = this.executeHttpMethod(this.urlForEntityClass(), HttpMethod.GET, null, queryParams);
    		XmlMuncher muncher = new XmlMuncher(response);
    		
    		List<XmlMuncher> subMunchers = muncher.getSubMunchersForContext("/:list/:" + this.getManagedType().getSimpleName());
    		Iterator<XmlMuncher> it = subMunchers.iterator();
    		while (!subMunchers.isEmpty()) {
    			if (!it.hasNext()) {
    				it = subMunchers.iterator();
    			}
    			
    			Status entity = newEntity();
    			try {
    				entity.fromXml(it.next(), this.getEntityResolvers());
    				result.add(entity);
    				it.remove();
    			} catch (XMLSerializationException ex) {
    				// ignore
    			}
    		}
    	} catch (PersistenceException ex) {
            ex.printStackTrace();
    	}
    	
    	return result;
    }
    
    @Override
    public List<Status> findAll() {
    	List<Status> result = new ArrayList<Status>();
    	try {
    		Map<String,String> queryParams = new HashMap<String,String>();
    		queryParams.put("detailed", "true");
    		String response = this.executeHttpMethod(this.urlForEntityClass(), HttpMethod.GET, null, queryParams);
    		XmlMuncher muncher = new XmlMuncher(response);
    		
    		List<XmlMuncher> subMunchers = muncher.getSubMunchersForContext("/:list/:" + this.getManagedType().getSimpleName());
    		Iterator<XmlMuncher> it = subMunchers.iterator();
    		while (!subMunchers.isEmpty()) {
    			if (!it.hasNext()) {
    				it = subMunchers.iterator();
    			}
    			
    			Status entity = newEntity();
    			try {
    				entity.fromXml(it.next(), this.getEntityResolvers());
    				result.add(entity);
    				it.remove();
    			} catch (XMLSerializationException ex) {
    				// ignore
    			}
    		}
    	} catch (PersistenceException ex) {
            ex.printStackTrace();
    	}
    	
    	return result;
    }

    private enum CounterType {ok, error, stale, none}
    @SuppressWarnings("rawtypes")
	private Map<String, StatusHistogram> mapFromXml(String xml, final Class carrierClass) throws Exception {
        final Map<String, StatusHistogram> result = new HashMap<String, StatusHistogram>();

        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setContentHandler(new ContentHandler(){
            Set<String> possibleCounterTypes = new HashSet<String>(Arrays.asList("ok", "error", "stale", "none"));
            String currentCode;
            StatusHistogram currentHistogram = null;
            CounterType currenCounterType = null;

            public void setDocumentLocator(Locator locator) {}
            public void startDocument() throws SAXException {}
            public void endDocument() throws SAXException {}
            public void startPrefixMapping(String prefix, String uri) throws SAXException {}
            public void endPrefixMapping(String prefix) throws SAXException {}
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                if (localName.equals(carrierClass.getSimpleName())) {
                    currentCode = atts.getValue(uri, "code");
                } else if (localName.equals("StatusHistogram")) {
                    currentHistogram = new StatusHistogram();
                    result.put(currentCode, currentHistogram);
                } else if (possibleCounterTypes.contains(localName)) {
                    currenCounterType = CounterType.valueOf(localName);
                }
            }
            public void characters(char[] ch, int start, int length) throws SAXException {
                if (currenCounterType != null && currentHistogram != null) {
                    int value = 0;
                    try {
                        StringBuilder builder = new StringBuilder();
                        for (int i = start; i < start + length; i++) {
                                builder.append(ch[i]);
                        }
                        value = Integer.parseInt(builder.toString());
                    } catch (NumberFormatException e) {
                        throw new SAXException("Unable to parse integer");
                    }
                    switch(currenCounterType) {
                        case ok:
                            currentHistogram.setOk(value);
                            break;
                        case error:
                            currentHistogram.setError(value);
                            break;
                        case stale:
                            currentHistogram.setStale(value);
                            break;
                        case none:
                            currentHistogram.setNone(value);
                            break;
                    }
                }
            }
            public void endElement(String uri, String localName, String qName) throws SAXException {}
            public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
            public void processingInstruction(String target, String data) throws SAXException {}
            public void skippedEntity(String name) throws SAXException {}
        });
        reader.parse(new InputSource(new CharArrayReader(xml.toCharArray())));

        return result;
    }
}
