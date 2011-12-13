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
package com.mercatis.lighthouse3.ui.event.base.services;

public interface EventConfiguration {
	public String getEventLoggerUrl();

	public String getJmsProviderClass();

	public String getJmsBrokerUrl();

	public String getJmsUser();

	public String getJmsPassword();

	public String getEventsPublicationTopic();

	public Long getEventFilterRegistrationRefreshInterval();
	
	public void setJmsProviderClass(String providerClass);
	
	public void setJmsBrokerUrl(String brokerUrl);
	
	public void setJmsUser(String jmsUser);
	
	public void setJmsPassword(String password);
	
	public void setEventsPublicationTopic(String eventTopic);
	
	public void setEventsFilterRegistrationRefreshInterval(long interval);

}
