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
package com.mercatis.lighthouse3.ui.status.base.service;

public interface StatusConfiguration {
	
	/**
	 * @return
	 */
	public String getStatusUrl();

	/**
	 * @return
	 */
	public String getJmsProviderClass();

	/**
	 * @return
	 */
	public String getJmsBrokerUrl();

	/**
	 * @return
	 */
	public String getJmsUser();

	/**
	 * @return
	 */
	public String getJmsPassword();

	/**
	 * @return
	 */
	public String getStatusPublicationTopic();
	
	/**
	 * @return
	 */
	public String getStatusNotificationTemplateFolder();

	/**
	 * @param providerClass
	 */
	public void setJmsProviderClass(String providerClass);
	
	/**
	 * @param brokerUrl
	 */
	public void setJmsBrokerUrl(String brokerUrl);
	
	/**
	 * @param jmsUser
	 */
	public void setJmsUser(String jmsUser);
	
	/**
	 * @param password
	 */
	public void setJmsPassword(String password);
	
	/**
	 * @param statusTopic
	 */
	public void setStatusPublicationTopic(String statusTopic);
	
	/**
	 * @param url
	 */
	public void setStatusUrl(String url);
	
	/**
	 * @param folder
	 */
	public void setStatusNotificationTemplateFolder(String folder);
	
	public int getStatusPageSize();
	
	public void setStatusPageSize(int pageSize);
	
	public int getStatusPageNo();
	
	public void setStatusPageNo(int pageNo);
}
