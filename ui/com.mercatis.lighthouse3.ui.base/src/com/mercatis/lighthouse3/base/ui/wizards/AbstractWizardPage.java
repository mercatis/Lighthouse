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
package com.mercatis.lighthouse3.base.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;


public abstract class AbstractWizardPage extends WizardPage implements Comparable<AbstractWizardPage> {

	private int orderNumber;
	
	public AbstractWizardPage(String pageName) {
		super(pageName);
	}

	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}

	public int compareTo(AbstractWizardPage o) {
		return new Integer(orderNumber).compareTo(new Integer(o.orderNumber));
	}
	
	/**
	 * Override this method to encapsulate another wizard page like the ones provided by the platform
	 * @return The page to be displayed
	 */
	public WizardPage getPage() {
		return this;
	}
	
	public void addToWizard(Wizard wizard) {
		wizard.addPage(this.getPage());
	}
	
	public void setTitle(String title) {
		if (this.getPage().equals(this)) {
			super.setTitle(title);
		}
		else {
			this.getPage().setTitle(title);
		}
	}
	
	public void setDescription(String description) {
		if (this.getPage().equals(this)) {
			super.setDescription(description);
		}
		else {
			this.getPage().setDescription(description);
		}
	}
	
	abstract public void setPropertiesFor(Object o);
	
	/**
	 * Override this method if your implementation should be capable of prefilling properties.
	 * 
	 * @param key
	 * @param value
	 */
	public void prefillProperty(Object key, Object value) {
		//do nothing
	}
}
