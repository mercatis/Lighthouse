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
package com.mercatis.lighthouse3.ui.operations.ui.editors.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.ide.IDE;
import com.mercatis.lighthouse3.base.UIBase;
import com.mercatis.lighthouse3.base.ui.editors.AbstractExtendableFormEditor;
import com.mercatis.lighthouse3.base.ui.editors.AbstractLighthouseEditorPage;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;
import com.mercatis.lighthouse3.ui.operations.base.OperationsChangedListener;
import com.mercatis.lighthouse3.ui.operations.base.model.Category;
import com.mercatis.lighthouse3.ui.operations.base.model.OperationInstallationWrapper;
import com.mercatis.lighthouse3.ui.operations.ui.actions.OpenInstallOperationWizardAction;
import com.mercatis.lighthouse3.ui.operations.ui.editors.JobEditor;
import com.mercatis.lighthouse3.ui.operations.ui.providers.JobTableContentProvider;
import com.mercatis.lighthouse3.ui.operations.ui.providers.JobTableLabelProvider;
import com.mercatis.lighthouse3.ui.operations.ui.providers.OperationsTreeContentProvider;
import com.mercatis.lighthouse3.ui.operations.ui.providers.OperationsTreeLabelProvider;


public class InstalledOperationsEditorPage extends AbstractLighthouseEditorPage implements OperationsChangedListener, MenuDetectListener {

	public static final String INSTALLATIONS_CONTEXTMENU_ID = "com.mercatis.lighthouse3.ui.operations.ui.operationseditorpage.installations.contextmenu";
	public static final String JOBS_CONTEXTMENU_ID = "com.mercatis.lighthouse3.ui.operations.ui.operationseditorpage.jobs.contextmenu";

	private Deployment deployment;
	private TreeViewer operationsTree;
	private TableViewer jobTable;
	private LighthouseDomain lighthouseDomain;
	private List<OperationInstallation> installations;
	private List<OperationInstallation> installationsToDelete = new ArrayList<OperationInstallation>();
	private List<Job> jobs;
	private List<Job> jobsToDelete = new ArrayList<Job>();

	public InstalledOperationsEditorPage(FormEditor editor, String id, String title, List<OperationInstallation> installations) {
		super(editor, id, title);
		this.installations = installations;
		OperationBase.addOperationsChangedListener(this);
	}

	@Override
	public ISelectionProvider refreshSelectionProvider() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		GenericEditorInput<Deployment> editorInput = ((GenericEditorInput<Deployment>) getEditor().getEditorInput());
		deployment = editorInput.getEntity();
		lighthouseDomain = editorInput.getDomain();

		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		form.setText("Operations on: " + LabelConverter.getLabel(deployment));
		toolkit.decorateFormHeading(form.getForm());
		form.getBody().setLayout(new GridLayout(2, false));

