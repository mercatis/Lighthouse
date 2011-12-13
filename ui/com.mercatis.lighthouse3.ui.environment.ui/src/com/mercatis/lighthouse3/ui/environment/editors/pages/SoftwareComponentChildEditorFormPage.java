/*
 * Copyright 2011 mercatis Technologies AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mercatis.lighthouse3.ui.environment.editors.pages;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.base.ui.widgets.chooser.SecuritySelectionListLabelProvider;
import com.mercatis.lighthouse3.base.ui.widgets.chooser.SecuritySelectionListModificationListener;
import com.mercatis.lighthouse3.base.ui.widgets.chooser.SecuritySelectionListWidget;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeEvent;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeListener;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;


public class SoftwareComponentChildEditorFormPage extends FormPage implements SecuritySelectionListModificationListener<SoftwareComponent>, DomainChangeListener {

	public static final String ID = SoftwareComponentChildEditorFormPage.class.getName();

	private SoftwareComponent softwareComponent;
	
	private SecuritySelectionListWidget<SoftwareComponent> chooser;
	
	private LighthouseDomain lighthouseDomain;
	
	private SecuritySelectionListLabelProvider<SoftwareComponent> scLabelProv = new SecuritySelectionListLabelProvider<SoftwareComponent>() {
		public String getLabel(SoftwareComponent sc) {
			return sc.getCode();
		}
	};
	
	@SuppressWarnings("unchecked")
	public SoftwareComponentChildEditorFormPage(FormEditor editor) {
		super(editor, ID, null);
		this.softwareComponent = ((GenericEditorInput<SoftwareComponent>) getEditor().getEditorInput()).getEntity();
		this.lighthouseDomain = ((GenericEditorInput<SoftwareComponent>) getEditor().getEditorInput()).getDomain();
		CommonBaseActivator.getPlugin().getDomainService().addDomainChangeListener(this);
	}
	
	private void refresh() {
		// there is no need to refresh if we ain't got no view
		if (chooser == null)
			return;
		
		// fetch all "available" software components (components that do not have a parent component)
		Set<SoftwareComponent> available = new HashSet<SoftwareComponent>(lighthouseDomain.getSoftwareComponentContainer().getSoftwareComponents());
		
		// remove this.softwareComponent from the available components (if included)
		available.remove(this.softwareComponent);
		
		// remove all parent components of this.softwareComponent from the available components (if included)
		SoftwareComponent parent = this.softwareComponent.getParentEntity();
		while (parent != null) {
			available.remove(parent);
			parent = parent.getParentEntity();
		}
		
		// remove all software components that the current user is not allowed to view & drag
		for (Iterator<SoftwareComponent> it = available.iterator(); it.hasNext();) {
			SoftwareComponent component = it.next();
			if (!CodeGuard.hasRole(Role.SOFTWARE_COMPONENT_VIEW, component)) {
				it.remove();
			}
		}
		chooser.setItems(available, Role.SOFTWARE_COMPONENT_DRAG);
		
		// fetch all direct sub components from this.softwareComponent
		Set<SoftwareComponent> selected = this.softwareComponent.getDirectSubEntities();
		
		// remove all software components that the current user is not allowed to view & drag
		for (Iterator<SoftwareComponent> it = selected.iterator(); it.hasNext();) {
			SoftwareComponent component = it.next();
			if (!CodeGuard.hasRole(Role.SOFTWARE_COMPONENT_VIEW, component)) {
				it.remove();
			}
		}
		chooser.setSelected(selected);
	}
	
	@Override
	public void dispose() {
		CommonBaseActivator.getPlugin().getDomainService().removeDomainChangeListener(this);
		super.dispose();
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		form.setText("Children of: " + LabelConverter.getLabel(softwareComponent));
		toolkit.decorateFormHeading(form.getForm());
		form.getBody().setLayout(new FillLayout());

		createHeaderSection(form, toolkit);
		refresh();
	}

	private void createHeaderSection(final ScrolledForm form, FormToolkit toolkit) {
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		section.clientVerticalSpacing = 5;
		section.marginHeight = 8;
		section.marginWidth = 8;

		Composite client = toolkit.createComposite(section);
		client.setLayout(new FillLayout());
		toolkit.paintBordersFor(client);

		chooser = new SecuritySelectionListWidget<SoftwareComponent>(client, scLabelProv);
		chooser.addModifiycationListener(this);
		
		section.setClient(client);
	}

	@Override
	public boolean isDirty() {
		if (chooser==null)
			return false;
		return chooser.getModified(true).size() != 0 || chooser.getModified(false).size() != 0;
	}

	public void updateModel() {
		Set<SoftwareComponent> removedItems = chooser.getModified(false);
		for (SoftwareComponent component : removedItems) {
			softwareComponent.removeSubEntity(component);
		}
		
		Set<SoftwareComponent> addedItems = chooser.getModified(true);
		for (SoftwareComponent component : addedItems) {
			softwareComponent.addSubEntity(component);
		}
		
		getEditor().editorDirtyStateChanged();
	}

	public void domainChange(DomainChangeEvent event) {
		refresh();
	}

	public void onListItemModified(SoftwareComponent item, boolean enabled, boolean modified) {
		getEditor().editorDirtyStateChanged();
	}
}
