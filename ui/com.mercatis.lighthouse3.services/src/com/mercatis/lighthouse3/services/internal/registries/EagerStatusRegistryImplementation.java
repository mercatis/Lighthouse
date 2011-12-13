/**
 * (c) Copyright 2010 mercatis technologies AG
 *
 * All rights reserved.
 *
 * Part of Lighthouse 3
 *
 * This source code is proprietary trade secret information of
 * mercatis information systems GmbH. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * mercatis information systems GmbH.
 */
package com.mercatis.lighthouse3.services.internal.registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusHistogram;
import com.mercatis.lighthouse3.domainmodel.status.StatusRegistry;
import com.mercatis.lighthouse3.persistence.status.rest.StatusRegistryImplementation;


public class EagerStatusRegistryImplementation extends EagerCodedDomainModelEntityDAOImplementation<Status> implements StatusRegistry {

	/**
	 * Maps a {@link HistogramCache} to an object.
	 */
	private volatile Map<String, HistogramCache> histogramCachesWithDeployments = new HashMap<String, HistogramCache>();
	
	private volatile Map<String, HistogramCache> histogramCachesWithoutDeployments = new HashMap<String, HistogramCache>();
	
	private StatusRegistryImplementation delegateRegistry = null;
	
	protected Map<Class<? extends StatusCarrier>, Map<String, StatusHistogram>> aggregationCacheWithDeployments = new HashMap<Class<? extends StatusCarrier>, Map<String, StatusHistogram>>();
	
	protected Map<Class<? extends StatusCarrier>, Map<String, StatusHistogram>> aggregationCacheWithoutDeployments = new HashMap<Class<? extends StatusCarrier>, Map<String, StatusHistogram>>();
	
	private EnvironmentRegistry environmentRegistry;
	private ProcessTaskRegistry processTaskRegistry;
	private SoftwareComponentRegistry softwareComponentRegistry;
	private DeploymentRegistry deploymentregistry;
	
	public EagerStatusRegistryImplementation(String serverUrl, EnvironmentRegistry environmentRegistry, ProcessTaskRegistry processTaskRegistry, DeploymentRegistry deploymentRegistry, SoftwareComponentRegistry softwareComponentRegistry, EventRegistry eventRegistry, String user, String password) {
		this.delegateRegistry = new StatusRegistryImplementation(serverUrl, environmentRegistry, processTaskRegistry, deploymentRegistry, softwareComponentRegistry, eventRegistry, user, password);
		this.environmentRegistry = environmentRegistry;
		this.processTaskRegistry = processTaskRegistry;
		this.softwareComponentRegistry = softwareComponentRegistry;
		this.deploymentregistry = deploymentRegistry;
		this.invalidate();
	}
	
