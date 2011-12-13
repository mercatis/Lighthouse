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
package com.mercatis.lighthouse3.ui.event.views.controls;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.ide.IDE;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.editors.DeploymentEditor;
import com.mercatis.lighthouse3.ui.event.common.EventUpdateService;

public class GeneralEventControl {

	private Composite component;

	private Event event;

	private Text codeText;
	private Text dateText;
	private Text messageText;
	private Text levelText;
	private Text originText;
	private Hyperlink deploymentLink;
	private ListViewer transactionIdsListViewer;
	private ListViewer tagsListViewer;
	private Text tagText;

	final DateFormat dateFormat = new SimpleDateFormat();

	public GeneralEventControl(Composite parent, FormToolkit toolkit) {

		component = new Composite(parent, SWT.FILL);

		toolkit.adapt(component);
		GridLayout layout = new GridLayout(1, false);
		component.setLayout(layout);

		GridData defaultSectionData = new GridData();
		defaultSectionData.grabExcessHorizontalSpace = true;
		defaultSectionData.horizontalAlignment = SWT.FILL;

		GridData generalSectionData = new GridData(SWT.FILL, SWT.FILL, true, true);
		generalSectionData.minimumHeight = 300;

		// the section for the general event information
		final Section generalSection = toolkit.createSection(component, Section.TITLE_BAR | Section.EXPANDED);
		generalSection.setLayoutData(generalSectionData);
		generalSection.setText("Event Details");

		final Composite generalComposite = toolkit.createComposite(generalSection, SWT.FILL);
		generalComposite.setLayout(new GridLayout(2, false));
		generalComposite.setLayoutData(generalSectionData);

		GridData horizontalGrowData = new GridData(SWT.FILL, SWT.FILL, true, true);
		horizontalGrowData.horizontalAlignment = SWT.FILL;
		horizontalGrowData.grabExcessHorizontalSpace = true;
		horizontalGrowData.verticalAlignment = SWT.CENTER;
		horizontalGrowData.grabExcessVerticalSpace = false;

		toolkit.createLabel(generalComposite, "Deployment:");
		deploymentLink = toolkit.createHyperlink(generalComposite, "", SWT.FILL);
		deploymentLink.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		deploymentLink.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(final HyperlinkEvent e) {
				Deployment deployment = (Deployment) event.getContext();
				LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(deployment);

				GenericEditorInput<Deployment> input = new GenericEditorInput<Deployment>(lighthouseDomain, deployment);
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, input, DeploymentEditor.class.getName());
				} catch (PartInitException ex) {
					CommonUIActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, ex.getMessage(), ex));
				}
			}
		});

		toolkit.createLabel(generalComposite, "Code:");
		codeText = toolkit.createText(generalComposite, "");
		codeText.setLayoutData(horizontalGrowData);
		codeText.setEditable(false);

		toolkit.createLabel(generalComposite, "Level:");
		levelText = toolkit.createText(generalComposite, "");
		levelText.setLayoutData(horizontalGrowData);
		levelText.setEditable(false);

		toolkit.createLabel(generalComposite, "Date:");
		dateText = toolkit.createText(generalComposite, "");
		dateText.setLayoutData(horizontalGrowData);
		dateText.setEditable(false);
		
		toolkit.createLabel(generalComposite, "Origin:");
		originText = toolkit.createText(generalComposite, "");
		originText.setLayoutData(horizontalGrowData);
		originText.setEditable(false);

		toolkit.createLabel(generalComposite, "Message:");
		messageText = toolkit.createText(generalComposite, "", SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData messageTextData = new GridData(SWT.FILL, SWT.FILL, true, true);
		messageTextData.horizontalSpan = 2;
		messageTextData.heightHint = 100;
		messageText.setLayoutData(messageTextData);
		messageText.setEditable(false);

		generalSection.setClient(generalComposite);

		// the section for the transaction ids final Section
		Section transactionIdsSection = toolkit.createSection(component, Section.TITLE_BAR | Section.EXPANDED);
		transactionIdsSection.setText("Transaction Ids");
		transactionIdsSection.setLayoutData(defaultSectionData);

		GridData listViewerData = new GridData(SWT.FILL, SWT.FILL, true, true);
		listViewerData.heightHint = 100;

		final Composite transactionIdsComposite = toolkit.createComposite(transactionIdsSection, SWT.FILL);
		transactionIdsComposite.setLayout(new GridLayout(1, false));
		transactionIdsListViewer = new ListViewer(transactionIdsComposite, SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL);
		transactionIdsListViewer.setContentProvider(new ArrayContentProvider());
		transactionIdsListViewer.getControl().setLayoutData(listViewerData);
		transactionIdsSection.setClient(transactionIdsComposite);

		// the section for the tags final Section tagsSection =
		Section tagsSection = toolkit.createSection(component, Section.TITLE_BAR | Section.EXPANDED);
		tagsSection.setText("Tags");
		tagsSection.setLayoutData(defaultSectionData);

		final Composite tagsComposite = toolkit.createComposite(tagsSection, SWT.FILL);
		tagsComposite.setLayout(new GridLayout(2, false));

		tagsListViewer = new ListViewer(tagsComposite, SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL);
		tagsListViewer.setContentProvider(new ArrayContentProvider());
		tagsListViewer.getControl().setLayoutData(listViewerData);

		final Button removeTagButton = toolkit.createButton(tagsComposite, "-", SWT.PUSH);

		removeTagButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@SuppressWarnings("unchecked")
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tagsListViewer.getSelection();
				Iterator<String> selectionIterator = selection.iterator();
				while (selectionIterator.hasNext()) {
					String tag = selectionIterator.next();
					if (tag != null && tag.length() > 0) {
						event.removeTag(tag);
					}
				}
				CommonBaseActivator.getPlugin().getEventService().updateEvent(event);
				EventUpdateService.getInstance().fireEventUpdate(event);
			}
		});

		removeTagButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		tagText = toolkit.createText(tagsComposite, "", SWT.BORDER);
		tagText.setLayoutData(horizontalGrowData);
		tagText.setEditable(false);
		final Button addTagButton = toolkit.createButton(tagsComposite, "+", SWT.PUSH);
		addTagButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				String tag = tagText.getText();
				if (tag != null && tag.length() > 0) {
					event.tag(tagText.getText());
					tagText.setText("");
					CommonBaseActivator.getPlugin().getEventService().updateEvent(event);
					EventUpdateService.getInstance().fireEventUpdate(event);
				}
			}
		});
		tagsSection.setClient(tagsComposite);

	}

	public Composite getComponent() {
		return component;
	}

	public void setEvent(Event event) {
		this.event = event;

		codeText.setText(event.getCode() != null ? event.getCode() : "");
		dateText.setText(event.getDateOfOccurrence() != null ? dateFormat.format(event.getDateOfOccurrence()) : "");
		messageText.setText(event.getMessage() != null ? event.getMessage() : "");
		originText.setText(event.getMachineOfOrigin() != null ? event.getMachineOfOrigin() : "");
		levelText.setText(event.getLevel() != null ? event.getLevel() : "");
		deploymentLink.setText((event.getContext() != null) ? LabelConverter.getLabel(event.getContext()) : "");
		getComponent().layout();

		transactionIdsListViewer.setInput(event.getTransactionIds().toArray());
		tagsListViewer.setInput(event.getTags().toArray());

		tagText.setEditable(event != null);

	}
}
