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
package com.mercatis.lighthouse3.ui.environment.editors.pages;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.base.ui.widgets.chooser.SecuritySelectionListLabelProvider;
import com.mercatis.lighthouse3.base.ui.widgets.chooser.SecuritySelectionListModificationListener;
import com.mercatis.lighthouse3.base.ui.widgets.chooser.SecuritySelectionListWidget;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeEvent;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeListener;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;


public class ProcessTaskChildEditorFormPage extends FormPage implements SecuritySelectionListModificationListener<ProcessTask>, IPropertyChangeListener, DomainChangeListener {
	
	public static final String ID = ProcessTaskChildEditorFormPage.class.getName();
	
	private ProcessTask processTask;
	
	private LighthouseDomain lighthouseDomain;
	
	private SecuritySelectionListWidget<ProcessTask> chooser;
	
	private SecuritySelectionListLabelProvider<ProcessTask> ptLabelProv = new SecuritySelectionListLabelProvider<ProcessTask>() {
		public String getLabel(ProcessTask pt) {
			return pt.getCode();
		}
	};

	@Override
	public void dispose() {
		CommonBaseActivator.getPlugin().getDomainService().removeDomainChangeListener(this);
		super.dispose();
	}

	@SuppressWarnings("unchecked")
	public ProcessTaskChildEditorFormPage(FormEditor editor) {
		super(editor, ID, null);
		this.processTask = ((GenericEditorInput<ProcessTask>) getEditor().getEditorInput()).getEntity();
		this.lighthouseDomain = ((GenericEditorInput<ProcessTask>) getEditor().getEditorInput()).getDomain();
		CommonBaseActivator.getPlugin().getDomainService().addDomainChangeListener(this);
	}

	private void refresh() {
		// there is no need to refresh if we ain't got no view
		if (chooser == null)
			return;
		
		// fetch all "available" process tasks (process tasks that do not have a parent process task)
		Set<ProcessTask> available = new HashSet<ProcessTask>(lighthouseDomain.getProcessTaskContainer().getProcessTasks());
		
		// remove this.processTask from the available components (if included)
		available.remove(this.processTask);
		
		// remove all parent process tasks of this.processTask from the available components (if included)
		ProcessTask parent = this.processTask.getParentEntity();
		while (parent != null) {
			available.remove(parent);
			parent = parent.getParentEntity();
		}
		
		// remove all process tasks that the current user is not allowed to view & drag
		for (Iterator<ProcessTask> it = available.iterator(); it.hasNext();) {
			ProcessTask task = it.next();
			if (!CodeGuard.hasRole(Role.PROCESS_TASK_VIEW, task)) {
				it.remove();
			}
		}
		
		// fetch all direct sub process tasks from this.processTask
		Set<ProcessTask> selected = this.processTask.getDirectSubEntities();
		
		// remove all process tasks that the current user is not allowed to view & drag
		for (Iterator<ProcessTask> it = selected.iterator(); it.hasNext();) {
			ProcessTask task = it.next();
			if (!CodeGuard.hasRole(Role.PROCESS_TASK_VIEW, task)) {
				it.remove();
			}
		}
		available.addAll(selected);

		chooser.setItems(available, Role.PROCESS_TASK_DRAG);
		chooser.setSelected(selected);
		
		getEditor().editorDirtyStateChanged();
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		form.setText("Children of: " + LabelConverter.getLabel(processTask));
		toolkit.decorateFormHeading(form.getForm());
		form.getBody().setLayout(new FillLayout());

		createHeaderSection(form, toolkit);
		refresh();
	}

	private void createHeaderSection(final ScrolledForm form, FormToolkit toolkit) {
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		section.clientVerticalSpacing = 5;
		section.marginHeight = 8;
		section.marginWidth = 8;

		Composite client = toolkit.createComposite(section);
		client.setLayout(new FillLayout());
		toolkit.paintBordersFor(client);

		chooser = new SecuritySelectionListWidget<ProcessTask>(client, ptLabelProv);
		chooser.addModifiycationListener(this);

		section.setClient(client);
	}

	@Override
	public boolean isDirty() {
		if (chooser==null)
			return false;
		return chooser.getModified(true).size() != 0 || chooser.getModified(false).size() != 0;
	}

	public void updateModel() {
		Set<ProcessTask> removedItems = chooser.getModified(false);
		boolean allRemoved = true;
		for (ProcessTask processTask : removedItems) {
			Set<Deployment> deployments = processTask.getDeployments();
			boolean remove = true;
			for (Deployment deployment : deployments) {
				if (CommonBaseActivator.getPlugin().getDomainService().isDeploymentPartOfStatusTemplate(this.processTask, deployment)) {
					remove = false;
				}
			}
			if (remove) {
				this.processTask.removeSubEntity(processTask);
			}
			else {
				allRemoved = false;
			}
		}
		if (!allRemoved) {
			Shell shell = Display.getCurrent().getActiveShell();
			MessageDialog.openWarning(shell, "ProcessTasks not detached", "One or more processtasks were not detached because their deployments are part of a status.");
		}
		
		Set<ProcessTask> addedItems = chooser.getModified(true);
		for (ProcessTask task : addedItems) {
			processTask.addSubEntity(task);
		}
		
		getEditor().editorDirtyStateChanged();
	}

	public void propertyChange(PropertyChangeEvent event) {
		refresh();
	}

	public void domainChange(DomainChangeEvent event) {
		refresh();
	}

	public void onListItemModified(ProcessTask item, boolean enabled, boolean modified) {
		getEditor().editorDirtyStateChanged();
	}
}
