package com.mercatis.lighthouse3.service.processinstance.rest;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstance;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinitionRegistry;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceRegistry;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource;
import com.mercatis.lighthouse3.service.commons.rest.ResourceEventListener;
import com.mercatis.lighthouse3.service.jaas.util.LighthouseAuthorizator;

@Path("/ProcessInstance")
public class ProcessInstanceResource extends DomainModelEntityResource<ProcessInstance> {

	@Override
	protected List<ResourceEventListener> returnInitialResourceEventListeners() {
		return super.returnInitialResourceEventListeners();
	}
	
	@Override
	public DomainModelEntityDAO<ProcessInstance> getEntityDAO() {
		return getServiceContainer().getDAO(ProcessInstanceRegistry.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public DomainModelEntityDAO[] getDeserializationDAOs() {
		return new DomainModelEntityDAO[] { getServiceContainer().getDAO(ProcessInstanceDefinitionRegistry.class), getServiceContainer().getDAO(EventRegistry.class) };
	}
	
	public ProcessInstanceRegistry getProcessInstanceRegistry() {
		return (ProcessInstanceRegistry) getEntityDAO();
	}

	public ProcessInstanceDefinitionRegistry getProcessInstanceDefinitionRegistry() {
		return (ProcessInstanceDefinitionRegistry) getDeserializationDAOs()[0];
	}
	
	public EventRegistry getEventRegistry() {
		return (EventRegistry) getDeserializationDAOs()[1];
	}
	
	@Override
	protected ProcessInstance createEntityTemplateFromQueryParameters(ProcessInstance emptyTemplate, MultivaluedMap<String, String> queryParams) {
		Map<String, String> cgiParameters = new HashMap<String, String>();
		for (String key : queryParams.keySet())
			cgiParameters.put(key, queryParams.getFirst(key));

		emptyTemplate.fromQueryParameters(cgiParameters, getProcessInstanceDefinitionRegistry());

		return emptyTemplate;
	}
	
	@GET
	@Path("ProcessTask/{code}")
	@Produces("application/xml")
	public Response findByProcessTaskCode(@PathParam("code") String code, @Context UriInfo ui, @Context HttpServletRequest servletRequest) {
		int pageSize = -1;
		int pageNo = -1;

		if (ui.getQueryParameters().containsKey("pageSize"))
			pageSize = Integer.parseInt(ui.getQueryParameters(true).get("pageSize").get(0));

		if (ui.getQueryParameters().containsKey("pageNo"))
			pageNo = Integer.parseInt(ui.getQueryParameters(true).get("pageNo").get(0));

		if (log.isDebugEnabled())
			log.debug("Retrieving " + getEntityDAO().getManagedType().getSimpleName() + " by ProcessTask#code " + code);

		List<ProcessInstance> entities = new LinkedList<ProcessInstance>();

		try {
			List<ProcessInstance> tmp;
			if (pageSize != -1 && pageNo != -1) {
				tmp = getProcessInstanceRegistry().findByProcessTask(getServiceContainer().getDAO(ProcessTaskRegistry.class).findByCode(code), pageSize, pageNo);
			} else {
				tmp = getProcessInstanceRegistry().findByProcessTask(getServiceContainer().getDAO(ProcessTaskRegistry.class).findByCode(code));
			}
			
			for (ProcessInstance pi : tmp)
				if (LighthouseAuthorizator.allowAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), getEntityContext(pi)))
					entities.add(pi);
			
			if (log.isDebugEnabled() && (entities != null))
				log.debug("Found  " + getEntityDAO().getManagedType().getSimpleName() + ": " + entities.size() + " instances");

		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving "
					+ getEntityDAO().getManagedType().getSimpleName(), exception);

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		if (entities == null) {
			if (log.isDebugEnabled())
				log.debug("Could not find " + getEntityDAO().getManagedType().getSimpleName() + " by ProcessTask#code " + code);

			return Response.status(Status.NOT_FOUND).entity("Could not find resource by ProcessTask#code " + code).build();
		}

		return Response.status(Status.OK).entity(createXmlListOfEntityReferences(entities)).build();
	}
	
