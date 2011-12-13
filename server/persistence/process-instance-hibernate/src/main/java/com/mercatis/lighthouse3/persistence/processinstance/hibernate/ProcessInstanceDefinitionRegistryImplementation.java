package com.mercatis.lighthouse3.persistence.processinstance.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinitionRegistry;
import com.mercatis.lighthouse3.persistence.commons.hibernate.CodedDomainModelEntityDAOImplementation;

public class ProcessInstanceDefinitionRegistryImplementation extends CodedDomainModelEntityDAOImplementation<ProcessInstanceDefinition> implements ProcessInstanceDefinitionRegistry {

	@SuppressWarnings("unchecked")
	public List<ProcessInstanceDefinition> findAll() {
		return unitOfWork.getCurrentSession().createQuery("from ProcessInstanceDefinition").list();
	}

	public ProcessInstanceDefinition findByProcessTask(ProcessTask processTask) {
		return (ProcessInstanceDefinition) unitOfWork.getCurrentSession().createQuery("from ProcessInstanceDefinition pid where pid.processTask = :processTask").setEntity("processTask", processTask).uniqueResult();
	}
	
	@Override
	protected Criteria entityToCriteria(Session session, ProcessInstanceDefinition entityTemplate) {
		Criteria criteria = super.entityToCriteria(session, entityTemplate);

		if (entityTemplate.getProcessTask() != null)
			criteria.add(Restrictions.eq("processTask", entityTemplate
					.getProcessTask()));

		return criteria;
	}
	
}
