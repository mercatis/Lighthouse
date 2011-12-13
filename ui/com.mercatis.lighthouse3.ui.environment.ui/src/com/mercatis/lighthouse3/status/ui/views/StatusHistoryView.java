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
package com.mercatis.lighthouse3.status.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusChange;
import com.mercatis.lighthouse3.services.Services;
import com.mercatis.lighthouse3.status.ui.views.controls.StatusHistoryTable;
import com.mercatis.lighthouse3.status.ui.views.controls.StatusHistoryTableContentProvider;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;

public class StatusHistoryView extends ViewPart implements EventHandler {

	public static final String ID = "lighthouse3.status.view.statushistory";

	private Status status;
	private StatusHistoryTable statusHistoryTable;

	public StatusHistoryView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout parentLayout = new GridLayout(1, false);
		parent.setLayout(parentLayout);
		statusHistoryTable = new StatusHistoryTable(parent, SWT.FULL_SELECTION | SWT.BORDER, getSite());
	}

	@Override
	public void dispose() {
		super.dispose();
		Services.unregisterEventHandler(this);
	}

	/**
	 * Refreshes the UI
	 */
	private void refreshUI() {
		((StatusHistoryTableContentProvider) statusHistoryTable.getContentProvider()).setStatus(status);
	}

	/**
	 * @return
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status
	 */
	public void setStatus(Status status) {
		if (this.status != null) {
			System.out.println("status already set for this view");
			return;
		}
		this.status = status;
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				refreshUI();
			}
		});
		setPartName("History for: " + LabelConverter.getLabel(status));
		String serverDomainKey = "" + status.getLighthouseDomain().hashCode();
		String filter = "(&(|(type=statusAggregationChanged)(type=statusCounterChanged))(code=" + status.getCode() + "))";
		Services.registerEventHandler(this, "com/mercatis/lighthouse3/event/" + serverDomainKey + "/*", filter);
	}

	/**
	 * 
	 */
	public void refreshStatus() {
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getStatusService().getLighthouseDomainForEntity(status);
		int pageSize = CommonBaseActivator.getPlugin().getStatusService().getStatusConfiguration(lighthouseDomain).getStatusPageSize();
		int pageNo = CommonBaseActivator.getPlugin().getStatusService().getStatusConfiguration(lighthouseDomain).getStatusPageNo();
		Status refreshedStatus = CommonBaseActivator.getPlugin().getStatusService().refresh(status, pageSize, pageNo);
		this.status = refreshedStatus;
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				refreshUI();
			}
		});
	}

	@Override
	public void setFocus() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event
	 * .Event)
	 */
	public void handleEvent(Event event) {
		String type = (String) event.getProperty("type");
		StatusChange change = (StatusChange) event.getProperty("statusChange");
		if (type.equals("statusAggregationChanged")) {
			status.addChangeToHistory(change);
		} else if (type.equals("statusCounterChanged")){
			StatusChange currentChange = status.getCurrent();
			if (change.getErrorCounter() > currentChange.getErrorCounter()) {
				currentChange.incrementErrorCounter();
			}
			if (change.getOkCounter() > currentChange.getOkCounter()) {
				currentChange.incrementOkCounter();
			}
			if (change.getStaleCounter() > currentChange.getStaleCounter()) {
				currentChange.incrementStaleCounter();
			}
		}
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				refreshUI();
			}
		});
	}
}
