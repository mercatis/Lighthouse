package com.mercatis.lighthouse3.base.ui.widgets.chooser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;

public class SecuritySelectionListWidget<T> implements SelectionListener {
	private static final String INITIALSTATE = SecuritySelectionListWidget.class.getCanonicalName()+".InitialStatus";
	private Table table;
	private HashMap<String, TableItem> reverseMap = new HashMap<String, TableItem>();
	private LinkedList<SecuritySelectionListModificationListener<T>> listeners = new LinkedList<SecuritySelectionListModificationListener<T>>();
	private SecuritySelectionListLabelProvider<T> labelProvider;
	private Color grey;
	
	public SecuritySelectionListWidget(Composite parent, SecuritySelectionListLabelProvider<T> labelProv) {
		labelProvider = labelProv;
		table = new Table(parent, SWT.BORDER | SWT.CHECK);
		table.addSelectionListener(this);
		grey = new Color(table.getDisplay(), 128, 128, 128);
	}
	
	public void setItems(Set<T> items, Role sucurityRole) {
		emptyTable();
		for (T t : items) {
			String label = labelProvider.getLabel(t); 
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(label);
			ti.setData(INITIALSTATE, Boolean.FALSE);
			if (sucurityRole!=null && CodeGuard.hasRole(sucurityRole, t))
				ti.setData(t);
			else
				ti.setForeground(grey);
			reverseMap.put(label, ti);
		}
	}
	
	private void emptyTable() {
		for (TableItem ti : table.getItems())
			ti.dispose();
		table.clearAll();
		reverseMap.clear();
	}
	
	public void setSelected(Set<T> items) {
		for (T t : items) {
			String label = labelProvider.getLabel(t);
			if (!reverseMap.containsKey(label))
				continue;
			TableItem ti = reverseMap.get(label);
			ti.setChecked(true);
			ti.setData(INITIALSTATE, Boolean.TRUE);
		}
	}
	
	public Set<T> getModified(boolean added) {
		Set<T> s = new HashSet<T>();
		for (TableItem ti : table.getItems()) {
			boolean current = ti.getChecked();
			Boolean initial = (Boolean) ti.getData(INITIALSTATE);
			@SuppressWarnings("unchecked")
			T item = (T) ti.getData();
			if (item!=null && current!=initial && current==added)
				s.add(item);
		}
		return s;
	}
	
	public void addModifiycationListener(SecuritySelectionListModificationListener<T> listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public void removeModificationListener(SecuritySelectionListModificationListener<T> listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	public void widgetSelected(SelectionEvent e) {
		TableItem ti = (TableItem) e.item;
		boolean current = ti.getChecked();
		Boolean initial = (Boolean) ti.getData(INITIALSTATE);
		@SuppressWarnings("unchecked")
		T item = (T) ti.getData();
		if (item!=null) {
			for (SecuritySelectionListModificationListener<T> listener : listeners)
				listener.onListItemModified(item, current, current!=initial);
		} else {
			ti.setChecked(initial);
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) { /* ignore */ }	
}
