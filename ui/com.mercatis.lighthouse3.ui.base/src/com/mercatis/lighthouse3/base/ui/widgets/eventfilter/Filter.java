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
package com.mercatis.lighthouse3.base.ui.widgets.eventfilter;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;



public class Filter extends Composite {
	
	private class FilterContainer {
		
		private SoftReference<Combo> propertyComboReference;
		
		private SoftReference<Control> inputControlReference;
		
		private SoftReference<Button> addButtonReference;
		
		private SoftReference<Button> removeButtonReference;
		
		public FilterContainer(Combo propertyCombo, Control inputControl, Button addButton, Button removeButton) {
			this.propertyComboReference = new SoftReference<Combo>(propertyCombo);
			this.inputControlReference = new SoftReference<Control>(inputControl);
			this.addButtonReference = new SoftReference<Button>(addButton);
			this.removeButtonReference = new SoftReference<Button>(removeButton);
		}
		
		public Button getAddButton() {
			return addButtonReference.get();
		}
		
		public Control getInputControl() {
			return inputControlReference.get();
		}
		
		public Combo getPropertyCombo() {
			return propertyComboReference.get();
		}
		
		public Button getRemoveButton() {
			return removeButtonReference.get();
		}
		
		public boolean isDisposed() {
			Combo propertyCombo = propertyComboReference.get();
			if (propertyCombo == null || propertyCombo.isDisposed())
				return true;
			
			Control inputControl = inputControlReference.get();
			if (inputControl == null || inputControl.isDisposed())
				return true;
			
			Button addButton = addButtonReference.get();
			if (addButton == null || addButton.isDisposed())
				return true;
			
			Button removeButton = removeButtonReference.get();
			if (removeButton == null || removeButton.isDisposed())
				return true;
			
			return false;
		}
		
		@SuppressWarnings("unused")
		public void setAddButton(Button addButton) {
			this.addButtonReference = new SoftReference<Button>(addButton);
		}
		
		public void setInputControl(Control inputControl) {
			this.inputControlReference = new SoftReference<Control>(inputControl);
		}
		
		@SuppressWarnings("unused")
		public void setPropertyCombo(Combo propertyCombo) {
			this.propertyComboReference = new SoftReference<Combo>(propertyCombo);
		}
		
		@SuppressWarnings("unused")
		public void setRemoveButton(Button removeButton) {
			this.removeButtonReference = new SoftReference<Button>(removeButton);
		}
		
	}
	
	private static final String KEY_SELECTION_INDEX = "KEY_SELECTION_INDEX";

	private InputControlFactory controlFactory;
	
	private FilterModel model;

	private final Composite composite = this;

	private List<FilterContainer> filterContainers = new LinkedList<FilterContainer>();
	
	private List<Listener> modifyListeners = new LinkedList<Listener>();
	
	private SelectionListener addButtonListener = new SelectionListener() {
		
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
		
		public void widgetSelected(SelectionEvent e) {
			composite.setLayoutDeferred(true);
			addFilterControl(-1, -1);
			composite.setLayoutDeferred(false);
			adjustCompositeSize();
			recalculateAvailableProperties();
			fireModifyListeners();
		}
	};
	
	private SelectionListener propertyComboSelectionListener = new SelectionListener() {
		
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		public void widgetSelected(SelectionEvent e) {
			Combo propertyCombo = (Combo) e.getSource();
			if (propertyCombo.getSelectionIndex() == (Integer) propertyCombo.getData(KEY_SELECTION_INDEX))
				return;
			
			propertyCombo.setData(KEY_SELECTION_INDEX, Integer.valueOf(propertyCombo.getSelectionIndex()));
			
			composite.setLayoutDeferred(true);
			recalculateAvailableProperties();
			switchInputControl((Combo) e.getSource());
			composite.setLayoutDeferred(false);
			adjustCompositeSize();
			fireModifyListeners();
		}
	};
	
	private SelectionListener removeButtonListener = new SelectionListener() {
		
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
		
		public void widgetSelected(SelectionEvent e) {
			Button removeButton = (Button) e.getSource();

			composite.setLayoutDeferred(true);
			removeFilterContainer(removeButton);
			composite.setLayoutDeferred(false);
			adjustCompositeSize();
			fireModifyListeners();
		}
	};

	/**
	 * @param model
	 * @param inputControlFactory
	 */
	public Filter(Composite parent, FilterModel model, InputControlFactory inputControlFactory, int style) {
		super(parent, style);
		this.model = model;
		this.controlFactory = inputControlFactory;
		
		this.setLayout(new GridLayout(4, false));
	}

	/**
	 * @param model
	 */
	public Filter(Composite parent, FilterModel model, int style) {
		this(parent, model, new EventInputControlFactory(), style);
	}
	
