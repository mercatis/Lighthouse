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
package com.mercatis.lighthouse3.ui.operations.ui.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.operations.Parameter;
import com.mercatis.lighthouse3.domainmodel.operations.ParameterValue;

/**
 * The ParameterComposite will add <b>two</b> composites to the given parent:
 * <ol>
 * <li>Label or Checkbox with label</li>
 * <li>Input widget (contains add/remove buttons)</li>
 */
public class ParameterComposite {

	private List<ParameterCompositeStatusListener> statusListener = new ArrayList<ParameterCompositeStatusListener>();
	private Map<String, List<Composite>> disposeGroups = new HashMap<String, List<Composite>>();
	private Map<String, Button> removeButtons = new HashMap<String, Button>();
	private Map<String, Button> addButtons = new HashMap<String, Button>();

	/**
	 * Each ParameterValue is represented by an InputWidget.
	 */
	private List<ParameterCompositeInputWidget> inputWidgets = new ArrayList<ParameterCompositeInputWidget>();

	/**
	 * The first Exception found in any input widget
	 */
	private Exception currentErrorStatus;
	
	/**
	 * The currently displayed parameter
	 */
	private Parameter parameter;
	
	/**
	 * Composite to add children
	 */
	private Composite parent;
	
	/**
	 * Holds the inputWidget and add/remove buttons
	 */
	private Composite inputWidgetContainer;
	
	/**
	 * Enabled state of this Parameter
	 * <br />Disabled parameters will deliver no values.
	 */
	private boolean enabled;
	
	/**
	 * This values will initially displayed and used to gather dirty state.
	 */
	private List<ParameterValue> initialValues;
	
	/**
	 * Create a new ParameterComposite
	 * 
	 * @param parent Composite to add children
	 * @param parameter The parameter to be represented
	 */
	public ParameterComposite(Composite parent, Parameter parameter) {
		this(parent, parameter, new ArrayList<ParameterValue>(0));
	}

	/**
	 * Create a new ParameterComposite with values
	 * <br />The ParameterComposite will add <b>two</b> composites to the given parent:
	 * <ol>
	 * <li>Label or Checkbox with label</li>
	 * <li>Input widget (contains add/remove buttons)</li>
	 * </ol>
	 * 
	 * @param parent Composite to add children
	 * @param parameter The parameter to be represented
	 * @param values in object or string representation
	 */
	public ParameterComposite(Composite parent, Parameter parameter, List<ParameterValue> values) {
		this.parent = parent;
		this.parameter = parameter;
		this.enabled = !parameter.isOptional() || (values != null && !values.isEmpty());
		this.initialValues = values == null ? new LinkedList<ParameterValue>() : values;
		if (!parameter.isHidden()) {
			init();
		}
	}

	/**
	 * Treat this listener as a ModifyListener. No listener will added when this parameter is hidden.
	 * <br /><code>violation(ParameterComposite, Exception)</code> will be fired on every change in the widget and reports errors if there are some.
	 * <br /><code>layoutChanged(Composite, Composite)</code> will be fired during add/remove input widgets.
	 * <br />First fire of <code>layoutChanged(Composite, Composite)</code> will happen right now ;)
	 * 
	 * @param listener
	 */
	public void addCompositeStatusListener(ParameterCompositeStatusListener listener) {
		if (!parameter.isHidden()) {
			statusListener.add(listener);
			listener.layoutChanged(parent, inputWidgetContainer);
		}
	}

	public void removeCompositeStatusListener(ParameterCompositeStatusListener listener) {
		statusListener.remove(listener);
	}

