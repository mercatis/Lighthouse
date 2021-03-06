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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public class LighthouseDomainWorkbenchAdapter implements IWorkbenchAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object adaptee) {
		if (adaptee instanceof LighthouseDomain) {
			LighthouseDomain lighthouseDomain = (LighthouseDomain) adaptee;
			if (!lighthouseDomain.getProject().isOpen()) {
				return new Object[0];
			}
			return ((HierarchicalEntityAdapter) Platform.getAdapterManager().getAdapter(adaptee, HierarchicalEntityAdapter.class)).getChildren(adaptee);
		}
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object adaptee) {
		if (adaptee instanceof LighthouseDomain) {
			if (((LighthouseDomain) adaptee).getProject().isOpen()) {
				return ImageDescriptor.createFromURL(this.getClass().getResource("/icons/prj_obj.gif"));
			}
			else {
				return ImageDescriptor.createFromURL(this.getClass().getResource("/icons/cprj_obj.gif"));
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object adaptee) {
		return LabelConverter.getLabel(adaptee);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object adaptee) {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
}
