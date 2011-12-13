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
package com.mercatis.lighthouse3.ui.event.base.services.impl;

import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.services.AbstractConfiguration;
import com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration;

public class EventConfigurationImpl extends AbstractConfiguration implements EventConfiguration {

	static final private String DOMAIN_EVENT_LOGGER_URL = "DOMAIN_EVENT_LOGGER_URL";

	static final private String DOMAIN_JMS_PROVIDER_CLASS = "DOMAIN_JMS_PROVIDER_CLASS";

	static final private String DOMAIN_JMS_BROKER_URL = "DOMAIN_JMS_BROKER_URL";

	static final private String DOMAIN_JMS_USER = "DOMAIN_JMS_USER";

	static final private String DOMAIN_JMS_PASSWORD = "DOMAIN_JMS_PASSWORD";

	static final private String DOMAIN_EVENTS_PUBLICATION_TOPIC = "DOMAIN_EVENTS_PUBLICATION_TOPIC";

	static final private String DOMAIN_EVENT_FILTER_REFRESH_INTERVAL = "DOMAIN_EVENT_FILTER_REFRESH_INTERVAL";
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration#getEventLoggerUrl()
	 */
	public String getEventLoggerUrl() {
		return this.getConfigurationProperty(DOMAIN_EVENT_LOGGER_URL);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration#getJmsProviderClass()
	 */
	public String getJmsProviderClass() {
		return this.getConfigurationProperty(DOMAIN_JMS_PROVIDER_CLASS);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration#getJmsBrokerUrl()
	 */
	public String getJmsBrokerUrl() {
		return this.getConfigurationProperty(DOMAIN_JMS_BROKER_URL);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration#getJmsPassword()
	 */
	public String getJmsPassword() {
		return this.getConfigurationProperty(DOMAIN_JMS_PASSWORD);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration#getJmsUser()
	 */
	public String getJmsUser() {
		return this.getConfigurationProperty(DOMAIN_JMS_USER);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration#getEventFilterRegistrationRefreshInterval()
	 */
	public Long getEventFilterRegistrationRefreshInterval() {
		if (this.getConfigurationProperty(DOMAIN_EVENT_FILTER_REFRESH_INTERVAL) == null)
			return 590000l;
		else
			return Long.parseLong(this.getConfigurationProperty(DOMAIN_EVENT_FILTER_REFRESH_INTERVAL));
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration#getEventsPublicationTopic()
	 */
	public String getEventsPublicationTopic() {
		return this.getConfigurationProperty(DOMAIN_EVENTS_PUBLICATION_TOPIC);
	}

	/**
	 * @param lighthouseDomain
	 */
	public EventConfigurationImpl(LighthouseDomain lighthouseDomain) {
		super(lighthouseDomain, CommonBaseActivator.OLD_EVENT_PLUGIN_ID);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration#setEventsFilterRegistrationRefreshInterval(long)
	 */
	public void setEventsFilterRegistrationRefreshInterval(long interval) {
		setConfigurationProperty(DOMAIN_EVENT_FILTER_REFRESH_INTERVAL, Long.toString(interval));
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration#setEventsPublicationTopic(java.lang.String)
	 */
	public void setEventsPublicationTopic(String eventTopic) {
		setConfigurationProperty(DOMAIN_EVENTS_PUBLICATION_TOPIC, eventTopic);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration#setJmsBrokerUrl(java.lang.String)
	 */
	public void setJmsBrokerUrl(String brokerUrl) {
		setConfigurationProperty(DOMAIN_JMS_BROKER_URL, brokerUrl);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration#setJmsPassword(java.lang.String)
	 */
	public void setJmsPassword(String password) {
		setConfigurationProperty(DOMAIN_JMS_PASSWORD, password);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration#setJmsProviderClass(java.lang.String)
	 */
	public void setJmsProviderClass(String providerClass) {
		setConfigurationProperty(DOMAIN_JMS_PROVIDER_CLASS, providerClass);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration#setJmsUser(java.lang.String)
	 */
	public void setJmsUser(String jmsUser) {
		setConfigurationProperty(DOMAIN_JMS_USER, jmsUser);
	}

}
