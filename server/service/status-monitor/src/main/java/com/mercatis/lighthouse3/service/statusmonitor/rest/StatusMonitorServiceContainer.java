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
package com.mercatis.lighthouse3.service.statusmonitor.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import javax.jms.Queue;
import javax.jms.Topic;

import com.mercatis.lighthouse3.commons.messaging.JmsConnection;
import com.mercatis.lighthouse3.commons.messaging.JmsProvider;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.domainmodel.status.EMailNotificationChannelConfiguration;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusRegistry;
import com.mercatis.lighthouse3.service.commons.rest.HibernateDomainModelEntityRestServiceContainer;
import com.mercatis.lighthouse3.service.commons.rest.ResourceEventTopicSubscriber;
import com.mercatis.lighthouse3.service.statusmonitor.StatusMonitorService;
import com.mercatis.lighthouse3.service.statusmonitor.StatusStalenessMonitorService;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;

/**
 * This class provides a servlet container around the status monitor service.
 */
public class StatusMonitorServiceContainer extends HibernateDomainModelEntityRestServiceContainer {

    private static final long serialVersionUID = 526853771461931229L;
    public static final String JMS_PROVIDER = "com.mercatis.lighthouse3.service.statusmonitor.jms.JmsProvider";
    public static final String JMS_CONFIG_FILE_LOCATION = "com.mercatis.lighthouse3.service.statusmonitor.jms.JmsConfigFileLocation";
    public static final String JMS_CONFIG_RESOURCE = "com.mercatis.lighthouse3.service.statusmonitor.jms.JmsConfigResource";
    public static final String JMS_PROVIDER_URL = "JmsProvider.URL";
    public static final String JMS_PROVIDER_PASSWORD = "JmsProvider.Password";
    public static final String JMS_PROVIDER_USER = "JmsProvider.User";
    public static final String TOPIC_STATUS_CHANGES = "Topic.StatusChanges";
    public static final String QUEUE_EVENTS_PROCESSING = "Queue.EventsProcessing";
    public static final String EMAIL_CONFIG_FILE_LOCATION = "com.mercatis.lighthouse3.service.statusmonitor.email.EmailConfigFileLocation";
    public static final String EMAIL_CONFIG_RESOURCE = "com.mercatis.lighthouse3.service.statusmonitor.email.EmailConfigResource";
    public static final String EMAIL_SERVER_PROTOCOL = "EMail.Server.Protocol";
    public static final String EMAIL_SERVER_ADDRESS = "EMail.Server.Address";
    public static final String EMAIL_SERVER_PORT = "EMail.Server.Port";
    public static final String EMAIL_SERVER_USER = "EMail.Server.User";
    public static final String EMAIL_SERVER_PASSWORD = "EMail.Server.Password";
    public static final String EMAIL_SENDER_ADDRESS = "EMail.Sender.Address";
    public static final String EMAIL_NOTICATION = "EMail.Enabled";
    public static final String TOPIC_STATUS_CHANGES_TTL = "Topic.StatusChanges.TTL";
    public static final String PUBLISH_STATUS_COUNTERS_CHANGES = "StatusCounters.Publish.Changes";

    /**
     * This property keeps a reference to the JMS provider to use.
     */
    private JmsProvider jmsProvider = null;
    /**
     * This property refers to the queue where received logged events are put
     * from the topic for later processing.
     */
    private Queue queueEventsProcessing = null;
    /**
     * This property refers to the topic for publishing status changes
     */
    private Topic topicStatusChanges = null;

    private long topicStatusChangesTTL = 0l;
    
    private boolean publishStatusCountersChanges = true;

    /**
     * This property refers to the email notification channel to use for email
     * notification of status changes.
     */
    private EMailNotificationChannelConfiguration emailNotificationChannel = null;

