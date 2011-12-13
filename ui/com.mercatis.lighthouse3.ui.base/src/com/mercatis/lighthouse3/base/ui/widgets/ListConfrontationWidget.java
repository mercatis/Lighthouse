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
package com.mercatis.lighthouse3.base.ui.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import com.mercatis.lighthouse3.base.UIBase;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;

/**
 * 
 * 
 * @param <T>
 * @deprecated @see {@link Chooser}
 */
public class ListConfrontationWidget<T> {
	private List availableList;
	private List selectedList;
	private ILabelProvider labelProvider;
	private Map<String, T> map = new HashMap<String, T>();
	private java.util.List<ModifyListener> selectionListeners = new LinkedList<ModifyListener>();
	private Composite parent;
	private Button buOpenEditor;
	private LighthouseDomain lighthouseDomain;
	private String editorID;

	public ListConfrontationWidget(Composite parent, ILabelProvider labelProvider) {
		this.labelProvider = labelProvider;
		this.parent = parent;
		createConfrontation();
	}

	public void setAttachedEditor(LighthouseDomain domain, String editorID) {
		this.lighthouseDomain = domain;
		this.editorID = editorID;
		updateEditButton();
	}

	public void addSelectionChangedListener(ModifyListener listener) {
		this.selectionListeners.add(listener);
	}

	public void removeSelectionChangedListener(ModifyListener listener) {
		this.selectionListeners.remove(listener);
	}

	public void setElements(java.util.List<T> availableElements, java.util.List<T> selectedElements) throws Exception {
		map.clear();
		setElements(availableElements, availableList);
		setElements(selectedElements, selectedList);
		fireListener();
	}

	public void setLabelProvider(ILabelProvider provider) {
		labelProvider = provider;
	}

	private void setElements(java.util.List<T> elements, List list) throws Exception {
		if (labelProvider == null)
			throw new Exception("ILabelProvider is null");
		list.removeAll();

		ArrayList<String> keys = new ArrayList<String>(elements.size());
		for (T element : elements) {
			String displayName = labelProvider.getText(element);
			if (displayName == null)
				throw new Exception("ILabelProvider " + labelProvider.getClass().getSimpleName()
						+ " returned null for " + element.getClass().getCanonicalName() + "@" + element.hashCode());
			if (displayName.length() == 0)
				throw new Exception("ILabelProvider " + labelProvider.getClass().getSimpleName()
						+ " returned empty String for " + element.getClass().getCanonicalName() + "@"
						+ element.hashCode());
			if (map.containsKey(displayName))
				throw new Exception("Duplicate key: \'" + displayName + "\' at "
						+ element.getClass().getCanonicalName() + "@" + element.hashCode());
			map.put(displayName, element);
			keys.add(displayName);
		}
		
		//sort the elements by name
		Collections.sort(keys);
		list.setItems(keys.toArray(new String[keys.size()]));
	}

	public java.util.List<T> getSelectedElements() {
		java.util.List<T> ret = new ArrayList<T>();
		for (String key : selectedList.getItems()) {
			ret.add(map.get(key));
		}
		return ret;
	}

	public java.util.List<T> getAvailableElements() {
		java.util.List<T> ret = new ArrayList<T>();
		for (String key : availableList.getItems()) {
			ret.add(map.get(key));
		}
		return ret;
	}

