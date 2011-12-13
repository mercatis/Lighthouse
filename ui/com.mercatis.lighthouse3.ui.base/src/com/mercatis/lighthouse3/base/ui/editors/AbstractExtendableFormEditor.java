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
package com.mercatis.lighthouse3.base.ui.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

import com.mercatis.lighthouse3.base.UIBase;


/**
 * This editor is capable of adding pages dynamically from factories. These factories are defined
 * in the plugin.xml
 * 
 */
public abstract class AbstractExtendableFormEditor extends FormEditor {

	/**
	 * Pages to be added before the editor places its static pages
	 */
	private List<AbstractLighthouseEditorPage> beforePages;

	/**
	 * Pages to be added after the editor places its static pages
	 */
	private List<AbstractLighthouseEditorPage> afterPages;
	
	/**
	 * The selectionprovider used for this editor. In this case, a multipage selectionprovider
	 * is registered to the platform, that in turn takes care of the selectionproviders of each page.
	 */
	private MultiPageSelectionProvider selectionProvider = new MultiPageSelectionProvider();

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		site.setSelectionProvider(selectionProvider);
		beforePages = new ArrayList<AbstractLighthouseEditorPage>();
		afterPages = new ArrayList<AbstractLighthouseEditorPage>();
		
		//get the extension point id for this editor or stop adding pages if "null" is returned
		String extensionPointID = getFactoryExtensionPoint();
		if (extensionPointID == null)
			return;
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		List<AbstractEditorPageFactory> factories = new ArrayList<AbstractEditorPageFactory>();
		for (IConfigurationElement element : reg.getConfigurationElementsFor(extensionPointID)) {
			try {
				AbstractEditorPageFactory factory = (AbstractEditorPageFactory) element.createExecutableExtension("class");
				factory.setOrderNumber(Integer.parseInt(element.getAttribute("ordernumber")));
				factories.add(factory);
			} catch (CoreException e) {
				UIBase.getDefault().getLog().log(new Status(IStatus.ERROR, UIBase.PLUGIN_ID, e.getMessage(), e));
			}
		}
		Collections.sort(factories);
		for (AbstractEditorPageFactory factory : factories) {
			switch (factory.getFactoryPosition()) {
			case BEFORE_STATIC:
				beforePages.addAll(factory.getPages(this));
				break;
			case AFTER_STATIC:
				afterPages.addAll(factory.getPages(this));
				break;
			}
		}
	}
	
	@Override
	public Object getSelectedPage() {
		int index = getActivePage();
		if (index == -1) {
			return null;
		}
		IEditorPart editor = getEditor(index);
		if (editor != null) {
			return editor;
		}
		return getControl(index);
	}

	@Override
	protected void createPages() {
		createPages(beforePages);
		addPages();
		createPages(afterPages);
	}
	
	private void createPages(List<AbstractLighthouseEditorPage> pages) {
		int requestedPage = -1;
		GenericEditorInput<?> editorInput = (GenericEditorInput<?>) getEditorInput();
		for (AbstractLighthouseEditorPage page : pages) {
			int idx;
			try {
				idx = addPage(page.getPage(), getEditorInput());
				page.setPageIndex(idx);
				setPageText(idx, page.getTitle());
				if (editorInput.getPayload() != null && editorInput.getPayload().equals(page.getPage().getClass()))
					requestedPage = idx;
			} catch (PartInitException e) {
				UIBase.getDefault().getLog().log(new Status(IStatus.ERROR, UIBase.PLUGIN_ID, e.getMessage(), e));
			}
		}
		if (requestedPage != -1)
			setActivePage(requestedPage);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		save();
		List<AbstractLighthouseEditorPage> pages = new ArrayList<AbstractLighthouseEditorPage>();
		pages.addAll(beforePages);
		pages.addAll(afterPages);
		for (AbstractLighthouseEditorPage page : pages) {
			if (page.getPage().isDirty()) {
				page.getPage().doSave(monitor);
			}
		}
	}

	/**
	 * Called during doSave, before the save methods of the dynamically added pages are called.
	 */
	abstract public void save();

	/**
	 * May return null if there are no pages loaded dynamically
	 * @return ID of a PageFactory - may be null
	 */
	abstract public String getFactoryExtensionPoint();

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void dispose() {
		List<AbstractLighthouseEditorPage> pages = new ArrayList<AbstractLighthouseEditorPage>();
		pages.addAll(beforePages);
		pages.addAll(afterPages);
		for (AbstractLighthouseEditorPage page : pages) {
			page.getPage().dispose();
		}
		super.dispose();
	}
	
	/**
	 * Editor pages will set themselfs as selectionprovider when they gain focus.
	 * The multipageselectionprovider will then deliver selection changed events to the platform.
	 * 
	 * @param selectionProvider
	 */
	public void setSelectionProvider(ISelectionProvider selectionProvider) {
		this.selectionProvider.setRealProvider(selectionProvider);
	}
	
	/**
	 * This selection provider is used to report selection changed events to the platform.
	 * It has the capability to choose the active selection provider from editor pages and passes it through.
	 */
	private class MultiPageSelectionProvider implements ISelectionProvider, ISelectionChangedListener {
		
		/**
		 * The selectionprovider registered to this mutlipagethingie
		 */
		private ISelectionProvider realProvider = null;
		
		/**
		 * Registered listeners of the eclipse platform
		 */
		private List<ISelectionChangedListener> listeners = new LinkedList<ISelectionChangedListener>();

		/**
		 * When registering the multipagethingie to the platform, it will register itself as listener.
		 */
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.add(listener);
		}

		/**
		 * Return the selection of the current active selection provider.
		 * 
		 * @return ISelection the current selection or null
		 */
		public ISelection getSelection() {
			if (realProvider == null)
				return null;
			return realProvider.getSelection();
		}

		/**
		 * Called by the platform to deregister listeners.
		 */
		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.remove(listener);
		}

		/**
		 * Called by the editor pages when updating their selections.
		 */
		public void setSelection(ISelection selection) {
			if (realProvider != null)
				realProvider.setSelection(selection);
		}
		
		/**
		 * This method is called by the editor, when an editor page tries to set its own instance as selection provider.
		 * It is than delegated to this multipagethingie.
		 */
		protected void setRealProvider(ISelectionProvider newProvider) {
			if (realProvider != null)
				realProvider.removeSelectionChangedListener(this);
			realProvider = newProvider;
			if (realProvider != null) {
				realProvider.addSelectionChangedListener(this);
				SelectionChangedEvent event = new SelectionChangedEvent(realProvider, getSelection());
				selectionChanged(event);
			}
		}

		/**
		 * Notify the platform listeners about changes in the selection.
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			for (ISelectionChangedListener listener : listeners) {
				listener.selectionChanged(event);
			}
		}
	}
}
