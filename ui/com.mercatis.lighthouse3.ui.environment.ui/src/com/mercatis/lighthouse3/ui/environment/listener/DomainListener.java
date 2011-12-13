package com.mercatis.lighthouse3.ui.environment.listener;

import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;

public class DomainListener implements LighthouseDomainListener {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#closeDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void closeDomain(LighthouseDomain domain) {
		closeRelatedEditors(domain);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#openDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void openDomain(LighthouseDomain domain) {
	}
	
	private void closeRelatedEditors(LighthouseDomain domain) {
		System.out.println("Close Related Editors");
		IEditorReference[] editors = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		for (int i = 0; i < editors.length; i++) {
			try {
				if(editors[i].getEditorInput() instanceof GenericEditorInput<?>) {
					GenericEditorInput<?> editor = (GenericEditorInput<?>) editors[i].getEditorInput();
					EditorPart e = (EditorPart)editors[i].getEditor(false);
					if(editor.getDomain().equals(domain)) {
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(e,false);
					}
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
