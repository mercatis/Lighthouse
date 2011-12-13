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
package com.mercatis.lighthouse3.service.eventlogger.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;

import com.mercatis.lighthouse3.commons.messaging.ActiveMQProvider;
import com.mercatis.lighthouse3.commons.messaging.JmsConnection;
import com.mercatis.lighthouse3.commons.messaging.JmsProvider;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.service.commons.rest.HibernateDomainModelEntityRestServiceContainer;
import com.mercatis.lighthouse3.service.eventlogger.EventFilterMatchHandler;
import com.mercatis.lighthouse3.service.eventlogger.EventLoggerService;
import com.mercatis.lighthouse3.service.eventlogger.jms.IncomingEventListener;
import com.mercatis.lighthouse3.service.eventlogger.jms.MatchingEventTopicPublisher;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;

/**
 * This class provides a service container for the event logger service to run
 * within a servlet container.
 * 
 * The container supports additional init parameters for setting up JMS
 * configurations:
 * 
 * <ul>
 * <li>
 * <code>com.mercatis.lighthouse3.service.eventlogger.jms.JmsProvider</code> for
 * passing the class name of the implementation of the <code>JmsProvider</code>
 * interface for the JMS broker to use.
 * <li>
 * <code>com.mercatis.lighthouse3.service.eventlogger.jms.JmsConfigResource</code>
 * for passing the resource name of the configuration file with the JMS
 * settings.
 * <li>
 * <code>com.mercatis.lighthouse3.service.eventlogger.jms.JmsConfigFileLocation</code>
 * for passing the path to the configuration file with the JMS settings.
 * 
 * <li>
 * <code>com.mercatis.lighthouse3.service.eventlogger.jms.JmsPastEventNotificationBatchSize</code>
 * for choosing the batch granularity for past event publication.
 * <li>
 * <code>com.mercatis.lighthouse3.service.eventlogger.jms.JmsPastEventNotificationBatchPurgeIntervalInMsecs</code>
 * for setting an automatic purge interval to ensure that past events get
 * published even if the batch is not yet full.
 * <li>
 * <code>com.mercatis.lighthouse3.service.eventlogger.EventFilterExpiryInMsecs</code>
 * for setting the time interval when non-refreshed event filters expire.
 * <li>
 * <code>com.mercatis.lighthouse3.service.eventlogger.EventFilterRegistrationThreadNumber</code>
 * the number of threads allocated for event filter registration.
 * <li>
 * <code>com.mercatis.lighthouse3.service.eventlogger.EventBatchSize</code> the
 * size of the batch into which logged events are collected before stored in the
 * data base. This value should equal the appropriate Hibernate batch setting.
 * <li>
 * <code>com.mercatis.lighthouse3.service.eventlogger.EventBatchPurgeIntervalInMsecs</code>
 * the for setting the time interval in which logged events are purged from the
 * batch even if the batch is not yet full.
 * <li>
 * <code>com.mercatis.lighthouse3.service.eventlogger.AutoCreateUnknownEventContext</code>
 * If this value is set to 'true', unknown contexts will be automatically created and added to the environment,
 * which is defined in <code>com.mercatis.lighthouse3.service.eventlogger.AutoDeployEnvironmentName</code>
 * <li>
 * <code>com.mercatis.lighthouse3.service.eventlogger.AutoDeployEnvironmentName</code>
 * The name of the Environment to which all automatically created context will be added
 * </ul>
 */
public class EventLoggerServiceContainer extends HibernateDomainModelEntityRestServiceContainer {

	private static final long serialVersionUID = 9142056059298508343L;

	public static final String JMS_PROVIDER = "com.mercatis.lighthouse3.service.eventlogger.jms.JmsProvider";
	public static final String JMS_CONFIG_FILE_LOCATION = "com.mercatis.lighthouse3.service.eventlogger.jms.JmsConfigFileLocation";
	public static final String JMS_CONFIG_RESOURCE = "com.mercatis.lighthouse3.service.eventlogger.jms.JmsConfigResource";

	public static final String PAST_EVENT_NOTIFICATION_BATCH_SIZE = "com.mercatis.lighthouse3.service.eventlogger.PastEventNotificationBatchSize";
	public static final String EVENT_FILTER_EXPIRY_TIME = "com.mercatis.lighthouse3.service.eventlogger.EventFilterExpiryInMsecs";
	public static final String EVENT_FILTER_REGISTRATION_THREAD_NUMBER = "com.mercatis.lighthouse3.service.eventlogger.EventFilterRegistrationThreadNumber";

