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
package com.mercatis.lighthouse3.service.eventlogger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.mercatis.lighthouse3.commons.commons.Ranger;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.commons.messaging.JmsConnection;
import com.mercatis.lighthouse3.commons.messaging.JmsMessageCreator;
import com.mercatis.lighthouse3.commons.messaging.JmsProvider;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.commons.UnitOfWork;
import com.mercatis.lighthouse3.domainmodel.commons.UnknownContextException;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.service.eventlogger.jms.LiveEventFilterProcessor;
import com.mercatis.lighthouse3.service.eventlogger.rest.EventLoggerServiceContainer;

/**
 * This class implements the event logging service. The event logging service is
 * responsible for receiving logged events, persisting them in the database, and
 * issue notifications to interested clients about events of interest. Interest
 * can be expressed via event filters based on templates.
 */
public class EventLoggerService {
	/**
	 * This property keeps a logger.
	 */
	protected Logger log = Logger.getLogger(this.getClass());

	/**
	 * This property maintains a reference to the thread pool used for event
	 * filter registration
	 */
	private Executor eventFilterRegistrationThreadPool = null;
	
	private boolean sanitizeUDFs = true;

	/**
	 * This private class provides the thread action for registering event
	 * filters.
	 */
	private class EventFilterRegistrationHandler implements Runnable {

		private EventLoggerService eventLoggerService = null;

		private EventFilter eventFilter = null;

		private boolean isStillActive = true;
		
//		private int limit;
		
		private UnitOfWork unitOfWork;

		private EventRegistry hibernateEventRegistry;
		
		public void cancelRegistration() {
			this.isStillActive = false;
		}

		public boolean registrationHasBeenCanceled() {
			return !this.isStillActive;
		}

		/**
		 * Perform event filter registration.
		 */
		public void run() {
			if (log.isDebugEnabled()) {
				log.debug("Registering event filter " + this.eventFilter.toString());
				log.debug("Looking up matching past events in registry");
			}
			hibernateEventRegistry = (EventRegistry) getEventRegistry();
			unitOfWork = hibernateEventRegistry.getUnitOfWork();

			List<Event> matchesInRegistry = hibernateEventRegistry .findByTemplate(this.eventFilter.getTemplate());

			if (log.isDebugEnabled())
				log.debug("Matching past events found in registry, publishing them");

			List<Event> pastEventNotificationBatch = new ArrayList<Event>();

			for (Event match : matchesInRegistry) {
				if (!this.registrationHasBeenCanceled()) {
					pastEventNotificationBatch.add(match);

					if (pastEventNotificationBatch.size() % getPastEventNotificationBatchSize() == 0) {
						this.eventLoggerService.getEventFilterMatchHandler().handleMatches(this.eventFilter,
								pastEventNotificationBatch);
						pastEventNotificationBatch.clear();
					}
				} else {
					if (log.isDebugEnabled())
						log.debug("Event filter has been deregistered. Stopping processing");

					unitOfWork.rollback();
					return;
				}
			}

			if (!pastEventNotificationBatch.isEmpty())
				this.eventLoggerService.getEventFilterMatchHandler().handleMatches(this.eventFilter,
						pastEventNotificationBatch);

			this.eventLoggerService.getEventRegistry().getUnitOfWork().rollback();

			if (log.isDebugEnabled())
				log.debug("Matching past events published, now registering filter for live events");

			boolean performedLiveEventFilterRegistration = false;

			if (!this.registrationHasBeenCanceled()) {
				this.eventLoggerService.registerFilterForLiveEvents(this.eventFilter);
				performedLiveEventFilterRegistration = true;
			} else {
				if (log.isDebugEnabled())
					log.debug("Event filter has been deregistered. Not registering for live events anymore.");
				unitOfWork.rollback();
				return;
			}

			if (this.registrationHasBeenCanceled() && performedLiveEventFilterRegistration) {
				if (log.isDebugEnabled())
					log
							.debug("Event filter has been deregistered while registering for live events. Deregistering again for safety reasons.");

				this.eventLoggerService.deregisterEventFilter(this.eventFilter.getUuid());
			}

			this.eventLoggerService.handlerFinishedEventRegistration(this.eventFilter.getUuid());

			if (log.isDebugEnabled())
				log.debug("Event filter registered for live events.");
			unitOfWork.rollback();
		}

