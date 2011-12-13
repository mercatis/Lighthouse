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
package com.mercatis.lighthouse3.status.providers;

import java.util.Hashtable;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.status.ui.LighthouseStatusDecorator;
import com.mercatis.lighthouse3.status.ui.LighthouseStatusDecorator.Size;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.status.base.model.StatusEditingObject;


public class StatusLabelProvider implements ILabelProvider {
	
	private static Hashtable<Integer, Image> imageCache = new Hashtable<Integer, Image>();
	
	public Image getImage(Object element) {
		if (!(element instanceof StatusEditingObject))
			return null;
		
		element = ((StatusEditingObject) element).getStatus();
		
		int current = CommonBaseActivator.getPlugin().getStatusService().getLastChangeForStatus((Status) element).getNewStatus();
		Image image = imageCache.get(current);
		if (image == null) {
			image = LighthouseStatusDecorator.getImageDescriptorForStatus(current, Size.x16).createImage();
			imageCache.put(current, image);
		}
		return image;
	}

	public String getText(Object element) {
		if (element instanceof StatusEditingObject)
			return LabelConverter.getLabel(((StatusEditingObject) element).getStatus());
		
		return "UNKNOWN ENTITY";
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
