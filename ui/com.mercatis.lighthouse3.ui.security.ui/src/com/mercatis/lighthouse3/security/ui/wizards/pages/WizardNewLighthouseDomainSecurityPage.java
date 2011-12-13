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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.mercatis.lighthouse3.base.ui.wizards.AbstractWizardPage;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.internal.Security;
import com.mercatis.lighthouse3.ui.security.internal.SecurityConfiguration;


public class WizardNewLighthouseDomainSecurityPage extends AbstractWizardPage {

	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
	private Text urlText;
	private String urlTextValue;
	
	@Override
	public void prefillProperty(Object key, Object value) {
		if (key != null && value != null) {
			if (key.equals("SERVER_URL")) {
				urlTextValue = (String) value;
			}
		}
	}

	/**
	 * @param pageName
	 */
	public WizardNewLighthouseDomainSecurityPage() {
		super("Security Page");
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.wizards.AbstractWizardPage#setPropertiesFor(java.lang.Object)
	 */
	@Override
	public void setPropertiesFor(Object o) {
		if (o instanceof LighthouseDomain) {
			LighthouseDomain lighthouseDomain = (LighthouseDomain) o;
			SecurityConfiguration securityConfiguration = Security.getDefault().getSecurityConfiguration(lighthouseDomain.getProject());
			securityConfiguration.setServerUrl(urlText.getText());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		initializeDialogUnits(parent);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;

		Group urlGroup = new Group(composite, SWT.NONE);
		urlGroup.setText("Security settings");
		GridLayout urlLayout = new GridLayout();
		urlLayout.numColumns = 2;
		urlGroup.setLayout(urlLayout);
		urlGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label urlLabel = new Label(urlGroup, SWT.NONE);
		urlLabel.setText("URL");
		urlLabel.setFont(parent.getFont());

		urlText = new Text(urlGroup, SWT.BORDER);
		urlText.setText(urlTextValue!=null ? urlTextValue : "http://localhost:8081");
		urlText.setLayoutData(data);
		urlText.setFont(parent.getFont());
		urlText.addListener(SWT.Modify, modifyListener);
		urlText.setFocus();
		
		Button testB = new Button(composite, SWT.PUSH);
		testB.setText("Test Connection");
		testB.setLayoutData(new GridData(SWT.END, SWT.TOP, true, false));
		testB.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				BufferedReader in = null;
				try {
					String txt = urlText.getText();
					StringBuilder sb = new StringBuilder(txt);
					if (!txt.endsWith("/"))
						sb.append('/');
					sb.append("Version");
					URL url = new URL(sb.toString());
					URLConnection conn = url.openConnection();
					in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String inputLine = in.readLine();
					Pattern p = Pattern.compile("^\\d.\\d.\\d(?:-SNAPSHOT)?$");
					Matcher m = p.matcher(inputLine);
					if (m.matches())
						MessageDialog.openInformation(getShell(), "Connection Test", "Connection test successful!\n\nLighthouse Server v"+inputLine+" was found.");
					else
						MessageDialog.openWarning(getShell(), "Connection Test", "Connection test failed!\n\nThere was a reply but it could not be validated.");
				} catch (Exception e1) {
					MessageDialog.openError(getShell(), "Connection Test", "Connection test failed!\n\n"+e1.getMessage());
				} finally {
					if (in!=null)
						try { in.close(); } catch (IOException e1) { /* ignore */ }
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) { /* ignore */ }
		});
		
		setPageComplete(validatePage());
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		Dialog.applyDialogFont(composite);
	}
	
	private boolean validatePage() {
		if (urlText.getText().length() == 0) {
			setErrorMessage(null);
			setMessage("Please enter a URL.");
			return false;
		}
		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	private Listener modifyListener = new Listener() {
		public void handleEvent(Event e) {
			boolean valid = validatePage();
			setPageComplete(valid);
		}
	};
}