		public EventFilterRegistrationHandler(EventLoggerService eventLoggerService, EventFilter eventFilter, int limit) {
			this.eventLoggerService = eventLoggerService;
			this.eventFilter = eventFilter;
		}
}

	/**
	 * The software component registry used by the service.
	 */
	private SoftwareComponentRegistry softwareComponentRegistry = null;

	/**
	 * Returns the software component registry used by the service.
	 * 
	 * @return the software component registry.
	 */
	public SoftwareComponentRegistry getSoftwareComponentRegistry() {
		return this.softwareComponentRegistry;
	}

	/**
	 * The deployment registry used by the service.
	 */
	private DeploymentRegistry deploymentRegistry = null;

	/**
	 * Returns the deployment registry used by the service.
	 * 
	 * @return the deployment registry.
	 */
	public DeploymentRegistry getDeploymentRegistry() {
		return this.deploymentRegistry;
	}

	/**
	 * The event registry used by the service.
	 */
	private EventRegistry eventRegistry = null;

	/**
	 * Returns the event registry used by the service.
	 * 
	 * @return the event registry.
	 */
	public EventRegistry getEventRegistry() {
		return this.eventRegistry;
	}

	/**
	 * This property maintains a reference to the handler that deals with events
	 * matched against an event filter.
	 */
	private EventFilterMatchHandler eventFilterMatchHandler = null;

	/**
	 * This method returns the event filter match handler to use when filters
	 * match live events.
	 */
	public EventFilterMatchHandler getEventFilterMatchHandler() {
		return this.eventFilterMatchHandler;
	}

	/**
	 * This property keeps the batch size to use for past event notification.
	 */
	private int pastEventNotificationBatchSize = 1000;

	/**
	 * This method returns the batch size to use for past event notification.
	 * 
	 * @return the batch size
	 */
	public int getPastEventNotificationBatchSize() {
		return this.pastEventNotificationBatchSize;
	}

	/**
	 * This property maintains the expiry interval for event filters
	 */
	private long eventFilterExpiryIntervalInMsecs = 0l;

	/**
	 * This method returns the expiry interval for event filters
	 * 
	 * @return the interval
	 */
	public long getEventFilterExpiryIntervalInMsecs() {
		return this.eventFilterExpiryIntervalInMsecs;
	}

	/**
	 * This property keeps the set of event filters registered by clients,
	 * indexed by their uuid.
	 */
	private Map<String, EventFilter> eventFiltersByUuid = new HashMap<String, EventFilter>();

	/**
	 * This property keeps a reference to the live event filter matcher, that
	 * listens to logged live events and checks which filters are matching.
	 */
	private JmsConnection liveEventFilterMatcher = null;

	/**
	 * This property keeps a registry of currently active event filter
	 * registration handler threads for possible cancelation.
	 */
	private Map<String, EventFilterRegistrationHandler> activeEventFilterRegistrationHandlers = new HashMap<String, EventFilterRegistrationHandler>();

	private final int EVENT_SIZE_LIMIT;

	/**
	 * This method creates an event filter registration handler
	 * 
	 * @param eventFilter
	 *            the event filter for which to create the registration handler
	 * @return the created registration handler for the filter
	 */
	synchronized public EventFilterRegistrationHandler createEventFilterRegistrationHandler(EventFilter eventFilter, int limit) {
		EventFilterRegistrationHandler registrationHandler = new EventFilterRegistrationHandler(this, eventFilter, limit);

		this.activeEventFilterRegistrationHandlers.put(eventFilter.getUuid(), registrationHandler);

		return registrationHandler;
	}

