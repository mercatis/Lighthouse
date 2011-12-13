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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

import com.mercatis.lighthouse3.commons.commons.Tuple;


public class IntervalDateTime extends Composite {

	private Button lowerBoundEnabledButton;
	
	private DateTime lowerBoundDate;
	
	private DateTime lowerBoundTime;
	
	private Button upperBoundEnabledButton;
	
	private DateTime upperBoundDate;
	
	private DateTime upperBoundTime;
	
	private List<Listener> modifyListeners;

	private SelectionListener lowerBoundEnabledButtonSelectionListener = new SelectionListener() {
		
		public void widgetSelected(SelectionEvent e) {
			lowerBoundDate.setEnabled(lowerBoundEnabledButton.getSelection());
			lowerBoundTime.setEnabled(lowerBoundEnabledButton.getSelection());
		}
		
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
	};

	private SelectionListener upperBoundEnabledButtonSelectionListener = new SelectionListener() {
		
		public void widgetSelected(SelectionEvent e) {
			upperBoundDate.setEnabled(upperBoundEnabledButton.getSelection());
			upperBoundTime.setEnabled(upperBoundEnabledButton.getSelection());
		}
		
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
	};
	
	/**
	 * @param parent
	 * @param style
	 */
	public IntervalDateTime(Composite parent, int style) {
		super(parent, style);
		placeWidgets();
	}
	
	/**
	 * @param parent
	 * @param style
	 * @param modifyListeners
	 */
	public IntervalDateTime(Composite parent, int style, List<Listener> modifyListeners) {
		super(parent, style);
		this.modifyListeners = modifyListeners;
		placeWidgets();
	}
	
	private void placeWidgets() {
		GridLayout layout = new GridLayout(2, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);
		
		Group lowerBound = new Group(this, SWT.NONE);
		lowerBound.setText("Lower Bound");
		lowerBound.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		lowerBound.setLayout(new GridLayout(3, false));
		lowerBoundEnabledButton = new Button(lowerBound, SWT.CHECK);
		lowerBoundEnabledButton.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
		lowerBoundEnabledButton.setSelection(true);
		lowerBoundEnabledButton.addSelectionListener(lowerBoundEnabledButtonSelectionListener);
		lowerBoundDate = new DateTime(lowerBound, SWT.DATE | SWT.LONG | SWT.DROP_DOWN);
		lowerBoundDate.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		lowerBoundTime = new DateTime(lowerBound, SWT.TIME | SWT.LONG | SWT.DROP_DOWN);
		lowerBoundTime.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		
		Group upperBound = new Group(this, SWT.NONE);
		upperBound.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		upperBound.setText("Upper Bound");
		upperBound.setLayout(new GridLayout(3, false));
		upperBoundEnabledButton = new Button(upperBound, SWT.CHECK);
		upperBoundEnabledButton.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
		upperBoundEnabledButton.setSelection(true);
		upperBoundEnabledButton.addSelectionListener(upperBoundEnabledButtonSelectionListener);
		upperBoundDate = new DateTime(upperBound, SWT.DATE | SWT.LONG | SWT.DROP_DOWN);
		upperBoundDate.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		upperBoundTime = new DateTime(upperBound, SWT.TIME | SWT.LONG | SWT.DROP_DOWN);
		upperBoundTime.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		
		if (modifyListeners != null) {
			for (final Listener listener : modifyListeners) {
				lowerBoundEnabledButton.addListener(SWT.Selection, listener);
				upperBoundEnabledButton.addListener(SWT.Selection, listener);
				
				lowerBoundDate.addListener(SWT.Selection, listener);
				upperBoundDate.addListener(SWT.Selection, listener);
				
				lowerBoundTime.addListener(SWT.Selection, listener);
				upperBoundTime.addListener(SWT.Selection, listener);
			}
		}
	}

	
	/**
	 * @param dateTuple
	 */
	@SuppressWarnings("deprecation")
	public void setDateTuple(Tuple<Date,Date> dateTuple) {
		Date lowerBound = dateTuple.getA();
		Date upperBound = dateTuple.getB();
		
		if (lowerBound == null) {
			lowerBoundEnabledButton.setSelection(false);
		} else {
			lowerBoundDate.setYear(lowerBound.getYear() + 1900);
			lowerBoundDate.setMonth(lowerBound.getMonth());
			lowerBoundDate.setDay(lowerBound.getDate());
			
			lowerBoundTime.setHours(lowerBound.getHours());
			lowerBoundTime.setMinutes(lowerBound.getMinutes());
			lowerBoundTime.setSeconds(lowerBound.getSeconds());
			
			lowerBoundEnabledButton.setSelection(true);
		}
		lowerBoundEnabledButtonSelectionListener.widgetSelected(null);
		
		if (upperBound == null) {
			upperBoundEnabledButton.setSelection(false);
		} else {
			upperBoundDate.setYear(upperBound.getYear() + 1900);
			upperBoundDate.setMonth(upperBound.getMonth());
			upperBoundDate.setDay(upperBound.getDate());
			
			upperBoundTime.setHours(upperBound.getHours());
			upperBoundTime.setMinutes(upperBound.getMinutes());
			upperBoundTime.setSeconds(upperBound.getSeconds());
			
			upperBoundEnabledButton.setSelection(true);
		}
		upperBoundEnabledButtonSelectionListener.widgetSelected(null);
	}

	/**
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public Tuple<Date,Date> getDateTuple() {
		Date lowerBound = null;
		if (lowerBoundEnabledButton.getSelection()) {
			lowerBound = new Date(0l);
			lowerBound.setYear(lowerBoundDate.getYear() - 1900);
			lowerBound.setMonth(lowerBoundDate.getMonth());
			lowerBound.setDate(lowerBoundDate.getDay());
			lowerBound.setHours(lowerBoundTime.getHours());
			lowerBound.setMinutes(lowerBoundTime.getMinutes());
			lowerBound.setSeconds(lowerBoundTime.getSeconds());
		}
		
		Date upperBound = null;
		if (upperBoundEnabledButton.getSelection()) {
			upperBound = new Date(999l);
			upperBound.setYear(upperBoundDate.getYear() - 1900);
			upperBound.setMonth(upperBoundDate.getMonth());
			upperBound.setDate(upperBoundDate.getDay());
			upperBound.setHours(upperBoundTime.getHours());
			upperBound.setMinutes(upperBoundTime.getMinutes());
			upperBound.setSeconds(upperBoundTime.getSeconds());
		}

		return new Tuple<Date,Date>(lowerBound,upperBound);
	}
	
}