	private void createConfrontation() {
		// LAYOUTS
		parent.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		parent.setLayout(new GridLayout(3, false));

		Composite left = new Composite(parent, SWT.NONE);
		left.setLayout(new GridLayout(1, false));
		left.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		GridData layoutLeftTop = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		GridData layoutLeftMiddle = new GridData(GridData.FILL, GridData.FILL, true, true);

		Composite middle = new Composite(parent, SWT.NONE);
		GridLayout layoutMiddle = new GridLayout(1, false);
		layoutMiddle.marginBottom = 1;
		middle.setLayout(layoutMiddle);
		middle.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		Composite right = new Composite(parent, SWT.NONE);
		right.setLayout(new GridLayout(1, false));
		right.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		GridData layoutRightTop = new GridData(GridData.HORIZONTAL_ALIGN_END);
		GridData layoutRightBottom = new GridData(GridData.FILL, GridData.FILL, true, true);

		Composite buttons = new Composite(parent, SWT.NONE);
		buttons.setLayout(new GridLayout(3, true));
		GridData buttonsGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		buttonsGridData.horizontalSpan = 2;
		buttons.setLayoutData(buttonsGridData);

		// LEFT
		Label lbAvail = new Label(left, SWT.NONE);
		lbAvail.setText("Available components");
		lbAvail.setLayoutData(layoutLeftTop);

		availableList = new List(left, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		availableList.setLayoutData(layoutLeftMiddle);

		// RIGHT
		Label lbSelected = new Label(right, SWT.NONE);
		lbSelected.setText("Selected components");
		lbSelected.setLayoutData(layoutRightTop);

		selectedList = new List(right, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		selectedList.setLayoutData(layoutRightBottom);

		// MIDDLE
		Label invisible = new Label(middle, SWT.NONE);
		invisible.setText(" ");

		Label fillTop = new Label(middle, SWT.NONE);
		fillTop.setText(" ");
		fillTop.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		Button buAddItem = new Button(middle, SWT.ARROW | SWT.RIGHT);
		buAddItem.setToolTipText("Add");

		Button buRemoveItem = new Button(middle, SWT.ARROW | SWT.LEFT);
		buRemoveItem.setToolTipText("Remove");

		Label fillBottom = new Label(middle, SWT.NONE);
		fillBottom.setText(" ");
		fillBottom.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		// BUTTONS
		Button buAddAllAvail = new Button(buttons, SWT.PUSH);
		buAddAllAvail.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		buAddAllAvail.setText("Add all");

		Label fillButtons = new Label(buttons, SWT.NONE);
		fillButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fillButtons.setText(" ");

		Composite rightButtons = new Composite(parent, SWT.NONE);
		rightButtons.setLayout(new GridLayout(3, false));
		rightButtons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		buOpenEditor = new Button(rightButtons, SWT.PUSH);
		buOpenEditor.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		buOpenEditor.setText("Edit...");
		buOpenEditor.setEnabled(false);
		buOpenEditor.setVisible(false);

		Label fillButtons2 = new Label(rightButtons, SWT.NONE);
		fillButtons2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fillButtons2.setText(" ");

		Button buRemoveAllSelected = new Button(rightButtons, SWT.PUSH);
		buRemoveAllSelected.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		buRemoveAllSelected.setText("Remove all");

		// Listener section
		availableList.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event evt) {
				moveSelectedListItems(availableList, selectedList);
			}
		});

		selectedList.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event evt) {
				moveSelectedListItems(selectedList, availableList);
			}
		});

		selectedList.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {
				updateEditButton();
			}
		});

		buOpenEditor.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {
				openEditors();
			}
		});

		buAddItem.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event evt) {
				moveSelectedListItems(availableList, selectedList);
			}
		});

		buRemoveItem.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event evt) {
				moveSelectedListItems(selectedList, availableList);
			}
		});

		buAddAllAvail.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {
				availableList.selectAll();
				moveSelectedListItems(availableList, selectedList);
			}
		});

		buRemoveAllSelected.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {
				selectedList.selectAll();
				moveSelectedListItems(selectedList, availableList);
			}
		});
	}

	private void moveSelectedListItems(List source, List dest) {
		dest.deselectAll();
		for (String item : source.getSelection()) {
			dest.add(item);
			source.remove(item);
			dest.select(dest.indexOf(item));
		}
		fireListener();
		updateEditButton();
	}

	private void fireListener() {
		Event evt = new Event();
		evt.display = parent.getDisplay();
		evt.widget = parent;
		ModifyEvent me = new ModifyEvent(evt);
		for (ModifyListener listener : selectionListeners) {
			listener.modifyText(me);
		}
	}

	private void updateEditButton() {
		buOpenEditor.setEnabled(this.selectedList.getSelectionIndices().length > 0);
		buOpenEditor.setVisible(lighthouseDomain != null && editorID != null);
	}

	private void openEditors() {
		for (int i : selectedList.getSelectionIndices()) {
			GenericEditorInput<T> input = new GenericEditorInput<T>(lighthouseDomain, map.get(selectedList.getItem(i)));

			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				IDE.openEditor(page, input, editorID);
			} catch (PartInitException ex) {
				UIBase.getDefault().getLog().log(new Status(IStatus.ERROR, UIBase.PLUGIN_ID, ex.getMessage(), ex));
			}
		}
	}
}