		createLeftSection(form, toolkit);
		createRightSection(form, toolkit);
	}

	private void createLeftSection(ScrolledForm form, FormToolkit toolkit) {
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		section.clientVerticalSpacing = 5;
		section.marginHeight = 3;
		section.marginWidth = 3;
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		section.setLayoutData(gd);
		section.setText("Installed Operations");

		Composite client = toolkit.createComposite(section);
		toolkit.paintBordersFor(client);
		client.setLayout(new GridLayout(1, false));

		operationsTree = new TreeViewer(client);
		operationsTree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		operationsTree.setContentProvider(new OperationsTreeContentProvider());
		operationsTree.setLabelProvider(new OperationsTreeLabelProvider());
		operationsTree.getControl().addFocusListener(new FocusListener() {
			
			public void focusLost(FocusEvent e) {
			}
			
			public void focusGained(FocusEvent e) {
				((AbstractExtendableFormEditor)getEditor()).setSelectionProvider(operationsTree);
			}
		});
		operationsTree.getTree().addMenuDetectListener(this);
		fillOperationInstallationTree();
		createOperationInstallationContextMenu();
		section.setClient(client);
	}

	private void createRightSection(ScrolledForm form, FormToolkit toolkit) {
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		section.clientVerticalSpacing = 5;
		section.marginHeight = 3;
		section.marginWidth = 3;
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		section.setLayoutData(gd);
		section.setText("Automated Jobs");

		Composite client = toolkit.createComposite(section);
		toolkit.paintBordersFor(client);
		client.setLayout(new GridLayout(1, false));

		jobTable = new TableViewer(client);
		jobTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		jobTable.setContentProvider(new JobTableContentProvider());
		jobTable.setLabelProvider(new JobTableLabelProvider());
		jobTable.getControl().addFocusListener(new FocusListener() {
			
			public void focusLost(FocusEvent e) {
			}
			
			public void focusGained(FocusEvent e) {
				((AbstractExtendableFormEditor)getEditor()).setSelectionProvider(jobTable);
			}
		});
		jobTable.addDoubleClickListener(new IDoubleClickListener() {
			
			public void doubleClick(DoubleClickEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				if (selection.getFirstElement() instanceof Job) {
					GenericEditorInput<Job> input = new GenericEditorInput<Job>(lighthouseDomain, (Job)selection.getFirstElement());
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IDE.openEditor(page, input, JobEditor.ID);
					} catch (PartInitException ex) {
						UIBase.getDefault().getLog().log(new Status(IStatus.ERROR, UIBase.PLUGIN_ID, ex.getMessage(), ex));
					}
				}
			}
		});
		fillJobTable();
		createJobContextMenu();
		section.setClient(client);
	}

	private void fillOperationInstallationTree() {
		if (installations == null) {
			installations = OperationBase.getOperationInstallationService().findAtDeployment(deployment);
		}
		Map<String, Category<OperationInstallationWrapper>> categories = new HashMap<String, Category<OperationInstallationWrapper>>();
		for (OperationInstallation installation : installations) {
			OperationInstallationWrapper loader = new OperationInstallationWrapper(installation);
			Category<OperationInstallationWrapper> category = categories.get(loader.getCategory());
			if (category == null) {
				category = new Category<OperationInstallationWrapper>(lighthouseDomain, loader.getCategory());
				categories.put(loader.getCategory(), category);
			}
			category.addOperation(loader);
		}
		List<Category<OperationInstallationWrapper>> installations = new ArrayList<Category<OperationInstallationWrapper>>(categories.values());
		Collections.sort(installations);
		installations.removeAll(installationsToDelete);
		operationsTree.setInput(installations);
		operationsTree.expandAll();
	}
	
	private void fillJobTable() {
		if (jobs == null) {
			jobs = OperationBase.getJobService().findAtDeployment(deployment);
		}
		jobs.removeAll(jobsToDelete);
		jobTable.setInput(jobs);
	}
	
	@Override
	public boolean isDirty() {
		return !jobsToDelete.isEmpty() || !installationsToDelete.isEmpty();
	}

	private void createOperationInstallationContextMenu() {
		MenuManager menuMgr = new MenuManager("Operations", INSTALLATIONS_CONTEXTMENU_ID);
		getSite().registerContextMenu(INSTALLATIONS_CONTEXTMENU_ID, menuMgr, operationsTree);
		operationsTree.getControl().setMenu(menuMgr.getMenu());
		Menu contextMenu = menuMgr.createContextMenu(getSite().getShell());
		operationsTree.getTree().setMenu(contextMenu);
	}
	
	private void createJobContextMenu() {
		MenuManager menuMgr = new MenuManager("Jobs", JOBS_CONTEXTMENU_ID);
		getSite().registerContextMenu(JOBS_CONTEXTMENU_ID, menuMgr, jobTable);
		jobTable.getControl().setMenu(menuMgr.getMenu());
		Menu contextMenu = menuMgr.createContextMenu(getSite().getShell());
		jobTable.getTable().setMenu(contextMenu);
	}

	public void operationsChanged(LighthouseDomain lighthouseDomain, Object source, String property, Object oldValue,
			Object newValue) {
		if (source != null && source instanceof OperationInstallation) {
			installations = null;
			fillOperationInstallationTree();
		} else if (source != null && source instanceof Job) {
			jobs = null;
			fillJobTable();
		}
	}

	@Override
	public void dispose() {
		OperationBase.removeOperationsChangedListener(this);
		super.dispose();
	}
	
	public void markJobForDelete(Job job) {
		if (MessageDialog.openConfirm(getSite().getShell(), "Remove Job", "The selected job will get deleted when you save your changes.\n\nDo you really want to remove \""+job.getCode()+"\"?")) {
			jobs.remove(job);
			jobsToDelete.add(job);
			fillJobTable();
			getEditor().editorDirtyStateChanged();
		}
	}
	
	public void markOperationInstallationForRemove(OperationInstallation operationInstallation) {
		if (MessageDialog.openConfirm(getSite().getShell(), "Remove Operation", "The selected operation will get deleted when you save your changes.\n\nDo you really want to remove this operation?")) {
			installations.remove(operationInstallation);
			installationsToDelete.add(operationInstallation);
			fillOperationInstallationTree();
			getEditor().editorDirtyStateChanged();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		for (Job job : jobsToDelete) {
			OperationBase.getJobService().delete(job);
		}
		for (OperationInstallation installation : installationsToDelete) {
			for (Job job : jobs) {
				if (job.getScheduledCall().getTarget().getInstalledOperationCode().equals(installation.getInstalledOperationCode()) && !jobsToDelete.contains(job))
					OperationBase.getJobService().delete(job);
			}
			OperationBase.getOperationInstallationService().delete(installation);
		}
		jobsToDelete.clear();
		installationsToDelete.clear();
		getEditor().editorDirtyStateChanged();
	}
	
	@Override
	public void setFocus() {
		operationsTree.getControl().setFocus();
		((AbstractExtendableFormEditor)getEditor()).setSelectionProvider(operationsTree);
	}

	public void menuDetected(MenuDetectEvent e) {
		Tree tree = (Tree) e.widget;
		ITreeSelection sel = (ITreeSelection) operationsTree.getSelection();
		int selCnt = tree.getSelectionCount();
		if (selCnt==0 || (selCnt==1 && sel.iterator().next() instanceof Category<?>)) {
			MenuManager mm = new MenuManager();
			mm.add(new OpenInstallOperationWizardAction(getSite().getShell(), lighthouseDomain, deployment));
			Menu m = mm.createContextMenu(tree);
			m.setLocation(e.x, e.y);
			m.setVisible(true);
			return;
		}
	}
}
