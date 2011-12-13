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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition;
import com.mercatis.lighthouse3.ui.swimlaneeditor.IInstanceView;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts.ProcessInstanceDefinitionEditPart;


public class ProcessInstanceDefinitionPropertySource implements IPropertySource {

	private ProcessInstanceDefinition processInstanceDefinition;
	private IInstanceView instanceView;

	private static final int PROCESS_INSTANCE_DEFINITION_LABEL = 0;
	private static final int SELECTED_PROCESS_INSTANCE = 1;
	private Map<Object, PropertyDescriptor> descriptors;

	public ProcessInstanceDefinitionPropertySource(ProcessInstanceDefinitionEditPart editPart) {
		this.processInstanceDefinition = editPart.getProcessInstanceDefinition();
		this.instanceView = editPart.getInstanceView();
		createPropertyDescriptors();
	}
	
	protected void createPropertyDescriptors() {
		descriptors = new HashMap<Object, PropertyDescriptor>();

		PropertyDescriptor codeDescriptor = new PropertyDescriptor(PROCESS_INSTANCE_DEFINITION_LABEL, "Process Task");
		codeDescriptor.setCategory("Process Instance Definition");
		descriptors.put(codeDescriptor.getId(), codeDescriptor);
		
		PropertyDescriptor activeInstance = new PropertyDescriptor(SELECTED_PROCESS_INSTANCE, "Active instance");
		activeInstance.setCategory("Process Instance Definition");
		descriptors.put(activeInstance.getId(), activeInstance);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
	 */
	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors.values().toArray(new PropertyDescriptor[0]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(java.lang.Object)
	 */
	public Object getPropertyValue(Object id) {
		switch ((Integer) id) {
		case PROCESS_INSTANCE_DEFINITION_LABEL:
			return LabelConverter.getLabel(processInstanceDefinition.getProcessTask());
		case SELECTED_PROCESS_INSTANCE:
			if (instanceView.getActiveProcessInstance() != null)
				return LabelConverter.getLabel(instanceView.getActiveProcessInstance());
		default:
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(java.lang.Object)
	 */
	public boolean isPropertySet(Object id) {
		return descriptors.containsKey(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(java.lang.Object)
	 */
	public void resetPropertyValue(Object id) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java.lang.Object, java.lang.Object)
	 */
	public void setPropertyValue(Object id, Object value) {
		// TODO Auto-generated method stub
	}
}
