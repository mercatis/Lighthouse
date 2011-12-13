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
package com.mercatis.lighthouse3.status.ui.wizards.pages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import com.mercatis.lighthouse3.base.ui.wizards.AbstractWizardParentSelectionPage;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.domainmodel.status.Status;

public class StatusWizardMainPage extends WizardPage {

	private Text code;
	private Combo clearanceType;
	private Text stalenessIntervalText;
	private Text stalenessIntervalInMsecs;
	private Button enableStaleness;
	private StatusCarrier statusCarrier;
	private Pattern regex = Pattern.compile("(?:^| *)(?i)(?:(\\d+) *(days|day|d|hours|hour|h|minutes|minute|min|m|seconds|second|sec|s))(?=\\d| |$)");

	public StatusWizardMainPage(String pageName) {
		super(pageName);
	}
	
	public StatusWizardMainPage(String pageName, StatusCarrier statusCarrier) {
		this(pageName);
		this.statusCarrier = statusCarrier;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
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
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		
		Label codeLabel = new Label(composite, SWT.NULL);
		codeLabel.setText("Code:");
		code = new Text(composite, SWT.BORDER);
		code.setLayoutData(gd);
		if (statusCarrier instanceof CodedDomainModelEntity) {
			code.append(AbstractWizardParentSelectionPage.getPrefilledCode((CodedDomainModelEntity) statusCarrier));
		}
		else if (statusCarrier instanceof Deployment) {
			Deployment deployment = (Deployment)statusCarrier;
			code.append(deployment.getDeployedComponent().getCode() + "-" + deployment.getLocation() + "-");
		}
		code.addListener(SWT.Modify, modifyListener);
		
		Label clearanceTypeLabel = new Label(composite, SWT.NONE);
		clearanceTypeLabel.setText("Clearance Type");
		clearanceType = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		clearanceType.add("auto");
		clearanceType.setData("auto", Status.AUTO_CLEARANCE);
		clearanceType.add("manual");
		clearanceType.setData("manual", Status.MANUAL_CLEARANCE);
		clearanceType.select(0);
		clearanceType.setLayoutData(gd);
		clearanceType.addListener(SWT.Modify, modifyListener);
		
		enableStaleness = new Button(composite, SWT.CHECK);
		enableStaleness.setText("Enable Staleness");
		GridData notyData = new GridData();
		notyData.horizontalSpan = 2;
		enableStaleness.setLayoutData(notyData);
		enableStaleness.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				stalenessIntervalText.setEnabled(enableStaleness.getSelection());
				setPageComplete(validatePage());
			}
		});

		Label stalenessLabel = new Label(composite, SWT.NULL);
		stalenessLabel.setText("Staleness Interval:");
		stalenessIntervalText = new Text(composite, SWT.BORDER);
		stalenessIntervalText.setText("0 days 1 hour 0 seconds");
		stalenessIntervalText.setToolTipText("Staleness interval in seconds if no modifier (days, hours, ...) is specified");
		stalenessIntervalText.setLayoutData(gd);
		stalenessIntervalText.setEnabled(false);
		stalenessIntervalText.addListener(SWT.Modify, modifyListener);
		stalenessLabel = new Label(composite, SWT.NONE);
		stalenessLabel.setText("»»»");
		stalenessLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		stalenessIntervalInMsecs = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		stalenessIntervalInMsecs.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}
	
	private long mods[] = {86400000L, 3600000L, 60000L, 1000L};
	private String names[] = {"Day", "Hour", "Minute", "Second"};
	
	private void updateHumanReadableStaleness(final long val) {
		long l = val;
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<mods.length; ++i) {
			long div = l / mods[i];
			if (div>0) {
				sb.append(div);
				sb.append(' ');
				sb.append(names[i]);
				if (div>1)
					sb.append('s');
				sb.append(' ');
				l %= mods[i];
			}
		}
		sb.append("(equals "+val/1000+"s)");
		stalenessIntervalInMsecs.setText(sb.toString());
	}

	private boolean validatePage() {
		try {
			if (code.getText().equals(""))
				throw new Exception("Code is not defined.");
			if (stalenessIntervalText.isEnabled() && stalenessIntervalText.getText().equals(""))
				throw new Exception("Staleness interval is not defined.");
			getClearanceType();
			long l = getStalenessInterval();
			updateHumanReadableStaleness(l);
		}
		catch (NumberFormatException e) {
			setErrorMessage("Error parsing input");
			stalenessIntervalInMsecs.setText("<input validation failed>");
			return false;
		}
		catch (Throwable t) {
			setMessage(t.getMessage());
			return false;
		}
		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	public String getCode() {
		return code.getText();
	}
	
	public int getClearanceType() {
		return (Integer) clearanceType.getData(clearanceType.getText());
	}
	
	public long getStalenessInterval() {
		if (stalenessIntervalText.isEnabled()) {
			String text = stalenessIntervalText.getText().trim();
			Matcher m = regex.matcher(text);
			long result = 0L;
			int lastEnd = 0;
			while (m.find()) {
				if (m.start()!=lastEnd) {
					// could not validate some part of the input string 
					break;
				}
				lastEnd = m.end();
				long val = Long.parseLong(m.group(1));
				String mult = m.group(2).toLowerCase();
				if ("days".equals(mult) || "day".equals(mult) || "d".equals(mult)) {
					result += val * 86400000L;
					continue;
				}
				if ("hours".equals(mult) || "hour".equals(mult) || "h".equals(mult)) {
					result += val * 3600000L;
					continue;
				}
				if ("minutes".equals(mult) || "minute".equals(mult) || "min".equals(mult) || "m".equals(mult)) {
					result += val * 60000L;
					continue;
				}
				if ("seconds".equals(mult) || "second".equals(mult) || "sec".equals(mult) || "s".equals(mult)) {
					result += val * 1000L;
					continue;
				}
			}
			if (text.length() == lastEnd)
				return result;
			// if everything else failed use the Long parser to read seconds
			return Long.parseLong(text)*1000;
		} else
			return 0L;
	}

	private Listener modifyListener = new Listener() {
		public void handleEvent(org.eclipse.swt.widgets.Event event) {
			setPageComplete(validatePage());
		}
	};
}
