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
package com.mercatis.lighthouse3.ui.event.providers;

import java.text.SimpleDateFormat;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.progress.UIJob;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventBuilder;
import com.mercatis.lighthouse3.services.Services;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.event.base.services.LighthouseEventListener;
import com.mercatis.lighthouse3.ui.event.editors.EventEditor;
import com.mercatis.lighthouse3.ui.event.providers.EventTableUIElementsConstants.ColumnType;

public class EventTableContentLabelProvider extends LabelProvider implements ITableLabelProvider, IStructuredContentProvider, ILazyContentProvider, LighthouseEventListener, IColorProvider {

	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private Map<Color, List<Event>> rowBackGroundColorConfiguration;
	
	private static Color lightGrey; 
	
	private TableViewer viewer;
	private Table table;
	
	private Event template;
	
	private LighthouseDomain lighthouseDomain;
	
	private boolean revealLatestElement = true;
	
	private boolean connected = false;
	
	private EventStore events = new EventStore();

	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final ReadLock rlock = rwLock.readLock();
	private final WriteLock wlock = rwLock.writeLock();
	public final static boolean isLegacyWindows;
	
	static {
		boolean tempBool = true;
		if (System.getProperty("os.name").startsWith("Windows")) {
			 tempBool = Float.parseFloat(System.getProperty("os.version")) < 6.0f;
		}
		isLegacyWindows = tempBool;
	}
	
	/**
	 * @param lighthouseDomain
	 */
	public EventTableContentLabelProvider(LighthouseDomain lighthouseDomain, Map<Color, List<Event>> rowBackGroundColorConfiguration, Display d) {
		this.lighthouseDomain = lighthouseDomain;
		this.rowBackGroundColorConfiguration = rowBackGroundColorConfiguration;
		lightGrey = new Color(d, 240, 240, 240); 
	}
	
	/**
	 * Clears all events
	 * 
	 */
	public void clear() {
		wlock.lock();
		events.clear();
		table.setItemCount(0);
		table.clearAll();
		wlock.unlock();
	}

	/**
	 * 
	 */
	public void connect() {
		Assert.isNotNull(lighthouseDomain);
		if (this.template != null)
			new WaitForEventsJob(events.getMaxEvents()).schedule();
	}
	
	/**
	 * 
	 */
	public void disconnect() {
		Assert.isNotNull(lighthouseDomain);
		if (this.template != null)
			CommonBaseActivator.getPlugin().getEventService().removeEventListener(lighthouseDomain, this, template);
		connected = false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		if (template != null)
			disconnect();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		rlock.lock();
		try {
			return events.getSortedArray();
		} finally {
			rlock.unlock();
		}
	}

