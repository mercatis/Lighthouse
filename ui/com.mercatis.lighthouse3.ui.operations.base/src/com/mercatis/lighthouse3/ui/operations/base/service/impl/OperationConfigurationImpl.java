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
package com.mercatis.lighthouse3.ui.operations.base.service.impl;

import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.services.AbstractConfiguration;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;
import com.mercatis.lighthouse3.ui.operations.base.service.OperationConfiguration;


public class OperationConfigurationImpl extends AbstractConfiguration implements OperationConfiguration {

	static final private String DOMAIN_OPERATION_URL = "DOMAIN_OPERATION_URL";
	
	/**
	 * @param lighthouseDomain
	 */
	public OperationConfigurationImpl(LighthouseDomain lighthouseDomain) {
		super(lighthouseDomain, OperationBase.PLUGIN_ID);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.operations.base.service.OperationConfiguration#getOperationUrl()
	 */
	public String getOperationUrl() {
		return getConfigurationProperty(DOMAIN_OPERATION_URL);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.operations.base.service.OperationConfiguration#setOperationUrl(java.lang.String)
	 */
	public void setOperationUrl(String url) {
		setConfigurationProperty(DOMAIN_OPERATION_URL, url);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.operations.base.service.OperationConfiguration#existsOperationUrl()
	 */
	public boolean existsOperationUrl() {
		return existsProperty(DOMAIN_OPERATION_URL);
	}

}
