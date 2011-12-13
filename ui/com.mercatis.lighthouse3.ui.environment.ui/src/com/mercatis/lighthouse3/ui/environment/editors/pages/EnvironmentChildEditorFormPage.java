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
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeEvent;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeListener;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;


public class EnvironmentChildEditorFormPage extends FormPage implements SecuritySelectionListModificationListener<Environment>, DomainChangeListener {

	public static final String ID = EnvironmentChildEditorFormPage.class.getName();
	
	private LighthouseDomain lighthouseDomain;
	
	private Environment environment;
	
	private SecuritySelectionListWidget<Environment> chooser;
	
	private SecuritySelectionListLabelProvider<Environment> envLabelProv = new SecuritySelectionListLabelProvider<Environment>() {
		public String getLabel(Environment e) {
			return e.getCode();
		}
	};
	
	@SuppressWarnings("unchecked")
	public EnvironmentChildEditorFormPage(FormEditor editor) {
		super(editor, ID, null);
		this.environment = ((GenericEditorInput<Environment>) getEditor().getEditorInput()).getEntity();
		this.lighthouseDomain = ((GenericEditorInput<Environment>) getEditor().getEditorInput()).getDomain();
		CommonBaseActivator.getPlugin().getDomainService().addDomainChangeListener(this);
	}

	@Override
	public void dispose() {
		CommonBaseActivator.getPlugin().getDomainService().removeDomainChangeListener(this);
		super.dispose();
	}
	
	private void refresh() {
		// there is no need to refresh if we ain't got no view
		if (chooser == null)
			return;
		
		// fetch all "available" environments (environments that do not have a parent environment)
		Set<Environment> available = new HashSet<Environment>(lighthouseDomain.getEnvironmentContainer().getEnvironments());
		
		// remove this.environment from the available components (if included)
		available.remove(this.environment);
		
		// remove all parent environments of this.environment from the available components (if included)
		Environment parent = this.environment.getParentEntity();
		while (parent != null) {
			available.remove(parent);
			parent = parent.getParentEntity();
		}
		
		// remove all environments that the current user is not allowed to view & drag
		for (Iterator<Environment> it = available.iterator(); it.hasNext();) {
			Environment environment = it.next();
			if (!CodeGuard.hasRole(Role.ENVIRONMENT_VIEW, environment)) {
				it.remove();
			}
		}
		
		// fetch all direct sub environments from this.environment
		Set<Environment> selected = this.environment.getDirectSubEntities();
		
		// remove all environments that the current user is not allowed to view & drag
		for (Iterator<Environment> it = selected.iterator(); it.hasNext();) {
			Environment environment = it.next();
			if (!CodeGuard.hasRole(Role.ENVIRONMENT_VIEW, environment)) {
				it.remove();
			}
		}
		available.addAll(selected);
		
		chooser.setItems(available, Role.ENVIRONMENT_DRAG);
		chooser.setSelected(selected);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		form.setText("Children of: " + LabelConverter.getLabel(environment));
		toolkit.decorateFormHeading(form.getForm());
		form.getBody().setLayout(new FillLayout());

		createHeaderSection(form, toolkit);
		refresh();
	}

	private void createHeaderSection(ScrolledForm form, FormToolkit toolkit) {
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		section.clientVerticalSpacing = 5;
		section.marginHeight = 8;
		section.marginWidth = 8;

		Composite client = toolkit.createComposite(section);
		client.setLayout(new FillLayout());
		toolkit.paintBordersFor(client);

		chooser = new SecuritySelectionListWidget<Environment>(client, envLabelProv);
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
		Set<Environment> removedItems = chooser.getModified(false);
		boolean allRemoved = true;
		for (Environment environment : removedItems) {
			Set<Deployment> deployments = environment.getDeployments();
			boolean remove = true;
			for (Deployment deployment : deployments) {
				if (CommonBaseActivator.getPlugin().getDomainService().isDeploymentPartOfStatusTemplate(this.environment, deployment)) {
					remove = false;
				}
			}
			if (remove) {
				this.environment.removeSubEntity(environment);
			}
			else {
				allRemoved = false;
			}
		}
		if (!allRemoved) {
			Shell shell = Display.getCurrent().getActiveShell();
			MessageDialog.openWarning(shell, "Environments not detached", "One or more environments were not detached because their deployments are part of a status.");
		}
		
		Set<Environment> addedItems = chooser.getModified(true);
		for (Environment environment : addedItems) {
			this.environment.addSubEntity(environment);
		}
		
		getEditor().editorDirtyStateChanged();
	}

	public void domainChange(DomainChangeEvent event) {
		refresh();
	}

	public void onListItemModified(Environment item, boolean enabled, boolean modified) {
		getEditor().editorDirtyStateChanged();
	}
}
