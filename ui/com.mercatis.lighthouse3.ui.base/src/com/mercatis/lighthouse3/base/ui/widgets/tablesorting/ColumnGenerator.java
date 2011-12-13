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

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.mercatis.lighthouse3.base.getterchain.GetterChain;

public class ColumnGenerator {

	public static final String GETTER_CHAIN = "getterChain";
	
	private Table table;
	
	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public ColumnGenerator(Table table) {
		this.table = table;
	}
	
	/**Creates a table column and set the  property which will be used to enable column sorting
	 * 
	 *  For example if you table displays Person object and you want 
	 *  Column 1 to sort by Person.name
	 *  Column 2 to sort by Person.age
	 *  
	 *  The person Class must contain the following public getter methods
	 *  
	 *  getName()
	 *  getAge()
	 *  
	 *  They must be spelled like this.
	 *  
	 *  The value sortProperty for the column that should be sorted by name must than be name
	 *  The value sortProperty for the column that should be sorted by age must than be age
	 *  
	 * @param sortProperty
	 * @param style
	 * @return
	 */
	public TableColumn createTableColumnWithSortProperty(GetterChain chain, int style) {
		TableColumn column = new TableColumn(table, style);
		if(chain != null) {
			column.setData(GETTER_CHAIN, chain);
		}
		return column;
	}
}