package com.mercatis.lighthouse3.persistence.processinstance.rest;

import static com.mercatis.lighthouse3.commons.commons.HttpRequest.appendPathElementToUrl;

import java.util.List;

import com.mercatis.lighthouse3.commons.commons.HttpRequest.HttpMethod;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstance;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinitionRegistry;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceRegistry;
import com.mercatis.lighthouse3.persistence.commons.rest.DomainModelEntityDAOImplementation;
import java.util.HashMap;
import java.util.Map;

public class ProcessInstanceRegistryImplementation extends
		DomainModelEntityDAOImplementation<ProcessInstance> implements
		ProcessInstanceRegistry {
	
	private ProcessInstanceDefinitionRegistry processInstanceDefinitionRegistry = null;
	
	private EventRegistry eventRegistry = null;
	
	public ProcessInstanceRegistryImplementation(String serverUrl, ProcessInstanceDefinitionRegistry processInstanceDefinitionRegistry, EventRegistry eventRegistry) {
		this.setServerUrl(serverUrl);
		this.processInstanceDefinitionRegistry = processInstanceDefinitionRegistry;
		this.eventRegistry = eventRegistry;
	}
	
	public ProcessInstanceRegistryImplementation(String serverUrl, ProcessInstanceDefinitionRegistry processInstanceDefinitionRegistry, EventRegistry eventRegistry, String user, String password) {
	    this(serverUrl, processInstanceDefinitionRegistry, eventRegistry);
	    this.user = user;
	    this.password = password;
	}

	public List<ProcessInstance> findByProcessInstanceDefinition(ProcessInstanceDefinition processInstanceDefinition) {
		String xml = null;
        try {
            xml = this.executeHttpMethod(this.urlForEntityProcessInstanceDefinition(processInstanceDefinition), HttpMethod.GET, null, null);
        } catch (PersistenceException persistenceException) {
            return null;
        }
        
        return this.resolveWebServiceResultList(xml);
	}
	
	public List<ProcessInstance> findByProcessTask(ProcessTask processTask) {
		String xml = null;
        try {
            xml = this.executeHttpMethod(this.urlForEntityProcessTask(processTask.getRootEntity()), HttpMethod.GET, null, null);
        } catch (PersistenceException persistenceException) {
            return null;
        }
        
        return this.resolveWebServiceResultList(xml);
	}

	public List<ProcessInstance> findByProcessTask(ProcessTask processTask, int pageSize, int pageNo) {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("pageSize", Integer.toString(pageSize));
            parameters.put("pageNo", Integer.toString(pageNo));
		String xml = null;
        try {
            xml = this.executeHttpMethod(this.urlForEntityProcessTask(processTask.getRootEntity()), HttpMethod.GET, null, parameters);
        } catch (PersistenceException persistenceException) {
            return null;
        }

        return this.resolveWebServiceResultList(xml);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected DomainModelEntityDAO[] getRealEntityResolvers() {
		return new DomainModelEntityDAO[] { processInstanceDefinitionRegistry, eventRegistry };
	}
	
	private String urlForEntityProcessTask(ProcessTask rootProcessTask) {
		return appendPathElementToUrl(appendPathElementToUrl(this.urlForEntityClass(), "ProcessTask"), rootProcessTask.getCode());
	}
	
	private String urlForEntityProcessInstanceDefinition(ProcessInstanceDefinition processInstanceDefinition) {
		return urlForEntityProcessTask(processInstanceDefinition.getProcessTask().getRootEntity());
	}

    public List<ProcessInstance> findAfterInstance(ProcessInstance instance, int maxResults) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("maxResults", Integer.toString(maxResults));
        String xml = null;
        try {
            xml = this.executeHttpMethod(appendPathElementToUrl(appendPathElementToUrl(this.urlForEntityClass(), "after"), Long.toString(instance.getId())), HttpMethod.GET, null, parameters);
        } catch (PersistenceException e) {
            return null;
        }
        return this.resolveWebServiceResultList(xml);
    }

    public List<ProcessInstance> findBeforeInstance(ProcessInstance instance, int maxResults) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("maxResults", Integer.toString(maxResults));
        String xml = null;
        try {
            xml = this.executeHttpMethod(appendPathElementToUrl(appendPathElementToUrl(this.urlForEntityClass(), "before"), Long.toString(instance.getId())), HttpMethod.GET, null, parameters);
        } catch (PersistenceException e) {
            return null;
        }
        return this.resolveWebServiceResultList(xml);
    }
}
