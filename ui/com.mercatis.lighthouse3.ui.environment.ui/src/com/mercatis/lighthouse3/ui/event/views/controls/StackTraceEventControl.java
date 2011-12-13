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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.mercatis.lighthouse3.domainmodel.events.Event;

public class StackTraceEventControl extends Composite {

	private Text stackTraceText;
	private Event event;

	public StackTraceEventControl(Composite parent, FormToolkit toolkit) {
		super(parent, SWT.FILL);
		toolkit.adapt(this);
		this.setLayout(new FillLayout(SWT.HORIZONTAL));

		final Section stackTraceSection = toolkit.createSection(this, Section.TITLE_BAR | Section.EXPANDED);
		stackTraceSection.setText("Stacktrace");

		final Composite stackTraceSectionComposite = toolkit.createComposite(stackTraceSection, SWT.FILL);
		stackTraceSectionComposite.setLayout(new FillLayout());

		stackTraceText = toolkit.createText(stackTraceSectionComposite, "", SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		stackTraceText.setEditable(false);
		stackTraceSection.setClient(stackTraceSectionComposite);
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		if (event == null) {
			throw new IllegalArgumentException("Null event");
		}

		this.event = event;

		stackTraceText.setText((event.getStackTrace() != null) ? event.getStackTrace() : "");
	}
}
