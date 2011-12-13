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

/**
 * Register as a listener in CronTriggerWidget to get hints for the current selected cron segment
 * 
 */
public interface CronExpressionWidgetListener {
	
	/**
	 * Fired when a cron segment is selected.
	 * 
	 * @param hint to display may be null (eg to reset in a wizard)
	 */
	public void updateHint(String hint);

	/**
	 * Fired when a cron segment was changed by user.
	 * <br />Complete expression followed by segments.
	 * 
	 * @param expression The comlpete expression
	 * @param second
	 * @param minute
	 * @param hour
	 * @param dayOfMonth
	 * @param month
	 * @param dayOfWeek
	 * @param year
	 */
	public void modified(String expression, String second, String minute, String hour, String dayOfMonth, String month, String dayOfWeek, String year);
}
