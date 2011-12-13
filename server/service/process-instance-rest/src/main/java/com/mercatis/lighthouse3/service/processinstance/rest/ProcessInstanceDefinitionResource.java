package com.mercatis.lighthouse3.service.processinstance.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.ConstraintViolationException;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinitionRegistry;
import com.mercatis.lighthouse3.service.commons.rest.CodedDomainModelEntityResource;
import com.mercatis.lighthouse3.service.commons.rest.ResourceEventListener;
import com.mercatis.lighthouse3.service.commons.rest.ResourceEventTopicPublisher;

@Path("/ProcessInstanceDefinition")
public class ProcessInstanceDefinitionResource extends CodedDomainModelEntityResource<ProcessInstanceDefinition> {

	private static ResourceEventTopicPublisher processInstanceDefinitionPublisher = null;
	
	@Override
	protected List<ResourceEventListener> returnInitialResourceEventListeners() {
		if (processInstanceDefinitionPublisher == null)
			processInstanceDefinitionPublisher = new ResourceEventTopicPublisher(ProcessInstanceDefinition.class, ProcessInstanceDefinitionRegistry.class, this.getServiceContainer());

		List<ResourceEventListener> initialListeners = super.returnInitialResourceEventListeners();
		initialListeners.add(processInstanceDefinitionPublisher);

		return initialListeners;
	}
	
	@Override
	public CodedDomainModelEntityDAO<ProcessInstanceDefinition> getEntityDAO() {
		return this.getServiceContainer().getDAO(ProcessInstanceDefinitionRegistry.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public DomainModelEntityDAO[] getDeserializationDAOs() {
		return new DomainModelEntityDAO[] { this.getServiceContainer().getDAO(ProcessTaskRegistry.class) };
	}
	
	public ProcessTaskRegistry getProcessTaskRegistry() {
		return (ProcessTaskRegistry) this.getDeserializationDAOs()[0];
	}

	public ProcessInstanceDefinitionRegistry getProcessInstanceDefinitionRegistry() {
		return (ProcessInstanceDefinitionRegistry) this.getEntityDAO();
	}
	
	@Override
    public Response findByTemplate(UriInfo ui, HttpServletRequest servletRequest) {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving " + this.getEntityDAO().getManagedType().getSimpleName() + " by template "
                    + ui.getRequestUri());
        }

        MultivaluedMap<String, String> queryParams = ui.getQueryParameters(true);
        ProcessInstanceDefinition template = null;

        try {
            template = this.createEntityTemplateFromQueryParameters(this.newEmptyEntityTemplate(), queryParams);
        } catch (ConstraintViolationException exception) {
            log.error("Constraint violation by " + this.getEntityDAO().getManagedType().getSimpleName() + " template",
                    exception);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }

        List<ProcessInstanceDefinition> matches = null;

        try {
            matches = this.getEntityDAO().findByTemplate(template);

            if (log.isDebugEnabled()) {
                log.debug("Matching " + this.getEntityDAO().getManagedType().getSimpleName() + "s found for template");
            }
        } catch (PersistenceException exception) {
            log.error("Persistence exception caught while retrieving "
                    + this.getEntityDAO().getManagedType().getSimpleName(), exception);

            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
        }

        boolean detailed = queryParams.containsKey("detailed") ? Boolean.parseBoolean(queryParams.get("detailed").iterator().next()) : false;
        if (detailed)
        	return Response.status(Status.OK).entity(this.createDetailedXmlListOfEntities(matches)).build();
        
        return Response.status(Status.OK).entity(this.createXmlListOfEntityReferences(matches)).build();
    }
	
	protected ProcessInstanceDefinition createEntityTemplateFromQueryParameters(ProcessInstanceDefinition emptyTemplate,
			MultivaluedMap<String, String> queryParams) {
		Map<String, String> cgiParameters = new HashMap<String, String>();
		for (String key : queryParams.keySet())
			cgiParameters.put(key, queryParams.getFirst(key));

		emptyTemplate.fromQueryParameters(cgiParameters, getProcessTaskRegistry());

		return emptyTemplate;
	}

	@Override
	protected String getContextRole(com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource.CtxOp op) {
		switch (op) {
			case FIND:
			case PERSIST:
			case UPDATE:
			case DELETE:
				return null;
			default:
				throw new PersistenceException("unsupported operation in role lookup", null);
		}
	}

	@Override
	protected StringBuilder getContextString() {
		return new StringBuilder("/");
	}
}
