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
package com.mercatis.lighthouse3.api.statuschange;

import com.mercatis.lighthouse3.domainmodel.status.StatusChange;

/**
 * This interface has to be implemented by anyone interested in notifications
 * about status changes via a status change monitor.
 */
public interface StatusChangeListener {

	/**
	 * This method is called by status change monitors on any registered
	 * listener whenever a status changed. It passes a variety of information.
	 * 
	 * @param statusPath
	 *            the path identifier of the status that changed
	 * @param statusCode
	 *            the code of the status that changed
	 * @param statusChange
	 *            the status change. Depending on the kind of monitor with which
	 *            the listener is registered, the status change may be fully
	 *            resolved pointing to the status it belongs to or not resolved,
	 *            where the client is responsible for obtain any more
	 *            information about the status.
	 * @param priorChange
	 *            the last status change that occurred before this notification.
	 *            Can be <code>null</code> Depending on the kind of monitor with
	 *            which the listener is registered, the status change may be
	 *            fully resolved pointing to the status it belongs to or not
	 *            resolved, where the client is responsible for obtain any more
	 *            information about the status.
	 */
	public void statusChanged(String statusPath, String statusCode, StatusChange statusChange, StatusChange priorChange);
}
