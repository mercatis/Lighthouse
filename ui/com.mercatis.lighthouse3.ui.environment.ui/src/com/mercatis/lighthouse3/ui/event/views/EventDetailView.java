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
package com.mercatis.lighthouse3.ui.event.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.ui.event.common.EventUpdateListener;
import com.mercatis.lighthouse3.ui.event.common.EventUpdateService;
import com.mercatis.lighthouse3.ui.event.views.controls.GeneralEventControl;
import com.mercatis.lighthouse3.ui.event.views.controls.StackTraceEventControl;
import com.mercatis.lighthouse3.ui.event.views.controls.UdfEventControl;


public class EventDetailView extends ViewPart implements EventUpdateListener {

	public static final String ID = "lighthouse3.events.view.eventdetailview";

	private Event event;
	private EventSelectionListener eventSelectionListener;

	private GeneralEventControl generalEventControl;
	private StackTraceEventControl stackTraceEventControl;
	private UdfEventControl udfEventControl;

	public EventDetailView() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		ScrolledForm tabbedForm = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(tabbedForm.getForm());
		tabbedForm.setText("Event Detail View");
		tabbedForm.getBody().setLayout(new GridLayout(1, true));

		CTabFolder tabFolder = new CTabFolder(tabbedForm.getBody(), SWT.BOTTOM);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		toolkit.adapt(tabFolder);

		CTabItem generalTab = new CTabItem(tabFolder, SWT.V_SCROLL);
		generalTab.setText("General Info");

		generalEventControl = new GeneralEventControl(tabFolder, toolkit);
		generalTab.setControl(generalEventControl.getComponent());

		CTabItem stackTraceTab = new CTabItem(tabFolder, SWT.BOTTOM);
		stackTraceTab.setText("Stacktrace");

		stackTraceEventControl = new StackTraceEventControl(tabFolder, toolkit);
		stackTraceTab.setControl(stackTraceEventControl);

		CTabItem udfTab = new CTabItem(tabFolder, SWT.BOTTOM);
		udfTab.setText("User Defined Fields");

		udfEventControl = new UdfEventControl(tabFolder, toolkit);
		udfTab.setControl(udfEventControl);

		// select by default the general tab
		tabFolder.setSelection(generalTab);

		// register as event-listener for selection and update events
		EventUpdateService.getInstance().addEventUpdateListener(this);
		eventSelectionListener = new EventSelectionListener(this);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(eventSelectionListener);
		selectIfEventSelected();
	}
	
	@Override
	public void dispose() {
		EventUpdateService.getInstance().removeEventUpdateListener(this);
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(eventSelectionListener);
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		generalEventControl.getComponent().setFocus();
	}

	public Event getEvent() {
		return event;
	}
	
	/**
	 * 
	 */
	private void selectIfEventSelected() {
		ISelection selection = getSite().getWorkbenchWindow().getSelectionService().getSelection();
		IStructuredSelection structuredSelection = null;
		if (selection instanceof IStructuredSelection) {
			structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.getFirstElement() instanceof Event) {
				showEvent((Event) structuredSelection.getFirstElement());
			}
		}
	}

	public void showEvent(final Event event) {
		this.event = event;
		refreshControls();
	}

	private void refreshControls() {
		generalEventControl.setEvent(event);
		stackTraceEventControl.setEvent(event);
		udfEventControl.setEvent(event);
	}

	public void eventUpdated(Event event) {
		if (this.event.getId() == event.getId()) {
			showEvent(event);
		}
	}

}
