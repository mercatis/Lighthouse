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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.ide.IDE;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;
import com.mercatis.lighthouse3.ui.environment.editors.SoftwareComponentEditor;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;

public class DeploymentRichEditorFormPage extends FormPage {

	public static final String ID = DeploymentRichEditorFormPage.class.getName();

	private Deployment deployment;

	private Text locationText;
	private Text descriptionText;
	private Text contactText;
	private Text contactEmailText;

	public DeploymentRichEditorFormPage(FormEditor editor) {
		super(editor, ID, null);
	}

	private ModifyListener modifyListener = new ModifyListener() {

		public void modifyText(ModifyEvent e) {
			getEditor().editorDirtyStateChanged();
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		deployment = ((GenericEditorInput<Deployment>) getEditor().getEditorInput()).getEntity();

		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		form.setText("Properties for: " + LabelConverter.getLabel(deployment));
		toolkit.decorateFormHeading(form.getForm());
		form.getBody().setLayout(new GridLayout(2, false));

		createHeaderSection(form, toolkit);
		
		if (!CodeGuard.hasRole(Role.DEPLOYMENT_MODIFY, deployment)) {
			descriptionText.setEditable(false);
			descriptionText.setEnabled(false);
			contactText.setEditable(false);
			contactText.setEnabled(false);
			contactEmailText.setEditable(false);
			contactEmailText.setEnabled(false);
		}
	}

	private void createHeaderSection(ScrolledForm form, FormToolkit toolkit) {
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		section.clientVerticalSpacing = 5;
		section.marginHeight = 3;
		section.marginWidth = 3;
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		section.setLayoutData(gd);

		Composite client = toolkit.createComposite(section);
		toolkit.paintBordersFor(client);
		client.setLayout(new GridLayout(2, false));

		toolkit.createLabel(client, "Location:");
		locationText = toolkit.createText(client, deployment.getLocation());
		locationText.setEditable(false);
		locationText.setEnabled(false);
		locationText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		locationText.addModifyListener(modifyListener);

		Hyperlink link = toolkit.createHyperlink(client, "SoftwareComponent:", SWT.NONE);
		link.addHyperlinkListener(new IHyperlinkListener() {

			@SuppressWarnings("unchecked")
			public void linkActivated(HyperlinkEvent e) {
				GenericEditorInput<SoftwareComponent> input = new GenericEditorInput<SoftwareComponent>(
						((GenericEditorInput<Deployment>) getEditor().getEditorInput()).getDomain(), deployment
								.getDeployedComponent());

				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, input, SoftwareComponentEditor.class.getName());
				} catch (PartInitException ex) {
					CommonUIActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, ex.getMessage(), ex));
				}
			}

			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkExited(HyperlinkEvent e) {
			}

		});
		Text componentText = toolkit.createText(client, (deployment.getDeployedComponent() == null ? ""
				: LabelConverter.getLabel(deployment.getDeployedComponent())));
		componentText.setEditable(false);
		componentText.setEnabled(false);
		componentText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		toolkit.createLabel(client, "Description:");
		descriptionText = toolkit.createText(client, deployment.getDescription());
		descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		descriptionText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Contact:");
		contactText = toolkit.createText(client, deployment.getContact());
		contactText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		contactText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Email:");
		contactEmailText = toolkit.createText(client, deployment.getContactEmail());
		contactEmailText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		contactEmailText.addModifyListener(modifyListener);

		section.setClient(client);
	}

	@Override
	public boolean isDirty() {
		if (descriptionText == null)
			return false;
		if (descriptionText.getText().length() > 0 && deployment.getDescription() == null
				|| !descriptionText.getText().equals(deployment.getDescription())
				&& deployment.getDescription() != null)
			return true;
		if (contactText.getText().length() > 0 && deployment.getContact() == null
				|| !contactText.getText().equals(deployment.getContact()) && deployment.getContact() != null)
			return true;
		if (contactEmailText.getText().length() > 0 && deployment.getContactEmail() == null
				|| !contactEmailText.getText().equals(deployment.getContactEmail())
				&& deployment.getContactEmail() != null)
			return true;
		return false;
	}

	public void updateModel() {
		deployment.setLocation(locationText.getText());
		deployment.setDescription(descriptionText.getText());
		deployment.setContact(contactText.getText());
		deployment.setContactEmail(contactEmailText.getText());
		getEditor().editorDirtyStateChanged();
	}
}
