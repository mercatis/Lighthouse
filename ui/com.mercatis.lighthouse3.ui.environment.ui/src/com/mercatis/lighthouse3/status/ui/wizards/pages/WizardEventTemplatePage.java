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
package com.mercatis.lighthouse3.status.ui.wizards.pages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import com.mercatis.lighthouse3.base.ui.widgets.eventfilter.Filter;
import com.mercatis.lighthouse3.commons.commons.Ranger;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventBuilder;
import com.mercatis.lighthouse3.status.ui.model.StatusEventFilterModel;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public class WizardEventTemplatePage extends WizardPage {

	public static enum EventType {
		OK, ERROR
	}
	
	private Event event;
	
	private EventType eventType;
	
	private StatusEventFilterModel filterModel;
	
	private Filter filter;
	
	private LighthouseDomain lighthouseDomain;

	public WizardEventTemplatePage(String pageName, LighthouseDomain domain, EventType eventType) {
		super(pageName);
		this.eventType = eventType;
		lighthouseDomain = domain;
	}
	
	public void setDeployments(Set<Deployment> deployments) {
		Set<String> levels;
		switch (eventType) {
		case ERROR:
			levels = new HashSet<String>();
			levels.add("ERROR");
			levels.add("FATAL");
			levels.add("WARNING");
			this.event = EventBuilder.template().setLevel(Ranger.enumeration(levels)).done();
			break;
		default:
			levels = new HashSet<String>();
			levels.add("INFO");
			levels.add("DEBUG");
			this.event = EventBuilder.template().setLevel(Ranger.enumeration(levels)).done();
		}
		if (deployments.size() == 1)
			event.setContext(deployments.iterator().next());
		else
			event.setContext(Ranger.enumeration(deployments));
		
		filterModel = new StatusEventFilterModel(lighthouseDomain, event);
		filterModel.setAllowedDeployments(new ArrayList<Deployment>(deployments));
		if (filter != null) {
			filter.setFilterModel(filterModel);
			filter.reset();
		}
	}

	public void createControl(Composite parent) {
		ScrolledComposite composite = new ScrolledComposite(parent, SWT.V_SCROLL);
		composite.setExpandHorizontal(true);
		composite.setExpandVertical(false);
		
		filter = new Filter(composite, filterModel, SWT.NONE);
		if (filterModel != null) {
			filter.setFilterModel(filterModel);
			filter.reset();
		}
		composite.setContent(filter);
		
		setControl(composite);
	}

	public boolean validatePage() {
		try {
			if (filter != null) {
				filter.apply();
			}
		} catch (Throwable t) {
			setMessage(t.getMessage());
			return false;
		}
		setErrorMessage(null);
		setMessage(null);
		return true;
	}
	
	public EventType getEventType() {
		return eventType;
	}

	public Event getTemplate() {
		return filterModel.getTemplate();
	}
}
