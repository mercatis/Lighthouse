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
package com.mercatis.lighthouse3.ui.environment.model.adapters;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;

public class SoftwareComponentPropertySource implements IPropertySource {

	private static final int ID_CODE = 0;
	private static final int ID_CONTACT = 1;
	private static final int ID_CONTACT_EMAIL = 2;
	private static final int ID_COPYRIGHT = 3;
	private static final int ID_DESCRIPTION = 4;
	private static final int ID_LONG_NAME = 5;
	private static final int ID_VERSION = 6;

	private SoftwareComponent component;

	private PropertyDescriptor[] descriptors;

	public SoftwareComponentPropertySource(SoftwareComponent component) {
		this.component = component;
		createPropertyDescriptors();
	}

	protected void createPropertyDescriptors() {
		descriptors = new PropertyDescriptor[7];

		PropertyDescriptor codeDescriptor = new PropertyDescriptor(ID_CODE, "Code");
		codeDescriptor.setCategory(null);
		codeDescriptor.setDescription("");
		descriptors[0] = codeDescriptor;

		PropertyDescriptor contactDescriptor = new PropertyDescriptor(ID_CONTACT, "Contact");
		contactDescriptor.setCategory(null);
		contactDescriptor.setDescription("");
		descriptors[1] = contactDescriptor;

		PropertyDescriptor contactEmailDescriptor = new PropertyDescriptor(ID_CONTACT_EMAIL, "Contact Email");
		contactEmailDescriptor.setCategory(null);
		contactEmailDescriptor.setDescription("");
		descriptors[2] = contactEmailDescriptor;

		PropertyDescriptor copyrightDescriptor = new PropertyDescriptor(ID_COPYRIGHT, "Copyright");
		copyrightDescriptor.setCategory(null);
		copyrightDescriptor.setDescription("");
		descriptors[3] = copyrightDescriptor;

		PropertyDescriptor descriptionDescriptor = new PropertyDescriptor(ID_DESCRIPTION, "Description");
		descriptionDescriptor.setCategory(null);
		descriptionDescriptor.setDescription("");
		descriptors[4] = descriptionDescriptor;

		PropertyDescriptor longNameDescriptor = new PropertyDescriptor(ID_LONG_NAME, "Long Name");
		longNameDescriptor.setCategory(null);
		longNameDescriptor.setDescription("");
		descriptors[5] = longNameDescriptor;

		PropertyDescriptor versionDescriptor = new PropertyDescriptor(ID_VERSION, "Version");
		versionDescriptor.setCategory(null);
		versionDescriptor.setDescription("");
		descriptors[6] = versionDescriptor;
	}

	public Object getEditableValue() {
		return null;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors;
	}

	public Object getPropertyValue(Object id) {
		switch ((Integer) id) {
		case ID_CODE:
			return component.getCode();
		case ID_CONTACT:
			return component.getContact();
		case ID_CONTACT_EMAIL:
			return component.getContactEmail();
		case ID_COPYRIGHT:
			return component.getCopyright();
		case ID_DESCRIPTION:
			return component.getDescription();
		case ID_LONG_NAME:
			return component.getLongName();
		case ID_VERSION:
			return component.getVersion();
		}

		return null;
	}

	public boolean isPropertySet(Object id) {
		return false;
	}

	public void resetPropertyValue(Object id) {
	}

	public void setPropertyValue(Object id, Object value) {
	}

}
