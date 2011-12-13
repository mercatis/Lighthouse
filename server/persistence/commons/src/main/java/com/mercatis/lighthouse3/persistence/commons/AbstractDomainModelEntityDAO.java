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
package com.mercatis.lighthouse3.persistence.commons;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This class provides an abstract implementation of common methods for DAOs.
 */
public abstract class AbstractDomainModelEntityDAO {
	
	protected Logger logger = Logger.getLogger(getClass());

    private Map<String, String> initParams;

    public void setInitParams(Map<String, String> initParams) {
	this.initParams = initParams;
    }

    protected Map<String, String> getInitParams() {
	return this.initParams;
    }

    private DAOProvider daoProvider;

    public void setDAOProvider(DAOProvider daoProvider) {
	this.daoProvider = daoProvider;
    }

    protected DAOProvider getDAOProvider() {
	return this.daoProvider;
    }
}
