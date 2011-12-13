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
package com.mercatis.lighthouse3.ui.operations.ui.wizards.pages;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.mercatis.lighthouse3.base.ui.wizards.AbstractWizardParentSelectionPage;
import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.domainmodel.operations.Operation;
import com.mercatis.lighthouse3.ui.operations.ui.widgets.CronExpressionWidget;
import com.mercatis.lighthouse3.ui.operations.ui.widgets.CronExpressionWidgetListener;


public class JobInstallationWizardMainPage extends WizardPage implements CronExpressionWidgetListener {

	private Text code;
	private Text expression;
	private CronExpressionWidget cronTriggerWidget;
	private Operation operation;

	public JobInstallationWizardMainPage(String title, Operation operation) {
		super("Job details");
		setTitle(title);
		this.operation = operation;
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		initializeDialogUnits(parent);

		composite.setLayout(new GridLayout(2, false));

		initUI(composite);

		setPageComplete(validatePage());
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}

	private void initUI(Composite composite) {
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);

		Label codeLabel = new Label(composite, SWT.NONE);
		codeLabel.setText("Code:");
		code = new Text(composite, SWT.BORDER);
		code.setLayoutData(gridData);
		code.append(AbstractWizardParentSelectionPage.getPrefilledCode(operation));
		code.addListener(SWT.Modify, modifyListener);
		code.setFocus();

		Label scheduleExpressionLabel = new Label(composite, SWT.NONE);
		scheduleExpressionLabel.setText("Schedule expression:");
		expression = new Text(composite, SWT.BORDER);
		expression.setLayoutData(gridData);
		expression.setEditable(false);
		expression.setEnabled(false);

		Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData separatorGridData = new GridData(GridData.FILL_HORIZONTAL);
		separatorGridData.horizontalSpan = 2;
		separator.setLayoutData(separatorGridData);
		
		new Label(composite, SWT.NONE).setText(" "); //filler
		cronTriggerWidget = new CronExpressionWidget(composite, SWT.NONE);
		cronTriggerWidget.addWidgetListener(this);
		placeDescriptions(composite);
		expression.setText(cronTriggerWidget.getExpression());
	}
	
	private void placeDescriptions(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData separatorGridData = new GridData(GridData.FILL_HORIZONTAL);
		separatorGridData.horizontalSpan = 2;
		separator.setLayoutData(separatorGridData);
		
		String descriptionText = "Cron expressions are able to create schedules such as:\n";
		String[][] examples = {
				{"0 0 12 * * ?", "Fire at 12pm (noon) every day"},
				{"0 15 10 * * ? 2005", "Fire at 10:15am every day during the year 2005"},
				{"0 15 10 ? * MON-FRI", "Fire at 10:15am every Monday, Tuesday, Wednesday, Thursday and Friday"}
		};
		
		String[][] descriptions = {
				{"sec",  "Seconds"},
				{"min",  "Minutes"},
				{"h",    "Hour"},
				{"dom",  "Day of month"},
				{"m",    "Month"},
				{"dow",  "Day of week"},
				{"yyyy", "Year"}
		};
		
		Label descriptionTextLabel = new Label(parent, SWT.NONE);
		GridData descriptionTextLayoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		descriptionTextLayoutData.horizontalSpan = 2;
		descriptionTextLabel.setLayoutData(descriptionTextLayoutData);
		descriptionTextLabel.setText(descriptionText);

		GridData descriptionElementLayoutData = new GridData(GridData.FILL, GridData.CENTER, true, false);

		for (String[] example : examples) {
			int i = 0;
			for (String exampleElement : example) {
				Label elementLabel = new Label(parent, SWT.NONE);
				elementLabel.setText(exampleElement);
				if (i > 0)
					elementLabel.setLayoutData(descriptionElementLayoutData);
				i++;
			}
		}

		Label label = new Label(parent, SWT.NONE);
		GridData labelLayoutData = new GridData(SWT.BEGINNING, SWT.FILL, false, true);
		labelLayoutData.horizontalSpan = 2;
		label.setLayoutData(labelLayoutData);
		label.setText(" ");

		for (String[] description : descriptions) {
			int i = 0;
			for (String descriptionElement : description) {
				Label elementLabel = new Label(parent, SWT.NONE);
				elementLabel.setText(descriptionElement);
				if (i > 0)
					elementLabel.setLayoutData(descriptionElementLayoutData);
				i++;
			}
		}
	}

	private boolean validatePage() {
		if (code == null)
			return false;
		if (code.getText().length() == 0) {
			setMessage("Code is not defined.");
			return false;
		}
		expression.setText(cronTriggerWidget.getExpression());
		if (expression.getText().length() == 0) {
			setMessage("Schedule expresion is not defined.");
			return false;
		}
		try {
			new Job().setScheduleExpression(getScheduleExpression());
			cronTriggerWidget.plausibilityCheck();
		} catch (Exception e) {
			setErrorMessage(e.getMessage());
			return false;
		}
		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	private Listener modifyListener = new Listener() {
		public void handleEvent(org.eclipse.swt.widgets.Event event) {
			setPageComplete(validatePage());
		}
	};
	
	public String getCode() {
		return code.getText();
	}

	public String getScheduleExpression() {
		return cronTriggerWidget.getExpression();
	}

	public void modified(String expression, String second, String minute, String hour, String dayOfMonth, String Month,
			String dayOfWeek, String year) {
		setPageComplete(validatePage());
	}

	public void updateHint(String hint) {
		setMessage(hint, DialogPage.INFORMATION);
	}
}
