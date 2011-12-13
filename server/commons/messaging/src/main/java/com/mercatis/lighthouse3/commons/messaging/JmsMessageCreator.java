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
package com.mercatis.lighthouse3.commons.messaging;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * This interface is intended to be implemented by anonymous classes in
 * conjunction with the <code>JmsConnection</code>. The implementation has
 * merely to create a JMS message given a JMS session, not taking care of
 * anything else.
 */
public interface JmsMessageCreator {

	/**
	 * Implement this method to return the JMS message you want to send.
	 * 
	 * @param jmsSession
	 *            the JMS session to use for message creation.
	 * @return the constructed message
	 * @throws JMSException
	 *             in case the message cannot be created.
	 */
	Message createMessage(Session jmsSession) throws JMSException;

}
