package com.mercatis.lighthouse3.status.ui.menu;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import com.mercatis.lighthouse3.base.ui.editors.EventEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.commons.commons.Ranger;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventBuilder;
import com.mercatis.lighthouse3.domainmodel.status.EventTriggeredStatusChange;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusChange;
import com.mercatis.lighthouse3.domainmodel.status.StatusRegistry;
import com.mercatis.lighthouse3.security.Security;
import com.mercatis.lighthouse3.services.StatusRegistryFactoryService;
import com.mercatis.lighthouse3.services.util.RegistryFactoryServiceUtil;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.ContextAdapter;
import com.mercatis.lighthouse3.ui.status.base.service.StatusService;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts.ProcessTaskModelEditPart;

public class StatusMenu extends ContributionItem {
	private static enum ITEMACTION { CLEAR, VIEWTRIGGER };
	private static final ImageDescriptor iconGreenDesc = ImageDescriptor.createFromURL(StatusMenu.class.getResource("/icons/green_x16.png"));
	private static final Image iconGreen = iconGreenDesc.createImage();
	private static final ImageDescriptor iconWhiteDesc = ImageDescriptor.createFromURL(StatusMenu.class.getResource("/icons/white_x16.png"));
	private static final Image iconWhite = iconWhiteDesc.createImage();
	private static final ImageDescriptor iconRedDesc = ImageDescriptor.createFromURL(StatusMenu.class.getResource("/icons/red_x16.png"));
	private static final Image iconRed = iconRedDesc.createImage();
	private static final ImageDescriptor iconOrangeDesc = ImageDescriptor.createFromURL(StatusMenu.class.getResource("/icons/orange_x16.png"));
	private static final Image iconOrange = iconOrangeDesc.createImage();
	
	
	private class StatusActionItem implements SelectionListener {
		private final Status status;
		public StatusActionItem(Menu parent, String text, ITEMACTION action, Status s) {
			status = s;

			boolean enable = false;
			StatusChange currentStatusChange = status.getCurrent();
			if (action==ITEMACTION.VIEWTRIGGER)
				enable = currentStatusChange instanceof EventTriggeredStatusChange;
			if (action==ITEMACTION.CLEAR)
				enable = status.isOk() || status.isError() || status.isStale();
			
			MenuItem mi = new MenuItem(parent, SWT.PUSH);
			mi.setText(text);
			mi.setData(action);
			mi.addSelectionListener(this);
			mi.setEnabled(enable);
		}

		public void widgetSelected(SelectionEvent e) {
			MenuItem mi = (MenuItem) e.widget;
			ITEMACTION action = (ITEMACTION) mi.getData();
			LighthouseDomain domain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomain(status.getLighthouseDomain());
			switch (action) {
				case CLEAR:
					InputDialog reason = new InputDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
							"Enter a reason", "Please provide a reason for clearing", null, null);
					if (reason.open() == Dialog.OK) {
						String context = ((ContextAdapter) domain.getAdapter(ContextAdapter.class)).toContext(domain);
						String loginName = Security.getLoginName(context);
						CommonBaseActivator.getPlugin().getStatusService().clearStatusManually(status, reason.getValue(), loginName);
					}
					break;
				case VIEWTRIGGER:
					StatusChange currentStatusChange = status.getCurrent();
					EventTriggeredStatusChange currentEventTriggeredStatusChange = (EventTriggeredStatusChange) currentStatusChange;
					Event triggeringEvent = currentEventTriggeredStatusChange.getTriggeringEvent();
					
					String title = LabelConverter.getLabel(status); 
					
					EventEditorInput input = null;
					
					Calendar from = new GregorianCalendar();
					from.setTime(triggeringEvent.getDateOfOccurrence());
					from.add(Calendar.MINUTE, -1);
					
					Calendar to = new GregorianCalendar();
					to.setTime(triggeringEvent.getDateOfOccurrence());
					to.add(Calendar.MINUTE, 1);
					
					//create template with time from: dateOfOccurence - 1Min to: dateOfOccurence + 1Min
					Event template = EventBuilder.template().setDateOfOccurrence(Ranger.interval(from.getTime(),to.getTime())).done();

					//set deployments
					template.setContext(Ranger.enumeration(status.getContext().getAssociatedDeployments()));
					
					input = new EventEditorInput(domain, template, title);
					input.setMaximumEventsForDisplay(200);
					
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IDE.openEditor(page, input, "lighthouse3.events.editor.event");
					} catch (PartInitException ex) {
						System.err.println(ex.getMessage());
					}
					break;
			}
		}

		public void widgetDefaultSelected(SelectionEvent e) { /* ignore */ }
	}
	
	@Override
	public void fill(Menu menu, int index) {
		IStructuredSelection selection = (IStructuredSelection) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (selection==null || selection.size()==0)
			return;
		Object o = selection.iterator().next();

		MenuItem submenuitem = new MenuItem(menu, SWT.CASCADE, index);
		submenuitem.setText("Status");

		Menu submenu = new Menu(menu);
		submenuitem.setMenu(submenu);
		

		List<Status> statusList = new ArrayList<Status>();
		StatusService service = CommonBaseActivator.getPlugin().getStatusService();
		if (o instanceof StatusCarrier) {
			LighthouseDomain domain = service.getLighthouseDomainForEntity(o);
			StatusRegistry reg = RegistryFactoryServiceUtil.getRegistryFactoryService(StatusRegistryFactoryService.class, domain.getProject(), this);
			statusList.addAll(reg.getStatusForCarrier((StatusCarrier) o));
		} else if (o instanceof ProcessTaskModelEditPart) {
			ProcessTaskModelEditPart part = (ProcessTaskModelEditPart) o;
			int pageNo = 0;
			List<Status> yasl; // yet another status list
			final int pageSize = 20;
			do {
				yasl = service.getPagedStatusesForCarrier(part.getProcessTask(), pageSize, pageNo++);
				statusList.addAll(yasl);
			} while (yasl.size()==pageSize);
		} else
			System.out.println(o.getClass().getCanonicalName());
		
		for (Status s : statusList) {
			MenuItem subsubmenuitem = new MenuItem(submenu, SWT.CASCADE);
			subsubmenuitem.setText(s.getCode());

			Menu subsubmenu = new Menu(submenu);
			subsubmenuitem.setMenu(subsubmenu);

			if (s.isOk())
				subsubmenuitem.setImage(iconGreen);
			if (s.isError())
				subsubmenuitem.setImage(iconRed);
			if (s.isStale())
				subsubmenuitem.setImage(iconOrange);
			if (s.isNone())
				subsubmenuitem.setImage(iconWhite);

			String desc = s.getDescription();
			if (desc!=null) {
				MenuItem mi = new MenuItem(subsubmenu, SWT.PUSH);
				mi.setText(desc);
				mi.setEnabled(false);
				new MenuItem(subsubmenu, SWT.SEPARATOR);
			}
			new StatusActionItem(subsubmenu, "Clear status", ITEMACTION.CLEAR, s);
			new StatusActionItem(subsubmenu, "View triggering event", ITEMACTION.VIEWTRIGGER, s);
		}
		submenuitem.setEnabled(!statusList.isEmpty());
		
	}
	
	@Override
	public boolean isDynamic() {
		return true;
	}
}
