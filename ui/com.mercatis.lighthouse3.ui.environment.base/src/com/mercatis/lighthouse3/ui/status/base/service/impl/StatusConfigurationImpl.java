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
package com.mercatis.lighthouse3.ui.status.base.service.impl;

import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.services.AbstractConfiguration;
import com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration;


public class StatusConfigurationImpl extends AbstractConfiguration implements StatusConfiguration {

	static final private String DOMAIN_STATUS_URL = "DOMAIN_STATUS_URL";

	static final private String DOMAIN_JMS_PROVIDER_CLASS = "DOMAIN_JMS_PROVIDER_CLASS";

	static final private String DOMAIN_JMS_BROKER_URL = "DOMAIN_JMS_BROKER_URL";

	static final private String DOMAIN_JMS_USER = "DOMAIN_JMS_USER";

	static final private String DOMAIN_JMS_PASSWORD = "DOMAIN_JMS_PASSWORD";

	static final private String DOMAIN_STATUS_PUBLICATION_TOPIC = "DOMAIN_STATUS_PUBLICATION_TOPIC";
	
	static final private String DOMAIN_STAUTS_NOTIFICATION_TEMPLATE_FOLDER = "DOMAIN_STAUTS_NOTIFICATION_TEMPLATE_FOLDER";

	static final private String DOMAIN_STATUS_HISTORY_PAGE_SIZE = "DOMAIN_STATUS_HISTORY_PAGE_SIZE";

	static final private String DOMAIN_STATUS_HISTORY_PAGE_NO = "DOMAIN_STATUS_HISTORY_PAGE_NO";
	
	public StatusConfigurationImpl(LighthouseDomain lighthouseDomain) {
		super(lighthouseDomain, CommonBaseActivator.OLD_STATUS_PLUGIN_ID);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#getJmsBrokerUrl()
	 */
	public String getJmsBrokerUrl() {
		return getConfigurationProperty(DOMAIN_JMS_BROKER_URL);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#getJmsPassword()
	 */
	public String getJmsPassword() {
		return getConfigurationProperty(DOMAIN_JMS_PASSWORD);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#getJmsProviderClass()
	 */
	public String getJmsProviderClass() {
		return getConfigurationProperty(DOMAIN_JMS_PROVIDER_CLASS);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#getJmsUser()
	 */
	public String getJmsUser() {
		return getConfigurationProperty(DOMAIN_JMS_USER);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#getStatusPublicationTopic()
	 */
	public String getStatusPublicationTopic() {
		String ret = getConfigurationProperty(DOMAIN_STATUS_PUBLICATION_TOPIC);
		return ret != null ? ret : "";
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#getStatusUrl()
	 */
	public String getStatusUrl() {
		return getConfigurationProperty(DOMAIN_STATUS_URL);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#setJmsBrokerUrl(java.lang.String)
	 */
	public void setJmsBrokerUrl(String brokerUrl) {
		setConfigurationProperty(DOMAIN_JMS_BROKER_URL, brokerUrl);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#setJmsPassword(java.lang.String)
	 */
	public void setJmsPassword(String password) {
		setConfigurationProperty(DOMAIN_JMS_PASSWORD, password);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#setJmsProviderClass(java.lang.String)
	 */
	public void setJmsProviderClass(String providerClass) {
		setConfigurationProperty(DOMAIN_JMS_PROVIDER_CLASS, providerClass);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#setJmsUser(java.lang.String)
	 */
	public void setJmsUser(String jmsUser) {
		setConfigurationProperty(DOMAIN_JMS_USER, jmsUser);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#setStatusPublicationTopic(java.lang.String)
	 */
	public void setStatusPublicationTopic(String statusTopic) {
		setConfigurationProperty(DOMAIN_STATUS_PUBLICATION_TOPIC, statusTopic);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#setStatusUrl(java.lang.String)
	 */
	public void setStatusUrl(String url) {
		setConfigurationProperty(DOMAIN_STATUS_URL, url);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#getStatusNotificationTemplateFolder()
	 */
	public String getStatusNotificationTemplateFolder() {
		String ret = this.getConfigurationProperty(DOMAIN_STAUTS_NOTIFICATION_TEMPLATE_FOLDER);
		return ret != null ? ret : "";
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#setStatusNotificationTemplateFolder(java.lang.String)
	 */
	public void setStatusNotificationTemplateFolder(String folder) {
		this.setConfigurationProperty(DOMAIN_STAUTS_NOTIFICATION_TEMPLATE_FOLDER, folder);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#getStatusPageNo()
	 */
	public int getStatusPageNo() {
		int pageNo = 1; //init to a default value
		try {
			pageNo = Integer.parseInt(getConfigurationProperty(DOMAIN_STATUS_HISTORY_PAGE_NO));
		}
		catch (NullPointerException e) {}
		catch (NumberFormatException e) {}
		return pageNo;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#getStatusPageSize()
	 */
	public int getStatusPageSize() {
		int pageSize = 10; //init to a default value
		try {
			pageSize = Integer.parseInt(getConfigurationProperty(DOMAIN_STATUS_HISTORY_PAGE_SIZE));
		}
		catch (NullPointerException e) {}
		catch (NumberFormatException e) {}
		return pageSize;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#setStatusPageNo()
	 */
	public void setStatusPageNo(int pageNo) {
		setConfigurationProperty(DOMAIN_STATUS_HISTORY_PAGE_NO, Integer.toString(pageNo));
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration#setStatusPageSize()
	 */
	public void setStatusPageSize(int pageSize) {
		setConfigurationProperty(DOMAIN_STATUS_HISTORY_PAGE_SIZE, Integer.toString(pageSize));
	}
}
