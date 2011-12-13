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
package com.mercatis.lighthouse3.domainmodel.events;


import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.events.aggregation.Aggregation;
import com.mercatis.lighthouse3.domainmodel.events.aggregation.AggregationCommand;

/**
 * The event registry interface abstractly defines the contract for a persistent
 * repository of events. It provides basic CRUD functionality. It can be
 * implemented using different persistence technologies.
 * 
 * With regard to persistence, the following rules must apply for all
 * implementations:
 * 
 * <ul>
 * <li>events are uniquely identified by their <code>id</code>.
 * <li>events are always fully loaded including the deployment which is the
 * context of the event, their assigned tags, transaction IDs, and UDFs. The
 * rules for loading deployments apply here as well.
 * <li>events are persisted along with their assigned tags, transaction IDs, and
 * UDFs.
 * </ul>
 * 
 * Events in general can best be queried using the <code>findByTemplate</code>
 * query method. With regard to templates, the following matching rules must be
 * supported:
 * 
 * In general, if a property in the template is set to <code>null</code>, it is
 * not of interest for the match. If it is not <code>null</code> a matching
 * event must have the property set to an equal value.
 * 
 * The following event properties receive special treatment:
 * <ul>
 * <li><code>context</code>: the template may use an enumeration range in order
 * to look for events from different deployments at the same time.
 * <li><code>code</code>: the template may use an enumeration range in order to
 * look for events with different codes at the same time.
 * <li><code>level</code>: the template may use an enumeration range in order to
 * look for events with different levels at the same time.
 * <li><code>dateOfOccurrence</code>: the template may use an interval range in
 * order to look for events within a given time interval.
 * <li><code>transactionIds</code>: if the template does not set a transaction
 * Id, transaction IDs are ignored for the matching. Otherwise, one of the
 * transaction IDs of the template must be found with each matching event.
 * <li><code>message</code>: if the template does not set a message, the message
 * is ignored for the matching. Otherwise, the message of the template must be
 * contained in the message of any matching event.
 * <li><code>stackTrace</code>: if the template does not set a stack trace, the
 * trace is ignored for the matching. Otherwise, the trace of the template must
 * be contained in the stack trace of any matching event.
 * <li><code>tags</code>: if the template does not set any tags, tags are
 * ignored for the matching. Otherwise, all tags of the template must be found
 * with each matching event.
 * <li><code>udfs</code>: if the template does not set any UDFs, UDFs are
 * ignored for the matching. Otherwise, all UDFs of the template must be found
 * with each matching event and carry the same value.
 * </ul>
 */
public interface EventRegistry extends DomainModelEntityDAO<Event> {

	/**
	 * This method is a synonym for <code>persist()</code>.
	 */
	public void log(Event event);

	/**
	 * This method logs a given event within a given context. I.e., a deployment
	 * is passed that overrides any potentially existing deployment referenced
	 * as the events context property. It is possible to leave the event's
	 * context property set to <code>code</code> when calling this method.
	 * 
	 * @param context
	 *            the deployment to use as the event context, overriding any
	 *            other context referenced by the event.
	 * @param event
	 *            the event to log.
	 * @throws PersistenceException
	 *             in case that the event could not be persisted.
	 */
	public void log(Deployment context, Event event);

	/**
	 * This method persists a given event within a given context. The event is
	 * given as the component name and deployment location of the software
	 * component issuing the event. The thusly given deployment overrides any
	 * potentially existing deployment referenced as the event's context
	 * property. It is possible to leave the event's context property set to
	 * <code>code</code> when calling this method.
	 * 
	 * @param deploymentRegistry
	 *            the deployment registry to use in order to resolve the
	 *            deployment described by the deployment location and software
	 *            component code.
	 * @param softwareComponentRegistry
	 *            the softwareComponent registry to use in order to resolve the
	 *            software component involved in the deployment.
	 * @param location
	 *            the deployment location of the deployment to serve as the
	 *            event context.
	 * @param componentCode
	 *            the component code of the deployed software component.
	 * @param event
	 *            the event to log.
	 * @throws PersistenceException
	 *             in case that the event could not be persisted or the
	 *             described deployment could not be found.
	 */
	public void log(DeploymentRegistry deploymentRegistry, SoftwareComponentRegistry softwareComponentRegistry,
			String location, String componentCode, Event event);

	/**
	 * This method returns an aggregation over a list of events. The list of
	 * events is defined by a template. The aggregation provides grouping to a
	 * certain time interval and to a certain attribute of the
	 * <code>Event</code> class at the same time. Grouping the events to the
	 * Group type EVENTS will create one single group called default. The
	 * aggregation functions to be used are Count, Sum, Average, Maximum and
	 * Minimum.
	 * 
	 * @param command
	 *            the detailed command description
	 * @throws AggregationException
	 *             in case the target type is not supported by a certain
	 *             function or the target itself does not exist.
	 * @return the iterable result of the aggregation
	 */
	public Aggregation aggregate(AggregationCommand command);
	
	public Deployment[] getAllDeployments();
	
	public String[] getAllUdfNames();
}