	/**
	 * This method notifies the event logger service that an event filter
	 * registration handler has successfully done its work.
	 * 
	 * @param eventFilterUuid
	 *            the UUID of the event filter registered by the handler
	 * @return the event filter registration handler that completed the
	 *         registration, or <code>null</code> if the handler is no longer
	 *         active
	 */
	synchronized EventFilterRegistrationHandler handlerFinishedEventRegistration(String eventFilterUuid) {
		EventFilterRegistrationHandler eventFilterRegistrationHandler = this.activeEventFilterRegistrationHandlers
				.get(eventFilterUuid);

		this.activeEventFilterRegistrationHandlers.remove(eventFilterUuid);

		return eventFilterRegistrationHandler;
	}

	/**
	 * Via this method, event filters are registered with the event logger
	 * service for live notification about events of interest.
	 * 
	 * @param filter
	 *            the filter to register
	 */
	synchronized public void registerFilterForLiveEvents(EventFilter filter) {
		if (log.isDebugEnabled())
			log.debug("Registering event filter for live events: " + filter.getUuid());

		// hibernate proxy prefetch workaround
		if (filter.getTemplate().getContext() != null) {
			if (Ranger.isEnumerationRange(filter.getTemplate().getContext())) {
				for (Deployment d : Ranger.castToEnumerationRange(filter.getTemplate().getContext()).getEnumeration()) {
					d.getDeployedComponent().getCode();
				}
			} else {
				filter.getTemplate().getContext().getDeployedComponent().getCode();
			}
		}
		this.eventFiltersByUuid.put(filter.getUuid(), filter);

		if (log.isDebugEnabled())
			log.debug("Event filter for live events registered.");
	}

	/**
	 * Via this method, event filters are registered with the event logger
	 * service for live notification about events of interest. Before this
	 * happens though, the template in the filter is used to query the event
	 * registry. Matching events in this registry are sent to the client first
	 * before live event notification is established via
	 * <code>doRegisterEventFilter()</code>.
	 * 
	 * @param filter
	 *            the event filter to register
	 */
	public void registerEventFilter(EventFilter filter, int limit) {
		if (log.isDebugEnabled())
			log.debug("Passing event filter on to registration: " + filter.toString());
		
		// hibernate proxy prefetch workaround
		if (filter.getTemplate().getContext() != null) {
			if (Ranger.isEnumerationRange(filter.getTemplate().getContext())) {
				for (Deployment d : Ranger.castToEnumerationRange(filter.getTemplate().getContext()).getEnumeration()) {
					d.getDeployedComponent().getCode();
				}
			} else {
				filter.getTemplate().getContext().getDeployedComponent().getCode();
			}
		}
		EventFilterRegistrationHandler eventFilterRegistrationHandler = this
				.createEventFilterRegistrationHandler(filter, limit);

		this.eventFilterRegistrationThreadPool.execute(eventFilterRegistrationHandler);
	}

	/**
	 * Via this method, clients can deregister event filters in order to be no
	 * longer notified about events of interest.
	 * 
	 * @param uuid
	 *            the uuid of the filter to unregister
	 */
	synchronized public void deregisterEventFilter(String uuid) {
		if (log.isDebugEnabled())
			log.debug("Trying to deregister event filter with UUID " + uuid);

		EventFilterRegistrationHandler registrationHandler = this.handlerFinishedEventRegistration(uuid);
		if (registrationHandler != null)
			registrationHandler.cancelRegistration();

		if (this.eventFiltersByUuid.containsKey(uuid)) {
			this.eventFiltersByUuid.remove(uuid);

			if (log.isDebugEnabled())
				log.debug("Deregistered event filter with UUID " + uuid);
		} else {
			if (log.isDebugEnabled())
				log.debug("Event filter with UUID " + uuid + " no longer there for deregistration");
		}
	}