	public void addModifyListener(Listener modifyListener) {
		this.modifyListeners.add(modifyListener);
		reset();
	}

	public void removeModifyListener(Listener modifyListener) {
		this.modifyListeners.remove(modifyListener);
		reset();
	}
	
	private void fireModifyListeners() {
		if (modifyListeners != null) {
			for (Listener listener : modifyListeners) {
				Event event = new Event();
				event.widget = this;
				listener.handleEvent(event);
			}
		}
	}
	
	private <T> void addFilterControl(int selectedProperty, T selectedValue) {
		List<String> properties = calculateAvailableProperties();
		if (properties.isEmpty())
			return;
		
		// propertyCombo
		Combo propertyCombo = new Combo(this.composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		propertyCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		propertyCombo.setItems(properties.toArray(new String[properties.size()]));
		if (selectedProperty != -1) {
			propertyCombo.select(propertyCombo.indexOf(model.getPropertyName(selectedProperty)));
		}
		propertyCombo.setData(KEY_SELECTION_INDEX, Integer.valueOf(propertyCombo.getSelectionIndex()));
		propertyCombo.addSelectionListener(propertyComboSelectionListener);
		
		// inputControl
		Control inputControl = null;
		if (selectedProperty != -1) {
			inputControl = controlFactory.getControl(composite, modifyListeners, model.getInputControlTypeFor(selectedProperty), model.getChoicesFor(selectedProperty), selectedValue);
		} else {
			// place holder
			inputControl = new Label(composite, SWT.NONE);
		}
		inputControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		// removeButton
		Button removeButton = new Button(composite, SWT.NONE);
		removeButton.addSelectionListener(removeButtonListener);
		removeButton.setText("-");
		removeButton.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false, false));

