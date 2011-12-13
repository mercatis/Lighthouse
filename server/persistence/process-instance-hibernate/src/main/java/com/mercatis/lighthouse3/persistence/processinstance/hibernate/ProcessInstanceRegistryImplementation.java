package com.mercatis.lighthouse3.persistence.processinstance.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstance;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceRegistry;
import com.mercatis.lighthouse3.persistence.commons.hibernate.DomainModelEntityDAOImplementation;

public class ProcessInstanceRegistryImplementation extends DomainModelEntityDAOImplementation<ProcessInstance> implements ProcessInstanceRegistry {

	@SuppressWarnings("unchecked")
	public List<ProcessInstance> findAll() {
		return unitOfWork.getCurrentSession().createQuery("from ProcessInstance").list();
	}

	@SuppressWarnings("unchecked")
	public List<ProcessInstance> findByProcessInstanceDefinition(ProcessInstanceDefinition processInstanceDefinition) {
		return unitOfWork.getCurrentSession().createQuery("from ProcessInstance pi where pi.processInstanceDefinition = :processInstanceDefinition").setEntity("processInstanceDefinition", processInstanceDefinition).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessInstance> findByProcessTask(ProcessTask processTask) {
		return unitOfWork.getCurrentSession().createQuery("from ProcessInstance pi where pi.processInstanceDefinition.processTask = :processTask").setEntity("processTask", processTask).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessInstance> findByProcessTask(ProcessTask processTask, int pageSize, int pageNo) {
                int limit = pageSize;
                int offset = limit * pageNo;
		return unitOfWork.getCurrentSession().createQuery("from ProcessInstance pi where pi.processInstanceDefinition.processTask = :processTask order by PI_START desc")
                        .setEntity("processTask", processTask)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .list();
	}

	@Override
	protected Criteria entityToCriteria(Session session,
			ProcessInstance entityTemplate) {
		Criteria criteria = super.entityToCriteria(session, entityTemplate);
		
		if (entityTemplate.getProcessInstanceDefinition() != null) {
			criteria.add(Restrictions.eq("processInstanceDefinition", entityTemplate.getProcessInstanceDefinition()));
		}
		
		if (entityTemplate.getStartDate() != null) {
			criteria.add(Restrictions.ge("startDate", entityTemplate.getStartDate()));
		}
		
		if (entityTemplate.getEndDate() != null) {
			criteria.add(Restrictions.le("endDate", entityTemplate.getEndDate()));
		}
		
		if (entityTemplate.isErroneous() == true) {
			criteria.add(Restrictions.eq("erroneous", Boolean.TRUE));
		}
		
		if (entityTemplate.isClosed() == true) {
			criteria.add(Restrictions.eq("closed", Boolean.TRUE));
		}
		
		return criteria;
	}

    @SuppressWarnings("unchecked")
	public List<ProcessInstance> findAfterInstance(ProcessInstance instance, int maxResults) {
		return unitOfWork.getCurrentSession()
                        .createQuery("from ProcessInstance pi where pi.startDate > :startDate order by PI_START")
                        .setDate("startDate", instance.getStartDate())
                        .setMaxResults(maxResults)
                        .list();
    }

    @SuppressWarnings("unchecked")
	public List<ProcessInstance> findBeforeInstance(ProcessInstance instance, int maxResults) {
		return unitOfWork.getCurrentSession()
                        .createQuery("from ProcessInstance pi where pi.startDate < :startDate order by PI_START desc")
                        .setDate("startDate", instance.getStartDate())
                        .setMaxResults(maxResults)
                        .list();
    }
}
