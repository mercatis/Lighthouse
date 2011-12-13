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
package com.mercatis.lighthouse3.ui.environment.dnd;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;
import com.mercatis.lighthouse3.ui.environment.model.adapters.DropTargetAdapter;


public class BaseDropAdapterAssistant extends CommonDropAdapterAssistant {

	private IAdapterManager adapterManager = Platform.getAdapterManager();

	private Object[] elements;

	private int operation;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public IStatus handleDrop(CommonDropAdapter dropAdapter, DropTargetEvent dropTargetEvent, Object target) {
		DropTargetAdapter adapter = (DropTargetAdapter) adapterManager.getAdapter(target, DropTargetAdapter.class);
		if (adapter == null)
			return new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, "Drop Target (" + target.getClass().getName()
					+ ") is not adaptable to " + DropTargetAdapter.class.getName());

		if (!adapter.handleDrop(operation, target, elements, getShell()))
			return new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, "Drop Target (" + target.getClass().getName()
					+ ") is failed to handle the drop.");

		elements = null;
		return Status.OK_STATUS;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public IStatus validateDrop(Object target, int operation, TransferData transferType) {
		this.elements = null;
		this.operation = operation;

		ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
		if (selection == null || !(selection instanceof IStructuredSelection))
			return new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, "Selection either null or no structured selection.");

		IStructuredSelection sselection = (IStructuredSelection) selection;
		Object[] elements = sselection.toArray();
		for (Object element : elements) {
			if (target == element)
				return new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID,
						"You cannot drop yourself on yourself, dickhead!.");
		}

		DropTargetAdapter adapter = (DropTargetAdapter) adapterManager.getAdapter(target, DropTargetAdapter.class);
		if (adapter == null)
			return new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, "Drop Target (" + target.getClass().getName()
					+ ") is not adaptable to " + DropTargetAdapter.class.getName());

		if (!adapter.validateDrop(operation, target, elements))
			return new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, "Drop Target (" + target.getClass().getName()
					+ ") does not accept " + elements.toString());

		this.elements = elements;
		return Status.OK_STATUS;
	}

}
