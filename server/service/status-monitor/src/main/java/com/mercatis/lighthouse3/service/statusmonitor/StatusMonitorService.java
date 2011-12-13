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
package com.mercatis.lighthouse3.service.statusmonitor;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.generationjava.io.xml.XmlEncXmlWriter;
import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.commons.messaging.JmsConnection;
import com.mercatis.lighthouse3.commons.messaging.JmsMessageCreator;
import com.mercatis.lighthouse3.domainmodel.commons.UnknownContextException;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.status.EMailNotificationChannelConfiguration;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusChange;
import com.mercatis.lighthouse3.domainmodel.status.StatusChangeNotificationChannel;
import com.mercatis.lighthouse3.domainmodel.status.StatusRegistry;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource;
import com.mercatis.lighthouse3.service.commons.rest.ResourceEventListener;

public class StatusMonitorService implements ResourceEventListener, MessageListener {

	protected Logger log = Logger.getLogger(this.getClass());
	
	protected SoftwareComponentRegistry softwareComponentRegistry = null;
	
	protected DeploymentRegistry deploymentRegistry = null;
	
	protected StatusRegistry statusRegistry = null;
	
	protected EMailNotificationChannelConfiguration notificationChannelConfiguration = null;
	
	protected boolean publishStatusCountersChanges = true;
	
	protected JmsConnection statusChangePublisher = null;
	
	protected Destination statusChangeNotificationDestination = null;
	
	protected Map<String, Set<String>> statusByDeploymentLookupTable = Collections.synchronizedMap(new HashMap<String, Set<String>>());

    private long statusChangesNotificationTopicTTL = 0l;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void publishStatusChangeEmailNotification(StatusChange statusChange) {
		if (notificationChannelConfiguration == null)
			return;
		
		for (StatusChangeNotificationChannel notificationChannel : statusChange.getStatus().getChangeNotificationChannels()) {
	        if (notificationChannel.getClass() == notificationChannelConfiguration.getStatusChangeNotificationChannelClass()) {
	            notificationChannel.statusChanged(notificationChannelConfiguration, statusChange);
	        }
	    }
	}
    
    /**
     * This method publishes a notification about a status change on the status
     * change notification topic.
     *
     * @param statusChange
     *            the change
     * @throws JMSException
     *             in case of trouble
     */
    protected void publishStatusChangeNotification(StatusChange statusChange) {
        if (log.isDebugEnabled()) {
            log.debug("Status change is being published on topic");
        }

        final StringWriter notification = new StringWriter();

        try {
            XmlWriter xml = new XmlEncXmlWriter(notification);
            xml.writeEntity("StatusChangeNotification");
            xml.writeAttribute("xmlns", XmlMuncher.MERCATIS_NS);

            xml.writeEntityWithText("statusPath", statusChange.getStatus().getPath());
            statusChange.writeToXmlWriter("change", xml);
            if (statusChange.getStatus() != null
                    && statusChange.getStatus().getPrevious() != null) {
                statusChange.getStatus().getPrevious().writeToXmlWriter(
                        "priorChange", xml);
            }

            xml.endEntity();
        } catch (IOException e) {
            log.error("Could not create status change notification message", e);

            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Publishing status change notification on topic: "
                    + notification.toString());
        }

        this.statusChangePublisher.sendToDestination(
                this.statusChangeNotificationDestination, new JmsMessageCreator() {

            public Message createMessage(javax.jms.Session jmsSession)
                    throws JMSException {
                TextMessage message = jmsSession.createTextMessage();

                message.setText(notification.toString());

                return message;
            }
        }, statusChangesNotificationTopicTTL);

