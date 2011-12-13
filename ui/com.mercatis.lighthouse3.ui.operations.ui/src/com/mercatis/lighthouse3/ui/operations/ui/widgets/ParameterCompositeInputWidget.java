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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.mercatis.lighthouse3.domainmodel.operations.Parameter;
import com.mercatis.lighthouse3.domainmodel.operations.ParameterValue;
import com.mercatis.lighthouse3.ui.operations.ui.OperationsUI;


class ParameterCompositeInputWidget extends Composite {

	private String uuid;
	private Object value;
	private String valueLabel;
	private Control input;
	private Method valueGetter;
	private Exception currentContainerErrorStatus = null;
	private Date date = new Date(); // special treatment for date
	private List<Control> controls = new ArrayList<Control>();
	private ParameterComposite parameterComposite;
	private Parameter parameter;

	/**
	 * Creates a new instance of ParameterCompositeInputWidget to represent a ParameterValue.
	 * <br />This Class has limited access and should only be used inside a ParameterComposite.
	 * 
	 * @param parameterComposite
	 */
	ParameterCompositeInputWidget(ParameterComposite parameterComposite, Parameter parameter, ParameterValue value) {
		super(parameterComposite.getInputWidgetContainer(), SWT.NONE);
		this.parameterComposite = parameterComposite;
		uuid = UUID.randomUUID().toString();
		setLayout(new GridLayout(2, false));
		GridData layoutData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		layoutData.verticalIndent = 0;
		setLayoutData(layoutData);
		this.parameter = parameter;
		if (value != null) {
			this.value = value.getValue();
			this.valueLabel = value.getValueLabel();
		}
		init();
	}
	
	String getUUID() {
		return uuid;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for (Control control : controls) {
			control.setEnabled(enabled);
		}
	}

	boolean focus() {
		if (input != null)
			return input.setFocus();
		return false;
	}

	ParameterValue getValue() {
		if (input == null)
			return null;
		try {
			Object value;
			if (valueGetter != null) {
				value = valueGetter.invoke(input);
			} else {
				value = this.value;
			}
			if (parameterComposite.getParameter().getType().equals(Parameter.DATE)) {
				value = date;
			}
			if (value == null) {
				return null;
			}
			ParameterValue parameterValue = parameter.createValue(value);
			parameterValue.setValueLabel(valueLabel);
			return parameterValue;
		} catch (IllegalArgumentException e) {
			OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
		} catch (IllegalAccessException e) {
			OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
		} catch (InvocationTargetException e) {
			OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
		} catch (Exception e) {
			OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
		}
		return null;
	}

	private Listener modifyListener = new Listener() {
		public void handleEvent(Event event) {
			refreshCurrentContainerErrorStatus(false);
			parameterComposite.modified();
		}
	};

	Exception getCurrentContainerErrorStatus(boolean silent) {
		refreshCurrentContainerErrorStatus(silent);
		return currentContainerErrorStatus;
	}

	void refreshCurrentContainerErrorStatus(boolean silent) {
		try {
			Object value;
			if (valueGetter != null) {
				value = valueGetter.invoke(input);
			} else {
				value = this.value;
			}
			if (parameterComposite.getParameter().getType().equals(Parameter.DATE)) {
				value = date;
			}
			if (!parameterComposite.getParameter().getType().equals(Parameter.BOOLEAN))
				input.setBackground(new Color(null, 255, 255, 255));
			ParameterValue parameterValue = parameterComposite.getParameter().createValue(value);
			currentContainerErrorStatus = null;
			if (!parameterComposite.getParameter().isOptional()) {
				if (parameterValue.getValue() == null) {
					currentContainerErrorStatus = new Exception("Mandatory parameters must not be null.");
				} else {
					if ((parameterValue.getValue() instanceof String && ((String) parameterValue.getValue()).length() == 0)) {
						currentContainerErrorStatus = new Exception("Mandatory parameters must not be empty Strings.");
					}
				}
			}
			input.setToolTipText(null);
		} catch (IllegalArgumentException e) {
			OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
		} catch (IllegalAccessException e) {
			OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
		} catch (InvocationTargetException e) {
			OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
		} catch (Exception e) {
			if (silent || !parameterComposite.isEnabled()) {
				currentContainerErrorStatus = new Exception("");
				if (!parameterComposite.getParameter().getType().equals(Parameter.BOOLEAN))
					input.setBackground(new Color(null, 255, 255, 255));
			} else {
				if (!parameterComposite.getParameter().getType().equals(Parameter.BOOLEAN))
					input.setBackground(new Color(null, 200, 50, 50));
				input.setToolTipText(e.getMessage());
				currentContainerErrorStatus = e;
			}
		}
	}

