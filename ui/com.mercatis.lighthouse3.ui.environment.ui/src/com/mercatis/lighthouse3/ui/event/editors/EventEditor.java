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
package com.mercatis.lighthouse3.ui.event.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.EditorPart;
import org.osgi.service.event.EventHandler;

import com.mercatis.lighthouse3.base.ui.editors.EventEditorInput;
import com.mercatis.lighthouse3.base.ui.widgets.eventfilter.Filter;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventBuilder;
import com.mercatis.lighthouse3.services.Services;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.event.providers.EventFilterModel;
import com.mercatis.lighthouse3.ui.event.providers.EventTable;

/**
 * Editor for displaying and filtering of events, based on an event template.
 * 
 * Note: Images taken from http:
 */ 
public class EventEditor extends EditorPart implements EventHandler {
	
	private class FilterDialog extends Dialog {
		
		private static final int RESET_ID = -1;
		
		private Filter filter;
		
		protected FilterDialog(IShellProvider shellProvider) {
			super(shellProvider);
			setShellStyle(getShellStyle() | SWT.RESIZE);
		}

		protected FilterDialog(Shell parentShell) {
			super(parentShell);
			setShellStyle(getShellStyle() | SWT.RESIZE);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
		 */
		@Override
		protected void buttonPressed(int buttonId) {
			switch (buttonId) {
				case IDialogConstants.OK_ID:
					filter.apply();
					if (eventReceiptStarted) {
						stopEventReceipt();
						startEventReceipt();
					}
					break;
				case IDialogConstants.CANCEL_ID:
					break;
				default:
					filter.reset();
			}
			super.buttonPressed(buttonId);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
			createButton(parent, RESET_ID, "Reset", false);
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		protected Control createDialogArea(Composite parent) {			
			Composite composite = (Composite) super.createDialogArea(parent);
			composite.setRedraw(false);
			ScrolledComposite sc = new ScrolledComposite(composite, SWT.V_SCROLL);
			sc.setExpandHorizontal(true);
			sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			sc.setLayout(new FillLayout());
			filter = new Filter(sc, eventFilterModel, SWT.NONE);
			sc.setContent(filter);
			sc.setMinHeight(400);
			filter.reset();
			composite.setRedraw(true);
			
			return composite;
		}
	}
	
	public static final String OSGI_EVENT_TOPIC = "com/mercatis/lighthouse3/viewevents";
	
	public static final String OSGI_EVENT_FILTER = "(tableViewer=%s)";

	private Image clearImage = ImageDescriptor.createFromURL(getClass().getResource("/icons/trash.gif")).createImage();

	private ToolItem clearItem;

	private Composite composite;

	private EventFilterModel eventFilterModel;

	private boolean eventReceiptStarted = false;

	private EventTable eventTable;

	private Combo maxEventsCombo;
	
	private Button showFilterDialog;

	private Image startImage = ImageDescriptor.createFromURL(getClass().getResource("/icons/control_play.gif")).createImage();
	
	private ToolItem startStopItem;
	
	private Image stopImage = ImageDescriptor.createFromURL(getClass().getResource("/icons/control_stop.gif")).createImage();

	private Image toggleImage = ImageDescriptor.createFromURL(getClass().getResource("/icons/arrows_up_down.gif")).createImage();

	private FormToolkit toolkit;
	
	private SelectionListener adjustFilterButtonSelectionListener = new SelectionListener() {
		
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
		
		public void widgetSelected(SelectionEvent e) {
			FilterDialog filterDialog = new FilterDialog(getSite());
			filterDialog.open();
		}
	};
	
	private SelectionListener clearItemSelectionListener = new SelectionListener() {
		
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
		
		public void widgetSelected(SelectionEvent e) {
			eventTable.clearTable();
		}
	};
	
	private ModifyListener maxEventsModifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			Combo combo = (Combo) e.getSource();
			int max = Integer.valueOf(combo.getText());
			eventTable.setMaxNumberOfEventsToDisplay(max);
		}
	};
	
