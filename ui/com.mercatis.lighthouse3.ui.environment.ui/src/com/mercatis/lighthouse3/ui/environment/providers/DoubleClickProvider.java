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
package com.mercatis.lighthouse3.ui.environment.providers;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;

public class DoubleClickProvider extends CommonActionProvider {

	private class DoubleClickAction extends Action {

		private Command doubleClickCommand;

		private ICommandService commandService;
		private IHandlerService handlerService;

		public DoubleClickAction() {
			commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
			handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
			doubleClickCommand = commandService.getCommand("lighthouse.env.cmd.edit");
		}
		
		@SuppressWarnings("deprecation")
		public void run() {
			if (!doubleClickCommand.isEnabled())
				return;
			ExecutionEvent evt = handlerService.createExecutionEvent(doubleClickCommand, new Event());
			try {
				doubleClickCommand.execute(evt);
			} catch (ExecutionException ex) {
				CommonUIActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, ex.getMessage(), ex));
			} catch (NotHandledException ex) {
				CommonUIActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, ex.getMessage(), ex));
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.Action#isEnabled()
		 */
		public boolean isEnabled() {
			return doubleClickCommand.isEnabled();
		}

		@Override
		public boolean isHandled() {
			return doubleClickCommand.isEnabled();
		}
	}

	@Override
	public void init(ICommonActionExtensionSite site) {
		super.init(site);
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, new DoubleClickAction());
		actionBars.updateActionBars();
	}
}
