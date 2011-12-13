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

import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.mercatis.lighthouse3.commons.commons.Tuple;


public class UDFWidget extends Composite {
	
	private static final String KEY_DATE_CONTROL = "KEY_DATE_CONTROL";
	
	private static final String KEY_TIME_CONTROL = "KEY_TIME_CONTROL";
	
	private String previousSelection;
	
	private Text keyText;
	
	private Control valueControl;
	
	private Combo typeCombo;
	
	private UDFWidget _this = this;

	private List<Listener> modifyListeners;

	private SelectionListener typeComboSelectionListener = new SelectionListener() {
		
		public void widgetSelected(SelectionEvent e) {
			String selection = typeCombo.getItem(typeCombo.getSelectionIndex());
			if (selection.equals(previousSelection))
					return;
			
			previousSelection = selection;
			Control oldControl = valueControl;
			
			setLayoutDeferred(true);
			
			if (selection.equals("Date")) {
				valueControl = new Composite(_this, SWT.NONE);
				((Composite) valueControl).setLayout(new FillLayout());
				DateTime date = new DateTime((Composite) valueControl, SWT.DATE | SWT.LONG | SWT.DROP_DOWN);
				DateTime time = new DateTime((Composite) valueControl, SWT.TIME | SWT.LONG | SWT.DROP_DOWN);
				valueControl.setData(KEY_DATE_CONTROL, date);
				valueControl.setData(KEY_TIME_CONTROL, time);
				valueControl.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false));
			} else {
				if (selection.equals("Boolean")) {
					valueControl = new Button(_this, SWT.CHECK);
					valueControl.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
				} else {
					valueControl = new Text(_this, SWT.BORDER);
					valueControl.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
					((Text) valueControl).setMessage("Value");
				}
			}
			
			valueControl.moveAbove(oldControl);
			layout(new Control[] { oldControl, valueControl });
			oldControl.dispose();
			setLayoutDeferred(false);
		}
		
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
	};

	/**
	 * @param parent
	 * @param style
	 */
	public UDFWidget(Composite parent, int style) {
		super(parent, style);
		placeWidgets();
	}
	
	/**
	 * @param parent
	 * @param style
	 * @param modifyListeners
	 */
	public UDFWidget(Composite parent, int style, List<Listener> modifyListeners) {
		super(parent, style);
		this.modifyListeners = modifyListeners;
		placeWidgets();
	}
	
	private void placeWidgets() {
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);
		
		keyText = new Text(this, SWT.BORDER);
		keyText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		keyText.setMessage("Key");
		
		typeCombo = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);
		typeCombo.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
		typeCombo.add("String");
		typeCombo.add("Float");
		typeCombo.add("Double");
		typeCombo.add("Integer");
		typeCombo.add("Long");
		typeCombo.add("Date");
		typeCombo.add("Boolean");
		typeCombo.select(typeCombo.indexOf("String"));
		typeCombo.addSelectionListener(typeComboSelectionListener );
		previousSelection = "String";
		
		valueControl = new Text(this, SWT.BORDER);
		valueControl.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		((Text) valueControl).setMessage("Value");

		if (modifyListeners != null) {
			for (Listener listener : modifyListeners) {
				keyText.addListener(SWT.Modify, listener);
				typeCombo.addListener(SWT.Modify, listener);
				valueControl.addListener(SWT.Modify, listener);
			}
		}
	}

	/**
	 * @param udf
	 */
	@SuppressWarnings("deprecation")
	public void setUdf(Tuple<String, Object> udf) {
		String key = udf.getA();
		Object value = udf.getB();
		
		keyText.setText(key);
		
		if (value instanceof String) {
			typeCombo.select(typeCombo.indexOf("String"));
			typeComboSelectionListener.widgetSelected(null);
			((Text) valueControl).setText((String) value);
		}
		if (value instanceof Float) {
			typeCombo.select(typeCombo.indexOf("Float"));
			typeComboSelectionListener.widgetSelected(null);
			((Text) valueControl).setText(((Float) value).toString());
		}
		if (value instanceof Double) {
			typeCombo.select(typeCombo.indexOf("Double"));
			typeComboSelectionListener.widgetSelected(null);
			((Text) valueControl).setText(((Double) value).toString());
		}
		if (value instanceof Integer) {
			typeCombo.select(typeCombo.indexOf("Integer"));
			typeComboSelectionListener.widgetSelected(null);
			((Text) valueControl).setText(((Integer) value).toString());
		}
		if (value instanceof Long) {
			typeCombo.select(typeCombo.indexOf("Long"));
			typeComboSelectionListener.widgetSelected(null);
			((Text) valueControl).setText(((Long) value).toString());
		}
		if (value instanceof Date) {
			typeCombo.select(typeCombo.indexOf("Date"));
			typeComboSelectionListener.widgetSelected(null);
			DateTime date = (DateTime) ((Composite) valueControl).getData(KEY_DATE_CONTROL);
			date.setYear(((Date) value).getYear() + 1900);
			date.setMonth(((Date) value).getMonth());
			date.setDay(((Date) value).getDate());

			DateTime time = (DateTime) ((Composite) valueControl).getData(KEY_TIME_CONTROL);
			time.setHours(((Date) value).getHours());
			time.setMinutes(((Date) value).getMinutes());
			time.setSeconds(((Date) value).getSeconds());
		}
		if (value instanceof Boolean) {
			typeCombo.select(typeCombo.indexOf("Boolean"));
			typeComboSelectionListener.widgetSelected(null);
			((Text) valueControl).setText(((Boolean) value).toString());
		}
	}
	
	/**
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public Tuple<String,Object> getUdf() {
		String selection = typeCombo.getItem(typeCombo.getSelectionIndex());
		if (selection.equals("String")) {
			return new Tuple<String,Object>(keyText.getText().trim(), ((Text) valueControl).getText().trim());
		}
		if (selection.equals("Float")) {
			return new Tuple<String,Object>(keyText.getText().trim(), Float.valueOf(((Text) valueControl).getText().trim()));
		}
		if (selection.equals("Double")) {
			return new Tuple<String,Object>(keyText.getText().trim(), Double.valueOf(((Text) valueControl).getText().trim()));
		}
		if (selection.equals("Long")) {
			return new Tuple<String,Object>(keyText.getText().trim(), Long.valueOf(((Text) valueControl).getText().trim()));
		}
		if (selection.equals("Integer")) {
			return new Tuple<String,Object>(keyText.getText().trim(), Integer.valueOf(((Text) valueControl).getText().trim()));
		}
		if (selection.equals("Boolean")) {
			return new Tuple<String,Object>(keyText.getText().trim(), Boolean.valueOf(((Button) valueControl).getSelection()));
		}
		if (selection.equals("Date")) {
			Date result = new Date(0l);
			
			DateTime date = (DateTime) ((Composite) valueControl).getData(KEY_DATE_CONTROL);
			result.setYear(date.getYear() - 1900);
			result.setMonth(date.getMonth());
			result.setDate(date.getDay());
			
			DateTime time = (DateTime) ((Composite) valueControl).getData(KEY_TIME_CONTROL);
			result.setHours(time.getHours());
			result.setMinutes(time.getMinutes());
			result.setSeconds(time.getSeconds());
			
			return new Tuple<String,Object>(keyText.getText().trim(), result);
		}
		
		return null;
	}
}
