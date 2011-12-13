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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;

import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.ui.environment.base.adapters.DomainBoundEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.ProcessTaskContainer;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;


public class ProcessTaskContainerAdapter implements HierarchicalEntityAdapter, DomainBoundEntityAdapter {

	private IAdapterManager adapterManager = Platform.getAdapterManager();
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object adaptee) {
		List<ProcessTask> tasks = ((ProcessTaskContainer) adaptee).getProcessTasks();
		List<ProcessTask> tmp = new LinkedList<ProcessTask>();
		for (ProcessTask processTask : tasks) {
			
			if (CodeGuard.hasRole(Role.PROCESS_TASK_VIEW, processTask)) {
				tmp.add(processTask);
			} else {
				HierarchicalEntityAdapter adapter = ((HierarchicalEntityAdapter) adapterManager.getAdapter(processTask, HierarchicalEntityAdapter.class));

				if (adapter.hasChildren(processTask)) {
					tmp.add(processTask);
				}
				
			}
			
		}
		Collections.sort(tmp, comparator);
		return tmp.toArray();
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalAdapter#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object adaptee) {
		return getChildren(adaptee).length > 0;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object adaptee) {
		return ((ProcessTaskContainer) adaptee).getLighthouseDomain();
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.DomainBoundEntityAdapter#getLighthouseDomain(java.lang.Object)
	 */
	public LighthouseDomain getLighthouseDomain(Object adaptee) {
		return ((ProcessTaskContainer) adaptee).getLighthouseDomain();
	}

	private Comparator<ProcessTask> comparator = new Comparator<ProcessTask>() {

		public int compare(ProcessTask o1, ProcessTask o2) {
			return getLabel(o1).compareTo(getLabel(o2));
		}
		
		private String getLabel(ProcessTask processTask) {
			return processTask.getLongName() == null || processTask.getLongName().length() == 0
				? processTask.getCode()
				: processTask.getLongName();
		}
	};
}