		// addButton
		Button addButton = new Button(composite, SWT.NONE);
		addButton.addSelectionListener(addButtonListener);
		addButton.setText("+");
		addButton.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false, false));
		
		// wire controls to remove button (needed for actual removal)
		filterContainers.add(new FilterContainer(propertyCombo, inputControl, addButton, removeButton));
		
		// trigger layout operation
		composite.layout(new Control[] { propertyCombo, inputControl, addButton, removeButton });
	}
	
	/**
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void apply() {
		for (int i = 0; i < model.getPropertyCount(); i++) {
			String property = model.getPropertyName(i);
			List values = new LinkedList();
			
			for (Iterator<FilterContainer> it = filterContainers.iterator(); it.hasNext();) {
				FilterContainer filterContainer = it.next();
				
				if (filterContainer.isDisposed()) {
					it.remove();
					continue;
				}
				
				Combo propertyCombo = filterContainer.getPropertyCombo();
				int selectionIndex = propertyCombo.getSelectionIndex();
				if (selectionIndex != -1 && property.compareTo(propertyCombo.getItem(selectionIndex)) == 0) {
					values.add(controlFactory.getValue(model.getInputControlTypeFor(i), filterContainer.getInputControl()));
				}
			}
			
			model.setValuesFor(values, i);
		}
	}
	
	private List<String> calculateAvailableProperties() {
		List<String> properties = new ArrayList<String>(model.getPropertyCount());
		for (int i = 0; i < model.getPropertyCount(); i++) {
			String property = model.getPropertyName(i);
			
			if (model.canRecur(i)) {
				properties.add(property);
				continue;
			}
			
			boolean alreadySelected = false;
			for (Iterator<FilterContainer> it = filterContainers.iterator(); it.hasNext();) {
				FilterContainer filterContainer = (FilterContainer) it.next();
				
				if (filterContainer.isDisposed()) {
					it.remove();
					continue;
				}
				
				Combo propertyCombo = filterContainer.getPropertyCombo();
				// ignore combos without a selected item
				if (propertyCombo.getSelectionIndex() == -1) {
					continue;
				}
				
				if (propertyCombo.getItem(propertyCombo.getSelectionIndex()).compareTo(property) == 0) {
					alreadySelected = true;
				}
			}
			
			if (!alreadySelected) {
				properties.add(property);
			}
			
		}
		
		return properties;
	}
	
	private int findIndexOfProperty(String item) {
		for (int i = 0; i < model.getPropertyCount(); i++) {
			if (model.getPropertyName(i).compareTo(item) == 0) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @return
	 */
	public FilterModel getFilterModel() {
		return this.model;
	}

	/**
	 * @return
	 */
	public InputControlFactory getInputControlFactory() {
		return this.controlFactory;
	}
	
	private void recalculateAvailableProperties() {
		for (Iterator<FilterContainer> it = filterContainers.iterator(); it.hasNext();) {
			FilterContainer filterContainer = (FilterContainer) it.next();
			
			if (filterContainer.isDisposed()) {
				it.remove();
				continue;
			}
			Combo propertyCombo = filterContainer.getPropertyCombo();
			
			// save and clear the selection
			String currentlySelectedProperty = propertyCombo.getSelectionIndex() == -1 ? null : propertyCombo.getItem(propertyCombo.getSelectionIndex());
			propertyCombo.deselectAll();
			
			List<String> properties = calculateAvailableProperties();
			propertyCombo.setItems(properties.toArray(new String[properties.size()]));
			
			// restore the selection
			if (currentlySelectedProperty != null) {
				propertyCombo.select(propertyCombo.indexOf(currentlySelectedProperty));
			
				// trigger remove button visibility
				int propertyIndex = findIndexOfProperty(currentlySelectedProperty);
				if (model.isMandatory(propertyIndex)) {
					filterContainer.getRemoveButton().setVisible(false);
					propertyCombo.setEnabled(false);
				}
				else {
					filterContainer.getRemoveButton().setVisible(true);
					propertyCombo.setEnabled(true);
				}
			}
		}
		
		// if there is only one filter container left, we MUST hide its remove button ;)
		if (filterContainers.size() == 1)
			filterContainers.get(0).getRemoveButton().setVisible(false);
	}

	private void removeFilterContainer(Button removeButton) {
		// find according filter container
		FilterContainer filterContainer = null;
		for (Iterator<FilterContainer> it = filterContainers.iterator(); it.hasNext();) {
			FilterContainer tmp = (FilterContainer) it.next();
			
			if (tmp.isDisposed()) {
				it.remove();
				continue;
			}
			
			if (removeButton.equals(tmp.getRemoveButton())) {
				filterContainer = tmp;
			}
		}
		
		removeFilterContainer(filterContainer);
	}

	private void removeFilterContainer(FilterContainer filterContainer) {
		Combo propertyCombo = filterContainer.getPropertyCombo();
		Control inputControl = filterContainer.getInputControl();
		Button removeButton = filterContainer.getRemoveButton();
		Button addButton = filterContainer.getAddButton();
		
		composite.layout(new Control[] {propertyCombo, inputControl, removeButton, addButton });
		
		// cleanup object references and dispose
		propertyCombo.dispose();
		inputControl.dispose();
		removeButton.dispose();
		addButton.dispose();
		
		filterContainers.remove(filterContainer);
		recalculateAvailableProperties();
	}
	
	/**
	 * 
	 */
	public void reset() {
		composite.setLayoutDeferred(true);
		
		//remove all filter controls
		List<FilterContainer> tmp = new ArrayList<FilterContainer>(filterContainers);
		for (FilterContainer filterContainer : tmp) {
			removeFilterContainer(filterContainer);
		}
		
		for (int currentProperty = 0; currentProperty < model.getPropertyCount(); currentProperty++) {
			if (!model.getValuesFor(currentProperty).isEmpty()) {
				for (Object value : model.getValuesFor(currentProperty)) {
					addFilterControl(currentProperty, value);
				}
				continue;
			}
			
			if (model.isMandatory(currentProperty)) {
				addFilterControl(currentProperty, null);
			}
		}
		recalculateAvailableProperties();
		
		// trigger layout changes
		composite.setLayoutDeferred(false);
		adjustCompositeSize();
	}

	/**
	 * @param model
	 */
	public void setFilterModel(FilterModel model) {
		this.model = model;
	}
	
	/**
	 * @param inputControlFactory
	 */
	public void setInputControlFactory(InputControlFactory inputControlFactory) {
		this.controlFactory = inputControlFactory;
	}

	private void switchInputControl(Combo propertyCombo) {
		
		// find according filter container
		FilterContainer filterContainer = null;
		for (Iterator<FilterContainer> it = filterContainers.iterator(); it.hasNext();) {
			FilterContainer tmp = (FilterContainer) it.next();
			
			if (tmp.isDisposed()) {
				it.remove();
				continue;
			}
			
			if (propertyCombo.equals(tmp.getPropertyCombo())) {
				filterContainer = tmp;
			}
		}
		
		// replace current input control
		Control oldInputControl = filterContainer.getInputControl();
		int propertyIndex = findIndexOfProperty(propertyCombo.getItem(propertyCombo.getSelectionIndex()));
		InputControlType type = model.getInputControlTypeFor(propertyIndex);
		List<?> choices = model.getChoicesFor(propertyIndex);
		Control newInputControl;
		if (choices != null)
			newInputControl = controlFactory.getControl(composite, modifyListeners, type, choices);
		else
			newInputControl = controlFactory.getControl(composite, modifyListeners, type);
		newInputControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		newInputControl.moveAbove(oldInputControl);
		filterContainer.setInputControl(newInputControl);
		
		composite.layout(new Control[] { oldInputControl, newInputControl });
		oldInputControl.dispose();
	}

	private void adjustCompositeSize() {
		Point size = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		composite.setSize(size);
	}
}
