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
package com.mercatis.lighthouse3.ui.environment.wizards.pages;

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
import com.mercatis.lighthouse3.persistence.environment.rest.SoftwareComponentRegistryImplementation;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainConfiguration;

public class WizardNewProjectLighthouseConnectionPage extends AbstractWizardPage {

	private static final int SIZING_TEXT_FIELD_WIDTH = 250;

	// Lighthouse
	private Text lighthouseUrl;
	private String lighthouseUrlValue;
	private String serverDomainKeyValue;

	// JMS

	private LighthouseDomain lighthouseDomain;

	private Listener modifyListener = new Listener() {
		public void handleEvent(Event e) {
			boolean valid = validatePage();
			setPageComplete(valid);
		}
	};
	
	public WizardNewProjectLighthouseConnectionPage() {
		super("Connection");
	}

	/**
	 * @param name
	 * @param lighthouseDomain the domain to edit, or null if new lighthouse domain should be created
	 */
	public WizardNewProjectLighthouseConnectionPage(String name, LighthouseDomain lighthouseDomain) {
		super(name);
		this.lighthouseDomain = lighthouseDomain;
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		initializeDialogUnits(parent);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createLighthouseLocationGroup(composite);
		setValues();
		
		setPageComplete(validatePage());
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		Dialog.applyDialogFont(composite);
	}

	private void setValues()  {
		if(lighthouseDomain != null) {
			lighthouseUrl.setText(CommonBaseActivator.getPlugin().getDomainService().getDomainConfiguration(lighthouseDomain).getUrl());
		} else {
			if (lighthouseUrlValue != null)
				lighthouseUrl.setText(lighthouseUrlValue);
		}
	}
	
	@Override
	public void prefillProperty(Object key, Object value) {
		if (key != null && value != null) {
			if (key.equals("DOMAIN_URL")) {
				lighthouseUrlValue = (String) value;
			} else if (key.equals("DOMAIN_SERVER_DOMAIN_KEY")) {
				serverDomainKeyValue = (String) value;
			}
		}
	}

	private void createLighthouseLocationGroup(Composite parent) {

		// common layout data
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;

		// lighthouse service group
		Group locationGroup = new Group(parent, SWT.NONE);
		locationGroup.setText("Server settings");
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		locationGroup.setLayout(layout);
		locationGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new env registry label
		Label environmentRegistryLabel = new Label(locationGroup, SWT.NONE);
		environmentRegistryLabel.setText("Lighthouse URL");
		environmentRegistryLabel.setFont(parent.getFont());

		// new env registry text field
		lighthouseUrl = new Text(locationGroup, SWT.BORDER);
		lighthouseUrl.setLayoutData(data);
		lighthouseUrl.setFont(parent.getFont());
		lighthouseUrl.addListener(SWT.Modify, modifyListener);
		lighthouseUrl.setFocus();
		lighthouseUrl.setText("http://localhost:8081");
		
		Button testB = new Button(parent, SWT.PUSH);
		testB.setText("Test Connection");
		testB.setLayoutData(new GridData(SWT.END, SWT.TOP, true, false));
		testB.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				BufferedReader in = null;
				try {
					String txt = lighthouseUrl.getText();
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
		});	}

	// lighthouse
	public String getLighthouseUrl() {
		return lighthouseUrl.getText();
	}

	protected boolean validatePage() {
		if (lighthouseUrl.getText().equals("")) {
			setErrorMessage(null);
			setMessage("Please enter URL");
			return false;
		}
		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	public void setPropertiesFor(Object o) {
		if (o instanceof DomainConfiguration) {
			DomainConfiguration domainConfiguration = (DomainConfiguration)o;
			domainConfiguration.setUrl(getLighthouseUrl());
			
			if (serverDomainKeyValue == null) {
				SoftwareComponentRegistryImplementation softwareComponentRegistry = new SoftwareComponentRegistryImplementation(getLighthouseUrl().trim());
				domainConfiguration.setServerDomainKey(softwareComponentRegistry.getLighthouseDomain());
			} else {
				domainConfiguration.setServerDomainKey(serverDomainKeyValue);
			}
		}
	}
}