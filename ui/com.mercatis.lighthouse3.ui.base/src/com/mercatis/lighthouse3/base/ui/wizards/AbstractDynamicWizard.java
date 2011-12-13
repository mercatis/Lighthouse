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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.Wizard;

import com.mercatis.lighthouse3.base.UIBase;

public abstract class AbstractDynamicWizard extends Wizard {

	private static IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
	private List<AbstractWizardPage> addedPages;
	private Map<Object, Object> storedProperties = new HashMap<Object, Object>();

	@Override
	public boolean performFinish() {
		return performFinish(addedPages);
	}
	
	abstract protected boolean performFinish(List<AbstractWizardPage> pages);
	
	/**
	 * Forwards given key/value pair to all pages for prefilling properties.
	 * 
	 * @param key
	 * @param value
	 */
	public void setProperty(Object key, Object value) {
		if (addedPages != null) {
			for (AbstractWizardPage page : addedPages) {
				page.prefillProperty(key, value);
			}
		} else {
			//no pages created yet
			//store property and add it later during creation
			storedProperties.put(key, value);
		}
	}
	
	/**
	 * Forwards a given object to all pages. The page itself decides whether and what to change in that object.
	 * 
	 * @param object - Object to manipulate
	 */
	public void manipulateObject(Object object) {
		for (AbstractWizardPage page : addedPages) {
			page.setPropertiesFor(object);
		}
	}
	
	/**
	 * Gets an id for the given wizard - ugly but works.
	 * Wrong behavior when using a Wizardclass mutliple times!
	 */
	private String lookupId(Class<?> clazz) {
		String id = null;
		for (IConfigurationElement element : extensionRegistry.getConfigurationElementsFor("org.eclipse.ui.newWizards")) {
			if (element.getAttribute("class") != null && element.getAttribute("class").equals(clazz.getCanonicalName())) {
				id = element.getAttribute("id");
				break;
			}
		}
		return id;
	}

	/**
	 * Look into com.mercatis.lighthouse3.ui.wizards.pages to get the extensions containing pages for this wizard 
	 */
	@Override
	public void addPages() {
		String wizardId = lookupId(this.getClass());
		addedPages = new ArrayList<AbstractWizardPage>();
		IConfigurationElement[] elements = extensionRegistry.getConfigurationElementsFor("com.mercatis.lighthouse3.ui.wizards.pages");
		for (IConfigurationElement element : elements) {
			if (element.getAttribute("id").equals(wizardId)) {
				for (IConfigurationElement child : element.getChildren()) {
					try {
						//get an instance
						Object o = child.createExecutableExtension("class");
						if (o != null && o instanceof AbstractWizardPage) {
							AbstractWizardPage page = (AbstractWizardPage)o;
							page.setTitle(child.getAttribute("title"));
							page.setDescription(child.getAttribute("description"));
							page.setOrderNumber(Integer.parseInt(child.getAttribute("ordernumber")));
							addedPages.add(page);
						}
					}
					catch (Throwable t) {
						UIBase.getDefault().getLog().log(new Status(IStatus.ERROR, UIBase.PLUGIN_ID, t.getMessage(), t));
					}
				}
			}
		}
		//sorting and adding to the wizard
		Collections.sort(addedPages);
		for (AbstractWizardPage page : addedPages) {
			//use the method AbstractWizardPage.addToWizard(Wizard wizard) to ensure that wrapped pages are added
			page.addToWizard(this);
			//set the previous stored properties
			for (Entry<Object, Object> entry : storedProperties.entrySet()) {
				page.prefillProperty(entry.getKey(), entry.getValue());
			}
		}
	}
}