	public EagerStatusRegistryImplementation(String serverUrl, EnvironmentRegistry environmentRegistry, ProcessTaskRegistry processTaskRegistry, DeploymentRegistry deploymentRegistry, SoftwareComponentRegistry softwareComponentRegistry, EventRegistry eventRegistry) {
		this(serverUrl, environmentRegistry, processTaskRegistry, deploymentRegistry, softwareComponentRegistry, eventRegistry, null, null);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.status.StatusRegistry#clearStatusManually(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void clearStatusManually(String code, String clearer, String reason) {
		delegateRegistry.clearStatusManually(code, clearer, reason);
		this.cache.remove(code);
		Status s = delegateRegistry.findByCode(code);
		if (s != null)
			this.cache.put(code, s);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries2.EagerDomainModelEntityDAOImplementation#delegateRegistry()
	 */
	@Override
	protected DomainModelEntityDAO<Status> delegateRegistry() {
		return delegateRegistry;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.status.StatusRegistry#findByCode(java.lang.String, int, int)
	 */
	public Status findByCode(String code, int pageSize, int pageNo) {
		return delegateRegistry.findByCode(code).createPaginatedStatus(pageSize, pageNo);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.status.StatusRegistry#getAggregatedCurrentStatusesForCarrierClass(java.lang.Class)
	 */
	public <T extends StatusCarrier> Map<String, StatusHistogram> getAggregatedCurrentStatusesForCarrierClass(Class<T> carrierClass) {
		return getAggregatedCurrentStatusesForCarrierClass(carrierClass, false);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.status.StatusRegistry#getAggregatedCurrentStatusesForCarrierClass(java.lang.Class, boolean)
	 */
	public <T extends StatusCarrier> Map<String, StatusHistogram> getAggregatedCurrentStatusesForCarrierClass(Class<T> carrierClass, boolean withDeployments) {
		if (withDeployments)
			return aggregationCacheWithDeployments.get(carrierClass);
		else
			return aggregationCacheWithoutDeployments.get(carrierClass);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.status.StatusRegistry#getAggregatedCurrentStatusForCarrier(com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier)
	 */
	public StatusHistogram getAggregatedCurrentStatusForCarrier(StatusCarrier carrier) {
		return getAggregatedCurrentStatusForCarrier(carrier, false);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.status.StatusRegistry#getAggregatedCurrentStatusForCarrier(com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier, boolean)
	 */
	public StatusHistogram getAggregatedCurrentStatusForCarrier(StatusCarrier carrier, boolean withDeployments) {
		HistogramCache cache;
		DomainModelEntity entity = (DomainModelEntity) carrier;
		if (withDeployments) {
			cache = histogramCachesWithDeployments.get(keyForCarrier(carrier));
			if (cache == null) {
				StatusHistogram initialHistogram = delegateRegistry.getAggregatedCurrentStatusForCarrier(carrier, withDeployments);
				cache = new HistogramCache(entity.getLighthouseDomain(), carrier, initialHistogram, withDeployments);
				histogramCachesWithDeployments.put(keyForCarrier(carrier), cache);
			}
		} else {
			cache = histogramCachesWithoutDeployments.get(keyForCarrier(carrier));
			if (cache == null) {
				StatusHistogram initialHistogram = delegateRegistry.getAggregatedCurrentStatusForCarrier(carrier, withDeployments);
				cache = new HistogramCache(entity.getLighthouseDomain(), carrier, initialHistogram, withDeployments);
				histogramCachesWithoutDeployments.put(keyForCarrier(carrier), cache);
			}
		}
		StatusHistogram histogram = null;
		if (cache.isDisposed()) {
			histogram = delegateRegistry.getAggregatedCurrentStatusForCarrier(carrier, withDeployments);
			cache = new HistogramCache(entity.getLighthouseDomain(), carrier, histogram, withDeployments);
			if (withDeployments) {
				histogramCachesWithDeployments.put(keyForCarrier(carrier), cache);
			} else {
				histogramCachesWithoutDeployments.put(keyForCarrier(carrier), cache);
			}
		}
		return cache.getAggregatedStatusHistogram();
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#getManagedType()
	 */
	@SuppressWarnings("rawtypes")
	public Class getManagedType() {
		return Status.class;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.status.StatusRegistry#getStatusForCarrier(com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier)
	 */
	public List<Status> getStatusForCarrier(StatusCarrier carrier) {
		List<Status> result = new ArrayList<Status>();
		
		for (Status status : cache.values()) {
			if (status.getContext().equals(carrier)) {
				result.add(status);
			}
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.status.StatusRegistry#getStatusForCarrier(com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier, int, int)
	 */
	public List<Status> getStatusForCarrier(StatusCarrier carrier, int pageSize, int pageNo) {
		List<Status> result = new ArrayList<Status>();
		
		for (Status status : cache.values()) {
			if (status.getContext().equals(carrier)) {
				result.add(status.createPaginatedStatus(pageSize, pageNo));
			}
		}
		
		return result;
	}

	@Override
	public void invalidate() {
		this.cache.clear();
		List<Status> entities = ((StatusRegistryImplementation) delegateRegistry()).findAll(0,0);
    	for (Status entity: entities) {
    		if (entity != null)
    			this.cache.put(keyForEntity(entity), entity);
		}
		
    	for (HistogramCache cache : histogramCachesWithDeployments.values()) {
    		cache.dispose();
    	}
    	histogramCachesWithDeployments.clear();
    	for (HistogramCache cache : histogramCachesWithoutDeployments.values()) {
    		cache.dispose();
    	}
    	histogramCachesWithoutDeployments.clear();
		
    	histogramCachesWithoutDeployments.putAll(getHistogramCachForCarrierClass(Environment.class, false));
    	histogramCachesWithoutDeployments.putAll(getHistogramCachForCarrierClass(ProcessTask.class, false));
    	histogramCachesWithoutDeployments.putAll(getHistogramCachForCarrierClass(Deployment.class, false));

    	histogramCachesWithDeployments.putAll(getHistogramCachForCarrierClass(Environment.class, true));
    	histogramCachesWithDeployments.putAll(getHistogramCachForCarrierClass(ProcessTask.class, true));
    	histogramCachesWithDeployments.putAll(getHistogramCachForCarrierClass(Deployment.class, true));
	}
	
	private Map<String, HistogramCache> getHistogramCachForCarrierClass(Class<? extends StatusCarrier> clazz, boolean withDeployments) {
		Map<String, HistogramCache> caches = new HashMap<String, HistogramCache>();
		if (clazz.equals(Environment.class)) {
			for (Entry<String, StatusHistogram> entry : delegateRegistry.getAggregatedCurrentStatusesForCarrierClass(clazz, withDeployments).entrySet()) {
				Environment object = environmentRegistry.findByCode(entry.getKey());
				if (object != null) {
					HistogramCache cache = new HistogramCache(object.getLighthouseDomain(), object, entry.getValue(), withDeployments);
					caches.put(entry.getKey(), cache);
				}
			}
		} else if (clazz.equals(ProcessTask.class)) {
			for (Entry<String, StatusHistogram> entry : delegateRegistry.getAggregatedCurrentStatusesForCarrierClass(clazz, withDeployments).entrySet()) {
				ProcessTask object = processTaskRegistry.findByCode(entry.getKey());
				if (object != null) {
					HistogramCache cache = new HistogramCache(object.getLighthouseDomain(), object, entry.getValue(), withDeployments);
					caches.put(entry.getKey(), cache);
				}
			}
		} else if (clazz.equals(Deployment.class)) {
			for (Entry<String, StatusHistogram> entry : delegateRegistry.getAggregatedCurrentStatusesForCarrierClass(clazz, withDeployments).entrySet()) {
				SoftwareComponent sc = getSoftwareComponentFromDeploymentCode(entry.getKey());
				String loc = getLocationFromDeploymentCode(entry.getKey());
				if (sc != null && loc != null) {
					Deployment object = deploymentregistry.findByComponentAndLocation(sc, loc);
					if (object != null) {
						HistogramCache cache = new HistogramCache(object.getLighthouseDomain(), object, entry.getValue(), withDeployments);
						caches.put(entry.getKey(), cache);
					}
				}
			}
		}
		return caches;
	}
	
	protected String keyForCarrier(StatusCarrier carrier) {
		if (carrier instanceof Deployment) {
			return ((Deployment) carrier).getDeployedComponent().getCode() + " @ " + ((Deployment) carrier).getLocation();
		}
		if (carrier instanceof CodedDomainModelEntity) {
			return carrier.getClass().getSimpleName() + ":" + ((CodedDomainModelEntity) carrier).getCode();
		}
		
		throw new IllegalArgumentException("Unknown StatusCarrier Class");
	}
	
	private String getLocationFromDeploymentCode(String code) {
		return  code.substring(code.indexOf("@") + 1).trim();
	}
	
	private SoftwareComponent getSoftwareComponentFromDeploymentCode(String code) {
		String componentCode = code.substring(0, code.indexOf("@") - 1).trim();
		return softwareComponentRegistry.findByCode(componentCode);
	}
}
