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
package com.mercatis.lighthouse3.domainmodel.status;

/**
 * This class captures exceptions wrt. status change notification channels.
 */
public class StatusChangeNotificationChannelException extends RuntimeException {

	private static final long serialVersionUID = 1940375671680876044L;

	public StatusChangeNotificationChannelException(String message, Exception cause) {
		super(message, cause);
	}
}
