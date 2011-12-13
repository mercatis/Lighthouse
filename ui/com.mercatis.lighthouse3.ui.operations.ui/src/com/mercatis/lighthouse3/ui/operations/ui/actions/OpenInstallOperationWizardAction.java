package com.mercatis.lighthouse3.ui.operations.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.event.providers.EventTable;
import com.mercatis.lighthouse3.ui.operations.ui.wizards.InstallOperationWizard;

	public class OpenInstallOperationWizardAction extends Action {
		
		private Shell shell;
		private InstallOperationWizard wizard = new InstallOperationWizard();
		
		public OpenInstallOperationWizardAction(Shell shell, LighthouseDomain lighthouseDomain, Deployment deployment) {
			this.shell = shell;
			setText("Install Operation...");
			wizard.setLighthouseDomain(lighthouseDomain);
			wizard.addDeployment(deployment);
		}

		public OpenInstallOperationWizardAction(IWorkbenchWindow window, EventTable table) {
			setText("Add Attribute as Filter criteria");
			setId("lighthouse.operations.actions.openInstallWizard");
		}
		
		@Override
		public void run() {
			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.open();
		}
}
