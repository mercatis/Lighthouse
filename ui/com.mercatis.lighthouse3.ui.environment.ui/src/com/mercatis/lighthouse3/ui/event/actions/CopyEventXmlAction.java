package com.mercatis.lighthouse3.ui.event.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import com.mercatis.lighthouse3.ui.event.util.ClipboardHelper;

public class CopyEventXmlAction extends Action implements ISelectionListener, IWorkbenchAction {
	public static final String ID = "lighthouse.events.actions.eventToXml";

	private IWorkbenchWindow window;
	private IStructuredSelection selection;
	
	public CopyEventXmlAction(IWorkbenchWindow window) {
		setText("Copy Event to clipboard as XML");
		setId(ID);
		this.window = window;
		this.window.getSelectionService().addSelectionListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(Event event) {
		com.mercatis.lighthouse3.domainmodel.events.Event ev =  (com.mercatis.lighthouse3.domainmodel.events.Event) selection.getFirstElement();
		ClipboardHelper.copyToClipboard(window.getShell().getDisplay(), ev.toXml());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		if (incoming instanceof IStructuredSelection) {
		    selection = (IStructuredSelection) incoming;
		    setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof com.mercatis.lighthouse3.domainmodel.events.Event);
		  } else {
		    setEnabled(false);
		  }
	}
	
	/**
	 * 
	 */
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

}