        if (log.isDebugEnabled()) {
            log.debug("Status change is published on topic");
        }
    }
	
	public StatusMonitorService(SoftwareComponentRegistry softwareComponentRegistry, DeploymentRegistry deploymentRegistry, StatusRegistry statusRegistry, EMailNotificationChannelConfiguration notificationChannelConfiguration, JmsConnection statusChangePublisher, long statusChangesNotificationTopicTTL, Destination statusChangeNotificationDestination, boolean publishStatusCountersChanges) {
		this.softwareComponentRegistry = softwareComponentRegistry;
		this.deploymentRegistry = deploymentRegistry;
		this.statusRegistry = statusRegistry;
		this.notificationChannelConfiguration = notificationChannelConfiguration;
		this.statusChangePublisher = statusChangePublisher;
		this.statusChangeNotificationDestination = statusChangeNotificationDestination;
		this.statusChangesNotificationTopicTTL = statusChangesNotificationTopicTTL;
		this.publishStatusCountersChanges = publishStatusCountersChanges;
		start();
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.service.commons.rest.ResourceEventListener#entityCreated(com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource, java.lang.String)
	 */
	public void entityCreated(DomainModelEntityResource<?> alwaysNull, String entityIdCode) {
		if (log.isDebugEnabled()) {
			log.debug("Received entityCreated event for Status: " + entityIdCode);
		}
		
		try {
			Status status = statusRegistry.findByCode(entityIdCode);
			startMonitorStatus(status);
			
			if (status.getCurrent().isNewStatusChange()) {
				publishStatusChangeNotification(status.getCurrent());
			}
		} finally {
			statusRegistry.getUnitOfWork().rollback();
		}
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.service.commons.rest.ResourceEventListener#entityUpdated(com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource, java.lang.String)
	 */
	public void entityUpdated(DomainModelEntityResource<?> alwaysNull, String entityIdCode) {
		if (log.isDebugEnabled()) {
			log.debug("Received entityUpdated event for Status: " + entityIdCode);
		}
		
		try { 
			stopMonitorStatus(entityIdCode);
			
			Status status = statusRegistry.findByCode(entityIdCode);
			startMonitorStatus(status);
			
			if (status.getCurrent().isNewStatusChange()) {
				publishStatusChangeNotification(status.getCurrent());
				publishStatusChangeEmailNotification(status.getCurrent());
			}
		} finally {
			statusRegistry.getUnitOfWork().rollback();
		}
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.service.commons.rest.ResourceEventListener#entityDeleted(com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource, java.lang.String)
	 */
	public void entityDeleted(DomainModelEntityResource<?> alwaysNull, String entityIdCode) {
		if (log.isDebugEnabled()) {
			log.debug("Received entityDeleted event for Status: " + entityIdCode);
		}
		
		stopMonitorStatus(entityIdCode);
	}
	
	/* Receives event messages from JMS, deserializes the contained event and matches it against the monitored status.
	 * 
	 * (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message eventMsg) {
		long messageTraceTimestamp = 0l;
		if (log.isTraceEnabled()) {
			messageTraceTimestamp = System.currentTimeMillis();
		}
		if (log.isDebugEnabled()) {
			log.debug("Received eventMessage.");
		}
		
		String eventXml = null;
		try {
			eventXml = ((TextMessage) eventMsg).getText();
		} catch (JMSException ex) {
			log.error("Could not deserialize event XML from event message.", ex);
			return;
		}
		
		long traceTimestamp = 0l;
		if (log.isTraceEnabled()) {
			traceTimestamp = System.currentTimeMillis();
		}
		Event event = new Event();
		Set<String> statusCodes = null;
		try {
			event.fromXml(eventXml, deploymentRegistry, softwareComponentRegistry);
			if (log.isTraceEnabled()) {
				log.trace("Deserializing event from XML: " + (System.currentTimeMillis() - traceTimestamp) + "ms.");
				traceTimestamp = System.currentTimeMillis();
			}
			
			synchronized (statusByDeploymentLookupTable) {
				statusCodes = statusByDeploymentLookupTable.get(new StringBuilder(event.getContext().getDeployedComponent().getCode()).append("@").append(event.getContext().getLocation()).toString());
			}	
		} catch (XMLSerializationException ex) {
			log.error("Could not deserialize event from event XML.", ex);
			deploymentRegistry.getUnitOfWork().rollback();
			softwareComponentRegistry.getUnitOfWork().rollback();
			return;
		} catch (UnknownContextException ex) {
			log.error("Could not deserialize event from event XML.", ex);
			deploymentRegistry.getUnitOfWork().rollback();
			softwareComponentRegistry.getUnitOfWork().rollback();
			return;
		} finally {
			softwareComponentRegistry.getUnitOfWork().commit();
			deploymentRegistry.getUnitOfWork().commit();
		}
		
		if (statusCodes == null || statusCodes.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("No status monitored for Deployment: " + event.getContext().getDeployedComponent().getCode() + "@" + event.getContext().getLocation());
			}
			return;
		}
		
		if (log.isTraceEnabled()) {
			log.trace("Affected status lookup: " + (System.currentTimeMillis() - traceTimestamp) + "ms.");
		}
		
		for (String statusCode : statusCodes) {
			boolean success = false;
			int iterations = 0;
			do {
				try {
					if (log.isTraceEnabled()) {
						traceTimestamp = System.currentTimeMillis();
					}
					Status status = statusRegistry.findByCode(statusCode);
					statusRegistry.getUnitOfWork().setReadOnly(status, false);
					if (log.isTraceEnabled()) {
						log.trace("Loading status: " + statusCode + ": " + (System.currentTimeMillis() - traceTimestamp) + "ms.");
					}
					if (log.isDebugEnabled()) {
						log.debug("Fetched status: " + statusCode);
					}
					if (log.isTraceEnabled()) {
						traceTimestamp = System.currentTimeMillis();
					}
					
					int changed = matchStatus(status, event);
					if (changed > 0) {
						statusRegistry.update(status);
						statusRegistry.getUnitOfWork().commit();
						status = statusRegistry.findByCode(statusCode);
					}
					if (log.isTraceEnabled()) {
						log.trace("Matching status: " + statusCode + ": " + (System.currentTimeMillis() - traceTimestamp) + "ms.");
						traceTimestamp = System.currentTimeMillis();
					}
//					statusRegistry.getUnitOfWork().flush();
					if (log.isTraceEnabled()) {
						log.trace("Flush status: " + statusCode + ": " + (System.currentTimeMillis() - traceTimestamp) + "ms.");
					}
					
					if (changed == 2) {
						if (log.isTraceEnabled()) {
							traceTimestamp = System.currentTimeMillis();
						}
						publishStatusChangeNotification(status.getCurrent());
						publishStatusChangeEmailNotification(status.getCurrent());
						if (log.isTraceEnabled()) {
							log.trace("Change propagation (2) for status: " + statusCode + ": " + (System.currentTimeMillis() - traceTimestamp) + "ms.");
						}
					}
					if (changed == 1 && publishStatusCountersChanges) {
						if (log.isTraceEnabled()) {
							traceTimestamp = System.currentTimeMillis();
						}
						publishStatusChangeNotification(status.getCurrent());
						if (log.isTraceEnabled()) {
							log.trace("Change propagation (1) for status: " + statusCode + ": " + (System.currentTimeMillis() - traceTimestamp) + "ms.");
						}
					}

					statusRegistry.getUnitOfWork().commit();
					if (log.isTraceEnabled()) {
						log.trace("Commit status: " + statusCode + ": " + (System.currentTimeMillis() - traceTimestamp) + "ms.");
					}
					
					success = true;
				} catch (Throwable ex) {
					log.warn("Encountered throwable '" + ex.getMessage() + "' while persisting status. Retrying...", ex);
					statusRegistry.getUnitOfWork().rollback();
				}
			} while (!success && ++iterations < 10);
			if (!success) {
				log.warn("Could not trigger status: " + statusCode + " due to repeating failures.");
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Processed message.");
		}
		if (log.isTraceEnabled()) {
			log.trace("Total message processing: " + (System.currentTimeMillis() - messageTraceTimestamp) + "ms.");
		}
	}
	
	/*
	 * Adds all deployments associated with the given status to the statusByDeploymentLookupTable, therefore enabling monitoring of the status.
	 */
	protected void startMonitorStatus(Status status) {
		Set<Deployment> contexts = new HashSet<Deployment>();
		contexts.addAll(status.getContext().getAssociatedDeployments());
		
		synchronized (statusByDeploymentLookupTable) {
			for (Deployment deployment : contexts) {
				Set<String> statusCodes = statusByDeploymentLookupTable.get(deployment.getDeployedComponent().getCode() + "@" + deployment.getLocation());
				if (statusCodes == null) {
					statusCodes = new HashSet<String>();
					statusByDeploymentLookupTable.put(deployment.getDeployedComponent().getCode() + "@" + deployment.getLocation(), statusCodes);
				}
				statusCodes.add(status.getCode());
			}
		}
		
		if (log.isInfoEnabled()) {
			log.info("Started monitoring of status: " + status.getCode());
		}
	}

	/*
	 * Removes all deployments associated with the given status from the statusByDeploymentLookupTable, therefore stopping monitoring of the status.
	 */
	protected void stopMonitorStatus(String code) {
		synchronized (statusByDeploymentLookupTable) {
			for (Iterator<Entry<String, Set<String>>> it = statusByDeploymentLookupTable.entrySet().iterator(); it.hasNext();) {
				Entry<String, Set<String>> entry = it.next();
				
				if (entry.getValue().contains(code)) {
					entry.getValue().remove(code);
					if (entry.getValue().isEmpty()) {
						it.remove();
					}
				}
			}
		}
		
		if (log.isInfoEnabled()) {
			log.info("Stopped monitoring of status: " + code);
		}
	}
	
	public void restart() {
		this.start();
	}
	
	public void start() {
		try {
			List<Status> statusList = statusRegistry.findAll();
			for (Status status : statusList) {
				startMonitorStatus(status);
			}
		} finally {
			statusRegistry.getUnitOfWork().commit();
		}
	}
	
	/*
	 * clears the statusByDeploymentLookupTable, therefore effectively stopping monitoring of any status.
	 */
	public void stop() {
		synchronized (statusByDeploymentLookupTable) {
			statusByDeploymentLookupTable.clear();
		}
	}
	
	/**
	 * Returns true, if the given status code is monitored by this status monitor service.
	 * 
	 * @param code
	 * @return
	 */
	public boolean monitorsStatus(String code) {
		for (Set<String> tmp : statusByDeploymentLookupTable.values()) {
			if (tmp.contains(code)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns a set of codes of the monitored status.
	 * 
	 * @return
	 */
	public Set<String> getMonitoredStatusCodes() {
		Set<String> codes = new HashSet<String>();
		for (Set<String> tmp : statusByDeploymentLookupTable.values()) {
			codes.addAll(tmp);
		}
		
		return codes;
	}

	/**
	 * tries to match the given status against the given event.
	 * 
	 * @param status
	 * @param event
	 * @return 0 if nothing changed, 1 if the current status was updated but not changed, 2 if the current status was changed.
	 */
	protected int matchStatus(Status status, Event event) {
		if (log.isDebugEnabled()) {
            log.debug("Processing event against status: " + status.getCode());
        }
		
		StatusChange preprocessStatus = status.getCurrent();

        int preprocessOk = preprocessStatus.getOkCounter();
        int preprocessError = preprocessStatus.getErrorCounter();
        int preprocessStale = preprocessStatus.getStaleCounter();

        status.processEvent(event);

        if (log.isDebugEnabled()) {
            log.debug("Event against status: " + status.getCode() + " processed");
        }

        StatusChange postprocessStatus = status.getCurrent();

        boolean statusCountersChanged = ((postprocessStatus.getOkCounter() - preprocessOk) + (postprocessStatus.getErrorCounter() - preprocessError) + (postprocessStatus.getStaleCounter() - preprocessStale)) != 0;

        if (preprocessStatus!=postprocessStatus) {
        	if (log.isDebugEnabled()) {
                log.debug("Current status was changed.");
            }
			
			return 2;
        } else {
        	if (statusCountersChanged) {
        		if (log.isDebugEnabled()) {
                    log.debug("Current status was just updated, not changed.");
                }

				return 1;
        	} else {
	        	if (log.isDebugEnabled()) {
	                log.debug("Current status was neither changed nor updated.");
	            }
	
				return 0;
        	}
        }
	}
}
