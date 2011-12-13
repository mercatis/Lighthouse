package com.mercatis.lighthouse3.ui.event.actions;

import java.util.ArrayList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import com.mercatis.lighthouse3.ui.event.providers.EventTable;
import com.mercatis.lighthouse3.ui.event.util.ClipboardHelper;

public class CopyEventAsCsvAction extends Action implements ISelectionListener, IWorkbenchAction {
	public static final String ID = "lighthouse.events.actions.eventToCsv";

	private IWorkbenchWindow window;
	private IStructuredSelection selection;
	private EventTable table;
	private static final String sep = System.getProperty("line.separator");

	public CopyEventAsCsvAction(IWorkbenchWindow window, EventTable table) {
		setText("Copy Columns to clipboard as CSV");
		setId(ID);
		this.window = window;
		this.window.getSelectionService().addSelectionListener(this);
		this.table = table;
	}

	public void runWithEvent(Event event) {
		StringBuilder sb = new StringBuilder();
		TableColumn tcs[] = table.getEventTable().getColumns();
		if (tcs.length == 0)
			return;
		ArrayList<Integer> indices = new ArrayList<Integer>(tcs.length);
		for (int i=0; i<tcs.length; ++i) {
			if (tcs[i].getResizable()) {
				indices.add(i);
				sb.append(tcs[i].getText()).append(", ");
			}
		}
		sb.setLength(sb.length()-2);
		sb.append(sep);
		for (TableItem ti : table.getEventTable().getSelection()) {
			for (Integer i : indices) {
				sb.append(ti.getText(i)).append(", ");
			}
			sb.setLength(sb.length()-2);
			sb.append(sep);
		}
		ClipboardHelper.copyToClipboard(window.getShell().getDisplay(), sb.toString());
	}

	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		if (incoming instanceof IStructuredSelection) {
			selection = (IStructuredSelection) incoming;
			setEnabled(selection.size() > 0 && selection.getFirstElement() instanceof com.mercatis.lighthouse3.domainmodel.events.Event);
		} else {
			setEnabled(false);
		}
	}

	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

}
