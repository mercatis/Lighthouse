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
package com.mercatis.lighthouse3.base.ui.editors;

import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public class EventEditorInput implements IEditorInput {

	private LighthouseDomain lighthouseDomain;
	
	private Event eventTemplate;
	
	private Set<Event> prefetchedEvents;
	
	private int maximumEventsForDisplay = 250;
	
	private String title;
	
	public EventEditorInput(LighthouseDomain lighthouseDomain, Event eventTemplate, String title) {
		this(lighthouseDomain, eventTemplate, null, title);
	}
	
	public EventEditorInput(LighthouseDomain lighthouseDomain, Event eventTemplate, Set<Event> prefetchedEvents, String title) {
		this.lighthouseDomain = lighthouseDomain;
		this.eventTemplate = eventTemplate;
		this.prefetchedEvents = prefetchedEvents;
		this.title = title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		return false;
	}
	
	/**
	 * @param maximumEventsForDisplay the maximumEventsForDisplay to set
	 */
	public void setMaximumEventsForDisplay(int maximumEventsForDisplay) {
		this.maximumEventsForDisplay = maximumEventsForDisplay;
	}
	
	/**
	 * @return the maximumEventsForDisplay
	 */
	public int getMaximumEventsForDisplay() {
		return maximumEventsForDisplay;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.getMissingImageDescriptor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		return title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return null;
	}

	public LighthouseDomain getLighthouseDomain() {
		return this.lighthouseDomain;
	}
	
	public Event getEventTemplate() {
		return eventTemplate;
	}
	
	public Set<Event> getPrefetchedEvents() {
		return prefetchedEvents;
	}

}
