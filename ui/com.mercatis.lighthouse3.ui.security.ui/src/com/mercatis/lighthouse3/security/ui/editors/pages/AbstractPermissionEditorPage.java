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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import com.mercatis.lighthouse3.base.ui.editors.AbstractExtendableFormEditor;
import com.mercatis.lighthouse3.base.ui.editors.AbstractLighthouseEditorPage;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.security.ui.model.AbstractAccessor;
import com.mercatis.lighthouse3.security.ui.providers.AccessorLabelProvider;
import com.mercatis.lighthouse3.ui.environment.base.adapters.DomainBoundEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.ContextAdapter;
import com.mercatis.lighthouse3.ui.security.Role;
import com.mercatis.lighthouse3.ui.security.internal.Security;
import com.mercatis.lighthouse3.ui.service.SecurityModelChangedListener;

/**
 * This class provides some basic functionality to editor pages dealing with
 * users or groups (accessors). <br />
 * The page is segmented in three sections (left, middle and right). Sections
 * for accessor and role selection are provided:
 * {@link #createAccessorSection(ScrolledForm, FormToolkit)},
 * {@link #createRoleSection(ScrolledForm, FormToolkit)}
 * 
 * @see AbstractAccessor
 */
public abstract class AbstractPermissionEditorPage<Accessor extends AbstractAccessor> extends
		AbstractLighthouseEditorPage implements SecurityModelChangedListener {

	/**
	 * The permission viewer is equipped with a context menu. Contribute to this permission menu in the plugin.xml via this ID.
	 */
	public static final String PERMISSION_CONTEXT_MENU_ID = "com.mercatis.lighthouse3.ui.security.ui.permissioneditor.permission.contextmenu";

	/**
	 * The LighthouseDomain this editor is working on
	 */
	protected LighthouseDomain lighthouseDomain;

	/**
	 * The currently selected accessor in the accessorViewer.
	 */
	protected Accessor selectedAccessor = null;

	/**
	 * The currently selected context in the contextViewer.
	 */
	protected String selectedContext = null;

	/**
	 * The focused selection provider for this editor page
	 */
	private ISelectionProvider currentSelectionProvider = null;

	/**
	 * This viewer shows a list of users or groups.
	 */
	private TableViewer accessorViewer;

	/**
	 * This viewer shows a list of available roles. <br />
	 * Matching roles for the current selected context and accessor will be
	 * checked.
	 */
	private CheckboxTableViewer roleViewer;

	/**
	 * Creates a new instance of this class and registers as
	 * SecurityModelChangedListener.
	 * 
	 * @param editor
	 * @param id
	 * @param title
	 */
	@SuppressWarnings("unchecked")
	public AbstractPermissionEditorPage(FormEditor editor, String ID, String title) {
		super(editor, ID, title);
		this.lighthouseDomain = ((GenericEditorInput<LighthouseDomain>) editor.getEditorInput()).getEntity();
		init(editor.getEditorSite(), editor.getEditorInput());
		Security.getService().addSecurityModelChangedListener(this);
	}

	@Override
	public void dispose() {
		super.dispose();
		Security.getService().removeSecurityModelChangedListener(this);
	}

	public LighthouseDomain getLighthouseDomain() {
		return lighthouseDomain;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		form.getBody().setLayout(new GridLayout(3, true));
		form.setText("Permissions on " + LabelConverter.getLabel(lighthouseDomain));
		managedForm.getToolkit().decorateFormHeading(form.getForm());
		FormToolkit toolkit = managedForm.getToolkit();

		createLeftSection(form, toolkit);
		createMiddleSection(form, toolkit);
		createRightSection(form, toolkit);
		createAccessorContextMenu();
		createPermissionContextMenu();
	}

	/**
	 * Will be called by {@link #createFormContent}. The layout of the forms
	 * body is a gridlayout with three columns. <br />
	 * The left section is the first element in the grid.
	 * 
	 * @param form
	 * @param toolkit
	 */
	protected abstract void createLeftSection(ScrolledForm form, FormToolkit toolkit);

	/**
	 * Will be called by {@link #createFormContent}. The layout of the forms
	 * body is a gridlayout with three columns. <br />
	 * The left section is the second element in the grid.
	 * 
	 * @param form
	 * @param toolkit
	 */
	protected abstract void createMiddleSection(ScrolledForm form, FormToolkit toolkit);

	/**
	 * Will be called by {@link #createFormContent}. The layout of the forms
	 * body is a gridlayout with three columns. <br />
	 * The left section is the third element in the grid.
	 * 
	 * @param form
	 * @param toolkit
	 */
	protected abstract void createRightSection(ScrolledForm form, FormToolkit toolkit);

	/**
	 * This is the {@link ISelectionChangedListener} that will be added to the
	 * accessorViewer during {@link createAccessorSection}.
	 * 
	 * @return an ISelectionChangedListener
	 */
	protected abstract ISelectionChangedListener getAccessorSelectionChangedListener();

	/**
	 * Use this method to create the accessor section. <br />
	 * {@link getAccessorSelectionChangedListener} and <br />
	 * {@link getAccessorSectionName} will be called from within this method.
	 * 
	 * @param form
	 * @param toolkit
	 * @param sectionText
	 *            to be displayed as headline of the form.
	 */
	protected void createAccessorSection(ScrolledForm form, FormToolkit toolkit) {
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		section.clientVerticalSpacing = 5;
		section.marginHeight = 3;
		section.marginWidth = 3;
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		section.setLayoutData(gd);
		section.setText(getAccessorSectionName());

		Composite client = toolkit.createComposite(section);
		toolkit.paintBordersFor(client);
		client.setLayout(new FillLayout());

		accessorViewer = new TableViewer(client, SWT.SINGLE | SWT.BORDER);
		getAccessorViewer().setContentProvider(ArrayContentProvider.getInstance());
		getAccessorViewer().setLabelProvider(new AccessorLabelProvider());
		getAccessorViewer().setSorter(new ViewerSorter());
		getAccessorViewer().addSelectionChangedListener(getAccessorSelectionChangedListener());
		getAccessorViewer().getControl().addFocusListener(new FocusListener() {
			

			public void focusGained(FocusEvent e) {
				setCurrentSelectionProvider(getAccessorViewer());
			}

			public void focusLost(FocusEvent e) {
			}
		});

		section.setClient(client);
	}

	@Override
	public ISelectionProvider refreshSelectionProvider() {
		return currentSelectionProvider;
	}

	public void setCurrentSelectionProvider(ISelectionProvider selectionProvider) {
		this.currentSelectionProvider = selectionProvider;
		if (getEditor() instanceof AbstractExtendableFormEditor) {
			((AbstractExtendableFormEditor) getEditor()).setSelectionProvider(currentSelectionProvider);
		}
	}

	/**
	 * Specify a name for the accessor section witch will be displayed at top.
	 * 
	 * @return the name of the section
	 */
	protected abstract String getAccessorSectionName();

	/**
	 * Specify a name for the permission section witch will be displayed at top.
	 * 
	 * @return the name of the section
	 */
	protected String getPermissionSectionName() {
		return "Permissions";
	}
	
	/**
	 * Specify wich type of accessor is edited at this page.
	 * 
	 * @return the class of the edited accessor
	 */
	public abstract Class<?> getEditedAccessorClass();

	/**
	 * Use this method to create the role section. Roles are displayed as a list
	 * of checkboxes.
	 * 
	 * @param form
	 * @param toolkit
	 * @param sectionText
	 */
	protected void createRoleSection(ScrolledForm form, FormToolkit toolkit) {
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		section.clientVerticalSpacing = 5;
		section.marginHeight = 3;
		section.marginWidth = 3;
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		section.setLayoutData(gd);
		section.setText(getPermissionSectionName());

		Composite client = toolkit.createComposite(section);
		toolkit.paintBordersFor(client);
		client.setLayout(new FillLayout());

		roleViewer = CheckboxTableViewer.newCheckList(client, SWT.BORDER);
		roleViewer.setSorter(new ViewerSorter());
		roleViewer.setContentProvider(ArrayContentProvider.getInstance());

		List<String> roles = new LinkedList<String>();
		for (Role role : Role.values()) {
			roles.add(role.roleAsString());
		}
		roleViewer.setInput(roles);
		roleViewer.addCheckStateListener(checkStateListener);

		section.setClient(client);
	}

	/**
	 * This listener is attached to the {@link #roleViewer}. (Un)checking a role
	 * will result in a modified accessor, resulting in a dirty state.
	 */
	private ICheckStateListener checkStateListener = new ICheckStateListener() {

		public void checkStateChanged(CheckStateChangedEvent event) {
			String role = (String) event.getElement();

			if (selectedAccessor == null || selectedContext == null) {
				roleViewer.setAllChecked(false);
				return;
			}

			if (event.getChecked()) {
				selectedAccessor.addRole(role, selectedContext);
			} else {
				selectedAccessor.removeRole(role, selectedContext);
			}

			getEditor().editorDirtyStateChanged();
		}
	};

	/**
	 * Use this method to convert a {@link TreePath} to a string that can be
	 * used as context.
	 * 
	 * @param treePath
	 * @return the context string
	 */
	protected String treePathToString(TreePath treePath) {
		ContextAdapter contextAdapter = (ContextAdapter) Platform.getAdapterManager().getAdapter(treePath.getLastSegment(), ContextAdapter.class);
		String context = contextAdapter.toContext(treePath.getLastSegment());

		DomainBoundEntityAdapter domainBoundEntityAdapter = (DomainBoundEntityAdapter) Platform.getAdapterManager().getAdapter(treePath.getLastSegment(), DomainBoundEntityAdapter.class);
		LighthouseDomain domain = domainBoundEntityAdapter.getLighthouseDomain(treePath.getLastSegment());
		String domainContext = ((ContextAdapter) Platform.getAdapterManager().getAdapter(domain, ContextAdapter.class)).toContext(domain);

		context = context.replaceFirst(domainContext, "");
		if (context.trim().length() == 0)
			context = "/";
		return context;
	}

	/**
	 * This method updates the selection state of the roles checkboxes for the
	 * selected accessor and context.
	 */
	protected void refreshRolesForSelectedAccessorAndContext() {
		roleViewer.setAllGrayed(selectedAccessor == null || selectedContext == null);
		Set<String> roles = null;
		if (selectedAccessor != null) {
			roles = selectedAccessor.getRoles(selectedContext);
		}
		if (roles != null) {
			roleViewer.setCheckedElements(roles.toArray());
		} else {
			roleViewer.setCheckedElements(new Object[] {});
		}
	}

	/**
	 * Get the {@link TableViewer} viewing the available accesssors
	 * 
	 * @return the viewer displaying the accessors
	 */
	public TableViewer getAccessorViewer() {
		return accessorViewer;
	}
	
	/**
	 * Get the {@link CheckboxTableViewer} viewing the available permissions
	 * 
	 * @return the viewer displaying the accessors
	 */
	public CheckboxTableViewer getPermissionViewer() {
		return roleViewer;
	}

	/**
	 * This method should be called by the enclosing editor on saving.
	 */
	public abstract void save();

	@Override
	public abstract boolean isDirty();

	/**
	 * For the "add" actions on the content provider, there will popup a
	 * {@link SelectionDialog}. <br />
	 * Provide a {@link StructuredViewer} for this dialog and fill it with
	 * content/label providers and input.
	 * 
	 * @return new viewer for selection dialog
	 */
	protected abstract StructuredViewer getSelectionDialogViewer(Composite parent);

	/**
	 * Opens a dialog with a {@link StructuredViewer}. To fill the viewer
	 * {@link #getSelectionDialogViewer} will be invoked.
	 */
	protected class SelectionDialog extends Dialog {

		/**
		 * The current selection.
		 */
		private IStructuredSelection selection;

		/**
		 * The title to be displayed in window.
		 */
		private String title;

		/**
		 * Creates a new instance of {@link SelectionDialog}
		 * 
		 * @param title
		 *            Title to be displayed at top.
		 */
		protected SelectionDialog(String title) {
			super(getSite());
			this.title = title;
			setShellStyle(getShellStyle() | SWT.RESIZE);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
		 * .swt.widgets.Composite)
		 */
		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
			getShell().setSize(500, 300);
			getShell().setText(title);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt
		 * .widgets.Composite)
		 */
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			composite.setLayout(new FillLayout());
			StructuredViewer viewer = getSelectionDialogViewer(composite);
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					selection = (IStructuredSelection) event.getSelection();
				}
			});
			return composite;
		}

		/**
		 * Returns the current selection even if this Dialog is closed.
		 * 
		 * @return The current selection.
		 */
		public IStructuredSelection getSelection() {
			return selection;
		}
	}

	/**
	 * Creates the context menu for the accessor viewer and register it to the
	 * platform.
	 */
	private void createAccessorContextMenu() {
		MenuManager menuMgr = new MenuManager(getAccessorSectionName(), getAccessorContextMenuId());
		getSite().registerContextMenu(getAccessorContextMenuId(), menuMgr, getAccessorViewer());
		getAccessorViewer().getControl().setMenu(menuMgr.getMenu());
		Menu contextMenu = menuMgr.createContextMenu(getSite().getShell());
		getAccessorViewer().getTable().setMenu(contextMenu);
	}
	
	/**
	 * Creates the context menu for the permission viewer and register it to the
	 * platform.
	 */
	private void createPermissionContextMenu() {
		MenuManager menuMgr = new MenuManager(getPermissionSectionName(), getPermissionContextMenuId());
		getSite().registerContextMenu(getPermissionContextMenuId(), menuMgr, getPermissionViewer());
		getPermissionViewer().getControl().setMenu(menuMgr.getMenu());
		Menu contextMenu = menuMgr.createContextMenu(getSite().getShell());
		getPermissionViewer().getTable().setMenu(contextMenu);
	}

	/**
	 * Gets the ID of the accessor viewer context menu as is registered in the
	 * plugin.xml
	 * 
	 * @return The context menu ID
	 */
	protected abstract String getAccessorContextMenuId();
	
	
	/**
	 * Gets the ID of the permission viewer context menu as is registered in the
	 * plugin.xml
	 * 
	 * @return The context menu ID
	 */
	protected String getPermissionContextMenuId() {
		return PERMISSION_CONTEXT_MENU_ID;
	}
	
	/**
	 * Gets the currently selected accessor in the accessor viewer
	 * 
	 * @return The selected accessor
	 */
	public Accessor getSelectedAccessor() {
		return selectedAccessor;
	}
	
	/**
	 * Gets the currently selected context in the context viewer
	 * 
	 * @return The selected context
	 */
	public String getSelectedContext() {
		return selectedContext;
	}
	
	public void setAllPermissionsChecked(boolean state) {

		if (selectedAccessor == null || selectedContext == null) {
			roleViewer.setAllChecked(false);
			return;
		}
		
		if (state == true) {
			roleViewer.setAllChecked(true);
			Object[] checkedElements = roleViewer.getCheckedElements();
			for (Object element : checkedElements) {
				if (!selectedAccessor.hasRole(element.toString(), selectedContext)) {
					selectedAccessor.addRole(element.toString(), selectedContext);
				}
			}
		} else {
			TableItem[] allElements = roleViewer.getTable().getItems();
			roleViewer.setAllChecked(false);
			for (TableItem element : allElements) {
				if (selectedAccessor.hasRole(element.getText(), selectedContext)) {
					selectedAccessor.removeRole(element.getText(), selectedContext);
				}
			}
		}
		
		getEditor().editorDirtyStateChanged();
		
	}
}
