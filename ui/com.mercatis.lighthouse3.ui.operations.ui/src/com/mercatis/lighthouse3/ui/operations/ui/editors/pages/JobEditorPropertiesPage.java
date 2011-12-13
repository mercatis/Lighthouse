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
package com.mercatis.lighthouse3.ui.operations.ui.editors.pages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.domainmodel.operations.Operation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationCall;
import com.mercatis.lighthouse3.domainmodel.operations.Parameter;
import com.mercatis.lighthouse3.ui.operations.ui.widgets.CronExpressionWidget;
import com.mercatis.lighthouse3.ui.operations.ui.widgets.CronExpressionWidgetListener;


public class JobEditorPropertiesPage extends FormPage implements CronExpressionWidgetListener {
	
	public static final String ID = JobEditorPropertiesPage.class.getName();
	
	private Job job;
	
	private String scheduleExpression;
	private CronExpressionWidget cronExpressionWidget;
	private Text code;
	private Text contact;
	private Text contactEmail;
	private Text description;
	private Text longName;
	private Operation operation;
	private OperationCall operationCall;

	public JobEditorPropertiesPage(FormEditor editor, Operation operation, OperationCall operationCall) {
		super(editor, ID, null);
		this.operation = operation;
		this.operationCall = operationCall;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		job = ((GenericEditorInput<Job>) getEditor().getEditorInput()).getEntity();
		
		String activeVariant = null;
		if (!operationCall.getParameterValues().isEmpty()) {
			String firstParameterWithValueName = operationCall.getParameterValues().iterator().next().getName();
			for (Parameter parameter : this.operation.getParameters()) {
				if (parameter.getName().equals(firstParameterWithValueName)) {
					activeVariant = parameter.getVariant();
					break;
				}
			}
		}

		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		form.setText("Job: " + LabelConverter.getLabel(job)
				+ (activeVariant != null
				? "\nGroup: " + activeVariant
				: ""));
		toolkit.decorateFormHeading(form.getForm());
		form.getBody().setLayout(new GridLayout(1, true));

		createPropertySection(form, toolkit);
		createCronSection(form, toolkit);
	}

	private void createPropertySection(ScrolledForm form, FormToolkit toolkit) {
		Section section = toolkit.createSection(form.getBody(), Section.EXPANDED | Section.TITLE_BAR);
		section.clientVerticalSpacing = 5;
		section.marginHeight = 3;
		section.marginWidth = 3;
		GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		section.setLayoutData(gridData);
		section.setText("Properties");

		Composite client = toolkit.createComposite(section);
		toolkit.paintBordersFor(client);
		client.setLayout(new GridLayout(2, false));

		toolkit.createLabel(client, "Code:");
		code = toolkit.createText(client, job.getCode());
		code.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		code.setEditable(false);
		code.setEnabled(false);

		toolkit.createLabel(client, "Long name:");
		longName = toolkit.createText(client, job.getLongName());
		longName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		longName.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Description:");
		description = toolkit.createText(client, job.getDescription());
		description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		description.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Contact:");
		contact = toolkit.createText(client, job.getContact());
		contact.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		contact.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Email:");
		contactEmail = toolkit.createText(client, job.getContactEmail());
		contactEmail.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		contactEmail.addModifyListener(modifyListener);

		section.setClient(client);
	}
	
	private void createCronSection(ScrolledForm form, FormToolkit toolkit) {
		Section section = toolkit.createSection(form.getBody(), Section.EXPANDED | Section.TITLE_BAR);
		section.clientVerticalSpacing = 5;
		section.marginHeight = 3;
		section.marginWidth = 3;
		GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		section.setLayoutData(gridData);
		section.setText("Schedule Expression");

		Composite client = toolkit.createComposite(section);
		toolkit.paintBordersFor(client);
		client.setLayout(new GridLayout(2, false));

		toolkit.createLabel(client, "Expression:");
		cronExpressionWidget = new CronExpressionWidget(client, SWT.NONE);
		cronExpressionWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cronExpressionWidget.setExpression(job.getScheduleExpression());
		scheduleExpression = cronExpressionWidget.getExpression();
		cronExpressionWidget.addWidgetListener(this);
		toolkit.adapt(cronExpressionWidget);

		section.setClient(client);
	}
	
	public void updateModel() {
		job.setScheduleExpression(scheduleExpression);
		job.setCode(code.getText());
		job.setContact(contact.getText());
		job.setContactEmail(contactEmail.getText());
		job.setDescription(description.getText());
		job.setLongName(longName.getText());
	}
	
	@Override
	public boolean isDirty() {
		if (code == null)
			return false;
		return !scheduleExpression.equals(getNullcheckedString(job.getScheduleExpression()))
			|| !code.getText().equals(getNullcheckedString(job.getCode()))
			|| !contact.getText().equals(getNullcheckedString(job.getContact()))
			|| !contactEmail.getText().equals(getNullcheckedString(job.getContactEmail()))
			|| !description.getText().equals(getNullcheckedString(job.getDescription()))
			|| !longName.getText().equals(getNullcheckedString(job.getLongName()));
	}
	
	private String getNullcheckedString(String string) {
		if (string == null)
			return "";
		return string;
	}

	private ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			getEditor().editorDirtyStateChanged();
		}
	};

	public void modified(String expression, String second, String minute, String hour, String dayOfMonth, String month,
			String dayOfWeek, String year) {
		this.scheduleExpression = expression;
		getEditor().editorDirtyStateChanged();
	}

	public void updateHint(String hint) {
	}
}