	private SelectionListener startStopItemSelectionListener = new SelectionListener() {
		
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
		
		public void widgetSelected(SelectionEvent e) {
			if (!eventReceiptStarted) {
				startEventReceipt();
			} else {
				stopEventReceipt();
			}
		}
	};
	
	private void createControls() {
		Group controlGroup = new Group(composite, SWT.NONE);
		controlGroup.setText("Event Controls");
		controlGroup.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		controlGroup.setLayout(new GridLayout(5, false));
		
		showFilterDialog = new Button(controlGroup, SWT.PUSH);
		showFilterDialog.setText("Adjust Filter");
		showFilterDialog.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		showFilterDialog.addSelectionListener(adjustFilterButtonSelectionListener );
		
		ToolBar bar = new ToolBar(controlGroup, SWT.NONE);
		bar.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false, false));
		
		startStopItem = new ToolItem(bar, SWT.PUSH);
		startStopItem.setImage(startImage);
		startStopItem.setToolTipText("Start Event Receipt");
		startStopItem.addSelectionListener(startStopItemSelectionListener);
		
		new ToolItem(bar, SWT.SEPARATOR);
		
		clearItem = new ToolItem(bar, SWT.PUSH);
		clearItem.setImage(clearImage);
		clearItem.setToolTipText("Clear Table");
		clearItem.addSelectionListener(clearItemSelectionListener);
		
		Label label = new Label(controlGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label l = new Label(controlGroup, SWT.NONE);
		l.setText("Maximum number of events");
		l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		maxEventsCombo = new Combo(controlGroup, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
		data.minimumWidth = 40;

		int[] vals = { 25, 50, 100, 250, 500 };
		for (int v : vals) {
			maxEventsCombo.add(Integer.toString(v));
		}
		int width = maxEventsCombo.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
		data.widthHint = width;
		maxEventsCombo.setLayoutData(data);
		maxEventsCombo.addModifyListener(maxEventsModifyListener);
	}
	
	private void createEventTable() {
		eventTable = new EventTable(((EventEditorInput) this.getEditorInput()).getLighthouseDomain(), composite, SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL, getSite(), eventFilterModel, ((EventEditorInput) getEditorInput()).getPrefetchedEvents());
		eventTable.getEventTable().setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		eventTable.getEventTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		maxEventsCombo.select(2);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		
		composite = toolkit.createComposite(parent, SWT.NONE);
		composite.setLayout(layout);
		createControls();
		LighthouseDomain domain = ((EventEditorInput) getEditorInput()).getLighthouseDomain();
		Event template = ((EventEditorInput) getEditorInput()).getEventTemplate();
		if (template == null)
			template = EventBuilder.template().done();
		eventFilterModel = new EventFilterModel(domain, template);
		createEventTable();
		
		toolkit.paintBordersFor(parent);
		Services.registerEventHandler(this, OSGI_EVENT_TOPIC, String.format(OSGI_EVENT_FILTER, eventTable.getIdentifier()));
		
		startEventReceipt();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		stopEventReceipt();
		composite.dispose();
		startImage.dispose();
		stopImage.dispose();
		clearImage.dispose();
		toggleImage.dispose();
		toolkit.dispose();
		Services.unregisterEventHandler(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
		setTitleToolTip(input.getToolTipText());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		composite.setFocus();
	}

	public void startEventReceipt() {
		startStopItem.setImage(stopImage);
		startStopItem.setToolTipText("Stop Event Receipt");
		eventTable.registerForEvents(eventFilterModel.getTemplate());
		eventReceiptStarted = true;
	}

	public void stopEventReceipt() {
		if (!this.composite.isDisposed()) {
			startStopItem.setImage(startImage);
			startStopItem.setToolTipText("Start Event Receipt");
		}
		eventTable.unregisterForEvents();
		eventReceiptStarted = false;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
	 */
	public void handleEvent(org.osgi.service.event.Event event) {
		if (event.getProperty("operation").equals("cancel")) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					stopEventReceipt();
				}
			});
		}
	}
}
