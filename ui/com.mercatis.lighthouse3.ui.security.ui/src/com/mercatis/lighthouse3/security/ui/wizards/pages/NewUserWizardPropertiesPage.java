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
package com.mercatis.lighthouse3.security.ui.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


public class NewUserWizardPropertiesPage extends WizardPage {

	private Text codeText;
	private Text givenNameText;
	private Text surNameText;
	private Text contactEmailText;
	private Text passwordText;
	private Text confirmPasswordText;
	
	public NewUserWizardPropertiesPage() {
		super("User properties");
		setMessage("Enter properties for the new user.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		initializeDialogUnits(parent);
		composite.setLayout(new GridLayout(2, false));
		GridData textLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		
		new Label(composite, SWT.NONE).setText("Login name");
		codeText = new Text(composite, SWT.BORDER);
		codeText.addListener(SWT.Modify, modifyListener);
		codeText.setLayoutData(textLayoutData);
		
		new Label(composite, SWT.NONE).setText("Given name");
		givenNameText = new Text(composite, SWT.BORDER);
		givenNameText.addListener(SWT.Modify, modifyListener);
		givenNameText.setLayoutData(textLayoutData);
		
		new Label(composite, SWT.NONE).setText("Surname");
		surNameText = new Text(composite, SWT.BORDER);
		surNameText.addListener(SWT.Modify, modifyListener);
		surNameText.setLayoutData(textLayoutData);
		
		new Label(composite, SWT.NONE).setText("Contact Email");
		contactEmailText = new Text(composite, SWT.BORDER);
		contactEmailText.addListener(SWT.Modify, modifyListener);
		contactEmailText.setLayoutData(textLayoutData);
		
		new Label(composite, SWT.NONE).setText("Password");
		passwordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		passwordText.addListener(SWT.Modify, modifyListener);
		passwordText.setLayoutData(textLayoutData);
		
		new Label(composite, SWT.NONE).setText("Confirm Password");
		confirmPasswordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		confirmPasswordText.addListener(SWT.Modify, modifyListener);
		confirmPasswordText.setLayoutData(textLayoutData);
		
		setControl(composite);
	}
	
	private boolean validatePage() {
		if (codeText == null)
			return false;
		if (codeText.getText().length() == 0) {
			setMessage("Code is not defined.");
			return false;
		}
		if (passwordText.getText().length() == 0) {
			setMessage("Password not defined.");
			return false;
		}
		if (!passwordText.getText().equals(confirmPasswordText.getText())) {
			setMessage("Passwords do not match.");
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
		return codeText.getText();
	}
	
	public String getGivenName() {
		return givenNameText.getText();
	}
	
	public String getSurName() {
		return surNameText.getText();
	}
	
	public String getContactEmail() {
		return contactEmailText.getText();
	}
	
	public String getPassword() {
		return passwordText.getText();
	}
}
