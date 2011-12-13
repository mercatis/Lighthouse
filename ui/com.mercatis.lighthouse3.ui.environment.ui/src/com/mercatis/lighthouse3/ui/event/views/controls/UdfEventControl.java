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
package com.mercatis.lighthouse3.ui.event.views.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.mercatis.lighthouse3.domainmodel.events.Event;

public class UdfEventControl extends Composite {

	UdfTable udfTable;

	public UdfEventControl(Composite parent, FormToolkit toolkit) {
		super(parent, SWT.FILL);
		toolkit.adapt(this);
		this.setLayout(new GridLayout(1, false));

		final Section udfSection = toolkit.createSection(this, Section.TITLE_BAR | Section.EXPANDED);
		udfSection.setText("User Defined Fields");
		udfSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Composite udfComposite = toolkit.createComposite(udfSection, SWT.FILL);
		udfComposite.setLayout(new GridLayout(1, true));

		udfTable = new UdfTable(udfComposite, SWT.FULL_SELECTION | SWT.BORDER);
		udfSection.setClient(udfComposite);
	}

	public void setEvent(Event event) {
		((UdfTableContentProvider) udfTable.getContentProvider()).setEvent(event);
	}

}
