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
package com.mercatis.lighthouse3.status.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;


public class EarlyStartup implements IStartup {

	public void earlyStartup() {
		try {
			PlatformUI.getWorkbench().getDecoratorManager().setEnabled(LighthouseStatusDecorator.id, true);
		} catch (CoreException e) {
			CommonUIActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, e.getMessage(), e));
		}
	}
}
