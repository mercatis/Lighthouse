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

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class LighthouseApplicationActionBarAdvisor extends ActionBarAdvisor {

	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.
	private IWorkbenchAction exitAction;
	private IContributionItem openWindowAction;
	private IContributionItem openPerspectiveAction;
	private IWorkbenchAction aboutAction;
	private IWorkbenchAction saveAction;
	private IWorkbenchAction resetPerspectiveAction;
	private IWorkbenchPartSite site;

	public LighthouseApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.
		openWindowAction = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
		openPerspectiveAction = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
		
		resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE.create(window);
		register(resetPerspectiveAction);
		
		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);
		
		aboutAction = ActionFactory.ABOUT.create(window);
		register(aboutAction);
		
		saveAction = ActionFactory.SAVE.create(window);
		register(saveAction);
		
		window.getPartService().addPartListener(iPartListener);
	}
	
	private IPartListener iPartListener = new IPartListener() {

		public void partActivated(IWorkbenchPart part) {
			LighthouseApplicationActionBarAdvisor.this.site = part.getSite();
			LighthouseApplicationActionBarAdvisor.this.registerFileMenu();
		}

		public void partBroughtToTop(IWorkbenchPart part) {}
		public void partClosed(IWorkbenchPart part) {}
		public void partDeactivated(IWorkbenchPart part) {}
		public void partOpened(IWorkbenchPart part) {}
	};
	private MenuManager fileMenu;
	
	private boolean fileMenuRegistered = false;
	private void registerFileMenu() {
		if (!fileMenuRegistered) {
			if (site != null && fileMenu != null)
				site.registerContextMenu("com.mercatis.lighthouse3.ui.filemenu", fileMenu, null);
			fileMenu.add(saveAction);
			fileMenu.add(exitAction);
			fileMenuRegistered = true;
		}
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		IToolBarManager toolBar = new ToolBarManager(coolBar.getStyle());
		coolBar.add(toolBar);
		toolBar.add(saveAction);
	}

	protected void fillMenuBar(IMenuManager menuBar) {
		fileMenu = new MenuManager("&File", "com.mercatis.lighthouse3.ui.filemenu");
		menuBar.add(fileMenu);
		
		MenuManager aboutMenu = new MenuManager("&Help", "Help");
		aboutMenu.add(aboutAction);

		MenuManager viewMenu = new MenuManager("&View", IWorkbenchActionConstants.M_WINDOW);
		viewMenu.add(openWindowAction);
		
		MenuManager perspectiveMenu = new MenuManager("&Perspective", "Perspective");
		perspectiveMenu.add(openPerspectiveAction);
		perspectiveMenu.add(resetPerspectiveAction);

		menuBar.add(viewMenu);
		menuBar.add(perspectiveMenu);
		menuBar.add(aboutMenu);
	}
}