	@GET
	@Path("after/{id}")
	@Produces("application/xml")
	public Response findAfterProcessInstance(@PathParam("id") String id, @Context UriInfo ui, @Context HttpServletRequest servletRequest) {
		int maxResults = -1;

		if (ui.getQueryParameters().containsKey("maxResults"))
			maxResults = Integer.parseInt(ui.getQueryParameters(true).get("maxResults").get(0));

		if (log.isDebugEnabled())
			log.debug("Retrieving " + getEntityDAO().getManagedType().getSimpleName() + " after instance#id " + id);
		
		List<ProcessInstance> entities = new LinkedList<ProcessInstance>();

		try {
			List<ProcessInstance> tmp = getProcessInstanceRegistry().findAfterInstance(getProcessInstanceRegistry().find(Long.parseLong(id)), maxResults);
			
			if (log.isDebugEnabled() && (entities != null))
				log.debug("Found  " + getEntityDAO().getManagedType().getSimpleName() + ": " + entities.size() + " instances");

			for (ProcessInstance pi : tmp)
				if (LighthouseAuthorizator.allowAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), getEntityContext(pi)))
					entities.add(pi);

		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving "
					+ getEntityDAO().getManagedType().getSimpleName(), exception);

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		if (entities == null) {
			if (log.isDebugEnabled())
				log.debug("Could not find " + getEntityDAO().getManagedType().getSimpleName() + " after ProcessInstance#id " + id);

			return Response.status(Status.NOT_FOUND).entity("Could not find " + getEntityDAO().getManagedType().getSimpleName() + " after ProcessInstance#id " + id).build();
		}

		return Response.status(Status.OK).entity(createXmlListOfEntityReferences(entities)).build();
	}

	@GET
	@Path("before/{id}")
	@Produces("application/xml")
	public Response findBeforeProcessInstance(@PathParam("id") String id, @Context UriInfo ui, @Context HttpServletRequest servletRequest) {
		int maxResults = -1;

		if (ui.getQueryParameters().containsKey("maxResults"))
			maxResults = Integer.parseInt(ui.getQueryParameters(true).get("maxResults").get(0));

		if (log.isDebugEnabled())
			log.debug("Retrieving " + getEntityDAO().getManagedType().getSimpleName() + " before instance#id " + id);
		List<ProcessInstance> entities = new LinkedList<ProcessInstance>();

		try {
			List<ProcessInstance> tmp = getProcessInstanceRegistry().findBeforeInstance(getProcessInstanceRegistry().find(Long.parseLong(id)), maxResults);

			if (tmp == null) {
				if (log.isDebugEnabled())
					log.debug("Could not find " + getEntityDAO().getManagedType().getSimpleName() + " before ProcessInstance#id " + id);

				return Response.status(Status.NOT_FOUND).entity("Could not find " + getEntityDAO().getManagedType().getSimpleName() + " before ProcessInstance#id " + id).build();
			}
			
			for (ProcessInstance pi : tmp)
				if (LighthouseAuthorizator.allowAccess(getServiceContainer(), servletRequest.getRemoteUser(), getContextRole(CtxOp.FIND), getEntityContext(pi)))
					entities.add(pi);

			if (log.isDebugEnabled())
				log.debug("Found  " + getEntityDAO().getManagedType().getSimpleName() + ": " + entities.size() + " instances");

		} catch (PersistenceException exception) {
			log.error("Persistence exception caught while retrieving "
					+ getEntityDAO().getManagedType().getSimpleName(), exception);

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		return Response.status(Status.OK).entity(createXmlListOfEntityReferences(entities)).build();
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

	@Override
	protected StringBuilder getEntityContext(ProcessInstance e) {
		return getContextString();
	}
}
