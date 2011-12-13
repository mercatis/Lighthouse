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
package com.mercatis.lighthouse3.security;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.mercatis.lighthouse3.security.api.LoginModule;

public class DefaultLoginModule extends TitleAreaDialog implements LoginModule {

	private Text nameText;

	private Text passwordText;

	private String name;

	private String header = "mercatis Security";

	private char[] password;

	public DefaultLoginModule() {
		super(null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() {
		name = "";
		password = new char[0];

		super.cancelPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		this.setTitle(header);

		Composite composite = (Composite) super.createDialogArea(parent);
		Composite c = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		c.setLayout(layout);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		applyDialogFont(c);

		Label label = new Label(c, SWT.NONE);
		label.setText("Username: ");
		label.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
		nameText = new Text(c, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		label = new Label(c, SWT.NONE);
		label.setText("Password: ");
		label.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
		passwordText = new Text(c, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		return c;
	}

	/**
	 * @return
	 */
	public String getHeader() {
		return header;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.api.LoginModule#login(java.lang.String)
	 */
	public SecurityBinding login(String context) {
		Display display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();

		display.syncExec(new Runnable() {
			public void run() {
				setBlockOnOpen(true);
				open();
			}
		});

		SecurityBindingBuilder builder = new SecurityBindingBuilder();
		return builder.username(context, name).password(context, password).build();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		name = nameText.getText().trim();
		password = passwordText.getText().toCharArray().clone();

		super.okPressed();
	}

	/**
	 * @param header
	 */
	public void setHeader(String header) {
		this.header = header;
	}
	
}
