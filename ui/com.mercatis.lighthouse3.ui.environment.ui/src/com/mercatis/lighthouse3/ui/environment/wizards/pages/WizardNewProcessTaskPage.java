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
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.ui.environment.providers.ProcessTasksOnlyContentProvider;

public class WizardNewProcessTaskPage extends AbstractWizardParentSelectionPage<ProcessTask> {

	private Text processTaskCodeText;
	private Text longName;
	private boolean autoCreate = true;
	
	private ModifyListener modifyLongNameListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			if (autoCreate) {
				int length = 0;
				processTaskCodeText.removeModifyListener(modifyListener);
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
				processTaskCodeText.setText(sb.toString());
				processTaskCodeText.addModifyListener(modifyListener);
			}
			setPageComplete(validatePage());
		}
	};

	private ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			autoCreate = processTaskCodeText.getText().length()==0;
			setPageComplete(validatePage());
		}
	};

	@Override
	protected ITreeContentProvider getContentProvider() {
		return new ProcessTasksOnlyContentProvider();
	}

	public WizardNewProcessTaskPage(String name) {
		super(name);
		setTitle("ProcessTask");
		setDescription("Create a new ProcessTask.");
	}

	public String getNewProcessTaskCode() {
		return processTaskCodeText.getText().trim();
	}
	
	public String getNewProcessTaskDescription() {
		return longName.getText().trim();
	}
	
	protected void initUI(Composite parent) {
		Label longNameLabel = new Label(parent, SWT.NONE);
		longNameLabel.setText("Process Task Name:");
		longNameLabel.setFont(parent.getFont());
		longNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		longName = new Text(parent, SWT.BORDER);
		longName.setFont(parent.getFont());
		longName.append(getPrefilledCode(getParentEntity()));
		longName.setFocus();
		longName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

		longName.addModifyListener(modifyLongNameListener);
		
		Label processTaskCodeLabel = new Label(parent, SWT.NONE);
		processTaskCodeLabel.setText("Process Task Code:");
		processTaskCodeLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		processTaskCodeText = new Text(parent, SWT.BORDER);
		processTaskCodeText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		processTaskCodeText.append(getPrefilledCode(getParentEntity()));

		processTaskCodeText.addModifyListener(modifyListener);
	}

	protected boolean validatePage() {
		if (!super.validatePage())
			return false;

		if (getNewProcessTaskCode().length() == 0) {
			setMessage("Please enter a code.", ERROR);
			return false;
		}

		setErrorMessage(null);
		setMessage(null);
		return true;
	}

}