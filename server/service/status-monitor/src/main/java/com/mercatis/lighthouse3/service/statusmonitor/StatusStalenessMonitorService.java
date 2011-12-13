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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.generationjava.io.xml.XmlEncXmlWriter;
import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.commons.messaging.JmsConnection;
import com.mercatis.lighthouse3.commons.messaging.JmsMessageCreator;
import com.mercatis.lighthouse3.domainmodel.status.EMailNotificationChannelConfiguration;
import com.mercatis.lighthouse3.domainmodel.status.StalenessChange;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusChange;
import com.mercatis.lighthouse3.domainmodel.status.StatusChangeNotificationChannel;
import com.mercatis.lighthouse3.domainmodel.status.StatusRegistry;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource;
import com.mercatis.lighthouse3.service.commons.rest.ResourceEventListener;

public class StatusStalenessMonitorService implements ResourceEventListener {
	
	protected Logger log = Logger.getLogger(this.getClass());
	
	private Timer stalenessTimer = null;
	
	private Map<String,StalenessTask> stalenessTaskLookupTable = null;
	
	private StatusRegistry statusRegistry = null;
	
	private JmsConnection statusChangePublisher = null;
	
	private Destination statusChangeNotificationDestination = null;
	
	private EMailNotificationChannelConfiguration notificationChannelConfiguration = null;
	
	private class StalenessTask extends TimerTask {
		
		private String code;

		public StalenessTask(String code) {
			this.code = code;
		}
		
		@Override
		public void run() {
			try {
			Status status = statusRegistry.findByCode(code);//, 1, 0);
				if (status != null) {
					if ((System.currentTimeMillis() - status.getLastStatusUpdate().getTime()) < status.getStalenessIntervalInMsecs()) {
						if (log.isDebugEnabled()) {
							long updated = System.currentTimeMillis() - status.getLastStatusUpdate().getTime();
							long staleness = Math.max(0l, status.getStalenessIntervalInMsecs() - updated);
							log.debug("Status: " + status.getCode() + " has already been updated " + updated + " ms ago. Rescheduling for staleness in " + staleness + " ms.");
						}
						resetStatusStalenessMonitoring(code);
						statusRegistry.getUnitOfWork().rollback();
						return;
					}
				
					if (log.isDebugEnabled()) {
						log.debug("Marking Status: " + code + " as stale.");
					}
					
					statusRegistry.getUnitOfWork().setReadOnly(status, false);
	
					boolean wasStale = status.isStale();
					status.processStalenessChange(new StalenessChange());
					publishStatusChangeNotification(status.getCurrent());
					if (!wasStale && status.isStale())
						publishStatusChangeEmailNotification(status.getCurrent());
					statusRegistry.getUnitOfWork().commit();
				}
			} catch (Exception ex) {
				log.info("Status: " + code + " has been modified in the meantime. Rescheduling for staleness.");
				resetStatusStalenessMonitoring(code);
				statusRegistry.getUnitOfWork().rollback();
			}
		}
		
	}
	
	public StatusStalenessMonitorService(StatusRegistry statusRegistry, JmsConnection statusChangePublisher, Destination statusChangeNotificationDestination, EMailNotificationChannelConfiguration notificationChannelConfiguration) {
		this.statusRegistry = statusRegistry;
		this.statusChangePublisher = statusChangePublisher;
		this.statusChangeNotificationDestination = statusChangeNotificationDestination;
		this.notificationChannelConfiguration = notificationChannelConfiguration;
		
		this.stalenessTimer = new Timer("StatusStalenessMonitorService", true);
		this.stalenessTaskLookupTable = new HashMap<String, StalenessTask>();
		
		try {
			List<Status> statusList = this.statusRegistry.findAll();
			for (Status status : statusList) {
				startStatusStalenessMonitoring(status.getCode());
			}
		} finally {
			this.statusRegistry.getUnitOfWork().commit();
		}
	}

	public void entityCreated(DomainModelEntityResource<?> alwaysNull, String entityIdCode) {
		startStatusStalenessMonitoring(entityIdCode);
	}

	public void entityUpdated(DomainModelEntityResource<?> alwaysNull, String entityIdCode) {
		resetStatusStalenessMonitoring(entityIdCode);
	}

	public void entityDeleted(DomainModelEntityResource<?> alwaysNull, String entityIdCode) {
		stopStatusStalenessMonitoring(entityIdCode);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void publishStatusChangeEmailNotification(StatusChange statusChange) {
		if (notificationChannelConfiguration == null) {
			if (log.isDebugEnabled()) {
				log.debug("No EMail Notification Channel Configuration provided. Therefore no email on status staleness change.");
			}
			
			return;
		}
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
        });

        if (log.isDebugEnabled()) {
            log.debug("Status change is published on topic");
        }
    }

	public void resetStatusStalenessMonitoring(String code) {
		startStatusStalenessMonitoring(code);
	}
	
	public void startStatusStalenessMonitoring(String code) {
		stopStatusStalenessMonitoring(code);
		
		long delay = 0l;
		long period = 0l;
		
		Status status = null;
		for (int i=0; i<10; ++i) {
			try {
				status = statusRegistry.findByCode(code); //, 0, 0);
				statusRegistry.getUnitOfWork().rollback();
				if (status!=null)
					break;
			} catch (Exception ex) {
				log.error("Cannot obtain Status: " + code + ". Retrying..", ex);
			}
		};

		if (status == null) {
			log.warn("Cannot obtain Status: " + code + ". Aborting status staleness monitoring.");
			return;
		}
		
		long stalenessInterval = status.getStalenessIntervalInMsecs();
		if (stalenessInterval == 0l) {
			if (log.isInfoEnabled()) {
				log.info("Status: " + status.getCode() + " has no staleness interval set. Ignoring.");
			}
			return;
		}
			
		delay = Math.max(0l, (status.getLastStatusUpdate().getTime() + stalenessInterval) - System.currentTimeMillis());
		period = stalenessInterval;
		
		StalenessTask stalenessTask = new StalenessTask(code);
		stalenessTimer.schedule(stalenessTask, delay, period);
		stalenessTaskLookupTable.put(code, stalenessTask);
		if (log.isInfoEnabled()) {
			log.info("Started Status Staleness Monitoring (Delay: " + delay + ", Period: " + period + ") for Status: " + code);
		}
	}
	
	public void stopStatusStalenessMonitoring(String code) {
		StalenessTask stalenessTask = stalenessTaskLookupTable.remove(code);
		if (stalenessTask != null) {
			stalenessTask.cancel();
			stalenessTimer.purge();
			if (log.isInfoEnabled()) {
				log.info("Stopped Status Staleness Monitoring for Status: " + code);
			}
		}
	}
	
	public void destroy() {
		for (Iterator<Entry<String, StalenessTask>> it = stalenessTaskLookupTable.entrySet().iterator(); it.hasNext();) {
			Entry<String,StalenessTask> entry = it.next();
			entry.getValue().cancel();
			it.remove();
		}
		stalenessTimer.cancel();
		stalenessTimer.purge();
		stalenessTimer = null;
		
		stalenessTaskLookupTable.clear();
		stalenessTaskLookupTable = null;
		if (log.isInfoEnabled()) {
			log.info("Destroyed Status Staleness Monitoring.");
		}
	}
	
}
