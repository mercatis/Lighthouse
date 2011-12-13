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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
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
import com.mercatis.lighthouse3.ui.service.SecurityModelChangedListener;

/**
 * This class enhances the {@link AbstractPermissionEditorPage} with context based selection.
 * <br />The sections are filled as following:
 * <ul>
 * <li>left section: context {@link TreeViewer}</li>
 * <li>middle section: accessor {@link TableViewer}</li>
 * <li>right section: roles selection</li>
 */
public abstract class AbstractContextBasedPermissionEditorPage<Accessor extends AbstractAccessor> extends
		AbstractPermissionEditorPage<Accessor> implements SecurityModelChangedListener {

	/**
	 * Maps a {@link List} of accessors to a specific context
	 */
	private HashMap<String, List<Accessor>> accessorsAtContext = new HashMap<String, List<Accessor>>();
	
	/**
	 * The added accessors. If this {@link Set} is not empty, this page may be dirty.
	 * The accessors are added to a specific context and should be persisted on save.
	 */
	private Set<AbstractAccessor> addedAccessors = new HashSet<AbstractAccessor>();
	
	/**
	 * The removed accessors. If this {@link Set} is not empty, this page may be dirty.
	 * The accessors are removed from a specific context and should be persisted on save.
	 */
	private Set<AbstractAccessor> removedAccessors = new HashSet<AbstractAccessor>();

	/**
	 * @param editor
	 * @param ID
	 * @param title
	 */
	public AbstractContextBasedPermissionEditorPage(FormEditor editor, String ID, String title) {
		super(editor, ID, title);
	}

	@Override
	protected void createLeftSection(ScrolledForm form, FormToolkit toolkit) {
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		section.clientVerticalSpacing = 5;
		section.marginHeight = 3;
		section.marginWidth = 3;
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		section.setLayoutData(gd);
		section.setText("Context Path");

		Composite client = toolkit.createComposite(section);
		toolkit.paintBordersFor(client);
		client.setLayout(new FillLayout());
		
		final TreeViewer contextViewer = new TreeViewer(toolkit.createTree(client, SWT.SINGLE | SWT.BORDER));
		contextViewer.setContentProvider(new ContextContentProvider());
		contextViewer.setLabelProvider(new WorkbenchLabelProvider());
		contextViewer.setInput(lighthouseDomain);
		contextViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@SuppressWarnings("unchecked")
			public void selectionChanged(SelectionChangedEvent event) {
				ITreeSelection selection = (ITreeSelection) event.getSelection();
				for (TreePath treePath : selection.getPaths()) {
					selectedContext = treePathToString(treePath);
					
					IStructuredSelection currentAccessorSelection = (IStructuredSelection) getAccessorViewer().getSelection();
					getAccessorViewer().setInput(getAccessorsForContext(selectedContext));
					getAccessorViewer().setSelection(currentAccessorSelection, true);
					selectedAccessor = (Accessor) ((IStructuredSelection)getAccessorViewer().getSelection()).getFirstElement();
					refreshRolesForSelectedAccessorAndContext();
				}
				getEditor().editorDirtyStateChanged();
			}
		});
		
		section.setClient(client);
	}

	@Override
	protected void createMiddleSection(ScrolledForm form, FormToolkit toolkit) {
		createAccessorSection(form, toolkit);
	}
	
	/**
	 * This method lets this editor page add an accessor to the viewer.
	 * It will call {@link #selectAccessor} to open a {@link SelectionDialog}.
	 */
	public void addAccessor() {
		addAccessor(selectAccessor());
	}
	
	/**
	 * Adds the given accessor to the editor.
	 * 
	 * @param accessor to add
	 */
	@SuppressWarnings("unchecked")
	public void addAccessor(AbstractAccessor accessor) {
		if (accessor == null)
			return;
		
		//the accessor is added to the currently selected context
		accessor.addContext(selectedContext);

		//adds the selected accessor to the set for special drity/persist treatment
		addedAccessors.add(accessor);

		//removes the selected accessor if it was removed before
		removedAccessors.remove(accessor);
		
		getAccessorsForContext(selectedContext).add((Accessor) accessor);
		getAccessorViewer().setInput(getAccessorsForContext(selectedContext));
		getAccessorViewer().refresh();
		getEditor().editorDirtyStateChanged();
	}
	
	/**
	 * Opens a dialog and lets the user select an {@link AbstractAccessor} to add to the accessor viewer.
	 * 
	 * @see #addAccessor
	 * @return The selected accessor
	 */
	protected abstract AbstractAccessor selectAccessor();
	
	/**
	 * Removes the given {@link AbstractAccessor} from the viewer.
	 * 
	 * @param accessor
	 */
	@SuppressWarnings("unchecked")
	public void removeAccessor(AbstractAccessor accessor) {
		//the accessor is removed from the currently selected context
		accessor.removeContext(selectedContext);
		
		//add the accessor to the set for dirty/persisting thingies
		removedAccessors.add(accessor);
		
		//removed from this set if it was added before
		addedAccessors.remove(accessor);
		
		getAccessorsForContext(selectedContext).remove((Accessor) accessor);
		getAccessorViewer().setInput(getAccessorsForContext(selectedContext));
		getAccessorViewer().refresh();
		getEditor().editorDirtyStateChanged();
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
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object selected = selection.getFirstElement();
				selectedAccessor = (Accessor) selected;
				refreshRolesForSelectedAccessorAndContext();
				getEditor().editorDirtyStateChanged();
			}
		};
	}
	
	/**
	 * Gets the accessors for the given context.
	 * 
	 * @param context The context to search for in the accessors
	 * @return The accessors assigned to the given context
	 */
	private List<Accessor> getAccessorsForContext(String context) {
		List<Accessor> accessors = accessorsAtContext.get(context);
		if (accessors == null) {
			accessors = loadAccessorsForContext(context);
			accessorsAtContext.put(context, accessors);
		}
		return accessors;
	}
	
	@Override
	public boolean isDirty() {
		for (AbstractAccessor accessor : addedAccessors) {
			if (accessor.isDirty())
				return true;
		}
		for (AbstractAccessor accessor : removedAccessors) {
			if (accessor.isDirty())
				return true;
		}
		for (Accessor accessor : getAllCachedAccessors()) {
			if (accessor.isDirty())
				return true;
		}
		return false;
	}

	@Override
	public void save() {
		for (Accessor accessor : getAllCachedAccessors()) {
			if (accessor.isDirty())
				accessor.save();
		}
		for (AbstractAccessor accessor : addedAccessors) {
			if (accessor.isDirty())
				accessor.save();
		}
		addedAccessors.clear();
		for (AbstractAccessor accessor : removedAccessors) {
			if (accessor.isDirty())
				accessor.save();
		}
		removedAccessors.clear();
		getEditor().editorDirtyStateChanged();
	}
	
	/**
	 * Gets all the accessors cached at {@link #accessorsAtContext}.
	 * 
	 * @return All cached accessors
	 */
	private Set<Accessor> getAllCachedAccessors() {
		Set<Accessor> accessors = new HashSet<Accessor>();
		for (List<Accessor> cache : accessorsAtContext.values()) {
			accessors.addAll(cache);
		}
		return accessors;
	}

	/**
	 * Loads the accessors for the given context from the LH3 security server
	 * 
	 * @param context The context to search for in the accessors
	 * @return The accessors assigned to the given context
	 */
	protected abstract List<Accessor> loadAccessorsForContext(String context);

	/**
	 * {@inheritDoc}
	 * Converts the Group to an {@link GroupAccessor} and calls {@link #deleteAccessor}.
	 * 
	 */
	public void groupDeleted(Group group) {
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomain(group.getLighthouseDomain());
		deleteAccessor(new GroupAccessor(lighthouseDomain, group));
	}

	/**
	 * {@inheritDoc}
	 * Converts the User to an {@link UserAccessor} and calls {@link #deleteAccessor}.
	 */
	public void userDeleted(User user) {
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomain(user.getLighthouseDomain());
		deleteAccessor(new UserAccessor(lighthouseDomain, user));
	}

	/**
	 * {@inheritDoc}
	 * Do nothing as a created {@link Group} has no context and will not be displayed
	 */
	public void groupCreated(Group group) {}

	/**
	 * {@inheritDoc}
	 * Do nothing as a created {@link User} has no context and will not be displayed
	 */
	public void userCreated(User user) {}

	/**
	 * Deletes the given accessor from all caching collections - it was never here.
	 * 
	 * @param deliquent {@link AbstractAccessor} to delete
	 */
	private void deleteAccessor(AbstractAccessor deliquent) {
		for (List<Accessor> accessors : accessorsAtContext.values()) {
			accessors.remove(deliquent);
		}
		removedAccessors.remove(deliquent);
		addedAccessors.remove(deliquent);
		if (getAccessorViewer() != null)
			getAccessorViewer().refresh();
		getEditor().editorDirtyStateChanged();
	}
}
