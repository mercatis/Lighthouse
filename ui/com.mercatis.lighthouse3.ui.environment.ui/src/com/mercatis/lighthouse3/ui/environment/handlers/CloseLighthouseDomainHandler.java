
package com.mercatis.lighthouse3.ui.environment.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.navigator.CommonNavigator;

import com.mercatis.lighthouse3.security.Security;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.ContextAdapter;


public class CloseLighthouseDomainHandler extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection == null || !(selection instanceof IStructuredSelection))
			return null;
		
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		
		if(structuredSelection.getFirstElement() instanceof LighthouseDomain) {
			LighthouseDomain domain = ((LighthouseDomain) structuredSelection.getFirstElement());
			CommonBaseActivator.getPlugin().getLighthouseDomainBroadCaster().notifyDomainClosed(domain);
			String context = ((ContextAdapter) domain.getAdapter(ContextAdapter.class)).toContext(domain);
			Security.logout(context);
		}
		CommonNavigator navigator = (CommonNavigator)HandlerUtil.getActivePart(event);
		navigator.getCommonViewer().refresh();
		return null;
	}
}
