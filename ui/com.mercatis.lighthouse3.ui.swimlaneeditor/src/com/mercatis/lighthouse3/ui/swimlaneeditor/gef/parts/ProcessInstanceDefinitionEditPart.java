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
package com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts;

import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition;
import com.mercatis.lighthouse3.ui.swimlaneeditor.IInstanceView;


public class ProcessInstanceDefinitionEditPart extends ProcessTaskEditPart {

	private IInstanceView instanceView;
	
	public ProcessInstanceDefinitionEditPart(ProcessInstanceDefinition definition) {
		super(definition.getProcessTask());
		setProcessInstanceDefinition(definition);
	}
	
	public void setInstanceView(IInstanceView instanceView) {
		this.instanceView = instanceView;
	}
	
	public IInstanceView getInstanceView() {
		return this.instanceView;
	}
}
