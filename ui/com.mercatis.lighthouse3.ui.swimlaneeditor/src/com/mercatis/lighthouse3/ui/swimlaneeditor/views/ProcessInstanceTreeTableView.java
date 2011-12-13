package com.mercatis.lighthouse3.ui.swimlaneeditor.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.part.ViewPart;

import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstance;

public class ProcessInstanceTreeTableView extends ViewPart {
	
	public static final String ID = "lighthouse3.view.processinstancetreetableview";
	
	protected class Tuple {
		
		String[] elements;
		
		Object payload;
		
		public Tuple(String e1, String e2) {
			this.elements = new String[] {e1, e2};
		}
		
		public Tuple(String e1, String e2, Object payload) {
			this(e1, e2);
			this.payload = payload;
		}
		
		public String get(int index) {
			try {
				return elements[index];
			} catch (IndexOutOfBoundsException ex) {
				return null;
			}
		}
		
	}

	protected class ProcessInstanceContentProvider implements ITreeContentProvider {

		protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		public Object[] getElements(Object inputElement) {
			return new Tuple[] { new Tuple("Process Instance", null, inputElement) };
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
		protected String formatDate(Date date) {
			if (date == null)
				return "-";
			return sdf.format(date);
		}

		public Object[] getChildren(Object parentElement) {
			
			
			if (parentElement instanceof Tuple) {
				Tuple tuple = (Tuple) parentElement;
				
				if (tuple.get(0).compareTo("Process Instance") == 0) {
					ProcessInstance pi = (ProcessInstance) tuple.payload;
					ArrayList<Tuple> tuples = new ArrayList<Tuple>();
					tuples.add(new Tuple("Start Date", formatDate(pi.getStartDate())));
					tuples.add(new Tuple("End Date", formatDate(pi.getEndDate())));
					tuples.add(new Tuple("Erroneous", Boolean.toString(pi.isErroneous())));
					tuples.add(new Tuple("Closed", Boolean.toString(pi.isClosed())));
					
					@SuppressWarnings("unchecked")
					final List<Event> tEvents = new LinkedList<Event>(pi.getEvents());
					//sort events
					Collections.sort(tEvents, new Comparator<Event>(){

						public int compare(Event o1, Event o2) {
							if(o1!=null && o2!=null){
								//chronological order - newest on top
								return o2.getDateOfOccurrence().compareTo(o1.getDateOfOccurrence());
							}
							return 0;
						}
						
					});
					for (Event event : tEvents) {
						tuples.add(new Tuple("Event", null, event));
					}
					
					return tuples.toArray();
				}
				
				if (tuple.get(0).compareTo("Event") == 0) {
					Event event = (Event) tuple.payload;
					ArrayList<Tuple> tuples = new ArrayList<Tuple>();
					tuples.add(new Tuple("Deployment", event.getContext().getDeployedComponent().getCode() + "@" + event.getContext().getLocation()));
					tuples.add(new Tuple("Code", event.getCode()));
					tuples.add(new Tuple("Level", event.getLevel()));
					tuples.add(new Tuple("Date", formatDate(event.getDateOfOccurrence())));
					tuples.add(new Tuple("Machine", event.getMachineOfOrigin()));
					tuples.add(new Tuple("Message", event.getMessage()));
					//sort transactional ids
					final List<String> tTransactionalIds = new LinkedList<String>(event.getTransactionIds());
					Collections.sort(tTransactionalIds);
					for (String txId : tTransactionalIds) {
						tuples.add(new Tuple("Transaction ID", txId));
					}
					//sort tags
					final List<String> tTags = new LinkedList<String>(event.getTags());
					Collections.sort(tTags);
					for (String tag : tTags) {
						tuples.add(new Tuple("Tag", tag));
					}
					//sort UDFs by the key
					final Map<String, Object> tUDFs = new TreeMap<String, Object>(event.getUdfs());
					for (Entry<String,Object> entry : tUDFs.entrySet()) {
						tuples.add(new Tuple("UDF: " + entry.getKey(), entry.getValue() == null ? "" : entry.getValue().toString()));
					}
					
					return tuples.toArray();
				}
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			// not supported
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof Tuple) {
				if (((Tuple) element).payload != null)
					return true;
			}
			return false;
		}
		
	}
	
	protected class ProcessInstanceLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

		private Image missingImage = null;
		
		public ProcessInstanceLabelProvider() {
			this.missingImage = ImageDescriptor.getMissingImageDescriptor().createImage();
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null; //missingImage;
		}

		public String getColumnText(Object element, int columnIndex) {
			return ((Tuple) element).get(columnIndex);
		}
		
		@Override
		public void dispose() {
			super.dispose();
			missingImage.dispose();
		}
	}
	
	private TreeViewer viewer = null;
	
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTree().setHeaderVisible(true);
		TreeColumn keyColumn = new TreeColumn(viewer.getTree(), SWT.LEFT);
		keyColumn.setAlignment(SWT.LEFT);
		keyColumn.setText("Key");
		keyColumn.setWidth(160);
		TreeColumn valueColumn = new TreeColumn(viewer.getTree(), SWT.LEFT);
		valueColumn.setAlignment(SWT.LEFT);
		valueColumn.setText("Value");
		valueColumn.setWidth(160);
		viewer.setContentProvider(new ProcessInstanceContentProvider());
		viewer.setLabelProvider(new ProcessInstanceLabelProvider());
		viewer.expandAll();
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void setProcessInstance(ProcessInstance processInstance) {
		this.viewer.setInput(processInstance);
		this.viewer.expandAll();
	}
}