	/**
	 * This method removes all event filters registered with the event filtering
	 * service.
	 */
	synchronized public void deregisterAllEventFilters() {
		for (String uuid : new HashSet<String>(this.eventFiltersByUuid.keySet()))
			this.deregisterEventFilter(uuid);

	}

	/**
	 * This method refreshes a given event filter.
	 * 
	 * @param uuid
	 *            the UUID of the filter
	 */
	synchronized public void refreshEventFilter(String uuid) {
		if (log.isDebugEnabled())
			log.debug("Trying to refresh event filter with UUID " + uuid);

		if (this.eventFiltersByUuid.containsKey(uuid)) {
			this.eventFiltersByUuid.get(uuid).refresh();

			if (log.isDebugEnabled())
				log.debug("Refreshed event filter with UUID " + uuid);
		} else {
			if (log.isDebugEnabled())
				log.debug("Event filter with UUID " + uuid + " no longer there for refreshing");
		}
	}

	/**
	 * This predicate determines whether a given event filter has already been
	 * registered with the service.
	 * 
	 * @param eventFilter
	 *            the filter to look for
	 * @return <code>true</true> iff the filter has already been registered.
	 */
	synchronized public boolean isRegistered(EventFilter eventFilter) {
		return this.eventFiltersByUuid.containsKey(eventFilter.getUuid());
	}

	/**
	 * This method returns all registered event filters.
	 * 
	 * @return the registered event filters
	 */
	synchronized public Set<EventFilter> getRegisteredEventFilters() {
		return new HashSet<EventFilter>(this.eventFiltersByUuid.values());
	}

	/**
	 * This service method stores an event in the lighthouse database. It does
	 * not commit the current unit of work, which has to be done by the caller.
	 * 
	 * @param event
	 *            the event to log.
	 * @throws PersistenceException
	 *             in case of a database problem.
	 */
	/*package*/ void log(Event event) {
		if (log.isDebugEnabled())
			log.debug("Logging event: " + event.toXml());

		// Check if dateOfOccurrence is set. If not, set it to the current dateTime and add a remark to the udfs.
		if (event.getDateOfOccurrence() == null) {
			log.warn("Event to log did not contain dateOfOccurrence. Setting it to current dateTime.");
			event.setDateOfOccurrence(new Date());
			event.setUdf("Lighthouse-Server-Warning", "Original Event did not contain dateOfOccurrence.");
		}

		// sanitize UDFs
		// MySQL throws a duplicate key exception on udfs that only differ in low/high cases,
		// eg. "Value" and "value".
		if (sanitizeUDFs) {
			Map<String, Object> udfs = event.getUdfs();
			Map<String, Object> sanitizedUdfs = new HashMap<String, Object>();
			for (Entry<String, Object> udf : udfs.entrySet()) {
				Object oldUdf = sanitizedUdfs.put(udf.getKey().toLowerCase(), udf.getValue());
				if (oldUdf != null) {
					log.info("Duplicate UDF detected. Key: " + udf.getKey().toLowerCase() + ", Value: " + oldUdf);
				}
			}
			event.setUdfs(sanitizedUdfs);
		}
		
		String eventXml = null;
		try {
			this.eventRegistry.log(event);
			eventXml = event.toXml();
			this.eventRegistry.getUnitOfWork().commit();
			if (log.isDebugEnabled()) {
				log.debug("Database transaction committed. Flushed Event Id: " + event.getId());
			}

		} catch (PersistenceException ex) {
			log.error("Could not log event to database", ex);

			throw ex;
		}

		if (eventXml != null) {
			this.publishEventForProcessing(eventXml);
		} else {
			if (log.isWarnEnabled()) {
				log.warn("Could not publish event for processing due to non available eventXml");
			}
		}
		
		if (log.isDebugEnabled())
			log.debug("Event successfully logged in registry");
	}
	
