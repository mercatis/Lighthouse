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
package com.mercatis.lighthouse3.ui.environment.base.model;

import java.util.List;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;


public class DeploymentContainer extends AbstractContainer {
	
	/**
	 * @param lighthouseDomain
	 */
	public DeploymentContainer(LighthouseDomain lighthouseDomain) {
		super(lighthouseDomain);
	}

	/**
	 * @return
	 */
	public List<Location> getLocations() {
		return CommonBaseActivator.getPlugin().getDomainService().getLocations(this);
	}

	/**
	 * @param location
	 * @return
	 */
	public List<Deployment> getDeployments(Location location) {
		return CommonBaseActivator.getPlugin().getDomainService().getDeployments(location);
	}

	/**
	 * @return
	 */
	public List<Deployment> getAllDeployments() {
		return CommonBaseActivator.getPlugin().getDomainService().getAllDeployments(getLighthouseDomain());
	}

	public boolean hasLocations() {
		return CommonBaseActivator.getPlugin().getDomainService().getDeploymentRegistry(getLighthouseDomain()).findAllLocations().size() != 0;
	}

}
