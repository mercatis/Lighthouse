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
package com.mercatis.lighthouse3.ui.environment.base.services.impl;

import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.services.AbstractConfiguration;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainConfiguration;


public class DomainConfigurationImpl extends AbstractConfiguration implements DomainConfiguration {
	
	static final private String DOMAIN_USERNAME = "DOMAIN_USERNAME";
	
	static final private String DOMAIN_PASSWORD = "DOMAIN_PASSWORD";
	
	static final private String DOMAIN_URL = "DOMAIN_URL";
	
	private static final String DOMAIN_SERVER_DOMAIN_KEY = "DOMAIN_SERVER_DOMAIN_KEY";
	
	/**
	 * @param lighthouseDomain
	 */
	public DomainConfigurationImpl(LighthouseDomain lighthouseDomain) {
		super(lighthouseDomain, CommonBaseActivator.OLD_ENVIRONMENT_PLUGIN_ID);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.services.impl.DomainConfiguration#getUsername()
	 */
	public String getUsername() {
		return getConfigurationProperty(DOMAIN_USERNAME);	
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.services.impl.DomainConfiguration#getPassword()
	 */
	public String getPassword() {
		return getConfigurationProperty(DOMAIN_PASSWORD);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.services.impl.DomainConfiguration#getUrl()
	 */
	public String getUrl() {
		return getConfigurationProperty(DOMAIN_URL);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.services.impl.DomainConfiguration#setUrl(java.lang.String)
	 */
	public void setUrl(String url) {
		setConfigurationProperty(DOMAIN_URL, url);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.services.impl.DomainConfiguration#setPassword(java.lang.String)
	 */
	public void setPassword(String password) {
		setConfigurationProperty(DOMAIN_PASSWORD, password);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.services.impl.DomainConfiguration#setUsername(java.lang.String)
	 */
	public void setUsername(String username) {
		setConfigurationProperty(DOMAIN_USERNAME, username);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.services.DomainConfiguration#getServerDomainKey()
	 */
	public String getServerDomainKey() {
		return getConfigurationProperty(DOMAIN_SERVER_DOMAIN_KEY);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.services.DomainConfiguration#setServerDomainKey(java.lang.String)
	 */
	public void setServerDomainKey(String serverDomainKey) {
		setConfigurationProperty(DOMAIN_SERVER_DOMAIN_KEY, serverDomainKey);
	}

}