	/**
	 * Place mandatory composites of this widget.
	 */
	private void init() {
		// place checkbox to enable/disable an optional parameter
		if (parameter.isOptional()) {
			Composite buttonEncloser = new Composite(parent, SWT.NONE);
			GridLayout buttonEncloserLayout = new GridLayout(1, false);
			buttonEncloserLayout.marginTop = 5;
			buttonEncloser.setLayout(buttonEncloserLayout);
			buttonEncloser.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
			final Button enableParameter = new Button(buttonEncloser, SWT.CHECK);
			enableParameter.setSelection(enabled);
			enableParameter.setText(LabelConverter.getLabel(parameter));
			enableParameter.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
			enableParameter.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				public void widgetSelected(SelectionEvent e) {
					enabled = enableParameter.getSelection();
					updateEnabledStatus();
					modified();
				}
			});
		} else {
			Label parameterName = new Label(parent, SWT.NONE);
			parameterName.setText(LabelConverter.getLabel(parameter) + ":");
			parameterName.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
		}

		// place input widget container
		inputWidgetContainer = new Composite(parent, SWT.NONE);
		GridLayout widgetContainerLayout = new GridLayout(2, false);
		widgetContainerLayout.verticalSpacing = 0;
		inputWidgetContainer.setLayout(widgetContainerLayout);
		GridData widgetContainerLayoutData = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
		inputWidgetContainer.setLayoutData(widgetContainerLayoutData);
		inputWidgetContainer.setBackgroundMode(SWT.INHERIT_FORCE);

		// place the first empty inputwidget if no values are given
		if (initialValues == null || initialValues.isEmpty()) {
			addInput(null);
		} else {
			for (ParameterValue value : initialValues) {
				addInput(value);
			}
		}
		updateEnabledStatus();
		updateButtonsVisibleStatus();
	}

	/**
	 * Gray out a disabled parameter and its children.
	 */
	private void updateEnabledStatus() {
		for (ParameterCompositeInputWidget inputWidget : inputWidgets) {
			inputWidget.setEnabled(enabled);
		}
		for (Button removeButton : removeButtons.values()) {
			removeButton.setEnabled(enabled);
		}
		for (Button addButton : addButtons.values()) {
			addButton.setEnabled(enabled);
		}
		inputWidgetContainer.setEnabled(enabled);
	}

	/**
	 * The remove button should only be visible if more then one InputWidget is displayed.
	 * <br />Will be called each time an InputWidget is added/removed.
	 */
	private void updateButtonsVisibleStatus() {
		boolean removeButtonVisible = inputWidgets.size() > 1;
		for (Button removeButton : removeButtons.values()) {
			removeButton.setVisible(removeButtonVisible);
		}
	}

	/**
	 * Tries to focus the first input widget available.
	 * @return true if success
	 */
	public boolean setFocus() {
		if (inputWidgets.isEmpty())
			return false;
		return inputWidgets.iterator().next().focus();
	}

	/**
	 * Adds an InputWidget to this parameter. The widget will be added to the <code>Composite inputWidgetContainer</code>.
	 * <br />Input controls and add/remove buttons will be displayed.
	 * 
	 * @param value Given value to display or <code>null</code>
	 */
	private void addInput(ParameterValue value) {
		final ParameterCompositeInputWidget inputWidget = new ParameterCompositeInputWidget(this, parameter, value);
		List<Composite> disposeGroup = new ArrayList<Composite>(2);
		disposeGroups.put(inputWidget.getUUID(), disposeGroup);
		inputWidgets.add(inputWidget);
		disposeGroup.add(inputWidget);
		
		// place buttons
		GridData buttonContainerLayoutData = new GridData(GridData.END, GridData.CENTER, false, false);
		buttonContainerLayoutData.widthHint = 60;

		//buttonContainer will be filled with buttons or labels an has a fixed size.
		Composite buttonContainer = new Composite(inputWidgetContainer, SWT.NONE);
		disposeGroup.add(buttonContainer);
		buttonContainer.setLayout(new GridLayout(2, true));
		buttonContainer.setLayoutData(buttonContainerLayoutData);
		GridData buttonLayoutData = new GridData(GridData.CENTER, GridData.CENTER, false, false);
		buttonLayoutData.widthHint = 25;
		buttonLayoutData.heightHint = 25;

		Button remove = new Button(buttonContainer, SWT.PUSH);
		removeButtons.put(inputWidget.getUUID(), remove);
		remove.setLayoutData(buttonLayoutData);
		remove.setText("-");
		remove.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
			}

			public void mouseUp(MouseEvent e) {
				removeInputWidget(inputWidget);
			}
		});
		Button add = new Button(buttonContainer, SWT.PUSH);
		addButtons.put(inputWidget.getUUID(), add);
		add.setVisible(parameter.isRepeatable());
		add.setLayoutData(buttonLayoutData);
		add.setText("+");
		add.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
			}

			public void mouseUp(MouseEvent e) {
				addInput(null);
				updateButtonsVisibleStatus();
				modified();
			}
		});

		//force some layout actions
		inputWidgetContainer.layout(disposeGroup.toArray(new Control[disposeGroup.size()]));
		inputWidgetContainer.layout(new Control[] { inputWidget, buttonContainer });
		
		//tell listener that some layout operations may be necessary
		for (ParameterCompositeStatusListener listener : statusListener.toArray(new ParameterCompositeStatusListener[statusListener.size()])) {
			listener.layoutChanged(parent, inputWidgetContainer);
		}
	}
	
	/**
	 * Remove the given widget and update/notify all those interested.
	 * 
	 * @param inputWidget to remove
	 */
	private void removeInputWidget(ParameterCompositeInputWidget inputWidget) {
		removeButtons.remove(inputWidget.getUUID());
		addButtons.remove(inputWidget.getUUID());
		for (Composite compositeToDispose : disposeGroups.get(inputWidget.getUUID())) {
			compositeToDispose.dispose();
		}
		for (ParameterCompositeStatusListener listener : statusListener.toArray(new ParameterCompositeStatusListener[statusListener.size()])) {
			listener.layoutChanged(parent, inputWidgetContainer);
		}
		inputWidgets.remove(inputWidget);
		updateButtonsVisibleStatus();
		modified();
	}

	/**
	 * Returns a list of values of the input widgets.
	 * <br />Each value is in its <code>String</code> representation except:
	 * <ul>
	 * <li><code>Parameter.BINARY</code></li>
	 * <li><code>Parameter.DATE</code></li>
	 * </ul>
	 * 
	 * @return A fresh list of objects or an empty list if this ParameterComposite is disabled
	 */
	public List<ParameterValue> getValues() {
		if (parameter.isHidden()) {
			return initialValues;
		}
		List<ParameterValue> values = new ArrayList<ParameterValue>();
		if (!enabled)
			return values;
		for (ParameterCompositeInputWidget inputWidget : inputWidgets) {
			ParameterValue value = inputWidget.getValue();
			if (value != null) {
				values.add(value);
			}
		}
		return values;
	}
	
	/**
	 * @deprecated Not tested/used yet.
	 */
	public void setValues(List<ParameterValue> values) {
		initialValues.clear();
		initialValues.addAll(values);
		for (ParameterCompositeInputWidget inputWidget : inputWidgets.toArray(new ParameterCompositeInputWidget[inputWidgets.size()])) {
			if (values.contains(inputWidget.getValue())) {
				values.remove(inputWidget.getValue());
			} else {
				removeInputWidget(inputWidget);
			}
		}
		for (ParameterValue value : values) {
			addInput(value);
		}
	}
	
	/**
	 * Sets the currently displayed values as persisted (initial).
	 */
	public void markAsClean() {
		initialValues.clear();
		initialValues.addAll(getValues());
		modified();
	}
	
	/**
	 * Checks if the displayed values differ from the initial values.
	 * 
	 * @return The dirty state
	 */
	public boolean isDirty() {
		List<ParameterValue> values = getValues();
		if (values.size() != initialValues.size()) {
			return true;
		}
		List<Object> initialValuesStringRepresentation = new ArrayList<Object>();
		for (ParameterValue object : initialValues) {
			initialValuesStringRepresentation.add(object.getValue().toString());
		}
		for (ParameterValue value : values) {
			if (!initialValuesStringRepresentation.contains(value.getValue().toString()))
				return true;
			//remove because multiple same values are possible - and it's faster
			initialValuesStringRepresentation.remove(value.toString());
		}
		return false;
	}

	/**
	 * Get the currently displayed parameter
	 * 
	 * @return The parameter displayed
	 */
	public Parameter getParameter() {
		return parameter;
	}

	/**
	 * The input widget stores an exception if something is wrong with the entered value.
	 * <br />This method will return the exception or null if there is none.
	 * <br />The input widget will show user feedback.
	 * 
	 * @param silent If true, the widget will show no feedback and a possible exception will contain an empty string as message.
	 * @return The first exception encountered or null
	 */
	public Exception getCurrentErrorStatus(boolean silent) {
		refreshErrorStatus(silent);
		return this.currentErrorStatus;
	}

	/**
	 * Lookup for the first error in <code>inputWidgets</code> and set <code>currentErrorStatus</code>.
	 * <br /><code>currentErrorStatus</code> will always be <code>null</code> if this <code>ParameterComposite</code> is disabled. 
	 * 
	 * @param silent Passed to the InputWidget to show/hide user feedback
	 */
	private void refreshErrorStatus(boolean silent) {
		currentErrorStatus = null;

		//always run through - even if this ParameterComposite is disabled to erase user feedback on InputWidget
		for (ParameterCompositeInputWidget inputWidget : inputWidgets) {
			Exception widgetStatus = inputWidget.getCurrentContainerErrorStatus(silent && isEnabled());

			//When disabled, all widgets should clear their status (no brake).
			if (!inputWidget.isDisposed() && widgetStatus != null && isEnabled()) {
				currentErrorStatus = widgetStatus;
				//Break to display the first error encountered.
				break;
			}
		}
	}

	void modified() {
		refreshErrorStatus(false);
		for (ParameterCompositeStatusListener listener : statusListener) {
			listener.violation(this, currentErrorStatus);
		}
	}
	
	Composite getInputWidgetContainer() {
		return inputWidgetContainer;
	}
	
	/**
	 * An optional parameter may be disabled.
	 * 
	 * @return the enabled state
	 */
	public boolean isEnabled() {
		return enabled;
	}
}
