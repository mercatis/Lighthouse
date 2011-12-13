package com.mercatis.lighthouse3.status.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.status.ui.wizards.StatusWizard;
import com.mercatis.lighthouse3.ui.event.providers.EventTable;

public class OpenNewStatusWizardAction extends Action {
		
		private Shell shell;
		private StatusWizard wizard;
		
		public OpenNewStatusWizardAction(Shell shell, StatusCarrier carrier) {
			this.shell = shell;
			setText("New Status...");
			wizard = new StatusWizard(carrier);
		}

		public OpenNewStatusWizardAction(IWorkbenchWindow window, EventTable table) {
			setText("Add Attribute as Filter criteria");
			setId("lighthouse.operations.actions.newStatusWizardAction");
		}
		
		@Override
		public void run() {
			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.open();
		}
}
