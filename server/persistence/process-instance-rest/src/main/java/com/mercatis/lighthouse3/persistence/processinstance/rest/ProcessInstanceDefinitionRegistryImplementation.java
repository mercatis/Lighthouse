package com.mercatis.lighthouse3.persistence.processinstance.rest;

import static com.mercatis.lighthouse3.commons.commons.HttpRequest.appendPathElementToUrl;

import com.mercatis.lighthouse3.commons.commons.HttpRequest.HttpMethod;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinitionRegistry;
import com.mercatis.lighthouse3.persistence.commons.rest.CodedDomainModelEntityDAOImplementation;

public class ProcessInstanceDefinitionRegistryImplementation extends
		CodedDomainModelEntityDAOImplementation<ProcessInstanceDefinition> implements
		ProcessInstanceDefinitionRegistry {
	
	private ProcessTaskRegistry processTaskRegistry = null;
	
	public ProcessInstanceDefinitionRegistryImplementation(String serverUrl, ProcessTaskRegistry processTaskRegistry) {
		this.setServerUrl(serverUrl);
		this.processTaskRegistry = processTaskRegistry;
	}
	
	public ProcessInstanceDefinitionRegistryImplementation(String serverUrl, ProcessTaskRegistry processTaskRegistry, String user, String password) {
	    this(serverUrl, processTaskRegistry);
	    this.user = user;
	    this.password = password;
	}

	public ProcessInstanceDefinition findByProcessTask(ProcessTask processTask) {
		String xml = null;
        try {
            xml = this.executeHttpMethod(this.urlForEntityProcessTask(processTask.getRootEntity()), HttpMethod.GET, null, null);
        } catch (PersistenceException persistenceException) {
            return null;
        }
        ProcessInstanceDefinition result = new ProcessInstanceDefinition();
        result.fromXml(xml, this.getEntityResolvers());
        
        return result;
	}
	
	private String urlForEntityProcessTask(ProcessTask rootProcessTask) {
		return appendPathElementToUrl(this.urlForEntityClass(), rootProcessTask.getCode());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getManagedType() {
		return ProcessInstanceDefinition.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected DomainModelEntityDAO[] getRealEntityResolvers() {
		return new DomainModelEntityDAO[] { processTaskRegistry };
	}

}
