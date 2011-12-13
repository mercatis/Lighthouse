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
package com.mercatis.lighthouse3.ui.operations.ui.providers;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.operations.Operation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.ui.operations.base.model.OperationInstallationWrapper;


public class OperationsTreeLabelProvider implements ILabelProvider {

	public Image getImage(Object element) {
		if (element instanceof Operation || element instanceof OperationInstallation) {
			return ImageDescriptor.createFromURL(getClass().getResource("/icons/operation.png")).createImage();
		} else if (element instanceof OperationInstallationWrapper) {
			if (((OperationInstallationWrapper)element).getOperation() == null) {
				return ImageDescriptor.createFromURL(getClass().getResource("/icons/operation_broken.png")).createImage();
			} else {
				return ImageDescriptor.createFromURL(getClass().getResource("/icons/operation.png")).createImage();
			}
		}
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER).createImage();
	}

	public String getText(Object element) {
		return LabelConverter.getLabel(element);
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}
}
