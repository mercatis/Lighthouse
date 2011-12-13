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
/**
 * 
 */
package com.mercatis.lighthouse3.ui.operations.ui.editors.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.domainmodel.operations.Operation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationCall;
import com.mercatis.lighthouse3.domainmodel.operations.Parameter;
import com.mercatis.lighthouse3.domainmodel.operations.ParameterValue;
import com.mercatis.lighthouse3.ui.operations.ui.widgets.ParameterComposite;
import com.mercatis.lighthouse3.ui.operations.ui.widgets.ParameterCompositeStatusListener;


public class JobEditorParameterPage extends FormPage implements ParameterCompositeStatusListener {

	public static final String ID = JobEditorParameterPage.class.getName();

	private Job job;
	private Operation operation;
	private OperationCall operationCall;
	private List<ParameterComposite> parameterComposites = new ArrayList<ParameterComposite>();
	private List<Parameter> mandatoryParameters = new ArrayList<Parameter>();
	private List<Parameter> optionalParameters = new ArrayList<Parameter>();

	private Section section;

	private String activeVariant;

	public JobEditorParameterPage(FormEditor editor, Job job, Operation operation, OperationCall operationCall) {
		super(editor, ID, null);
		this.job = job;
		this.operation = operation;
		this.operationCall = operationCall;
		
		activeVariant = null;
		if (!operationCall.getParameterValues().isEmpty()) {
			String firstParameterWithValueName = operationCall.getParameterValues().iterator().next().getName();
			for (Parameter parameter : this.operation.getParameters()) {
				if (parameter.getName().equals(firstParameterWithValueName)) {
					activeVariant = parameter.getVariant();
					break;
				}
			}
		}
		List<Parameter> parametersToDisplay = new LinkedList<Parameter>();
		for (Parameter parameter : this.operation.getParameters()) {
			if (parameter.getVariant() != null && parameter.getVariant().equals(activeVariant)) {
				parametersToDisplay.add(parameter);
			} else if (activeVariant == null) {
				parametersToDisplay.add(parameter);
			}
		}
		
		for (Parameter parameter : parametersToDisplay) {
			if (!parameter.isOptional()) {
				mandatoryParameters.add(parameter);
			} else {
				optionalParameters.add(parameter);
			}
		}
		getEditor().editorDirtyStateChanged();
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		form.setText("Job: " + LabelConverter.getLabel(job) + (activeVariant != null ? " - Group: " + activeVariant : ""));
		toolkit.decorateFormHeading(form.getForm());
		form.getBody().setLayout(new GridLayout(1, true));

		if (!mandatoryParameters.isEmpty()) {
			createParameterSection(form, toolkit, mandatoryParameters, "Mandatory Parameters", "The values for this parameters are required.");
		}
		if (!optionalParameters.isEmpty()) {
			createParameterSection(form, toolkit, optionalParameters, "Optional Parameters", "The values for this parameters are optional.\nCheck to use a parameter for this call.");
		}
	}

	private void createParameterSection(ScrolledForm form, FormToolkit toolkit, List<Parameter> parameters, String headLine, String description) {
		section = toolkit.createSection(form.getBody(), Section.EXPANDED | Section.TWISTIE | Section.TITLE_BAR
				| Section.DESCRIPTION);
		form.setContent(section);
		section.clientVerticalSpacing = 5;
		section.marginHeight = 3;
		section.marginWidth = 3;
		GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		section.setLayoutData(gridData);
		section.setText(headLine);
		section.setDescription(description);

		Composite client = toolkit.createComposite(section);
		toolkit.paintBordersFor(client);
		client.setLayout(new GridLayout(2, false));

		Map<String, LinkedList<ParameterValue>> parameterValues = new HashMap<String, LinkedList<ParameterValue>>();
		for (ParameterValue value : operationCall.getParameterValues()) {
			LinkedList<ParameterValue> values = parameterValues.get(value.getName());
			if (values == null) {
				values = new LinkedList<ParameterValue>();
				parameterValues.put(value.getName(), values);
			}
			values.add(value);
		}
		for (Parameter parameter : parameters) {
			List<ParameterValue> values = parameterValues.get(parameter.getName());
			ParameterComposite parameterComposite = new ParameterComposite(client, parameter, values);
			parameterComposites.add(parameterComposite);
			parameterComposite.addCompositeStatusListener(this);
		}
		section.setClient(client);
	}
	
	public Map<Parameter, List<ParameterValue>> getValues() {
		Map<Parameter, List<ParameterValue>> values = new HashMap<Parameter, List<ParameterValue>>();
		for (ParameterComposite parameterComposite : parameterComposites) {
			if (parameterComposite.isEnabled())
				values.put(parameterComposite.getParameter(), parameterComposite.getValues());
		}
		return values;
	}
	
	@Override
	public boolean isDirty() {
		if (parameterComposites.isEmpty()) {
			return false;
		}
		for (ParameterComposite parameterComposite : parameterComposites) {
			if (parameterComposite.isDirty())
				return true;
		}
		return false;
	}
	
	public void markSave() {
		for (ParameterComposite parameterComposite : parameterComposites) {
			parameterComposite.markAsClean();
		}
	}
	
	public void violation(ParameterComposite parameterComposite, Exception e) {
		getEditor().editorDirtyStateChanged();
	}

	public void layoutChanged(Composite parent, Composite inputWidgetContainer) {
		section.setExpanded(false);
		section.setExpanded(true);
	}
}
