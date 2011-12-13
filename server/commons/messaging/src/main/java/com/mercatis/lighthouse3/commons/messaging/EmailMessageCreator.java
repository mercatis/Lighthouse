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

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * This interface should be implemented to create an email message to send given
 * a certain SMTP session.
 */
public interface EmailMessageCreator {

	/**
	 * Implement this method to return an email message that should be sent via
	 * an STMP connection.
	 * 
	 * @param smtpSession
	 *            the SMTP session for which to build the email.
	 * @return the email message.
	 * @throws MessagingException
	 *             in case of an error
	 */
	public MimeMessage createEmailMessage(Session smtpSession) throws MessagingException;
}
