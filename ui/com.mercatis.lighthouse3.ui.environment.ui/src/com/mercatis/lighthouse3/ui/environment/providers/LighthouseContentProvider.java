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
package com.mercatis.lighthouse3.ui.environment.providers;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWTException;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.UIJob;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeEvent;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeListener;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainService;


public class LighthouseContentProvider implements ITreeContentProvider, IResourceChangeListener, DomainChangeListener, IAdaptable, IPersistableElement, IElementFactory {

	private IAdapterManager adapterManager = Platform.getAdapterManager();

	private TreeViewer viewer;

	private DomainService domainService;

	class ResourceDeltaVisitor implements IResourceDeltaVisitor {

		public boolean newProject = false;

		public boolean visit(IResourceDelta delta) throws CoreException {
			if (newProject)
				return false;

			IResource resource = delta.getResource();
			newProject = (resource.getType() == IResource.PROJECT);
			return newProject;
		}
	}

	public LighthouseContentProvider() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		this.domainService = CommonBaseActivator.getPlugin().getDomainService();
		this.domainService.addDomainChangeListener(this);
	}

	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			IResourceDelta delta = event.getDelta();
			IResourceDelta[] children = delta.getAffectedChildren(IResourceDelta.ADDED);
			boolean project = false;
			for (IResourceDelta child : children) {
				if (child.getResource().getType() == IResource.PROJECT)
					project = true;
			}
			if (children.length == 0 || !project)
				return;

			if (event.getSource().equals(ResourcesPlugin.getWorkspace())) {
				new UIJob("refresh viewer") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						viewer.refresh(ResourcesPlugin.getWorkspace().getRoot(), true);
						return Status.OK_STATUS;
					}
				}.schedule();
			}
		}
	}

	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	protected IWorkbenchAdapter getWorkbenchAdapter(Object element) {
		return (IWorkbenchAdapter) adapterManager.getAdapter(element, IWorkbenchAdapter.class);
	}
	
	protected HierarchicalEntityAdapter getHierarchicalEntityAdapter(Object element) {
		return (HierarchicalEntityAdapter) adapterManager.getAdapter(element, HierarchicalEntityAdapter.class);
	}

	public Object[] getChildren(Object parentElement) {
		return getWorkbenchAdapter(parentElement).getChildren(parentElement);
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IWorkspaceRoot) {
			IWorkspaceRoot root = (IWorkspaceRoot) inputElement;
			IProject[] projects = root.getProjects();
			List<LighthouseDomain> domains = new ArrayList<LighthouseDomain>(projects.length);
			for (int i = 0; i < projects.length; i++) {
				LighthouseDomain lighthouseDomain = domainService.getLighthouseDomain(projects[i]);
				if (lighthouseDomain != null)
					domains.add(lighthouseDomain);
			}

			return domains.toArray();
		}

		if (inputElement == null) {
			return new Object[0];
		}

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
		this.viewer = (TreeViewer) viewer;
	}

	public void domainChange(DomainChangeEvent event) {
		try {
			this.viewer.refresh(event.getSource(), true);
			this.viewer.reveal(event.getSource());
		} catch (SWTException ex) {
			CommonUIActivator.getPlugin().getLog().log(new Status(Status.WARNING, CommonUIActivator.PLUGIN_ID, ex.getMessage(), ex.throwable));
		}
	}


	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		//This is neccessary otherwise setSaveAndRestore will not work for the common navigator
		if (adapter == IPersistableElement.class) 
			return this;
		if (adapter == IWorkbenchAdapter.class)
			return ResourcesPlugin.getWorkspace().getRoot().getAdapter(adapter);
		return null;
	}

	public String getFactoryId() {
		return this.getClass().getCanonicalName();
	}

	public void saveState(IMemento memento) {
		return;
	}

	public IAdaptable createElement(IMemento memento) {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
}