    /**
     * This method sets up the JMS connection and topics to use for monitoring
     * status and publishing status change notifications.
     */
    private Properties setUpJmsProviderAndTopics() {
        Properties jmsConfig = new Properties();
        
		//Loading of configured JMS provider implementation
		String jmsProviderClassName = null;
		if (this.getInitParameter(JMS_PROVIDER) != null) {
			jmsProviderClassName = this.getInitParameter(JMS_PROVIDER); //Loading from web.xml parameters
		} else {
			this.jmsProvider = null;
		}
		if (this.jmsProvider == null) { //Not using default values
			try {	
				this.jmsProvider = (JmsProvider) Class.forName(jmsProviderClassName).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
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
            this.jmsProvider.setProviderUrl((String) jmsConfig.get(JMS_PROVIDER_URL));
        }

        if (jmsConfig.get(JMS_PROVIDER_USER) != null) {
            this.jmsProvider.setProviderUser((String) jmsConfig.get(JMS_PROVIDER_USER));
        }

        if (jmsConfig.get(JMS_PROVIDER_PASSWORD) != null) {
            this.jmsProvider.setProviderUserPassword((String) jmsConfig.get(JMS_PROVIDER_PASSWORD));
        }

        String topicStatusChangesName = "com.mercatis.lighthouse3.service.statusmonitor.statuschanges";

        if (jmsConfig.get(TOPIC_STATUS_CHANGES) != null) {
            topicStatusChangesName = (String) jmsConfig.get(TOPIC_STATUS_CHANGES);
        }

        if (jmsConfig.get(TOPIC_STATUS_CHANGES_TTL) != null) {
            String ttl = (String) jmsConfig.get(TOPIC_STATUS_CHANGES_TTL);
            try {
            	this.topicStatusChangesTTL = Long.parseLong(ttl);
            } catch (Exception ex) {
            	this.topicStatusChangesTTL = 0l;
            }
        }
        
        if (jmsConfig.get(PUBLISH_STATUS_COUNTERS_CHANGES) != null) {
        	String publish = (String) jmsConfig.get(PUBLISH_STATUS_COUNTERS_CHANGES);
        	try {
        		this.publishStatusCountersChanges = Boolean.parseBoolean(publish);
        	} catch (Exception ex) {
        		this.publishStatusCountersChanges = true;
        	}
        }

        String queueEventsProcessingName = "com.mercatis.lighthouse3.service.eventlogger.events.processing.queue";

        if (jmsConfig.get(QUEUE_EVENTS_PROCESSING) != null) {
            queueEventsProcessingName = (String) jmsConfig.get(QUEUE_EVENTS_PROCESSING);
        }
        if (log.isDebugEnabled()) {
        	log.debug("QUEUE_EVENTS_PROCESSING: '" + queueEventsProcessingName + "'.");
        }

        this.topicStatusChanges = this.jmsProvider.getTopic(topicStatusChangesName);
        this.queueEventsProcessing = this.jmsProvider.getQueue(queueEventsProcessingName);
        
        return jmsConfig;
    }

    /**
     * This method sets up the email notification channel for email notification
     * about status changes.
     */
    private void setUpEMailNotificationChannel() {
    	Properties emailConfig = null;
    	if (this.configuration != null) {
    		emailConfig = this.configuration;
    	} else {
    		emailConfig = new Properties();
    	}
    	
        if (this.getInitParameter(EMAIL_CONFIG_RESOURCE) != null) {
            try {
                InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(this.getInitParameter(EMAIL_CONFIG_RESOURCE));
                emailConfig.load(resourceAsStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (this.getInitParameter(EMAIL_CONFIG_FILE_LOCATION) != null) {
            try {
                emailConfig.load(new FileInputStream(this.getInitParameter(EMAIL_CONFIG_FILE_LOCATION)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        //Check if Email notification is enabled. If not do not start it
        if (emailConfig.get(EMAIL_NOTICATION)!= null) {
        	String emailEnabled = (String)emailConfig.get(EMAIL_NOTICATION);
        	if (emailEnabled.equalsIgnoreCase("N")) {
        		log.info("Email notification disabled");
        		return;
        	}
        } else if (emailConfig.isEmpty()) {
            return;
        }
        if (log.isDebugEnabled()) {
        	log.debug("Extracting email config details");
        }
        
        String emailServerProtocol = (String) emailConfig.get(EMAIL_SERVER_PROTOCOL);
        if (!("smtp".equals(emailServerProtocol) || "smtps".equals(emailServerProtocol))) {
            emailServerProtocol = "smtp";
        }

        String emailServerAddress = (String) emailConfig.get(EMAIL_SERVER_ADDRESS);
        if (emailServerAddress == null) {
            emailServerAddress = "localhost";
        }

        int emailServerPort = 25;
        String emailServerPortString = (String) emailConfig.get(EMAIL_SERVER_PORT);
        if (emailServerPortString != null) {
            emailServerPort = Integer.parseInt(emailServerPortString);
        }

        String emailUser = (String) emailConfig.get(EMAIL_SERVER_USER);

        String emailPassword = (String) emailConfig.get(EMAIL_SERVER_PASSWORD);
        if (emailUser == null) {
            emailPassword = null;
        }

        String senderEmailAddress = (String) emailConfig.get(EMAIL_SENDER_ADDRESS);
        if (senderEmailAddress == null) {
            senderEmailAddress = "lh3-statusmonitor@mercatis.com";
        }

        this.emailNotificationChannel = new EMailNotificationChannelConfiguration(emailServerProtocol,
                emailServerAddress, emailServerPort, emailUser, emailPassword, senderEmailAddress, this.getDAO(EventRegistry.class));
    }
    /**
     * This property keeps a reference to the status monitor service.
     */
    private StatusMonitorService statusMonitorService = null;

    /**
     * This method returns the status monitor service instance.
     *
     * @return the status monitor service instance.
     */
    public StatusMonitorService getStatusMonitorService() {
        return this.statusMonitorService;
    }
    
    public StatusStalenessMonitorService getStatusStalenessMonitorService() {
        return this.statusStalenessMonitorService;
    }

    /**
     * This method sets up the status monitor service.
     */
    private void setUpStatusMonitorService(Properties config) {
    	statusMonitorServiceConnection = new JmsConnection(jmsProvider);
        
        statusMonitorService = new StatusMonitorService(getDAO(SoftwareComponentRegistry.class), getDAO(DeploymentRegistry.class), getDAO(StatusRegistry.class), emailNotificationChannel, statusMonitorServiceConnection, topicStatusChangesTTL, topicStatusChanges, publishStatusCountersChanges);

        statusMonitorServiceConnection.registerDestinationConsumer(queueEventsProcessing, statusMonitorService);
        
        statusChangeSubscriber = new ResourceEventTopicSubscriber(Status.class, this, statusMonitorService, getConfiguration());
        
        this.statusStalenessMonitorService = new StatusStalenessMonitorService(getDAO(StatusRegistry.class), new JmsConnection(jmsProvider), topicStatusChanges, emailNotificationChannel);
		this.statusUpdateListener = new ResourceEventTopicSubscriber(Status.class, this, this.statusStalenessMonitorService, null);
    }

    private StatusStalenessMonitorService statusStalenessMonitorService = null;
    
    private ResourceEventTopicSubscriber statusUpdateListener = null;
    
    private JmsConnection statusMonitorServiceConnection = null;
    
    private ResourceEventTopicSubscriber statusChangeSubscriber = null;
    
    @Override
    protected void configure(Map<String, String> initParams, ResourceConfig rc, WebApplication wa) {
        super.configure(initParams, rc, wa);

        Properties config = this.setUpJmsProviderAndTopics();
        this.setUpEMailNotificationChannel();
        this.setUpStatusMonitorService(config);
    }

    @Override
    public void destroy() {
    	this.statusUpdateListener.stopJmsConnection();
		this.statusStalenessMonitorService.destroy();
		
    	if (statusMonitorServiceConnection != null)
        	statusMonitorServiceConnection.close();
    	
    	if (statusChangeSubscriber != null)
    		statusChangeSubscriber.stopJmsConnection();
    	
    	if (this.statusMonitorService != null) {
            this.statusMonitorService.stop();
        }
        
        super.destroy();
    }
}
