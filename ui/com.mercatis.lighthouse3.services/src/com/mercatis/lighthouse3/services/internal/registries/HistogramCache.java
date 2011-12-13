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
package com.mercatis.lighthouse3.services.internal.registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusChange;
import com.mercatis.lighthouse3.domainmodel.status.StatusHistogram;
import com.mercatis.lighthouse3.services.Services;

/**
 * A HistogramCache is used to store the current status aggregation of an object.
 * <br />The cache registers itself as {@link EventHandler} for status changes and computes further aggregation
 * without consulting the LH3 server.
 * 
 */
public class HistogramCache implements EventHandler {
	
	/**
	 * Needed to construct the right topcic to listen on.
	 */
	private static Map<Class<?>, String> statusPathPrefix = new HashMap<Class<?>, String>();
	static {
		statusPathPrefix.put(ProcessTask.class, "processtaskstatus");
		statusPathPrefix.put(Environment.class, "environmentstatus");
		statusPathPrefix.put(Deployment.class, "deploymentstatus");
		statusPathPrefix.put(Object.class, "Object");
	}

	/**
	 * The histogram containing the current aggregation.
	 */
	private StatusHistogram histogram;
	
	/**
	 * Expected start of the topic of the incoming events.
	 */
	private String topicPrefix = "com/mercatis/lighthouse3/event/";
	
	/**
	 * LDAP filter string to enshure that only matching events are computed.
	 */
	private String filter = "(|(type=statusChanged)(type=statusDirty))";
	
	/**
	 * The topics this {@link EventHandler} is listening.
	 */
	private List<String> topics = new ArrayList<String>();
	
	/**
	 * A disposed HistogramCache should not be used anymore.
	 */
	private boolean disposed = false;

	/**
	 * Creates a new HistogramCache and registers it as {@link EventHandler}. It's responsible to listen on
	 * status changes for the given object.
	 * 
	 * @param serverDomainKey
	 * @param object
	 * @param initialHistogram
	 */
	public HistogramCache(String serverDomainKey, Object object, StatusHistogram initialHistogram, boolean withDeployments) {
		this.histogram = initialHistogram;
		topicPrefix += serverDomainKey.hashCode() + "/";
		if (object instanceof HierarchicalDomainModelEntity<?>) {
			String path = "" + ((HierarchicalDomainModelEntity<?>) object).getCode().hashCode();
			HierarchicalDomainModelEntity<?> parent = ((HierarchicalDomainModelEntity<?>) object).getParentEntity();
			while (parent != null) {
				path = parent.getCode().hashCode() + "/" + path;
				parent = parent.getParentEntity();
			}
			path = getPathPrefixForObejct(object.getClass()) + "/" + path;
			topics.add(topicPrefix + path + "/*");
		} else if (object instanceof Deployment) {
			Deployment deployment = (Deployment) object;
			topics.add(topicPrefix + getPathPrefixForObejct(object.getClass()) + "/"
					+ deployment.getLocation().hashCode() + "/"
					+ deployment.getDeployedComponent().getCode().hashCode() + "/*");
		}
		if (object instanceof DeploymentCarryingDomainModelEntity<?> && withDeployments) {
			DeploymentCarryingDomainModelEntity<?> entity = (DeploymentCarryingDomainModelEntity<?>) object;
			for (Deployment deployment : entity.getAllDeployments()) {
				topics.add(topicPrefix + getPathPrefixForObejct(deployment.getClass()) +"/"
						+ deployment.getLocation().hashCode() + "/"
						+ deployment.getDeployedComponent().getCode().hashCode() + "/*");
			}
		}
		Services.registerEventHandler(this, topics.toArray(new String[0]), filter);
	}
	
	private String getPathPrefixForObejct(Class<?> clazz) {
		String prefix = statusPathPrefix.get(clazz);
		if (prefix == null) {
			prefix = getPathPrefixForObejct(clazz.getSuperclass());
		}
		return prefix;
	}

	/**
	 * Disposes this HistogramCache. The registered {@link EventHanlder} is unregistered.
	 */
	public void dispose() {
		Services.unregisterEventHandler(this);
		disposed = true;
	}

	/**
	 * Gets the current {@link StatusHistogram} that is kept up to date via status change events.
	 * 
	 * @return the current {@link StatusHistogram}
	 */
	public StatusHistogram getAggregatedStatusHistogram() {
		if (isDisposed())
			throw new IllegalStateException("HistogramCache was disposed");
		return histogram;
	}

	/**
	 * Check if this StatusHistogram is disposed.
	 * <br />In case of an aggregation failure, the HistogramCache can dispose itself.
	 * 
	 * @return true if this StatusHistogram should not be used anymore.
	 */
	public boolean isDisposed() {
		return disposed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event
	 * .Event)
	 */
	public void handleEvent(Event event) {
		String eventType = (String) event.getProperty("type");
		if (eventType != null && eventType.equals("statusDirty")) {
			this.dispose();
			return;
		}
		StatusChange statusChange = (StatusChange) event.getProperty("statusChange");
		StatusChange priorChange = (StatusChange) event.getProperty("priorChange");
		switch (statusChange.getNewStatus()) {
		case Status.ERROR:
			histogram.incError();
			break;
		case Status.STALE:
			histogram.incStale();
			break;
		case Status.OK:
			histogram.incOk();
			break;
		case Status.NONE:
			histogram.incNone();
			break;
		}
		if (priorChange != null) {
			switch (priorChange.getNewStatus()) {
			case Status.ERROR:
				histogram.setError(histogram.getError() - 1);
				break;
			case Status.STALE:
				histogram.setStale(histogram.getStale() - 1);
				break;
			case Status.OK:
				histogram.setOk(histogram.getOk() - 1);
				break;
			case Status.NONE:
				histogram.setNone(histogram.getNone() - 1);
				break;
			}
		}
		
		//In case of an aggregation failure, this HistogramCache should not be used anymore.
		if (histogram.getError() < 0 || histogram.getNone() < 0 || histogram.getOk() < 0 || histogram.getStale() < 0) {
			dispose();
		}
	}
}
