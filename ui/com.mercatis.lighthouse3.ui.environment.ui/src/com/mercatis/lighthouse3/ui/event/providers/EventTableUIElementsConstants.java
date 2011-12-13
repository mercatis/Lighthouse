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
package com.mercatis.lighthouse3.ui.event.providers;

public class EventTableUIElementsConstants {

	public static enum ColumnType { CONTEXT, TIMESTAMP, CODE, LEVEL, TAGS, ORIGIN, MESSAGE, UDF, TRANSACTION }; 
	
	public static int transformColumnNameToFilterPropertyIndex(ColumnType type) {
		switch(type) {
			case CONTEXT:
				return EventFilterModel.DEPLOYMENT;
			case TIMESTAMP:
				return EventFilterModel.DATE;
			case CODE:
				return EventFilterModel.CODE;
			case LEVEL:
				return EventFilterModel.SEVERITY;
			case TAGS:
				return EventFilterModel.TAG;
			case ORIGIN:
				return EventFilterModel.ORIGIN;
			case MESSAGE:
				return EventFilterModel.MESSAGE;
			case UDF:
				return EventFilterModel.UDF;
			case TRANSACTION:
				return EventFilterModel.TRANSACTION_ID;
		}
		throw new RuntimeException("column not defined");
	}
	
	public static String getColumnText(ColumnType type) {
		switch(type) {
			case CONTEXT:
				return "Context";
			case TIMESTAMP:
				return "Date";
			case CODE:
				return "Code";
			case LEVEL:
				return "Level";
			case TAGS:
				return "Tags";
			case ORIGIN:
				return "Origin";
			case MESSAGE:
				return "Message";
			case UDF:
				return "UDFs";
			case TRANSACTION:
				return "Transaction IDs";
		}
		throw new RuntimeException("column not defined");
	}
}
