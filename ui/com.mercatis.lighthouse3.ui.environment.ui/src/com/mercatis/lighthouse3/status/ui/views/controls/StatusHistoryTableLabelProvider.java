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
package com.mercatis.lighthouse3.status.ui.views.controls;

import java.text.SimpleDateFormat;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.mercatis.lighthouse3.domainmodel.status.EventTriggeredStatusChange;
import com.mercatis.lighthouse3.domainmodel.status.ManualStatusClearance;
import com.mercatis.lighthouse3.domainmodel.status.StalenessChange;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusChange;

public class StatusHistoryTableLabelProvider extends LabelProvider implements ITableLabelProvider {

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.mmm");
	
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof StatusChange) {
			switch (columnIndex) {
			case 0:
				return dateFormat.format(((StatusChange) element).getDateOfChange());
			case 1:
				return translateErrorToString(((StatusChange) element).getNewStatus());
			case 2:
				return ((StatusChange) element).getErrorCounter() + ""; 
			case 3:
				return ((StatusChange) element).getOkCounter() + ""; 
			case 4:
				return ((StatusChange) element).getStaleCounter() + ""; 
			case 5:
					return translateStatusChangeToString((StatusChange) element); 
			default:
				break;
			}
		}
		return "";
	}
	
	public static String translateStatusChangeToString(StatusChange change) {
		if(change instanceof EventTriggeredStatusChange) {
			return "Event";
		}
		if(change instanceof ManualStatusClearance) {
			return "Manual Clearance";
		}
		if(change instanceof StalenessChange) {
			return "Stale";
		}
		return "";
	}

	public static String translateErrorToString(int error) {
		switch (error) {
			case Status.NONE:
				return "NONE";
			case Status.OK:
				return "OK";
			case Status.STALE:
				return "STALE";
			case Status.ERROR:
				return "ERROR";
			default:
				return "UNKNOWN";
		}
	}
}
