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
package com.mercatis.lighthouse3.ui.environment.base.adapters.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;


public class ProcessTaskAdapter extends DeploymentCarryingDomainModelEntityAdapter {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.impl.HierarchicalDomainModelEntityAdapter#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object adaptee) {
		
		Object[] children = super.getChildren(adaptee);
		List<Object> tmp = new LinkedList<Object>();
		for (Object obj : children) {
			if (hasChildren(obj) || CodeGuard.hasRole(Role.PROCESS_TASK_VIEW, obj)) {
				tmp.add(obj);
			}
		}
		Object[] processTasks = tmp.toArray();
		Object[] deployments = new Object[0];
		if (CodeGuard.hasRole(Role.DEPLOYMENT_VIEW, adaptee)) {
			deployments = super.getDeployments(adaptee);
		}

		Object[] result = new Object[processTasks.length + deployments.length];
		System.arraycopy(processTasks, 0, result, 0, processTasks.length);
		System.arraycopy(deployments, 0, result, processTasks.length, deployments.length);
		Arrays.sort(result, new Comparator<Object>() {

			public int compare(Object o1, Object o2) {
				
				return getLabel(o1).compareTo(getLabel(o2));
			}
			private String getLabel(Object obj) {
				if(obj instanceof ProcessTask){
					ProcessTask processTask = (ProcessTask)obj;
				return processTask.getLongName() == null || processTask.getLongName().length() == 0
					? processTask.getCode()
					: processTask.getLongName();
				}
				else if(obj instanceof Deployment){
					return ((Deployment)obj).getLocation();
				}
				return null;
			}
		});
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.impl.HierarchicalDomainModelEntityAdapter#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object adaptee) {
		Object parent = super.getParent(adaptee);
		if (parent != null)
			return parent;
		
		return CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(adaptee).getProcessTaskContainer();
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.impl.DeploymentCarryingDomainModelEntityAdapter#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object adaptee) {
		return getChildren(adaptee).length > 0;
	}

}
