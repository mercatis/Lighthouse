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
package com.mercatis.lighthouse3.ui.application;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class LighthouseApplication implements IApplication {

	public final File profileDir;

	public LighthouseApplication() {
		String os = System.getProperty("os.name").toLowerCase();
		String subDir = System.getProperty("user.home");
		String appDir = ".lighthouse";
		// possible values: AIX, Digital Unix, FreeBSD, HP UX, Irix, Linux, Mac OS, Mac OS X, MPE/iX, Netware 4.11, OS/2, Solaris,
		//                  Windows 2000, Windows 7, Windows 95, Windows 98, Windows NT, Windows Vista, Windows XP
		if (os.startsWith("windows")) {
			subDir = System.getenv("APPDATA");
			appDir = "Lighthouse";
		}
		if (os.equals("mac os x")) {
			subDir = System.getProperty("user.home") + "/Library/Application Support";
			appDir = "Lighthouse";
		}
		profileDir = new File(subDir, appDir);
	}
	
	public Object start(IApplicationContext context) throws Exception {
		Display display = PlatformUI.createDisplay();
		
		try {
			Location loc = Platform.getInstanceLocation();
			loc.release();
			loc.set(profileDir.toURL(), false);
			
			int returnCode = PlatformUI.createAndRunWorkbench(display, new LighthouseApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}
	}

	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}

}
