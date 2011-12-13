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
package com.mercatis.lighthouse3.base.ui.widgets.tablesorting;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.mercatis.lighthouse3.base.UIBase;
import com.mercatis.lighthouse3.base.getterchain.GetterChain;

public class ColumnComparator extends ViewerComparator {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		Table table = (Table) viewer.getControl();

		if (table.getSortDirection() == SWT.DOWN) {
			Object tmp = e1;
			e1 = e2;
			e2 = tmp;
		}

		TableColumn column = table.getSortColumn();
		if (column == null)
			return super.compare(viewer, e1, e2);

		
		GetterChain chain = (GetterChain)column.getData(ColumnGenerator.GETTER_CHAIN);
		
		if(chain != null) {
			try {
				Object o1 = chain.getProperty(e1);
				Object o2 = chain.getProperty(e2);
				if(o1 == null && o2 == null) {
					return 0;
				}
				if(o1 == null || o2 == null) {
					return (o1 == null) ? -1: 1;
				}
				if (o1 instanceof Comparable && o1 instanceof Comparable) {
					return ((Comparable) o1).compareTo(o2);
				}
			} catch (Exception e) {
				UIBase.getDefault().getLog().log(new Status(IStatus.ERROR, UIBase.PLUGIN_ID, e.getMessage(), e));
			}
		}
		return super.compare(viewer, e1, e2);

	}
}
