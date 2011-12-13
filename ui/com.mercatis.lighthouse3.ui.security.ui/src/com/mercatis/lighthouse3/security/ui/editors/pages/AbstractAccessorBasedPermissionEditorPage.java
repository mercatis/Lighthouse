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
package com.mercatis.lighthouse3.security.ui.editors.pages;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.security.ui.model.AbstractAccessor;
import com.mercatis.lighthouse3.security.ui.model.GroupAccessor;
import com.mercatis.lighthouse3.security.ui.model.UserAccessor;
import com.mercatis.lighthouse3.security.ui.providers.ContextContentProvider;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.Role;

/**
 * This class enhances the {@link AbstractPermissionEditorPage} with accessor based selection.
 * <br />The sections are filled as following:
 * <ul>
 * <li>left section: accessor {@link TableViewer}</li>
 * <li>middle section: a simple context {@link TableViewer}</li>
 * <li>right section: roles selection</li>
 */
public abstract class AbstractAccessorBasedPermissionEditorPage<Accessor extends AbstractAccessor> extends
		AbstractPermissionEditorPage<Accessor> {

	/**
	 * The context viewer is equipped with a context menu. Contribute to this context menu in the plugin.xml via this ID.
	 */
	public static final String CONTEXT_MENU_ID = "com.mercatis.lighthouse3.ui.security.ui.permissioneditor.context.contextmenu";
	
	/**
	 * The viewer viewing the available contexts for a selected accessor.
	 */
	private TableViewer contextViewer;
	
	/**
	 * This list caches the accessors displayed in the accessor viewer.
	 */
	private List<Accessor> accessors;
	
	/**
	 * Deleting a context will not result in an immediate persisting operation.
	 * Instead the editor is marked dirty at least until this set is not empty.
	 */
	protected Set<String> contextsToRemove = new HashSet<String>();

	/**
	 * @param editor
	 * @param ID
	 * @param title
	 */
	public AbstractAccessorBasedPermissionEditorPage(FormEditor editor, String ID, String title) {
		super(editor, ID, title);
	}

	@Override
	protected void createLeftSection(ScrolledForm form, FormToolkit toolkit) {
		createAccessorSection(form, toolkit);
		getAccessorViewer().setInput(getAccessors());
	}

	@Override
	protected void createMiddleSection(ScrolledForm form, FormToolkit toolkit) {
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		section.clientVerticalSpacing = 5;
		section.marginHeight = 3;
		section.marginWidth = 3;
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		section.setLayoutData(gd);
		section.setText("Context Strings");

		Composite client = toolkit.createComposite(section);
		toolkit.paintBordersFor(client);
		client.setLayout(new FillLayout());

		contextViewer = new TableViewer(toolkit.createTable(client, SWT.SINGLE | SWT.BORDER));
		contextViewer.setSorter(new ViewerSorter());
		contextViewer.getControl().addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
				setCurrentSelectionProvider(contextViewer);
			}

			public void focusLost(FocusEvent e) {
			}});
		contextViewer.setContentProvider(ArrayContentProvider.getInstance());
		contextViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object selected = selection.getFirstElement();
				selectedContext = (String) selected;
				refreshRolesForSelectedAccessorAndContext();
			}
		});

		section.setClient(client);
		createContextMenu();
	}

	/**
	 * Creates the context menu for the context viewer and register it to the platform.
	 */
	private void createContextMenu() {
		MenuManager menuMgr = new MenuManager("Contexts", CONTEXT_MENU_ID);
		getSite().registerContextMenu(CONTEXT_MENU_ID, menuMgr, contextViewer);
		contextViewer.getControl().setMenu(menuMgr.getMenu());
		Menu contextMenu = menuMgr.createContextMenu(getSite().getShell());
		contextViewer.getTable().setMenu(contextMenu);
	}

	@Override
	protected void createRightSection(ScrolledForm form, FormToolkit toolkit) {
		createRoleSection(form, toolkit);
	}

	@Override
	protected ISelectionChangedListener getAccessorSelectionChangedListener() {
		return new ISelectionChangedListener() {
			@SuppressWarnings("unchecked")
			public void selectionChanged(SelectionChangedEvent event) {
				selectedAccessor = (Accessor) ((IStructuredSelection) event.getSelection()).getFirstElement();
				selectedContext = null;
				contextViewer.setInput(null);
				contextViewer.setSelection(null);
				if (selectedAccessor != null) {
					contextViewer.setInput(selectedAccessor.getContexts());
					Object contextElement = contextViewer.getElementAt(0);
					if (contextElement != null) {
						contextViewer.setSelection(new StructuredSelection(contextElement),true);
					}
					selectedContext = (String) ((IStructuredSelection)contextViewer.getSelection()).getFirstElement();
				}
				contextsToRemove.clear();
				refreshRolesForSelectedAccessorAndContext();
			}
		};
	}

	/**
	 * Opens a {@link com.mercatis.lighthouse3.security.ui.editors.pages.AbstractPermissionEditorPage.SelectionDialog}
	 */
	public void addContext() {
		SelectionDialog selectContext = new SelectionDialog("Select Context");
		if (selectContext.open() == IDialogConstants.OK_ID) {
			ITreeSelection treeSelection = (ITreeSelection) selectContext.getSelection();
			String context = null;
			for (TreePath treePath : treeSelection.getPaths()) {
				context = treePathToString(treePath);
				selectedAccessor.addContext(context);
				
				// Default Roles
				List<Role> viewRoles = Role.getViewRoles();
				for (Role role : viewRoles) {
					selectedAccessor.addRole(role.roleAsString(), context);
				}
			}
			contextViewer.setInput(selectedAccessor.getContexts());
			contextViewer.refresh();
			if (context != null) {
				contextViewer.setSelection(new StructuredSelection(context),true);
			}
			getEditor().editorDirtyStateChanged();
		}
	}

	@Override
	protected StructuredViewer getSelectionDialogViewer(Composite parent) {
		StructuredViewer viewer = new TreeViewer(parent, SWT.SINGLE);
		viewer.setContentProvider(new ContextContentProvider());
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		viewer.setInput(lighthouseDomain);
		return viewer;
	}

	@SuppressWarnings("unchecked")
	public void removeContext(String context) {
		List<String> contexts = (List<String>) contextViewer.getInput();
		if (contexts.contains(context)) {
			selectedAccessor.removeContext(context);
			contextViewer.setInput(selectedAccessor.getContexts());
			contextViewer.refresh();
			getEditor().editorDirtyStateChanged();
		}
	}

	@Override
	public boolean isDirty() {
		for (Accessor accessor : getAccessors()) {
			if (accessor.isDirty())
				return true;
		}
		return false;
	}

	@Override
	public void save() {
		for (Accessor accessor : accessors) {
			if (accessor.isDirty())
				accessor.save();
		}
		getEditor().editorDirtyStateChanged();
	}

	/**
	 * Gets the available accessors lazily
	 * 
	 * @return A <code>List&lt;Accessor&gt;</code>
	 */
	protected List<Accessor> getAccessors() {
		if (accessors == null)
			accessors = loadAccessors();
		return accessors;
	}
	
	/**
	 * {@inheritDoc}
	 * <br />Converts the {@link Group} into a {@link GroupAccessor} and removes it from cached and viewed accessors.
	 */
	public void groupDeleted(Group group) {
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomain(group.getLighthouseDomain());
		GroupAccessor accessor = new GroupAccessor(lighthouseDomain, group);
		getAccessors().remove(accessor);
		if (getAccessorViewer() != null) {
			getAccessorViewer().remove(accessor);
			getAccessorViewer().refresh();
		}
	}

	/**
	 * {@inheritDoc}
	 * <br />Converts the {@link User} into a {@link UserAccessor} and removes it from cached and viewed accessors.
	 */
	public void userDeleted(User user) {
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomain(user.getLighthouseDomain());
		UserAccessor accessor = new UserAccessor(lighthouseDomain, user);
		getAccessors().remove(accessor);
		if (getAccessorViewer() != null) {
			getAccessorViewer().remove(accessor);
			getAccessorViewer().refresh();
		}
	}

	/**
	 * Loads the available accessors from the LH3 security server.
	 * 
	 * @return A <code>List&lt;Accessor&gt;</code>
	 */
	protected abstract List<Accessor> loadAccessors();
}