	private void init() {
		try {
			GridData inputLayoutData = new GridData(GridData.FILL, GridData.CENTER, true, false);
			inputLayoutData.horizontalSpan = 2;
			String type = parameterComposite.getParameter().getType();
			if (type.equals(Parameter.BINARY) || type.equals(Parameter.CLOB)) {
				inputLayoutData.horizontalSpan = 1;
				Text text = new Text(this, SWT.BORDER);
				text.setEnabled(false);
				text.setEditable(false);
				if (value != null && valueLabel != null) {
					text.setText(valueLabel);
				}
				controls.add(text);
				input = text;
				valueGetter = null;
				text.addListener(SWT.Modify, modifyListener);
				Composite buttonHolder = new Composite(this, SWT.None);
				buttonHolder.setLayout(new GridLayout(2, false));
				GridData buttonLayoutData = new GridData(SWT.END, SWT.END, true, false);
				buttonLayoutData.heightHint = 26;
				
				if (value == null) {
					buttonLayoutData.horizontalSpan = 2;
				} else {
					Button downloadButton = new Button(buttonHolder, SWT.PUSH);
					downloadButton.setLayoutData(buttonLayoutData);
					downloadButton.setText("Save as...");
					downloadButton.addListener(SWT.Selection, downloadButtonListener);
				}
				
				Button fileChooser = new Button(buttonHolder, SWT.PUSH);
				controls.add(fileChooser);
				fileChooser.setLayoutData(buttonLayoutData);
				fileChooser.setText("Browse...");
				if (type.equals(Parameter.BINARY)) {
					fileChooser.addListener(SWT.Selection, binaryFileChooserListener);
				} else if (type.equals(Parameter.CLOB)) {
					fileChooser.addListener(SWT.Selection, clobFileChooserListener);
				}
			} else if (type.equals(Parameter.BOOLEAN)) {
				Button check = new Button(this, SWT.CHECK);
				if (value != null)
					check.setSelection(Boolean.parseBoolean(value.toString()));
				controls.add(check);
				input = check;
				valueGetter = check.getClass().getMethod("getSelection");
				check.addListener(SWT.SELECTED, modifyListener);
			} else if (type.equals(Parameter.DATE)) {
				setLayout(new GridLayout(2, true));
				inputLayoutData.horizontalSpan = 1;
				final DateTime dateControl = new DateTime(this, SWT.DATE | SWT.LONG | SWT.CALENDAR);
				final DateTime timeControl = new DateTime(this, SWT.TIME | SWT.LONG | SWT.CALENDAR);
				if (value != null) {
					//TODO might not hit
					SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
					try {
						date = sdf.parse(value.toString());
					} catch (ParseException e) {
						OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
					}
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date);
					dateControl.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar
							.get(Calendar.DAY_OF_MONTH));
					timeControl.setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar
							.get(Calendar.SECOND));
				}
				dateControl.setLayoutData(inputLayoutData);
				timeControl.setLayoutData(inputLayoutData);
				controls.add(dateControl);
				controls.add(timeControl);
				dateControl.addListener(SWT.Modify, modifyListener);
				timeControl.addListener(SWT.Modify, modifyListener);
				input = dateControl;
				valueGetter = dateControl.getClass().getMethod("getDay");

