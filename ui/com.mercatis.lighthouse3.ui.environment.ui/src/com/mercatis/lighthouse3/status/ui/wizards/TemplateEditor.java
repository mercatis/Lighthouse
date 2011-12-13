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
/**
 * 
 */
package com.mercatis.lighthouse3.status.ui.wizards;

import java.lang.reflect.Field;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.mercatis.lighthouse3.base.ui.widgets.eventfilter.Filter;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.status.ui.model.StatusEventFilterModel;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.event.providers.EventFilterModel;

/**
 * Simple Wizard to edit an event template.
 * 
 */
public class TemplateEditor extends Wizard {

	private Filter filterForm;
	private EventFilterModel filterModel;
	private Event event;

	public TemplateEditor(LighthouseDomain lighthouseDomain, Event event) {
		filterModel = new StatusEventFilterModel(lighthouseDomain);
		this.event = event;
	}
	
	@Override
	public boolean performFinish() {
		filterForm.apply();
		event = filterModel.getTemplate();
		return true;
	}

	@Override
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		createFilter(pageContainer);

		try {
			Field finishButton = getContainer().getClass().getDeclaredField("finishButton");
			finishButton.setAccessible(true);
			((Button) finishButton.get(getContainer())).setText("&Save");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean canFinish() {
		return true;
	}
	
	private void createFilter(final Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sc.setLayout(new FillLayout());
		filterForm = new Filter(sc, filterModel, SWT.NONE);
		sc.setContent(filterForm);

		filterForm.addControlListener(new ControlListener() {
			
			public void controlResized(ControlEvent e) {
				parent.layout(true, true);
			}
			
			public void controlMoved(ControlEvent e) {
			}
		});
		filterModel.setTemplate(event);
		filterForm.reset();
	}
}
