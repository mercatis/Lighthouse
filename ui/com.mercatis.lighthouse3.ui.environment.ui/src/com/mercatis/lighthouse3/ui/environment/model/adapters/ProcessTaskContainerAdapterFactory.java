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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;

import com.mercatis.lighthouse3.ui.environment.base.model.ProcessTaskContainer;


@SuppressWarnings("rawtypes")
public class ProcessTaskContainerAdapterFactory implements IAdapterFactory {

	private ProcessTaskContainerWorkbenchAdapter workbenchAdapter = new ProcessTaskContainerWorkbenchAdapter();

	private ProcessTaskContainerDropTargetAdapter dropTargetAdapter = new ProcessTaskContainerDropTargetAdapter();

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (!(adaptableObject instanceof ProcessTaskContainer))
			return null;

		if (adapterType.equals(IWorkbenchAdapter.class))
			return workbenchAdapter;

		if (adapterType.equals(DropTargetAdapter.class))
			return dropTargetAdapter;

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class,DropTargetAdapter.class };
	}

}
