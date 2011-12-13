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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.mercatis.lighthouse3.base.ui.wizards.AbstractWizardParentSelectionPage;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.ui.environment.providers.EnvironmentsOnlyContentProvider;

public class WizardNewEnvironmentPage extends AbstractWizardParentSelectionPage<Environment> {

	private Text environmentCodeText;
	private Text longName;
	private boolean autoCreate = true;
	
	private ModifyListener modifyLongNameListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			if (autoCreate) {
				int length = 0;
				environmentCodeText.removeModifyListener(modifyListener);
				String s = longName.getText().toUpperCase();
				StringBuilder sb = new StringBuilder();
				for (char c : s.toCharArray()) {
					if (length>255)
						break;
					if ((c>='A' && c<='Z') || (c>='0' && c<='9')) {
						sb.append(c);
						++length;
						continue;
					}
				}
				environmentCodeText.setText(sb.toString());
				environmentCodeText.addModifyListener(modifyListener);
			}
			setPageComplete(validatePage());
		}
	};

	private ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			autoCreate = environmentCodeText.getText().length()==0;
			setPageComplete(validatePage());
		}
	};
	
	@Override
	protected ITreeContentProvider getContentProvider() {
		return new EnvironmentsOnlyContentProvider();
	}

	public WizardNewEnvironmentPage(String name) {
		super(name);
	}

	public String getNewEnvironmentCode() {
		return environmentCodeText.getText().trim();
	}

	public String getNewEnvironmentDescription() {
		return longName.getText().trim();
	}

	protected void initUI(Composite parent) {
		Label longNameLabel = new Label(parent, SWT.NONE);
		longNameLabel.setText("Environment Name:");
		longNameLabel.setFont(parent.getFont());
		longNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		longName = new Text(parent, SWT.BORDER);
		longName.setFont(parent.getFont());
		longName.append(getPrefilledCode(getParentEntity()));
		longName.setFocus();
		longName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

		longName.addModifyListener(modifyLongNameListener);
		
		Label environmentCodeLabel = new Label(parent, SWT.NONE);
		environmentCodeLabel.setText("Environment Code:");
		environmentCodeLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		environmentCodeText = new Text(parent, SWT.BORDER);
		environmentCodeText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		environmentCodeText.append(getPrefilledCode(getParentEntity()));

		environmentCodeText.addModifyListener(modifyListener);
	}

	protected boolean validatePage() {
		if (!super.validatePage())
			return false;

		if (environmentCodeText.getText().length() == 0) {
			setMessage("Please enter a code.", ERROR);
			return false;
		}

		setErrorMessage(null);
		setMessage(null);
		return true;
	}
}