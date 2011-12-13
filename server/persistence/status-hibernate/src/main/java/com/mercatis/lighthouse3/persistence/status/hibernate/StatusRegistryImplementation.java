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
package com.mercatis.lighthouse3.persistence.status.hibernate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.type.LongType;

import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.domainmodel.status.ManualStatusClearance;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusHistogram;
import com.mercatis.lighthouse3.domainmodel.status.StatusRegistry;
import com.mercatis.lighthouse3.persistence.commons.hibernate.CodedDomainModelEntityDAOImplementation;

/**
 * This class provides a hibernate implementation of the status registry
 * interface.
 */
public class StatusRegistryImplementation extends CodedDomainModelEntityDAOImplementation<Status> implements
        StatusRegistry {

    /**
     * This property keeps a logger.
     */
    protected Logger log = Logger.getLogger(this.getClass());

    @Override
    protected Criteria entityToCriteria(Session session, Status entityTemplate) {
        Criteria criteria = super.entityToCriteria(session, entityTemplate);

        if (entityTemplate.getLongName() != null) {
            criteria.add(Restrictions.eq("longName", entityTemplate.getLongName()));
        }

        if (entityTemplate.getDescription() != null) {
            criteria.add(Restrictions.eq("description", entityTemplate.getDescription()));
        }

        if (entityTemplate.getContact() != null) {
            criteria.add(Restrictions.eq("contact", entityTemplate.getContact()));
        }

        if (entityTemplate.getContactEmail() != null) {
            criteria.add(Restrictions.eq("contactEmail", entityTemplate.getContactEmail()));
        }
        
        criteria.setCacheable(true);

        return criteria;
    }

    public void clearStatusManually(String code, String clearer, String reason) {
        Status status = this.findByCode(code);

        if (status == null) {
            throw new PersistenceException(
                    "Could not find status with code for clearance", null);
        }

        ManualStatusClearance clearance = new ManualStatusClearance();
        clearance.setClearer(clearer);
        clearance.setReason(reason);

        status.change(clearance);
        this.update(status);
    }

    public List<Status> getStatusForCarrier(StatusCarrier carrier,
            int pageSize, int pageNo) {
        List<Status> statusInDatabase = this.getStatusForCarrier(carrier);
        List<Status> paginatedStatuus = new ArrayList<Status>();

        for (Status status : statusInDatabase) {
        	Status paginatedStatus = status.createPaginatedStatus(pageSize, pageNo);
            paginatedStatuus.add(paginatedStatus);
        }

        return paginatedStatuus;
    }

    public Status findByCode(String code, int pageSize, int pageNo) {
        Status status = this.findByCode(code);

        if (status == null) {
            return null;
        } else {
        	Status paginatedStatus = status.createPaginatedStatus(pageSize, pageNo);
            return paginatedStatus;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Status> findAll() {
    	return this.unitOfWork.getCurrentSession().createQuery("from Status").setCacheable(true).list();
    }

    @SuppressWarnings("unchecked")
    public List<Status> getStatusForCarrier(StatusCarrier carrier) {
        List<Status> result = this.unitOfWork.getCurrentSession().createCriteria(Status.class).add(
                Restrictions.sqlRestriction(
                "{alias}.STA_CONTEXT_ID = ?", carrier.getId(),
                LongType.INSTANCE)).list();

        return result;
    }

    public StatusHistogram getAggregatedCurrentStatusForCarrier(StatusCarrier carrier) {
        return getAggregatedCurrentStatusForCarrier(carrier, false);
    }

    public StatusHistogram getAggregatedCurrentStatusForCarrier(StatusCarrier carrier, boolean withDeployments) {
    	
    	long queryTimestamp = System.currentTimeMillis();
    	
        if (log.isDebugEnabled()) {
            log.debug("aggregating carrier " + carrier.getPath() + " with" + (withDeployments ? "" : "out") + " deployments");
        }
        StatusHistogram result = new StatusHistogram();

        // add the given carrier and its subcarriers to the to-be-inspected lot
        Set<StatusCarrier> carriersToAggregate = new HashSet<StatusCarrier>();
        carriersToAggregate.add(carrier);
        carriersToAggregate.addAll(carrier.getSubCarriers());
        
        // add the associated deployments, if wished for
        if (withDeployments && carrier instanceof DeploymentCarryingDomainModelEntity) {
			carriersToAggregate.addAll(((DeploymentCarryingDomainModelEntity<?>) carrier).getAllDeployments());
        }
        
        // get all status' of the to-be-inspected entities
        Set<Status> statusSet = new HashSet<Status>();
        for (StatusCarrier statusCarrier : carriersToAggregate) {
			statusSet.addAll(this.getStatusForCarrier(statusCarrier));
		}
        
        for (Status status : statusSet) {
        	if (status.isEnabled()) {
	        	if (status.isOk()) {
	        		result.incOk();
	        	} else if (status.isError()) {
	        		result.incError();
	        	} else if (status.isStale()) {
	        		result.incStale();
	        	} else {
	        		result.incNone();
	        	}
        	}
		}
        
        if (log.isDebugEnabled()) {
        	log.debug("select all newStatus query took " + (System.currentTimeMillis() - queryTimestamp) + " ms.");
        }
        
        return result;
    }

    @Override
    public void delete(Status statusToDelete) {
    	Status status = this.findByCode(statusToDelete.getCode());
    	if (status == null) {
    		throw new PersistenceException("Entity not persistent", null);
    	}
    	Session session = unitOfWork.getCurrentSession();

        try {
        	// null potential foreign key references
        	session.createSQLQuery("delete from STATUS_METADATA where MD_STATUS_ID = :status_id").setParameter("status_id", status.getId()).executeUpdate();
        	session.createSQLQuery("update STATUS set LATEST_STATUS_CHANGE = null where STA_ID = :id").setLong("id", status.getId()).executeUpdate();
        	session.createSQLQuery("update STATUS_CHANGES set NEXT_STATUS_CHANGE = null where SCH_STATUS_ID = :id").setLong("id", status.getId()).executeUpdate();
        	session.createSQLQuery("update STATUS_CHANGES set PREVIOUS_STATUS_CHANGE = null where SCH_STATUS_ID = :id").setLong("id", status.getId()).executeUpdate();
        	
        	session.createSQLQuery("delete from STATUS_CHANGES where SCH_STATUS_ID = :id").setLong("id", status.getId()).executeUpdate();
        	session.createSQLQuery("delete from STATUS_NOTIFICATION_CHANNELS where SNC_STATUS_ID = :id").setLong("id", status.getId()).executeUpdate();
        	session.createSQLQuery("delete from STATUS where STA_ID = :id").setLong("id", status.getId()).executeUpdate();
        } catch (Exception e) {
            throw new PersistenceException("Deletion of status failed", e);
        }

        session.evict(status);
    }

    @Override
    public Status findByCode(String code) {
        Status status = super.findByCode(code);
        if ((status != null)
                && (this.unitOfWork.getSqlDialect() instanceof HSQLDialect)) {
            this.unitOfWork.getCurrentSession().refresh(status.getContext());
        }
        return status;
    }

    @Override
    public Status find(long id) {
        Status status = super.find(id);
        if ((status != null)
                && (this.unitOfWork.getSqlDialect() instanceof HSQLDialect)) {
            this.unitOfWork.getCurrentSession().refresh(status.getContext());
        }
        return status;
    }

    public <T extends StatusCarrier> Map<String, StatusHistogram> getAggregatedCurrentStatusesForCarrierClass(Class<T> carrierClass) {
        return getAggregatedCurrentStatusesForCarrierClass(carrierClass, false);
    }

    @SuppressWarnings("unchecked")
	public <T extends StatusCarrier> Map<String, StatusHistogram> getAggregatedCurrentStatusesForCarrierClass(Class<T> carrierClass, boolean withDeployments) {
        Map<String, StatusHistogram> result = new HashMap<String, StatusHistogram>();
        List<T> carriers = (List<T>)this.unitOfWork.getCurrentSession().createCriteria(carrierClass).setCacheable(true).list();
        Method codeFinder = getCodeFinder(carrierClass);
        for (T carrier : carriers) {
            result.put(getCode(codeFinder, carrier), getAggregatedCurrentStatusForCarrier(carrier, withDeployments));
        }
        return result;
    }

    private String getCode(Method codeFinder, Object arg) {
        String code = "";
        if (codeFinder != null) {
            try {
                code = (String) codeFinder.invoke(this, arg);
            } catch (IllegalAccessException ex) {
                java.util.logging.Logger.getLogger(StatusRegistryImplementation.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                java.util.logging.Logger.getLogger(StatusRegistryImplementation.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                java.util.logging.Logger.getLogger(StatusRegistryImplementation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return code;
    }

    @SuppressWarnings("unused")
    private String getCodeForDeployment(Deployment deployment) {
        return deployment.getDeployedComponent().getCode() + " @ " + deployment.getLocation();
    }

    @SuppressWarnings("unused")
    private String getCodeForDomainModelEntity(CodedDomainModelEntity entity) {
        return entity.getCode();
    }

    @SuppressWarnings("rawtypes")
	private Method getCodeFinder(Class carrierClass) {
        try {
            if (CodedDomainModelEntity.class.isAssignableFrom(carrierClass)) {
                return this.getClass().getDeclaredMethod("getCodeForDomainModelEntity", CodedDomainModelEntity.class);
            } else if (Deployment.class.isAssignableFrom(carrierClass)) {
                return this.getClass().getDeclaredMethod("getCodeForDeployment", Deployment.class);
            }
        } catch (NoSuchMethodException ex) {
            log.error(ex.getMessage(), ex);
        } catch (SecurityException ex) {
        	log.error(ex.getMessage(), ex);
        }
        return null;
    }
}
