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
package com.mercatis.lighthouse3.ui.operations.ui.views;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

import com.mercatis.lighthouse3.domainmodel.operations.Operation;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;
import com.mercatis.lighthouse3.ui.operations.base.OperationsChangedListener;
import com.mercatis.lighthouse3.ui.operations.base.model.Category;
import com.mercatis.lighthouse3.ui.operations.ui.providers.OperationsTreeContentProvider;
import com.mercatis.lighthouse3.ui.operations.ui.providers.OperationsTreeLabelProvider;


public class OperationsOverView extends ViewPart implements OperationsChangedListener {
	
	public static final String ID = "com.mercatis.lighthouse3.ui.operations.ui.operationsoverview";
	public static final String CONTEXT_MENU_ID = "com.mercatis.lighthouse3.ui.operations.ui.operationsoverview.contextmenu";
	
	private LighthouseDomain lighthouseDomain;
	private TreeViewer operationsTree;
	private Label domain;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		domain = new Label(parent, SWT.NONE);
		domain.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		operationsTree = new TreeViewer(parent);
		operationsTree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		operationsTree.setContentProvider(new OperationsTreeContentProvider());
		operationsTree.setLabelProvider(new OperationsTreeLabelProvider());
		getSite().setSelectionProvider(operationsTree);
		createMenu();
		OperationBase.addOperationsChangedListener(this);
	}

	@Override
	public void dispose() {
		OperationBase.removeOperationsChangedListener(this);
		super.dispose();
	}

	@Override
	public void setFocus() {
	}
	
	public void loadData(LighthouseDomain lightouseDomain) {
		TreePath[] expanded = null;
		if (operationsTree != null)
			expanded = operationsTree.getExpandedTreePaths();
		this.lighthouseDomain = lightouseDomain;
		List<Category<Operation>> categories = OperationBase.getOperationService().findAllCategories(lighthouseDomain);
		Collections.sort(categories);
		this.domain.setText("Operations on " + lighthouseDomain.getProject().getName());
		this.setPartName("Operations on " + lighthouseDomain.getProject().getName());
		if (operationsTree != null) {
			operationsTree.setInput(categories);
			operationsTree.setExpandedTreePaths(expanded);
		}
	}

	private void createMenu() {
		MenuManager menuMgr = new MenuManager("Operation", CONTEXT_MENU_ID);
		getSite().registerContextMenu(CONTEXT_MENU_ID, menuMgr, operationsTree);
		operationsTree.getControl().setMenu(menuMgr.getMenu());
		Menu contextMenu = menuMgr.createContextMenu(getSite().getShell());
		operationsTree.getTree().setMenu(contextMenu);
	}

	public void operationsChanged(LighthouseDomain lighthouseDomain, Object source, String property, Object oldValue,
			Object newValue) {
		if (this.lighthouseDomain.equals(lighthouseDomain))
			loadData(this.lighthouseDomain);
	}
}
