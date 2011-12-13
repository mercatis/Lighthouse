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

import org.eclipse.core.runtime.PlatformObject;

import com.mercatis.lighthouse3.domainmodel.environment.Deployment;


public class Location extends PlatformObject {

	protected DeploymentContainer deploymentContainer;

	protected String label;

	/**
	 * @param deploymentContainer
	 * @param label
	 */
	public Location(DeploymentContainer deploymentContainer, String label) {
		this.deploymentContainer = deploymentContainer;
		this.label = label;
	}

	/**
	 * @return
	 */
	public DeploymentContainer getDeploymentContainer() {
		return deploymentContainer;
	}

	/**
	 * @return
	 */
	public List<Deployment> getDeployments() {
		return deploymentContainer.getDeployments(this);
	}

	/**
	 * @return
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param deploymentContainer
	 */
	protected void setDeploymentContainer(DeploymentContainer deploymentContainer) {
		this.deploymentContainer = deploymentContainer;
	}

	/**
	 * @param label
	 */
	protected void setLabel(String label) {
		this.label = label;
	}

	public boolean hasDeployments() {
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((deploymentContainer == null) ? 0 : deploymentContainer
						.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (deploymentContainer == null) {
			if (other.deploymentContainer != null)
				return false;
		} else if (!deploymentContainer.equals(other.deploymentContainer))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}
	
}
