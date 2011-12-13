package com.mercatis.lighthouse3.ui.environment.wizards.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;

public class WizardProcessTaskAddDeploymentsPage extends WizardPage implements Comparator<Deployment> {
	private Table available;
	private java.util.List<Deployment> deployments;
	private int selectionCount = 0;
	private static enum OP { ALL, NONE, INVERT };

	private SelectionListener buttonAction = new SelectionListener() {
		public void widgetSelected(SelectionEvent e) {
			OP op = (OP) e.widget.getData();
			switch (op) {
				case ALL:
					selectionHelper(true);
					selectionCount = deployments.size();
					break;
				case NONE:
					selectionHelper(false);
					selectionCount = 0;
					break;
				case INVERT:
					selectionHelper(null);
					selectionCount = deployments.size() - selectionCount;
					break;
			}
			setPageComplete(canFlipToNextPage());
		}
		public void widgetDefaultSelected(SelectionEvent e) { /* ignore */ }
	};
	
	private SelectionListener checkboxListener = new SelectionListener() {
		public void widgetSelected(SelectionEvent e) {
			TableItem ti = (TableItem) e.item;
			if (ti.getChecked())
				++selectionCount;
			else
				--selectionCount;
			setPageComplete(canFlipToNextPage());
		}
		public void widgetDefaultSelected(SelectionEvent e) { /* ignore */ }
	};	

	public WizardProcessTaskAddDeploymentsPage(String pageName, java.util.List<Deployment> deps) {
		super(pageName);
		setTitle("Add Deployments");
		setDescription("Add Deployments to the new ProcessTask.");
		deployments = new ArrayList<Deployment>(deps);
		Collections.sort(deployments, this);
	}
	
	private String deploymentToString(Deployment d) {
		return d.getDeployedComponent().getCode()+" @ "+d.getLocation();
	}
	
	private void selectionHelper(Boolean active) {
		for (TableItem ti : available.getItems()) {
			if (active==null)
				ti.setChecked(!ti.getChecked());
			else
				ti.setChecked(active);
		}
	}

	public void createControl(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(1, false));
		
		available = new Table(c, SWT.BORDER | SWT.CHECK);
		available.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		available.addSelectionListener(checkboxListener);
		
		Composite buttonComp = new Composite(c, SWT.NONE);
		buttonComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		buttonComp.setLayout(new RowLayout());
		
		for (Deployment d : deployments) {
			TableItem ti = new TableItem(available, SWT.NONE);
			ti.setData(d);
			ti.setText(deploymentToString(d));
		}
		
		addButton(buttonComp, "All", OP.ALL);
		addButton(buttonComp, "None", OP.NONE);
		addButton(buttonComp, "Invert", OP.INVERT);

		setControl(c);
	}
	
	private void addButton(Composite parent, String name, OP op) {
		Button all = new Button(parent, SWT.PUSH);
		all.setText(name);
		all.setData(op);
		all.addSelectionListener(buttonAction);
	}
	
	public Set<Deployment> getDeploymentSet() {
		Set<Deployment> selected = new HashSet<Deployment>(available.getItemCount());
		for (TableItem ti : available.getItems())
			if (ti.getChecked())
				selected.add((Deployment) ti.getData());
		return selected;
	}
	
	public java.util.List<Deployment> getDeploymentList() {
		java.util.List<Deployment> selected = new ArrayList<Deployment>(available.getItemCount());
		for (TableItem ti : available.getItems())
			if (ti.getChecked())
				selected.add((Deployment) ti.getData());
		return selected;
	}

	@Override
	public boolean canFlipToNextPage() {
		return selectionCount>0;
	}
	
	@Override
	public boolean isPageComplete() {
		return canFlipToNextPage();
	}

	public int compare(Deployment o1, Deployment o2) {
		return deploymentToString(o1).compareTo(deploymentToString(o2));
	}
}
