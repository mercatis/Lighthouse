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

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
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
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeEvent;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeListener;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;

/**
 * This editor page provides fuctions for detaching and attaching deployments to a DeploymentCarrier
 * 
 */
public class AttachedDeploymentsEditorFormPage extends FormPage implements SecuritySelectionListModificationListener<Deployment>, DomainChangeListener {

	public static final String ID = AttachedDeploymentsEditorFormPage.class.getName();
	
	private DeploymentCarryingDomainModelEntity<?> carrier;
	
	private SecuritySelectionListWidget<Deployment> chooser;
	
	private LighthouseDomain lighthouseDomain;
	
	private SecuritySelectionListLabelProvider<Deployment> dLabelProv = new SecuritySelectionListLabelProvider<Deployment>() {
		public String getLabel(Deployment d) {
			return d.getDeployedComponent().getCode() + " @ " + d.getLocation();
		}
	};
	
	public AttachedDeploymentsEditorFormPage(FormEditor editor) {
		super(editor, ID, null);
		this.carrier = (DeploymentCarryingDomainModelEntity<?>) ((GenericEditorInput<?>) getEditor().getEditorInput()).getEntity();
		this.lighthouseDomain = ((GenericEditorInput<?>) getEditor().getEditorInput()).getDomain();
		CommonBaseActivator.getPlugin().getDomainService().addDomainChangeListener(this);
	}

	@Override
	public void dispose() {
		CommonBaseActivator.getPlugin().getDomainService().removeDomainChangeListener(this);
		super.dispose();
	}

	private Comparator<Deployment> deploymentComparator = new Comparator<Deployment>(){

		public int compare(Deployment o1, Deployment o2) {
			String s1 = LabelConverter.getLabel(o1);
			String s2 = LabelConverter.getLabel(o2);
			return s1.compareTo(s2);
		}
		
	};
	
	private void refresh() {
		// there is no need to refresh if we ain't got no view
		if (chooser == null)
			return;

		// fetch all "available" deployments
		TreeSet<Deployment> available = new TreeSet<Deployment>(deploymentComparator);
		available.addAll(lighthouseDomain.getDeploymentContainer().getAllDeployments());
		
		// remove all deployments that the current user is not allowed to view & install
		for (Iterator<Deployment> it = available.iterator(); it.hasNext();) {
			Deployment deployment = it.next();
			if (!CodeGuard.hasRole(Role.DEPLOYMENT_VIEW, deployment)) {
				it.remove();
			}
		}
		chooser.setItems(available, Role.DEPLOYMENT_INSTALL);
		
		// fetch all deployments that are already attached to this.carrier
		Set<Deployment> selected = new TreeSet<Deployment>(deploymentComparator);
		selected.addAll(this.carrier.getDeployments());
		
		// remove all deployments that the current user is not allowed to view & drag
		for (Iterator<Deployment> it = selected.iterator(); it.hasNext();) {
			Deployment deployment = it.next();
			if (!CodeGuard.hasRole(Role.DEPLOYMENT_VIEW, deployment)) {
				it.remove();
			}
		}
		chooser.setSelected(selected);
		getEditor().editorDirtyStateChanged();
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		form.setText("Deployments on: " + LabelConverter.getLabel(carrier));
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

		chooser = new SecuritySelectionListWidget<Deployment>(client, dLabelProv);
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
		Set<Deployment> removedItems = chooser.getModified(false);
		boolean allDetached = true;
		for (Deployment deployment : removedItems) {
			if (!CommonBaseActivator.getPlugin().getDomainService().isDeploymentPartOfStatusTemplate(carrier, deployment)) {
				carrier.detachDeployment(deployment);
			} else {
				allDetached = false;
			}
		}
		if (!allDetached) {
			Shell shell = Display.getCurrent().getActiveShell();
			MessageDialog.openWarning(shell, "Deployments not detached", "One or more deployments were not detached because they are part of a status.");
		}
		
		Set<Deployment> addedItems = chooser.getModified(true);
		for (Deployment deployment : addedItems) {
			carrier.attachDeployment(deployment);
		}
		
		getEditor().editorDirtyStateChanged();
	}

	public void domainChange(DomainChangeEvent event) {
		refresh();
	}

	public void onListItemModified(Deployment item, boolean enabled, boolean modified) {
		getEditor().editorDirtyStateChanged();
	}
}
