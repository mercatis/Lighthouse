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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;


public class LighthouseDomain extends PlatformObject {

	private DeploymentContainer deploymentContainer;

	private EnvironmentContainer environmentContainer;

	private ProcessTaskContainer processTaskContainer;

	private IProject project;
	
	private SoftwareComponentContainer softwareComponentContainer;

	/**
	 * @param project
	 */
	public LighthouseDomain(IProject project) {
		this.project = project;
	}

	/**
	 * @return
	 */
	public DeploymentContainer getDeploymentContainer() {
		if (deploymentContainer == null)
			deploymentContainer = new DeploymentContainer(this);
		return deploymentContainer;
	}

	/**
	 * @return
	 */
	public EnvironmentContainer getEnvironmentContainer() {
		if (environmentContainer == null)
			environmentContainer = new EnvironmentContainer(this);
		return environmentContainer;
	}

	/**
	 * @return
	 */
	public ProcessTaskContainer getProcessTaskContainer() {
		if (processTaskContainer == null)
			processTaskContainer = new ProcessTaskContainer(this);
		return processTaskContainer;
	}

	/**
	 * @return
	 */
	public SoftwareComponentContainer getSoftwareComponentContainer() {
		if (softwareComponentContainer == null)
			softwareComponentContainer = new SoftwareComponentContainer(this);
		return softwareComponentContainer;
	}
	
	public AbstractContainer getContainerFor(Object element) {
		if (element instanceof Environment) {
			return getEnvironmentContainer();
		}
		else if (element instanceof Deployment || element instanceof Location) {
			return getDeploymentContainer();
		}
		else if (element instanceof ProcessTask) {
			return getProcessTaskContainer();
		}
		else if (element instanceof SoftwareComponent) {
			return getSoftwareComponentContainer();
		}
		else {
			return null;
		}
	}

	/**
	 * @return
	 */
	public IProject getProject() {
		return project;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LighthouseDomain other = (LighthouseDomain) obj;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		return true;
	}

	/**
	 * @return
	 */
	public String getServerDomainKey() {
		boolean projectOpen = project.isOpen();
		if (!projectOpen)
			try {
				project.open(new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
		}
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode("com.mercatis.lighthouse3.ui.environment.base");
		String serverDomainKey = preferences.get("DOMAIN_SERVER_DOMAIN_KEY", "");
		if (!projectOpen) {
			try {
				project.close(new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return serverDomainKey;
	}
	
}
