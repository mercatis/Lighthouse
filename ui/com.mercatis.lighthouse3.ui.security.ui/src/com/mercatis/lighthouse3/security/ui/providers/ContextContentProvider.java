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
package com.mercatis.lighthouse3.security.ui.providers;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.model.IWorkbenchAdapter;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;


public class ContextContentProvider implements ITreeContentProvider {//, IResourceChangeListener, DomainChangeListener, IAdaptable, IPersistableElement, IElementFactory {

	private IAdapterManager adapterManager = Platform.getAdapterManager();

	public void dispose() {
	}

	protected IWorkbenchAdapter getWorkbenchAdapter(Object element) {
		return (IWorkbenchAdapter) adapterManager.getAdapter(element, IWorkbenchAdapter.class);
	}
	
	protected HierarchicalEntityAdapter getHierarchicalEntityAdapter(Object element) {
		return (HierarchicalEntityAdapter) adapterManager.getAdapter(element, HierarchicalEntityAdapter.class);
	}

	private LighthouseDomain viewedDomain;
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof LighthouseDomain && viewedDomain == null) {
			viewedDomain = (LighthouseDomain) parentElement;
			return new Object[] {viewedDomain};
		}
		List<Object> children = new LinkedList<Object>();
		for (Object child : getWorkbenchAdapter(parentElement).getChildren(parentElement)) {
			children.add(child);
		}
		if (parentElement instanceof StatusCarrier) {
			StatusCarrier statusCarrier = (StatusCarrier) parentElement;
			children.addAll(CommonBaseActivator.getPlugin().getStatusService().getPagedStatusesForCarrier(statusCarrier, 1, 1));
		}
		if (parentElement instanceof Deployment) {
			Deployment deployment = (Deployment) parentElement;
			children.addAll(OperationBase.getOperationInstallationService().findAtDeployment(deployment));
		}
		return children.toArray();
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public Object getParent(Object element) {
		return getWorkbenchAdapter(element).getParent(element);
	}

	public boolean hasChildren(Object element) {
		HierarchicalEntityAdapter adapter = getHierarchicalEntityAdapter(element);
		if (adapter == null)
			return getChildren(element).length > 0;
		return adapter.hasChildren(element);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
