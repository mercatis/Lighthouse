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
package com.mercatis.lighthouse3.ui.operations.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.PlatformUI;

import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;
import com.mercatis.lighthouse3.ui.operations.base.OperationsChangedListener;


public class LighthouseOperationsDecorator implements ILightweightLabelDecorator, OperationsChangedListener {

	public static final String id = "com.mercatis.lighthouse3.operations.ui.decorator";
	private static ImageDescriptor image = ImageDescriptor.createFromURL(LighthouseOperationsDecorator.class.getResource("/icons/operation_x8.png"));
	private Map<Deployment, Integer> decoratedDeployments = new HashMap<Deployment, Integer>();

	public LighthouseOperationsDecorator() {
		OperationBase.addOperationsChangedListener(this);
	}
	
	static int i = 0;
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof Deployment) {
			Deployment deployment = (Deployment) element;
			Integer installedCounter = decoratedDeployments.get(deployment);
			if (installedCounter == null) {
				installedCounter = OperationBase.getOperationInstallationService().findAtDeployment(deployment).size();
				decoratedDeployments.put(deployment, installedCounter);
			}
			if (installedCounter > 0) {
				decoration.addOverlay(image, IDecoration.TOP_LEFT);
			}
		}
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
		OperationBase.removeOperationsChangedListener(this);
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	public void operationsChanged(LighthouseDomain lighthouseDomain, final Object source, String property, Object oldValue,
			Object newValue) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (source instanceof OperationInstallation) {
					decoratedDeployments.remove(((OperationInstallation)source).getInstallationLocation());
				}
				PlatformUI.getWorkbench().getDecoratorManager().update(id);
			}
		});
	}
}
