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
package com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.adapters;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts.ProcessTaskModelEditPart;


public class ProcessTaskModelEditPartPropertySource implements IPropertySource {

	private static final int ID_CODE = 0;

	private ProcessTaskModelEditPart processTaskModelEditPart;

	private PropertyDescriptor[] descriptors;
	
	public ProcessTaskModelEditPartPropertySource(ProcessTaskModelEditPart processTaskModel) {
		this.processTaskModelEditPart = processTaskModel;
		createPropertyDescriptors();
	}

	protected void createPropertyDescriptors() {
		descriptors = new PropertyDescriptor[1];

		PropertyDescriptor codeDescriptor = new PropertyDescriptor(ID_CODE, "Code");
		codeDescriptor.setCategory("Process Task");
		codeDescriptor.setDescription("");
		descriptors[0] = codeDescriptor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
	 */
	public Object getEditableValue() {
		return null;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors;
	}
	
	public Object getPropertyValue(Object id) {
		switch ((Integer) id) {
		case ID_CODE:
			return processTaskModelEditPart.getProcessTask().getCode();
		default:
			return null;
		}
	}

	public boolean isPropertySet(Object id) {
		return false;
	}

	public void resetPropertyValue(Object id) {
	}

	public void setPropertyValue(Object id, Object value) {
	}
}