	public static final String JMS_PROVIDER_URL = "JmsProvider.URL";
	public static final String JMS_PROVIDER_PASSWORD = "JmsProvider.Password";
	public static final String JMS_PROVIDER_USER = "JmsProvider.User";

	public static final String QUEUE_EVENTS_INCOMING = "Queue.EventsIncoming";
	public static final String TOPIC_EVENTS_PROCESSING = "Topic.EventsProcessing";
	public static final String TOPIC_EVENTS_NOTIFICATION = "Topic.EventsNotification";

	public static final String AUTO_CREATE_UNKNOWN_EVENT_CONTEXT = "com.mercatis.lighthouse3.service.eventlogger.AutoCreateUnknownEventContext";
	public static final String AUTO_DEPLOY_ENVIRONMENT_NAME = "com.mercatis.lighthouse3.service.eventlogger.AutoDeployEnvironmentName";
    public static final String TOPIC_EVENTS_NOTIFICATION_TTL = "Topic.EventsNotification.TTL";
    
    public static final String SANITIZE_UDFS = "com.mercatis.lighthouse3.service.eventlogger.SanitizeUDFs";
    public static final String MAX_LOGMESSAGE_SIZE = "com.mercatis.lighthouse3.service.eventlogger.MaxLogSizeKiloBytes";

	private JmsProvider eventProcessingJmsProvider = null;

	private String eventsIncomingQueue = null;
	private String eventsProcessingTopic = null;
	private String eventsNotificationTopic = null;
    private String eventsNotificationTopicTTL = null;
	private JmsProvider jmsReceiverProvider = null;
	private JmsProvider eventsNotificationProvider = null;
	private JmsConnection eventLoggingConnection = null;
	private EventLoggerService eventLoggerService = null;

	public JmsProvider getJmsReceiverProvider() {
		return this.jmsReceiverProvider;
	}

	public JmsProvider getJmsNotificationProvider() {
		return this.eventsNotificationProvider;
	}

	public String getEventsIncomingQueue() {
		return this.eventsIncomingQueue;
	}

	public String getEventsNotificationTopic() {
		return this.eventsNotificationTopic;
	}

    public long getEventsNotificationTopicTTL() {
		try {
			return Long.parseLong(this.eventsNotificationTopicTTL);
		} catch (Exception ex) {
			return 0l;
		}
	}

	public EventLoggerService getEventLoggerService() {
		return this.eventLoggerService;
	}