				SelectionListener dateListener = new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
					}

					public void widgetSelected(SelectionEvent e) {
						int year = dateControl.getYear();
						int month = dateControl.getMonth();
						int day = dateControl.getDay();
						int hour = timeControl.getHours();
						int minute = timeControl.getMinutes();
						int second = timeControl.getSeconds();
						Calendar calendar = Calendar.getInstance();
						calendar.set(year, month, day, hour, minute, second);
						date = calendar.getTime();
						modifyListener.handleEvent(null);
					}
				};
				dateControl.addSelectionListener(dateListener);
				timeControl.addSelectionListener(dateListener);
			} else {// type is string or numeric
				if (parameterComposite.getParameter().getChoice() != null && !parameterComposite.getParameter().getChoice().isEmpty()) {
					Combo combo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
					controls.add(combo);
					for (String choice : parameterComposite.getParameter().getChoice()) {
						combo.add(choice);
					}
					if (value != null) {
						combo.select(combo.indexOf(value.toString()));
					} else {
						combo.select(0);
					}
					input = combo;
					valueGetter = combo.getClass().getMethod("getText");
					combo.addListener(SWT.Modify, modifyListener);
				} else {
					Text text = null;
					if (type.equals(Parameter.PASSWORD))
						text = new Text(this, SWT.BORDER | SWT.PASSWORD);
					else
						text = new Text(this, SWT.BORDER);
					if (value != null) {
						text.setText(value.toString());
					}
					controls.add(text);
					input = text;
					valueGetter = text.getClass().getMethod("getText");
					text.addListener(SWT.Modify, modifyListener);
				}
			}
			input.setLayoutData(inputLayoutData);
		} catch (SecurityException e) {
			OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
		} catch (NoSuchMethodException e) {
			OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
		}
		refreshCurrentContainerErrorStatus(true);
		parameterComposite.modified();
	}

	private byte[] getBytesFromFile(File file) throws Exception {
		byte[] data = null;
		FileInputStream fileInputStream = new FileInputStream(file);
		data = new byte[(int) file.length()];
		fileInputStream.read(data);
		fileInputStream.close();
		return data;
	}
	
	private char[] getCharactersFromFile(File file) throws Exception {
		char[] data = null;
		FileReader fileReader = new FileReader(file);
		data = new char[(int) file.length()];
		fileReader.read(data);
		fileReader.close();
		return data;
	}
	
	private void writeBytesToFile(File file, byte[] value) throws Exception {
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(value);
		fos.close();
	}
	
	protected void writeCharsToFile(File file, char[] value) throws Exception {
		FileWriter writer = new FileWriter(file);
		writer.write(value);
		writer.close();
	}

	private Listener downloadButtonListener = new Listener() {
		public void handleEvent(Event event) {
			FileDialog fileDialog = new FileDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), SWT.SAVE);
			fileDialog.setText("Select file destination");
			fileDialog.setFileName(valueLabel);
			fileDialog.setOverwrite(true);
			String filename = fileDialog.open();
			if (filename != null) {
				File file = new File(filename);
				try {
					if (value instanceof byte[]) {
						writeBytesToFile(file, (byte[]) value);
					} else if (value instanceof char[]) {
						writeCharsToFile(file, (char[]) value);
					}
				} catch (Exception e) {
					OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
	};
	
	private Listener binaryFileChooserListener = new Listener() {
		public void handleEvent(Event event) {
			FileDialog fileDialog = new FileDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), SWT.OPEN);
			fileDialog.setText("Select a file with binary data");
			String filename = fileDialog.open();
			if (filename != null) {
				File file = new File(filename);
				valueLabel = file.getName();
				try {
					value = getBytesFromFile(file);
					((Text)input).setText(valueLabel);
				} catch (Exception e) {
					OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
	};

	private Listener clobFileChooserListener = new Listener() {
		public void handleEvent(Event event) {
			FileDialog fileDialog = new FileDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), SWT.OPEN);
			fileDialog.setText("Select a text file");
			String filename = fileDialog.open();
			if (filename != null) {
				File file = new File(filename);
				valueLabel = file.getName();
				try {
					value = getCharactersFromFile(file);
					((Text)input).setText(valueLabel);
				} catch (Exception e) {
					OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
	};
}