	/**
	 * This service method stores an event in XML format in the lighthouse
	 * database. It does not commit the current unit of work, which has to be
	 * done by the caller.
	 * 
	 * @param event
	 *            the XML of the event to log.
	 * @throws PersistenceException
	 *             in case of a database problem.
	 * @throws XMLSerializationException
	 *             in case of a problem with the event XML.
	 */
	public void log(String eventXml) {
		if (log.isDebugEnabled())
			log.debug("Logging the following event XML : " + eventXml);
		if (eventXml.getBytes().length>=EVENT_SIZE_LIMIT ) {
			log.warn("Log message exceeds maximum allowed log message size of "+EVENT_SIZE_LIMIT+" Byte. Message will be discarded.");
		}

		Event eventToLog = new Event();

		int retries = 3;
		while (true) {
			try {
				try {
					eventToLog.fromXml(eventXml, this.deploymentRegistry, this.softwareComponentRegistry);
				} catch (XMLSerializationException ex) {
					log.error("Could not parse event to log:\n" + eventXml + "\n", ex);
					throw ex;
				} catch (UnknownContextException unknownContextException) {
					if (log.isDebugEnabled()) {
						log.debug("Event could not be parsed due to unknown context: " + eventXml);
					}
					if(autoDeployUnknownEvents) {
						createContextForEvent(eventXml);
						eventToLog.fromXml(eventXml, this.deploymentRegistry, this.softwareComponentRegistry);
					}
					else {
						log.error("Could not parse event to log:\n" + eventXml + "\n", unknownContextException);
						throw new XMLSerializationException(unknownContextException.getMessage(), unknownContextException);
					}
				}
		
				this.log(eventToLog);
				
				if (log.isDebugEnabled()) {
					log.debug("Logged event XML");
				}
				break;
				
			} catch (Throwable ex) {
				log.warn("Failed to persist event: " + eventXml, ex);
				eventRegistry.getUnitOfWork().rollback();
				if (--retries == 0) {
					break;
				}
			}
		}
	}
	
	
	private void createContextForEvent(String eventXml) {
		if(log.isDebugEnabled()) {
			log.debug("Starting to create context for event: " + eventXml);
		}
		XmlMuncher xmlDocument = new XmlMuncher(eventXml);
		
		String deploymentLocation = xmlDocument.readValueFromXml("/*/:context/:deploymentLocation");
		String deployedComponentCode = xmlDocument.readValueFromXml("/*/:context/:deployedComponentCode");
		
		if (log.isDebugEnabled()) {
			log.debug("Create if missing - softwareComponent: " + deployedComponentCode);
		}
		SoftwareComponent softwareComponent = softwareComponentRegistry.findByCode(deployedComponentCode);
		if(softwareComponent == null) {
			createSoftwareComponent(deployedComponentCode);
			softwareComponent = softwareComponentRegistry.findByCode(deployedComponentCode);
			if(log.isDebugEnabled()) {
				log.debug("Successfully created and persisted SoftwareComponent: " + softwareComponent.getCode());
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("SoftwareComponent: " + deployedComponentCode + " already exists.");
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Create if missing - deployment: " + deployedComponentCode + " @ " + deploymentLocation);
		}
		Deployment deployment = deploymentRegistry.findByComponentCodeAndLocation(deployedComponentCode, deploymentLocation);
		if(deployment == null) { 
			createDeployment(softwareComponent, deploymentLocation);
			deployment = deploymentRegistry.findByComponentAndLocation(softwareComponent, deploymentLocation);
			if(log.isDebugEnabled()) {
				log.debug("Successfully created and persisted Deployment: " + deployment.getLocation() + "@" + deployment.getDeployedComponent().getCode());
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Deployment: " + deployedComponentCode + " @ " + deploymentLocation + " already exists.");
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Create if missing - environment: " + autoDeployEnvironmentName);
		}
		EnvironmentRegistry environmentRegistry = EventLoggerServiceContainer.getServiceContainer().getDAO(EnvironmentRegistry.class);
		Environment environment = environmentRegistry.findByCode(autoDeployEnvironmentName);
		if(environment == null) {
			if(log.isDebugEnabled()) {
				log.debug("Environment " + autoDeployEnvironmentName +" missing, creating it....");
			}
			createEnvironment(environmentRegistry, autoDeployEnvironmentName);
			environment = environmentRegistry.findByCode(autoDeployEnvironmentName);
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Environment: " + autoDeployEnvironmentName + " already exists.");
			}
		}
		
		if(!environment.hasAttachedDeployment(deployment)) {
			if (log.isDebugEnabled()) {
				log.debug("Attaching Deployment: " + deployedComponentCode + " @ " + deploymentLocation + " to Environment: " + autoDeployEnvironmentName);
			}
			addDeploymentToEnvironment(environmentRegistry, environment, deployment);
		}
		
		if(log.isDebugEnabled()) {
			log.debug("Successfully created context for event: " + eventXml);
		}
	}

	private void createSoftwareComponent(String code) {
		SoftwareComponent softwareComponent= new SoftwareComponent();
		softwareComponent.setCode(code);
		softwareComponent.setLongName(code);
		
		softwareComponentRegistry.persist(softwareComponent);
	}
	
	private void createDeployment(SoftwareComponent softwareComponent, String location) {
		Deployment deployment = new Deployment();
		deployment.setLocation(location);
		deployment.setDeployedComponent(softwareComponent);
		
		deploymentRegistry.persist(deployment);
	}
	
	private void createEnvironment(EnvironmentRegistry environmentRegistry, String code) {
		Environment environment = new Environment();
		environment.setCode(code);
		environment.setLongName(code);
		
		environmentRegistry.persist(environment);
		
		if(log.isDebugEnabled()) {
			log.debug("Successfully created Environment: " + environment.getCode());
		}
	}
	
	private void addDeploymentToEnvironment(EnvironmentRegistry environmentRegistry, Environment environment, Deployment deployment) {
		environment.attachDeployment(deployment);
		environmentRegistry.update(environment);
		if(log.isDebugEnabled()) {
			log.debug("Successfully added deployment: " + deployment.getLocation() + "@" + deployment.getDeployedComponent().getCode() + " to Environment: " + environment.getCode());
		}
	}
	
	/**
	 * This property maintains a reference to the JMS provider to use for logged
	 * event processing.
	 */
	private JmsProvider eventProcessingJmsProvider = null;

	/**
	 * This property maintains a reference to the JMS topic on which to publish
	 * logged events for processing.
	 */
	private Destination eventProcessingTopic = null;

	/**
	 * This JMS connection serves for the publication of logged events on the
	 * event processing topic.
	 */
	private JmsConnection loggedEventPublicationConnection = null;

	
	/**If this value is true, for incoming events with unknown context, the context will be automatically deployed 
	 * 
	 */
	private boolean autoDeployUnknownEvents = false;
	
	/**If autoDeployUnknownEvents = true, automatically created contexts will be automatically added to the Environment with this name
	 * 
	 */
	private String autoDeployEnvironmentName	= "";
	
	
	/**
	 * This method publishes a logged event for further processing.
	 * 
	 * @param event
	 *            the event to publish for further processing
	 */
	public void publishEventForProcessing(final String eventXml) {
		if (log.isDebugEnabled())
			log.debug("Publishing event for processing: " + eventXml + " on topic: "
					+ this.eventProcessingTopic.toString());

		this.loggedEventPublicationConnection.sendToDestination(this.eventProcessingTopic, new JmsMessageCreator() {
			public Message createMessage(javax.jms.Session jmsSession) throws JMSException {
				TextMessage textMessage = jmsSession.createTextMessage();
				textMessage.setText(eventXml);
				return textMessage;
			}
		});

		if (log.isDebugEnabled())
			log.debug("Event published for processing.");

	}

	/**
	 * This call closes the live event filter matcher JMS connection.
	 */
	public void closeLiveEventFilterMatcher() {
		if (this.liveEventFilterMatcher != null)
			this.liveEventFilterMatcher.close();
	}

	/**
	 * The public constructor of the event logger service.
	 * 
	 * @param deploymentRegistry
	 *            the deployment registry to use by the service.
	 * @param softwareComponentRegistry
	 *            the software component registry to use by the service.
	 * @param eventRegistry
	 *            the event registry to use by the service.
	 * @param eventProcessingJmsProvider
	 *            the provider to use for connecting the different event
	 *            processors to the event processing topic
	 * @param eventProcessingTopic
	 *            the name of the topic to use for scheduling event processing
	 *            jobs
	 * @param eventFilterMatchHandler
	 *            the event filter match handler to use for events by the
	 *            service.
	 * @param pastEventNotificationBatchSize
	 *            this batch size to use for notification about past events.
	 * @param eventFilterExpiryIntervalInMsecs
	 *            the interval to use for expiring registered event filters
	 * @param eventFilterRegistrationThreadPoolSize
	 *            the number of threads in event filtering related thread pools
	 */
	public EventLoggerService(DeploymentRegistry deploymentRegistry,
			SoftwareComponentRegistry softwareComponentRegistry, EventRegistry eventRegistry,
			JmsProvider eventProcessingJmsProvider, String eventProcessingTopic,
			EventFilterMatchHandler eventFilterMatchHandler, int pastEventNotificationBatchSize,
			long eventFilterExpiryIntervalInMsecs, int eventFilterRegistrationThreadPoolSize, boolean autoDeployUnknownEvents, String autoDeployEnvironmentName, boolean sanitizeUDFs, int logMaxSize) {
		
		EVENT_SIZE_LIMIT = logMaxSize;

		this.deploymentRegistry = deploymentRegistry;
		this.softwareComponentRegistry = softwareComponentRegistry;

		this.autoDeployUnknownEvents = autoDeployUnknownEvents;
		this.autoDeployEnvironmentName = autoDeployEnvironmentName;
		this.sanitizeUDFs = sanitizeUDFs;
		
		if (eventRegistry != null)
			this.eventRegistry = eventRegistry;

		this.eventProcessingJmsProvider = eventProcessingJmsProvider;
		this.eventProcessingTopic = this.eventProcessingJmsProvider.getTopic(eventProcessingTopic);

		try {
			String loggedEventPublisherClientId = InetAddress.getLocalHost().getHostAddress()
					+ "#service-event-logger-loggedeventpub#" + System.currentTimeMillis();
			this.eventProcessingJmsProvider.setClientId(loggedEventPublisherClientId);
		} catch (UnknownHostException e) {
			log.error("Could not create JMS client ID", e);
		}

		this.loggedEventPublicationConnection = new JmsConnection(this.eventProcessingJmsProvider);

		this.eventFilterMatchHandler = eventFilterMatchHandler;

		this.pastEventNotificationBatchSize = pastEventNotificationBatchSize;
		this.eventFilterExpiryIntervalInMsecs = eventFilterExpiryIntervalInMsecs;
		this.eventFilterRegistrationThreadPool = Executors.newFixedThreadPool(eventFilterRegistrationThreadPoolSize);

		try {
			String liveEventFilterMatcherClientInd = InetAddress.getLocalHost().getHostAddress()
					+ "#service-event-logger-live-event-filter-matcher#" + System.currentTimeMillis();
			this.eventProcessingJmsProvider.setClientId(liveEventFilterMatcherClientInd);
		} catch (UnknownHostException e) {
			log.error("Could not create JMS client ID", e);
		}

		this.liveEventFilterMatcher = new JmsConnection(this.eventProcessingJmsProvider);
		this.liveEventFilterMatcher.registerDestinationConsumer(this.eventProcessingTopic,
				new LiveEventFilterProcessor(this));
	}

}
