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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.mercatis.lighthouse3.commons.commons.Tuple;


public class EventInputControlFactory implements InputControlFactory {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.widgets.eventfilter.InputControlFactory#getControl(org.eclipse.swt.widgets.Composite, com.mercatis.lighthouse3.base.ui.widgets.eventfilter.InputControlType)
	 */
	public Control getControl(Composite parent, List<Listener> modifyListeners, InputControlType type) {
		return getControl(parent, modifyListeners, type, Collections.emptyList(), null);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.widgets.eventfilter.InputControlFactory#getControl(org.eclipse.swt.widgets.Composite, com.mercatis.lighthouse3.base.ui.widgets.eventfilter.InputControlType, java.util.List)
	 */
	public <T> Control getControl(Composite parent, List<Listener> modifyListeners, InputControlType type, List<T> choices) {
		return getControl(parent, modifyListeners, type, choices, null);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.widgets.eventfilter.InputControlFactory#getControl(org.eclipse.swt.widgets.Composite, com.mercatis.lighthouse3.base.ui.widgets.eventfilter.InputControlType, java.util.List, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public <T> Control getControl(Composite parent, List<Listener> modifyListeners, InputControlType type, List<T> choices, T selectedChoice) {
		switch (type) {
		case Text:
			Text text = new Text(parent, SWT.BORDER);
			if (modifyListeners != null) {
				for (Listener listener : modifyListeners) {
					text.addListener(SWT.Modify, listener);
				}
			}
			if (selectedChoice != null)
				text.setText((String) selectedChoice);
			return text;
		case Combo:
			Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
			if (modifyListeners != null) {
				for (Listener listener : modifyListeners) {
					combo.addListener(SWT.Modify, listener);
				}
			}
			for (T choice : choices) {
				combo.add((String) choice);
			}
			if (selectedChoice != null) {
				combo.select(combo.indexOf((String) selectedChoice));
			}
			return combo;
		case Checkbox:
			Button checkbox = new Button(parent, SWT.CHECK);
			if (modifyListeners != null) {
				for (Listener listener : modifyListeners) {
					checkbox.addListener(SWT.Modify, listener);
				}
			}
			if (selectedChoice != null) {
				checkbox.setSelection((Boolean) selectedChoice);
			}
			return checkbox;
		case IntervalDateTime:
			IntervalDateTime intervalDateTime = new IntervalDateTime(parent, SWT.NONE, modifyListeners);
			if (selectedChoice != null)
				intervalDateTime.setDateTuple((Tuple<Date,Date>) selectedChoice);
			return intervalDateTime;
		case UserDefinedField:
			UDFWidget udfWidget = new UDFWidget(parent, SWT.NONE, modifyListeners);
			if (selectedChoice != null)
				udfWidget.setUdf((Tuple<String,Object>) selectedChoice);
			return udfWidget;
		}
		
		Label errorLabel = new Label(parent, SWT.BORDER);
		errorLabel.setText("UNKNOWN CONTROL TYPE");
		return errorLabel;
	}
	
	@SuppressWarnings("unchecked")
	public <T, V> V getValue(InputControlType type, T control) {
		switch (type) {
		case Text:
			return (V) ((Text) control).getText().trim();
		case Combo:
			Combo combo = (Combo) control;
			int selectionIndex = combo.getSelectionIndex();
			if (selectionIndex != -1)
				return (V) combo.getItem(selectionIndex);
			else
				return null;
		case Checkbox:
			return (V) Boolean.valueOf(((Button) control).getSelection());
		case IntervalDateTime:
			return (V) ((IntervalDateTime) control).getDateTuple();
		case UserDefinedField:
			return (V) ((UDFWidget) control).getUdf();
		}
		
		return null;
	}
}
