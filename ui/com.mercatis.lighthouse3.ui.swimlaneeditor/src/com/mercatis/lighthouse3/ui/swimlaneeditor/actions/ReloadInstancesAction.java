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
/**
 * 
 */
package com.mercatis.lighthouse3.ui.swimlaneeditor.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.mercatis.lighthouse3.ui.swimlaneeditor.IInstanceView;


public class ReloadInstancesAction extends Action {

	private IInstanceView instanceView;
	
	public ReloadInstancesAction(IInstanceView instanceView) {
		super(null, AS_PUSH_BUTTON);
		this.setImageDescriptor(ImageDescriptor.createFromURL(this.getClass().getResource("/icons/refresh.gif")));
		this.setText("Reload Instances");
		this.instanceView = instanceView;
	}

	@Override
	public void run() {
		instanceView.reloadInstances();
	}
}
