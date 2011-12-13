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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import com.mercatis.lighthouse3.base.ui.wizards.AbstractWizardPage;

public class WizardNewProjectLighthousePage extends AbstractWizardPage {
	
	private WizardNewProjectCreationPage wrappedPage;

	public WizardNewProjectLighthousePage() {
		super("Project");
		wrappedPage = new WizardNewProjectCreationPage("Project");
	}

	@Override
	public void setPropertiesFor(Object o) {
	}

	public void createControl(Composite parent) {
		wrappedPage.createControl(parent);
	}

	@Override
	public WizardPage getPage() {
		return wrappedPage;
	}

	@Override
	public void prefillProperty(Object key, Object value) {
		if (key instanceof String && ((String)key).equals("projectName")) {
			wrappedPage.setInitialProjectName((String) value);
		}
	}
}