	private Properties configureJmsProvider() {
		Properties jmsConfig = new Properties();
		
		//Loading of configured JMS provider implementation
		String jmsProviderClassName = null;
		if (this.getInitParameter(JMS_PROVIDER) != null) {
			jmsProviderClassName = this.getInitParameter(JMS_PROVIDER); //Loading from web.xml parameters
		} else {
			this.jmsReceiverProvider = new ActiveMQProvider();
			this.eventsNotificationProvider = new ActiveMQProvider();
			this.eventProcessingJmsProvider = new ActiveMQProvider();
		}
		if (this.jmsReceiverProvider == null) { //Not using default values
			try {	
				this.jmsReceiverProvider = (JmsProvider) Class.forName(jmsProviderClassName).newInstance();
				this.eventsNotificationProvider = (JmsProvider) Class.forName(jmsProviderClassName).newInstance();
				this.eventProcessingJmsProvider = (JmsProvider) Class.forName(jmsProviderClassName).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//If present create a new Properties object from the configuration, where all the settings are configured
		if (this.getInitParameter(JMS_CONFIG_RESOURCE) != null) {
			try {
				InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(this.getInitParameter(JMS_CONFIG_RESOURCE));
				jmsConfig.load(resourceAsStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (this.getInitParameter(JMS_CONFIG_FILE_LOCATION) != null) {
			try {
				jmsConfig.load(new FileInputStream(this.getInitParameter(JMS_CONFIG_FILE_LOCATION)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (jmsConfig.get(JMS_PROVIDER_URL) != null) {
			this.jmsReceiverProvider.setProviderUrl((String) jmsConfig.get(JMS_PROVIDER_URL));
			this.eventsNotificationProvider.setProviderUrl((String) jmsConfig.get(JMS_PROVIDER_URL));
			this.eventProcessingJmsProvider.setProviderUrl((String) jmsConfig.get(JMS_PROVIDER_URL));
		}

		if (jmsConfig.get(JMS_PROVIDER_USER) != null) {
			this.jmsReceiverProvider.setProviderUser((String) jmsConfig.get(JMS_PROVIDER_USER));
			this.eventsNotificationProvider.setProviderUser((String) jmsConfig.get(JMS_PROVIDER_USER));
			this.eventProcessingJmsProvider.setProviderUser((String) jmsConfig.get(JMS_PROVIDER_USER));
		}
		if (jmsConfig.get(JMS_PROVIDER_PASSWORD) != null) {
			this.jmsReceiverProvider.setProviderUserPassword((String) jmsConfig.get(JMS_PROVIDER_PASSWORD));
			this.eventsNotificationProvider.setProviderUserPassword((String) jmsConfig.get(JMS_PROVIDER_PASSWORD));
			this.eventProcessingJmsProvider.setProviderUserPassword((String) jmsConfig.get(JMS_PROVIDER_PASSWORD));
		}

		if (jmsConfig.get(QUEUE_EVENTS_INCOMING) != null)
			this.eventsIncomingQueue = (String) jmsConfig.get(QUEUE_EVENTS_INCOMING);

		if (jmsConfig.get(TOPIC_EVENTS_PROCESSING) != null)
			this.eventsProcessingTopic = (String) jmsConfig.get(TOPIC_EVENTS_PROCESSING);

		if (jmsConfig.get(TOPIC_EVENTS_NOTIFICATION) != null)
			this.eventsNotificationTopic = (String) jmsConfig.get(TOPIC_EVENTS_NOTIFICATION);

        if (jmsConfig.get(TOPIC_EVENTS_NOTIFICATION_TTL) != null)
                    this.eventsNotificationTopicTTL = (String) jmsConfig.get(TOPIC_EVENTS_NOTIFICATION_TTL);

		String clientId = null;
		try {
			clientId = InetAddress.getLocalHost().getHostAddress() + "#service-event-logger-logging#"
					+ System.currentTimeMillis();
			this.jmsReceiverProvider.setClientId(clientId);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		try {
			clientId = InetAddress.getLocalHost().getHostAddress() + "#service-event-logger-pastpub#"
					+ System.currentTimeMillis();
			this.eventsNotificationProvider.setClientId(clientId);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return jmsConfig;
	}

	private void setUpJmsLogReceiver() {
		this.eventLoggingConnection = new JmsConnection(this.jmsReceiverProvider);
		this.eventLoggingConnection.registerDestinationConsumer(this.jmsReceiverProvider.getQueue(this.eventsIncomingQueue),
				new IncomingEventListener(this.getEventLoggerService()));
	}

	private void setUpEventLoggerService(Properties configuration) {
		EventFilterMatchHandler eventFilterMatchHandler = null;

		if (this.eventsNotificationTopic != null) {
			eventFilterMatchHandler = new MatchingEventTopicPublisher(this.eventsNotificationProvider,
					this.eventsNotificationProvider.getTopic(this.eventsNotificationTopic), this.getEventsNotificationTopicTTL());

		}

		int pastEventNotificationBatchSize = 1000;
		if	(configuration.get(PAST_EVENT_NOTIFICATION_BATCH_SIZE) != null) {
			try {
				pastEventNotificationBatchSize = Integer.parseInt((String)configuration.get(PAST_EVENT_NOTIFICATION_BATCH_SIZE));
			} catch (Exception e) {
			}
		} else if (this.getInitParameter(PAST_EVENT_NOTIFICATION_BATCH_SIZE) != null) {
			try {
				pastEventNotificationBatchSize = Integer.parseInt(this
						.getInitParameter(PAST_EVENT_NOTIFICATION_BATCH_SIZE));
			} catch (Exception e) {
			}
		}

		long eventFilterExpiryTime = 600000l;
		if	(configuration.get(EVENT_FILTER_EXPIRY_TIME) != null) {
			try {
				eventFilterExpiryTime = Long.parseLong((String)configuration.get(EVENT_FILTER_EXPIRY_TIME));
			} catch (Exception e) {
			}
		} else if (this.getInitParameter(EVENT_FILTER_EXPIRY_TIME) != null) {
			try {
				eventFilterExpiryTime = Long.parseLong(this.getInitParameter(EVENT_FILTER_EXPIRY_TIME));
			} catch (Exception e) {
			}
		}

		int eventFilterRegistrationThreadNumber = 10;
		if	(configuration.get(EVENT_FILTER_REGISTRATION_THREAD_NUMBER) != null) {
			try {
				eventFilterRegistrationThreadNumber = Integer.parseInt((String)configuration.get(EVENT_FILTER_REGISTRATION_THREAD_NUMBER));
			} catch (Exception e) {
			}
		} else if (this.getInitParameter(EVENT_FILTER_REGISTRATION_THREAD_NUMBER) != null) {
			try {
				eventFilterRegistrationThreadNumber = Integer.parseInt(this
						.getInitParameter(EVENT_FILTER_REGISTRATION_THREAD_NUMBER));
			} catch (Exception e) {
			}
		}
		
		boolean autoDeployUnknownEvents = true;
		if	(configuration.get(AUTO_CREATE_UNKNOWN_EVENT_CONTEXT) != null) {
			try {
				autoDeployUnknownEvents = ((String)configuration.get(AUTO_CREATE_UNKNOWN_EVENT_CONTEXT)).equals("true");
			} catch (Exception e) {
			}
		} else if (this.getInitParameter(AUTO_CREATE_UNKNOWN_EVENT_CONTEXT) != null) {
			try {
				autoDeployUnknownEvents = this.getInitParameter(AUTO_CREATE_UNKNOWN_EVENT_CONTEXT).equals("true");
			} catch (Exception e) {
			}
		}
		
		String autoDeployEnvironmentName = "UNKNOWN";
		if	(configuration.get(AUTO_DEPLOY_ENVIRONMENT_NAME) != null) {
			try {
				autoDeployEnvironmentName = (String)configuration.get(AUTO_DEPLOY_ENVIRONMENT_NAME);
			} catch (Exception e) {
			}
		} else if (this.getInitParameter(AUTO_DEPLOY_ENVIRONMENT_NAME) != null) {
			try {
				autoDeployEnvironmentName = this.getInitParameter(AUTO_DEPLOY_ENVIRONMENT_NAME);
			} catch (Exception e) {
			}
		}
		
		boolean sanitizeUDFs = true;
		if	(configuration.get(SANITIZE_UDFS) != null) {
			try {
				sanitizeUDFs = ((String)configuration.get(SANITIZE_UDFS)).equals("true");
			} catch (Exception e) {
			}
		} else if (this.getInitParameter(SANITIZE_UDFS) != null) {
			try {
				sanitizeUDFs = this.getInitParameter(SANITIZE_UDFS).equals("true");
			} catch (Exception e) {
			}
		}
		if (log.isInfoEnabled()) {
			log.info("Sanitize UDFs: " + Boolean.toString(sanitizeUDFs));
		}
		int maxLogSize = 1024;
		try {
			maxLogSize *= Integer.parseInt((String) configuration.get(MAX_LOGMESSAGE_SIZE));
		} catch (Exception e) {
			maxLogSize *= 128;
		}
		log.info("Maximum log event size: " + maxLogSize + " Byte");
		
		this.eventLoggerService = new EventLoggerService(
				this.getDAO(DeploymentRegistry.class),
				this.getDAO(SoftwareComponentRegistry.class),
				this.getDAO(EventRegistry.class),
				this.eventProcessingJmsProvider,
				this.eventsProcessingTopic,
				eventFilterMatchHandler,
				pastEventNotificationBatchSize,
				eventFilterExpiryTime,
				eventFilterRegistrationThreadNumber,
				autoDeployUnknownEvents,
				autoDeployEnvironmentName,
				sanitizeUDFs,
				maxLogSize);
	}

	@Override
	protected void configure(Map<String, String> initParams, ResourceConfig rc, WebApplication wa) {
		super.configure(initParams, rc, wa);

		Properties configuration = this.configureJmsProvider();

		this.setUpEventLoggerService(configuration);

		this.setUpJmsLogReceiver();
	}

	@Override
	public void destroy() {
		if (this.eventLoggerService != null) {
			this.eventLoggerService.deregisterAllEventFilters();
			this.eventLoggerService.closeLiveEventFilterMatcher();
		}

		if (this.eventLoggingConnection != null)
			this.eventLoggingConnection.close();

		super.destroy();
	}

}
