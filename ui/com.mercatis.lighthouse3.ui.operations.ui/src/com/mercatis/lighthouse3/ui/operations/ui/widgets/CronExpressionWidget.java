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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


public class CronExpressionWidget extends Composite {

	private LinkedList<CronExpressionWidgetListener> widgetListeners = new LinkedList<CronExpressionWidgetListener>();
	private Map<Object, String> descriptions = new HashMap<Object, String>();
	private Text second;
	private Text minute;
	private Text hour;
	private Text dayOfMonth;
	private Text month;
	private Text dayOfWeek;
	private Text year;
	
	public CronExpressionWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(7, true));
		placeCronSegments(this);
	}
	
	public void placeCronSegments(Composite composite) {
		Label secondLabel = new Label(composite, SWT.NONE);
		secondLabel.setText("sec");
		Label minuteLabel = new Label(composite, SWT.NONE);
		minuteLabel.setText("min");
		Label hourLabel = new Label(composite, SWT.NONE);
		hourLabel.setText("h");
		Label dayLabel = new Label(composite, SWT.NONE);
		dayLabel.setText("dom");
		Label monthLabel = new Label(composite, SWT.NONE);
		monthLabel.setText("m");
		Label dayOfWeekLabel = new Label(composite, SWT.NONE);
		dayOfWeekLabel.setText("dow");
		Label yearLabel = new Label(composite, SWT.NONE);
		yearLabel.setText("yyyy");

		GridData segmentGridData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
		segmentGridData.widthHint = 45;

		second = new Text(composite, SWT.BORDER);
		second.setText("*");
		second.setLayoutData(segmentGridData);
		second.addListener(SWT.Modify, modifyListener);
		second.addFocusListener(focusListener);
		descriptions.put(second, "Second: 0-59 (, - * /)");

		minute = new Text(composite, SWT.BORDER);
		minute.setText("*");
		minute.setLayoutData(segmentGridData);
		minute.addListener(SWT.Modify, modifyListener);
		minute.addFocusListener(focusListener);
		descriptions.put(minute, "Minute: 0-59 (, - * /)");

		hour = new Text(composite, SWT.BORDER);
		hour.setText("*");
		hour.setLayoutData(segmentGridData);
		hour.addListener(SWT.Modify, modifyListener);
		hour.addFocusListener(focusListener);
		descriptions.put(hour, "Hour: 0-23 (, - * /)");

		dayOfMonth = new Text(composite, SWT.BORDER);
		dayOfMonth.setText("*");
		dayOfMonth.setLayoutData(segmentGridData);
		dayOfMonth.addListener(SWT.Modify, modifyListener);
		dayOfMonth.addFocusListener(focusListener);
		descriptions.put(dayOfMonth, "Day of month: 1-31 (, - * ? / L W C)");

		month = new Text(composite, SWT.BORDER);
		month.setText("*");
		month.setLayoutData(segmentGridData);
		month.addListener(SWT.Modify, modifyListener);
		month.addFocusListener(focusListener);
		descriptions.put(month, "Month: 1-12 or JAN-DEC (, - * /)");

		dayOfWeek = new Text(composite, SWT.BORDER);
		dayOfWeek.setText("?");
		dayOfWeek.setLayoutData(segmentGridData);
		dayOfWeek.addListener(SWT.Modify, modifyListener);
		dayOfWeek.addFocusListener(focusListener);
		descriptions.put(dayOfWeek, "Day of week: 1-7 or SUN-SAT (, - * / L C #)");

		year = new Text(composite, SWT.BORDER);
		year.setText("");
		year.setLayoutData(segmentGridData);
		year.addListener(SWT.Modify, modifyListener);
		year.addFocusListener(focusListener);
		descriptions.put(year, "Year: empty, 1970-2099 (, - * /)");
	}
	
	public String getExpression() {
		String expression = second.getText() + " "
		+ minute.getText() + " "
		+ hour.getText() + " "
		+ dayOfMonth.getText() + " "
		+ month.getText() + " "
		+ dayOfWeek.getText() + " "
		+ (year.getText().trim().length() == 0 ? "" : year.getText());
		return expression.trim();
	}
	
	public void setExpression(String expression) {
		//TODO would be nicer with pattern matcher, but regular expression is not ready yet
		StringTokenizer stringTokenizer = new StringTokenizer(expression, " ");
		int i = 0;
		while (stringTokenizer.hasMoreTokens()) {
			String expressionSegment = stringTokenizer.nextToken();
			switch (i) {
			case 0:
				second.setText(expressionSegment);
				break;
			case 1:
				minute.setText(expressionSegment);
				break;
			case 2:
				hour.setText(expressionSegment);
				break;
			case 3:
				dayOfMonth.setText(expressionSegment);
				break;
			case 4:
				month.setText(expressionSegment);
				break;
			case 5:
				dayOfWeek.setText(expressionSegment);
				break;
			case 6:
				year.setText(expressionSegment);
				break;
			}
			i++;
		}
	}
	
	public String getSecond() {
		return second.getText();
	}
	
	public String getMinute() {
		return minute.getText();
	}
	
	public String getHour() {
		return hour.getText();
	}
	
	public String getDayOfMonth() {
		return dayOfMonth.getText();
	}
	
	public String getMonth() {
		return month.getText();
	}
	
	public String getDayOfWeek() {
		return dayOfWeek.getText();
	}
	
	public String getYear() {
		return year.getText();
	}
	
	public void plausibilityCheck() throws Exception {
		if (dayOfWeek.getText().equals("*") && dayOfMonth.getText().equals("*"))
			throw new Exception("Either Day of week or Day of month should be \"*\"");
	}
	
	public void addWidgetListener(CronExpressionWidgetListener listener) {
		widgetListeners.add(listener);
	}
	
	public void removeWidgetListener(CronExpressionWidgetListener listener) {
		widgetListeners.remove(listener);
	}

	private Listener modifyListener = new Listener() {
		public void handleEvent(org.eclipse.swt.widgets.Event event) {
			for (CronExpressionWidgetListener listener : widgetListeners.toArray(new CronExpressionWidgetListener[widgetListeners.size()])) {
				listener.modified(getExpression(),
						getSecond(),
						getMinute(),
						getHour(),
						getDayOfMonth(),
						getMonth(),
						getDayOfWeek(),
						getYear());
			}
		}
	};

	private FocusListener focusListener = new FocusListener() {
		private String hint;
		
		public void focusGained(FocusEvent e) {
			hint = descriptions.get(e.getSource());
			updateHintOnListeners();
		}

		public void focusLost(FocusEvent e) {
			hint = null;
			updateHintOnListeners();
		}
		
		private void updateHintOnListeners() {
			for (CronExpressionWidgetListener listener : widgetListeners.toArray(new CronExpressionWidgetListener[widgetListeners.size()])) {
				listener.updateHint(hint);
			}
		}
	};
}