	/**
	 * @return the lighthouseDomain
	 */
	public LighthouseDomain getLighthouseDomain() {
		return lighthouseDomain;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		Assert.isLegal(viewer instanceof TableViewer);
		this.viewer = (TableViewer) viewer;
		table = ((TableViewer) viewer).getTable();
		
		Assert.isTrue(this.template == oldInput, "EventTableViewer#inputChanged: Object oldInput != this.template");
		disconnect();

		if (newInput instanceof Event) {
			wlock.lock();
			events.clear();
			wlock.unlock();
			this.template = (Event) newInput;
			connect();
		} else if (newInput instanceof Set) {
			wlock.lock();
			Set<Event> set = (Set<Event>) newInput;
			events.addAll(set);
			wlock.unlock();
			rlock.lock();
			this.table.setItemCount(events.size());
			this.table.clearAll();
			rlock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.LighthouseEventListener#onEvents(java.util.List)
	 */
	public void onEvents(final List<Event> newEvents) {
		// push refresh on the UI thread
		new UIJob(Display.getDefault(), "Event Refresh") {
			/* (non-Javadoc)
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				wlock.lock();
				int oldSelIdx = table.getSelectionIndex();
				Event e = (Event) events.getSorted(oldSelIdx);
				events.addAll(newEvents);
				// save current selection
				int selIdx = events.indexOf(e);
				
				int topIdx = table.getTopIndex();
				
				table.setItemCount(events.size());
				table.clearAll();

				if (!revealLatestElement) {
					int newIdx = topIdx+(selIdx-oldSelIdx);
					table.setTopIndex(newIdx);
				}
				
				// restore selection
				table.select(selIdx);
				wlock.unlock();
				return Status.OK_STATUS;
			}
		}.schedule();
	}
	
	/**
	 * @param lighthouseDomain the lighthouseDomain to set
	 */
	public void setLighthouseDomain(LighthouseDomain lighthouseDomain) {
		this.lighthouseDomain = lighthouseDomain;
	}

	/**
	 * @param maxNumberOfEvents the maxNumberOfEvents to set
	 */
	public void setMaxNumberOfEvents(int maxNumberOfEvents) {
		if (events.setMaxEvents(maxNumberOfEvents)) {
			rlock.lock();
			table.setItemCount(events.size());
			table.clearAll();
			rlock.unlock();
		}
	}

	/**
	 * @param revealLatestElement the revealLatestElement to set
	 */
	public void setRevealLatestElement(boolean revealLatestElement) {
		this.revealLatestElement = revealLatestElement;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILazyContentProvider#updateElement(int)
	 */
	public void updateElement(int index) {
		rlock.lock();
		viewer.replace(events.getSorted(index), index);
		rlock.unlock();
	}

	private class WaitForEventsJob extends Job {
		private int limit;

		public WaitForEventsJob(int limit) {
			super("Receiving Events");
			setUser(true);
			this.limit = limit;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Receiving Events", IProgressMonitor.UNKNOWN);
			
			monitor.subTask("Registering Event Listener");
			rlock.lock();
			int initialAmmountOfEvents = events.size();
			rlock.unlock();
			IStatus result = Status.CANCEL_STATUS;
			try {
				monitor.subTask("Connecting");
				CommonBaseActivator.getPlugin().getEventService().addEventListener(lighthouseDomain, EventTableContentLabelProvider.this, template, limit);
				connected = true;
				monitor.worked(1);
				
				monitor.subTask("Waiting for Events");
				int size = initialAmmountOfEvents;
				while(connected && size==initialAmmountOfEvents) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					rlock.lock();
					size = events.size();
					rlock.unlock();
				}
				result = Status.OK_STATUS;
				monitor.worked(1);
			} catch (RuntimeException e) {
				// catch  EventFilteringException
				canceling();
				result = new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, e.getMessage());
			} finally {
				monitor.done();
			}
			return result;
		}

		@Override
		protected void canceling() {
			Dictionary<String, Object> eventProperties = new Hashtable<String, Object>();
			eventProperties.put("tableViewer", viewer.toString());
			eventProperties.put("operation", "cancel");
			org.osgi.service.event.Event event = new org.osgi.service.event.Event(EventEditor.OSGI_EVENT_TOPIC, eventProperties);
			Services.postEvent(event);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		if (rowBackGroundColorConfiguration != null) {
			for (Color color : rowBackGroundColorConfiguration.keySet()) {
				for (Event template : rowBackGroundColorConfiguration.get(color)) {
					if (((Event) element).matches(template)) {
						return color;
					}
				}
			}

		}
		if (isLegacyWindows) {
			rlock.lock();
			int i = events.indexOf(element);
			rlock.unlock();
			if (i>=0 && i%2==1)
				return lightGrey;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		return null;
	}

	/**
	 * @return the rowBackGroundColorConfiguration
	 */
	public Map<Color, List<Event>> getRowBackGroundColorConfiguration() {
		return rowBackGroundColorConfiguration;
	}

	/**
	 * @param rowBackGroundColorConfiguration
	 *            the rowBackGroundColorConfiguration to set
	 */
	public void setRowBackGroundColorConfiguration(Map<Color, List<Event>> rowBackGroundColorConfiguration) {
		this.rowBackGroundColorConfiguration = rowBackGroundColorConfiguration;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang
	 * .Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang
	 * .Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof Event) {
			switch (columnIndex) {
			case 0:
				return dateFormat.format(((Event) element).getDateOfOccurrence());
			case 1:
				Deployment d = ((Event) element).getContext();
				return LabelConverter.getLabel(d);
			case 2:
				return ((Event) element).getCode();
			case 3:
				return ((Event) element).getLevel();
			case 4:
				return ((Event) element).getMachineOfOrigin();
			case 5:
				return ((Event) element).getMessage();
			case 6:
				return events.transformSetToString(((Event) element).getTransactionIds());
			case 7:
				return events.transformMapToString(((Event) element).getUdfs());
			case 8:
				return events.transformSetToString(((Event) element).getTags());
			}
		}
		return "";
	}

	/**
	 * Generates a default color highlighting
	 * 
	 * @return
	 */
	public static Map<Color, List<Event>> generateDefaultColorConfiguration() {
		Map<Color, List<Event>> defaultConfiguration = new HashMap<Color, List<Event>>();

		List<Event> red = new LinkedList<Event>();
		red.add(EventBuilder.template().setLevel(Event.FATAL).done());
		defaultConfiguration.put(Display.getCurrent().getSystemColor(SWT.COLOR_RED), red);

		List<Event> darkRed = new LinkedList<Event>();
		darkRed.add(EventBuilder.template().setLevel(Event.ERROR).done());
		defaultConfiguration.put(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED), darkRed);

		List<Event> orange = new LinkedList<Event>();
		orange.add(EventBuilder.template().setLevel(Event.WARNING).done());
		defaultConfiguration.put(new Color(Display.getCurrent(), 255, 127, 0), orange);

		return defaultConfiguration;
	}

	/**
	 * Generates a default color highlighting in the 80s style
	 * 
	 * @return
	 */
	public static Map<Color, List<Event>> generateDefaultColorConfigurationFromTheEighties() {
		Map<Color, List<Event>> defaultConfiguration = new HashMap<Color, List<Event>>();

		List<Event> red = new LinkedList<Event>();
		red.add(EventBuilder.template().setLevel(Event.FATAL).done());
		defaultConfiguration.put(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_MAGENTA), red);

		List<Event> darkRed = new LinkedList<Event>();
		darkRed.add(EventBuilder.template().setLevel(Event.ERROR).done());
		defaultConfiguration.put(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN), darkRed);

		List<Event> orange = new LinkedList<Event>();
		orange.add(EventBuilder.template().setLevel(Event.WARNING).done());
		defaultConfiguration.put(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW), orange);

		return defaultConfiguration;
	}

	public void setSortColumn(ColumnType data, int dir) {
		wlock.lock();
		events.setSortColumn(data, dir);
		table.clearAll();
		wlock.unlock();
	}
}
