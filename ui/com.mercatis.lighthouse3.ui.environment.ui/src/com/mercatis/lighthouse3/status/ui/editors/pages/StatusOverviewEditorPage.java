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
package com.mercatis.lighthouse3.status.ui.editors.pages;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.osgi.service.event.EventHandler;
import com.mercatis.lighthouse3.base.ui.editors.AbstractLighthouseEditorPage;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.services.Services;
import com.mercatis.lighthouse3.status.providers.StatusContentProvider;
import com.mercatis.lighthouse3.status.providers.StatusLabelProvider;
import com.mercatis.lighthouse3.status.ui.LighthouseStatusDecorator;
import com.mercatis.lighthouse3.status.ui.actions.OpenNewStatusWizardAction;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;
import com.mercatis.lighthouse3.ui.status.base.StatusModelChangedListener;
import com.mercatis.lighthouse3.ui.status.base.model.StatusEditingObject;


public class StatusOverviewEditorPage extends AbstractLighthouseEditorPage implements ISelectionChangedListener,
		StatusModelChangedListener, EventHandler, MenuDetectListener {

	public static final String CONTEXT_MENU_ID = "com.mercatis.lighthouse3.status.ui.statuseditorpage.ContextMenu";
	private final GenericEditorInput<? extends StatusCarrier> editorInput;

	private List<StatusEditingObject> statuus;
	private List<StatusEditingObject> statuusToDelete;
	private StatusEditingObject currentStatus;
	private TableViewer statusViewer;

	/**
	 * modifyListener should be enabled when page is loaded and disabled during updates
	 */
	private boolean modifyListenerEnabled = false;
	
	/**
	 * needed to get dirty state faster
	 */
	private StatusEditingObject lastDirty = null;

	private Text codeText;
	private Text longNameText;
	private Text descriptionText;
	private Text contactText;
	private Text contactEmailText;
	private Combo clearanceType;
	private Text stalenessIntervalInMsecs;
	private Button enableForAggregation;

	private FormToolkit toolkit;

	@SuppressWarnings("unchecked")
	public StatusOverviewEditorPage(FormEditor editor, String id, String title, List<StatusEditingObject> statuus) {
		super(editor, id, title);
		this.editorInput = (GenericEditorInput<? extends StatusCarrier>) editor.getEditorInput();
		this.statuus = statuus;
		this.statuusToDelete = new LinkedList<StatusEditingObject>();
		CommonBaseActivator.getPlugin().getStatusService().addStatusModelChangedListener(this);
		String serverDomainKey = editorInput.getDomain().getServerDomainKey();
		String filter = "(type=statusAggregationChanged)";
		Services.registerEventHandler(this, "com/mercatis/lighthouse3/event/"
				+ serverDomainKey.hashCode()
				+ "/*", filter);
	}

	@Override
	public void dispose() {
		super.dispose();
		CommonBaseActivator.getPlugin().getStatusService().removeStatusModelChangedListener(this);
		Services.unregisterEventHandler(this);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (isDirty()) {
			if (statuusToDelete != null && !statuusToDelete.isEmpty()) {
				for (StatusEditingObject std : statuusToDelete) {
					CommonBaseActivator.getPlugin().getStatusService().deleteStatus(std.getStatus());
				}
				PlatformUI.getWorkbench().getDecoratorManager().update(LighthouseStatusDecorator.id);
				statuusToDelete.clear();
			}
			if (getCurrentStatus() != null)
				updateStatusEditingObject();
			for (StatusEditingObject status : statuus) {
				try {
					status.updateModel();
					CommonBaseActivator.getPlugin().getStatusService().updateStatus(status.getStatus());
				} catch (Exception ex) {
					new MessageDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Save Status", null, ex
							.getMessage(), MessageDialog.ERROR, new String[] { "OK" }, 0).open();
				}
			}
		}
		getEditor().editorDirtyStateChanged();
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		toolkit = managedForm.getToolkit();
		toolkit.setBorderStyle(SWT.BORDER);
		form.setText("Status for: " + LabelConverter.getLabel(getEditorInput()));
		toolkit.decorateFormHeading(form.getForm());
		form.getBody().setLayout(new FillLayout());
		form.getBody().setBackgroundMode(SWT.INHERIT_FORCE);

		Composite top = new Composite(form.getBody(), SWT.NONE);
		GridLayout topLayout = new GridLayout(2, false);
		topLayout.horizontalSpacing = 5;
		topLayout.verticalSpacing = 0;
		top.setLayout(topLayout);

		Composite left = new Composite(top, SWT.NONE);
		GridData leftGridData = new GridData(SWT.BEGINNING, SWT.FILL, false, true);
		leftGridData.widthHint = 250;
		left.setLayoutData(leftGridData);
		left.setLayout(new FillLayout());
		createLeftSection(left, toolkit);

		Composite right = new Composite(top, SWT.NONE);
		right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout rightLayout = new GridLayout(1, true);
		rightLayout.horizontalSpacing = 0;
		rightLayout.verticalSpacing = 5;
		rightLayout.marginHeight = 0;
		rightLayout.marginWidth = 0;
		right.setLayout(rightLayout);
		createDetailSection(right);

		form.layout();
		statusViewer.addSelectionChangedListener(this);
		fillElementsForStatus(getCurrentStatus());
		if (getCurrentStatus() != null) {
			statusViewer.setSelection(new StructuredSelection(new Object[] { getCurrentStatus() }));
		}
		modifyListenerEnabled = true;
	}

	private void createLeftSection(Composite left, FormToolkit toolkit) {
		Composite client = new Composite(left, SWT.NONE);
		client.setLayout(new FillLayout());

		statusViewer = new TableViewer(client);
		statusViewer.setContentProvider(new StatusContentProvider());
		statusViewer.setLabelProvider(new StatusLabelProvider());
		statusViewer.setInput(statuus);
		createMenu(statusViewer);
		
		statusViewer.getTable().addMenuDetectListener(this);

		toolkit.paintBordersFor(client);
	}

	private void createDetailSection(Composite right) {
		Section section = toolkit.createSection(right, Section.EXPANDED | Section.TITLE_BAR);
		section.setText("Details");
		section.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		Composite client = toolkit.createComposite(section);
		toolkit.paintBordersFor(client);
		client.setLayout(new GridLayout(2, false));

		toolkit.createLabel(client, "Code:");
		codeText = toolkit.createText(client, "");
		codeText.setEditable(false);
		codeText.setEnabled(false);
		codeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		toolkit.createLabel(client, "Staleness Interval:");
		stalenessIntervalInMsecs = toolkit.createText(client, "");
		stalenessIntervalInMsecs.setToolTipText("Staleness interval in milliseconds");
		stalenessIntervalInMsecs.addModifyListener(modifyListener);
		stalenessIntervalInMsecs.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		toolkit.createLabel(client, "Clearance Type:");
		clearanceType = new Combo(client, SWT.DROP_DOWN);
		toolkit.adapt(clearanceType);
		clearanceType.add("auto");
		clearanceType.setData("auto", Status.AUTO_CLEARANCE);
		clearanceType.add("manual");
		clearanceType.setData("manual", Status.MANUAL_CLEARANCE);
		clearanceType.select(0);
		clearanceType.addModifyListener(modifyListener);
		clearanceType.addListener(SWT.Selection, commonChangeListener);
		clearanceType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		toolkit.createLabel(client, "Long name:");
		longNameText = toolkit.createText(client, "");
		longNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		longNameText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Description:");
		descriptionText = toolkit.createText(client, "");
		descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		descriptionText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Contact:");
		contactText = toolkit.createText(client, "");
		contactText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		contactText.addModifyListener(modifyListener);
		
		toolkit.createLabel(client, "Email:");
		contactEmailText = toolkit.createText(client, "");
		contactEmailText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		contactEmailText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Enable Aggregation:");
		enableForAggregation = toolkit.createButton(client, "", SWT.CHECK);
		enableForAggregation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		enableForAggregation.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				modifyListener.modifyText(null);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		// label necessary for win32 to render the enableForAggregation checkbox
		toolkit.createLabel(client, "");
		
		section.setClient(client);
	}

	private void createMenu(TableViewer viewer) {
		MenuManager menuMgr = new MenuManager("Status", CONTEXT_MENU_ID);
		getEditor().getSite().registerContextMenu(CONTEXT_MENU_ID, menuMgr, viewer);
		viewer.getControl().setMenu(menuMgr.getMenu());
		Menu contextMenu = menuMgr.createContextMenu(getEditor().getSite().getShell());
		viewer.getTable().setMenu(contextMenu);
	}

	private ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			if (modifyListenerEnabled) {
				updateStatusEditingObject();
				getEditor().editorDirtyStateChanged();
			}
		}
	};
	
	private Listener commonChangeListener = new Listener() {
		public void handleEvent(Event event) {
			modifyListener.modifyText(null);
		}
	};

	public StatusEditingObject getCurrentStatus() {
		if (currentStatus == null && statuus.size() > 0) {
			currentStatus = statuus.iterator().next();
		}
		return currentStatus;
	}

	private void setCurrentStatus(StatusEditingObject newStatus) {
		boolean oldModifyListenerEnabledState = modifyListenerEnabled;
		modifyListenerEnabled = false;
		if (currentStatus != null && currentStatus.equals(newStatus)) {
			modifyListenerEnabled = oldModifyListenerEnabledState;
			return;
		}
		currentStatus = newStatus;
		fillElementsForStatus(currentStatus);
		modifyListenerEnabled = oldModifyListenerEnabledState;
	}

	public void selectionChanged(SelectionChangedEvent event) {
		boolean oldModifyListenerEnabledState = modifyListenerEnabled;
		modifyListenerEnabled = false;
		StatusEditingObject selectedStatus = (StatusEditingObject) ((StructuredSelection) event.getSelection())
				.getFirstElement();
		if (selectedStatus != null && selectedStatus.equals(getCurrentStatus())) {
			modifyListenerEnabled = oldModifyListenerEnabledState;
			return;
		}
		setCurrentStatus(selectedStatus);
		modifyListenerEnabled = oldModifyListenerEnabledState;
	}

	private void updateStatusEditingObject() {
		StatusEditingObject seo = getCurrentStatus(); 
		if (seo==null)
			return;
		seo.setLongName(longNameText.getText());
		seo.setDescription(descriptionText.getText());
		seo.setContact(contactText.getText());
		seo.setContactEmail(contactEmailText.getText());
		long interval;
		try {
			interval = Long.parseLong(stalenessIntervalInMsecs.getText());
		} catch (NumberFormatException e) {
			String intervalStr = stalenessIntervalInMsecs.getText();
			if (intervalStr.length() == 0) {
				intervalStr = "0";
			} else {
				intervalStr = intervalStr.substring(0, intervalStr.length() - 1);
			}
			stalenessIntervalInMsecs.setText(intervalStr);
			interval = Long.parseLong(intervalStr);
		}
		seo.setStalenessIntervalInMsecs(interval);
		seo.setClearanceType((Integer) clearanceType.getData(clearanceType.getText()));
		seo.setEnabledForAggregation(enableForAggregation.getSelection());
	}

	private void fillElementsForStatus(StatusEditingObject status) {
		if (status == null) {
			setEditingEnabled(false);
			return;
		} else {
		}
		codeText.setText(status.getCode());
		stalenessIntervalInMsecs.setText(Long.toString(status.getStalenessIntervalInMsecs()));
		clearanceType.select(status.getClearanceType() == Status.AUTO_CLEARANCE ? 0 : 1);
		longNameText.setText(status.getLongName() == null ? "" : status.getLongName());
		descriptionText.setText(status.getDescription() == null ? "" : status.getDescription());
		contactText.setText(status.getContact() == null ? "" : status.getContact());
		contactEmailText.setText(status.getContactEmail() == null ? "" : status.getContactEmail());
		enableForAggregation.setSelection(status.isEnabledForAggregation());
		
		boolean hasRole = CodeGuard.hasRole(Role.STATUS_MODIFY, currentStatus);
		setEditingEnabled(hasRole);
	}
	
	private void setEditingEnabled(boolean enabled) {
		stalenessIntervalInMsecs.setEditable(enabled);
		stalenessIntervalInMsecs.setEnabled(enabled);
		clearanceType.setEnabled(enabled);
		longNameText.setEditable(enabled);
		longNameText.setEnabled(enabled);
		descriptionText.setEditable(enabled);
		descriptionText.setEnabled(enabled);
		contactText.setEditable(enabled);
		contactText.setEnabled(enabled);
		contactEmailText.setEditable(enabled);
		contactEmailText.setEnabled(enabled);
		enableForAggregation.setEnabled(enabled);
	}

	@Override
	public boolean isDirty() {
		if (!statuusToDelete.isEmpty()) {
			return true;
		}

		if (getCurrentStatus() == null)
			return false;

		if (lastDirty != null && lastDirty.isDirty())
			return true;

		StatusEditingObject status = getCurrentStatus();
		if (status.isDirty()) {
			lastDirty = status;
			return true;
		}

		for (StatusEditingObject statusEditingObject : statuus) {
			if (statusEditingObject.equals(status)) //TODO test if this is needed
				continue;
			if (statusEditingObject.isDirty()) {
				lastDirty = statusEditingObject;
				return true;
			}
		}
		lastDirty = null;
		return false;
	}

	public void markStatusForDelete(StatusEditingObject status) {
		if (MessageDialog.openConfirm(getSite().getShell(), "Remove Status", "The selected status will get deleted when you save your changes.\n\nDo you really want to remove \""+status.getCode()+"\"?")) {
			statuusToDelete.add(status);
			statuus.remove(status);
			statusViewer.remove(status);
			getEditor().editorDirtyStateChanged();
		}
	}

	public void unmarkStatusForDelete(StatusEditingObject status) {
		statuusToDelete.remove(status);
		statuus.add(status);
		statusViewer.add(status);
		getEditor().editorDirtyStateChanged();
	}

	@Override
	public ISelectionProvider refreshSelectionProvider() {
		return statusViewer;
	}

	public void statusModelChanged(LighthouseDomain lighthouseDomain, Object source, String property, Object oldValue,
			Object newValue) {
		if (newValue != null && newValue instanceof Status) {
			if (((Status)newValue).getContext().equals(editorInput.getEntity())) {
				if (oldValue == null) {
					StatusEditingObject status = new StatusEditingObject((Status) newValue);
					statuus.add(status);
					setCurrentStatus(status);
				}
				statusViewer.refresh();
			}
		}
		if (oldValue != null && oldValue instanceof Status && newValue == null) {
			statuus.remove(new StatusEditingObject((Status)oldValue));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#setFocus()
	 */
	@Override
	public void setFocus() {
		statusViewer.getControl().setFocus();
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
	 */
	public void handleEvent(org.osgi.service.event.Event event) {
		final String statusCode = (String) event.getProperty("code");
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			public void run() {
				// do i display it this status?
				boolean isShown = false;
				for (StatusEditingObject status : statuus) {
					if (status.getCode().equals(statusCode)) {
						isShown = true;
						break;
					}
				}
				for (StatusEditingObject statusToDelete : statuusToDelete) {
					if (statusToDelete.getCode().equals(statusCode)) // it's about to be deleted - ingore this change...
						return;
				}
				if (isShown)
					statusViewer.refresh();
			}
		});
	}
	
	public void menuDetected(MenuDetectEvent e) {
		Table t = (Table) e.widget;
		if (t.getSelectionCount()==0) {
			IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			System.out.println(editor);
			if (editor instanceof FormEditor) {
				FormEditor fe = (FormEditor) editor;
				IFormPage page = fe.getActivePageInstance();
				if (page.getEditorInput() instanceof GenericEditorInput<?>) {
					StatusCarrier sc = (StatusCarrier) ((GenericEditorInput<?>)page.getEditorInput()).getEntity();
					MenuManager mm = new MenuManager();
					mm.add(new OpenNewStatusWizardAction(getSite().getShell(), sc));
					Menu m = mm.createContextMenu(t);
					m.setLocation(e.x, e.y);
					m.setVisible(true);
				}
			}
		}
	}
}
