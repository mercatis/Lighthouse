package com.mercatis.lighthouse3.ui.event.providers;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;

import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.ui.event.providers.EventTableUIElementsConstants.ColumnType;

public class EventStore {
	protected class EventComparator implements Comparator<Event> {
		public ColumnType sortColumn = null;
		public int sortDir = SWT.DOWN;
		
		private int getCategory(final Event event){		
			if(event.getLevel().equals(Event.FATAL)){
        		return 0;
        	}
        	if(event.getLevel().equals(Event.ERROR)){
        		return 1;
        	}
        	if(event.getLevel().equals(Event.WARNING)){
        		return 2;
        	}
        	if(event.getLevel().equals(Event.INFO)){
        		return 3;
        	}
        	if(event.getLevel().equals(Event.DETAIL)){
        		return 4;
        	}
        	if(event.getLevel().equals(Event.DEBUG)){
        		return 5;
        	}
			return 6;
		}
		
		public int compare(Event e1, Event e2) {
			int result;
			if (e2==null || e1==null)
				return 0;
			try {
				switch(sortColumn) {
					case CONTEXT:
						result = LabelConverter.getLabel(e2.getContext()).compareTo(LabelConverter.getLabel(e1.getContext()));
						break;
					case TIMESTAMP:
						result = e2.getDateOfOccurrence().compareTo(e1.getDateOfOccurrence());
						break;
					case CODE:
						result = e2.getCode().compareTo(e1.getCode());
						break;					
					case TAGS:
						result = transformSetToString(e2.getTags()).compareTo(transformSetToString(e1.getTags()));
						break;
					case ORIGIN:
						result = e2.getMachineOfOrigin().compareTo(e1.getMachineOfOrigin());
						break;
					case MESSAGE:
						result = e2.getMessage().compareTo(e1.getMessage());
						break;
					case UDF:
						result = transformMapToString(e2.getUdfs()).compareTo(transformMapToString(e1.getUdfs()));
						break;
					case TRANSACTION:
						result = transformSetToString(e2.getTransactionIds()).compareTo(transformSetToString(e1.getTransactionIds()));
						break;
					default:
						result = getCategory(e1)-getCategory(e2);
				}
			} catch (NullPointerException e) {
				result = getCategory(e1)-getCategory(e2);
			}
			if (sortDir==SWT.DOWN)
				return result;
			return -1*result; 
		}
	};
	private EventComparator eventComparator = new EventComparator();
	
	private Comparator<Event> eventComparatorIncoming = new Comparator<Event>() {
		public int compare(Event e1, Event e2) {
			return e1.getDateOfOccurrence().compareTo(e2.getDateOfOccurrence());
		}
	};
	
	private int maxEvents = 100;

	private List<Event> incoming = new LinkedList<Event>();
	private List<Event> sorted = new LinkedList<Event>();
	
	private void shrink() {
		int size = incoming.size();
		if (size<=maxEvents)
			return;
		while (size>maxEvents) {
			Event e = incoming.remove(0);
			sorted.remove(e);
			--size;
		}
	}
	
	/**
	 * @param set
	 * @return
	 */
	public String transformMapToString(Map<String, Object> map) {
		StringBuffer buf = new StringBuffer();
		for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			buf.append(key);
			buf.append("=");
			buf.append(map.get(key));
			if (iterator.hasNext()) {
				buf.append("; ");
			}

		}

		return buf.toString();
	}

	/**
	 * @param set
	 * @return
	 */
	public String transformSetToString(Set<String> set) {
		StringBuffer buf = new StringBuffer();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			buf.append(string);
			if (iterator.hasNext()) {
				buf.append(",");
			}
		}
		return buf.toString();
	}

	
	public Object[] getSortedArray() {
		return sorted.toArray();
	}

	public void clear() {
		incoming.clear();
		sorted.clear();
	}

	public void addAll(Collection<Event> events) {
		incoming.addAll(events);
		Collections.sort(incoming, eventComparatorIncoming);
		sorted.addAll(0, events);
		Collections.sort(sorted, eventComparator);
		shrink();
	}
	
	public int size() {
		return incoming.size();
	}

	public int getMaxEvents() {
		return maxEvents;
	}

	public boolean setMaxEvents(int newmax) {
		boolean ret = newmax < maxEvents;
		maxEvents = newmax;
		shrink();
		return ret;
	}

	public Object getSorted(int index) {
		if (index<0 || index>=sorted.size())
			return null;
		return sorted.get(index);
	}

	public int indexOf(Object element) {
		return sorted.indexOf(element);
	}
	
	public void setSortColumn(ColumnType data, int dir) {
		eventComparator.sortColumn = data;
		eventComparator.sortDir = dir;
		Collections.sort(sorted, eventComparator);
	}
}
