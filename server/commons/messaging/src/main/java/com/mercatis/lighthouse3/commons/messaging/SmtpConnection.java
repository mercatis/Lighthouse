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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

/**
 * This class represents a connection to an SMTP server via which email messages
 * can be sent asynchronously.
 */
public class SmtpConnection {
	/**
	 * This property keeps a logger.
	 */
	protected Logger log = Logger.getLogger(this.getClass());

	/**
	 * This property keeps the SMTP connection properties as required by the
	 * Java Mail API.
	 */
	private Properties smtpConnectionProperties = new Properties();

	/**
	 * Keeps the user with whom to authenticate to the SMTP server, or
	 * <code>null</code> in case authentication is not necessary.
	 */
	private String user = null;

	/**
	 * Keeps the password of the user or <code>null</code> if no password is
	 * required.
	 */
	private String password = null;

	/**
	 * Keeps the SMTP protocol variant to use, either <code>smtp</code> or
	 * <code>smtps</code>
	 */
	private String protocol = null;

	/**
	 * Keeps a reference to the current SMTP transport
	 */
	private Transport smtpTransport = null;

	/**
	 * Keeps a reference to the current SMTP session.
	 */
	private Session smtpSession = null;

	private class EmailSender extends Thread {
		private boolean isActive = false;

		public void deactivate() {
			this.isActive = false;
		}

		public void activate() {
			this.isActive = true;
		}

		public void run() {
			while (true) {
				Runnable job = null;

				if (log.isDebugEnabled())
					log.debug("Waiting for email jobs.");

				try {
					job = emailJobs.take();
				} catch (InterruptedException e) {
					log.error("Could not fetch email job from queue");
				}

				if (log.isDebugEnabled())
					log.debug("EMail job fetched, executing");

				while (!this.isActive) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}

				job.run();

				if (!this.isActive)
					close();

				if (log.isDebugEnabled())
					log.debug("EMail job executed");
			}
		}
	}

	/**
	 * The worker thread performing the actual email send.
	 */
	private EmailSender emailSender = new EmailSender();

	/**
	 * This queue keeps a list of email jobs for the worker thread
	 */
	private BlockingQueue<Runnable> emailJobs = new LinkedBlockingQueue<Runnable>();

	/**
	 * This method really sends an email message as constructed by the given
	 * message creator to a bunch of recipients via SMTP.
	 * 
	 * @param recipients
	 *            the recipients' email addresses, separated by comma or
	 *            semicolon.
	 * @param messageCreator
	 *            the message creator used for creation the email message to
	 *            send.
	 */
	synchronized private void performSendToRecipients(String recipients, EmailMessageCreator messageCreator) {
		if (recipients == null) {
			log.warn("Encountered email send request with null recipients");
			return;
		}

		if (log.isDebugEnabled())
			log.debug("Sending email message to: " + recipients);

		if (log.isDebugEnabled())
			log.debug("Sending email message to: " + recipients);

		if (this.smtpTransport == null) {
			this.createSession();
		}

		List<Address> recipientAddresses = new ArrayList<Address>();
		StringTokenizer recipientsTokenizer = new StringTokenizer(recipients, ";,");

		while (recipientsTokenizer.hasMoreTokens()) {
			try {
				recipientAddresses.add(new InternetAddress(recipientsTokenizer.nextToken().trim()));
			} catch (AddressException e) {
				log.error("Invalid recipient email address given for recipient, message not sent.", e);
				return;
			}
		}

		try {
			if (log.isDebugEnabled())
				log.debug("Creating MIME message");

			MimeMessage messageToSend = messageCreator.createEmailMessage(this.smtpSession);

			if (messageToSend.getFrom() == null || messageToSend.getFrom().length == 0)
				messageToSend.setFrom();

			if (messageToSend.getContentType() == null)
				messageToSend.addHeader("Content-Type", "text/ascii");

			if (log.isDebugEnabled())
				log.debug("MIME message created. Sending message to recipients");

			for (Address recipient : recipientAddresses) {
				if (log.isDebugEnabled())
					log.debug("Sending message to recipient: " + recipient.toString());

				messageToSend.setRecipient(RecipientType.TO, recipient);

				Transport.send(messageToSend);
			}

			if (log.isDebugEnabled())
				log.debug("Message sent to recipients");

		} catch (MessagingException e) {
			log.error("Could not send email message. Recreating session and retrying after 10 secs", e);

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
			}

			this.close();
			this.createSession();

			this.performSendToRecipients(recipients, messageCreator);
		}

		if (log.isDebugEnabled())
			log.debug("Email message to " + recipients + " sent");
	}

	/**
	 * This method is called by clients to send an email message to recipients.
	 * The send is performed asynchronously.
	 * 
	 * @param recipients
	 *            the recipients' email addresses, separated by comma or
	 *            semicolon.
	 * @param messageCreator
	 *            the message creator used for creation the email message to
	 *            send.
	 */
	public void sendToRecipients(final String recipients, final EmailMessageCreator messageCreator) {
		if (log.isDebugEnabled())
			log.debug("Putting email job into queue");

		try {
			this.emailJobs.put(new Runnable() {
				public void run() {
					performSendToRecipients(recipients, messageCreator);
				}
			});
		} catch (InterruptedException e) {
			log.error("Could not create email job", e);
		}

		if (log.isDebugEnabled())
			log.debug("Email job in queue");
	}

	/**
	 * This method closes the open SMTP session to the configured server.
	 */
	synchronized public void close() {
		if (log.isDebugEnabled())
			log.debug("Closing SMTP session");

		if (Thread.currentThread() != this.emailSender)
			this.emailSender.deactivate();

		try {
			if (this.smtpTransport != null)
				this.smtpTransport.close();
		} catch (MessagingException e) {
			log.error("Could not close SMTP session", e);
		} finally {
			this.smtpTransport = null;
			this.smtpSession = null;
		}

		if (log.isDebugEnabled())
			log.debug("SMTP session closed");
	}

	/**
	 * This method opens an SMTP session to the configured server. If it could
	 * not be established the operation is retried after a sleep interval.
	 */
	synchronized public void createSession() {
		if (log.isDebugEnabled())
			log.debug("Opening SMTP session");

		this.smtpSession = Session.getInstance(this.smtpConnectionProperties, null);

		if (log.isDebugEnabled())
			log.debug("Establishing " + this.protocol.toUpperCase() + " transport");

		try {
			this.smtpTransport = this.smtpSession.getTransport(this.protocol);
		} catch (NoSuchProviderException e) {
			log.error("Invalid protocol for SMTP session given: " + this.protocol, e);
			log.error("Aborting session creation. SMTP connection object not usable.");

			this.smtpSession = null;

			return;
		}

		try {
			if (this.user != null) {
				if (log.isDebugEnabled())
					log.debug("Establishing transport with authentication");

				this.smtpTransport.connect(this.user, this.password);
			} else {
				if (log.isDebugEnabled())
					log.debug("Establishing transport without authentication");

				this.smtpTransport.connect();
			}
		} catch (MessagingException e) {
			log.error("Could not establish " + this.protocol.toUpperCase() + " transport. Retrying in 10 secs", e);

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
			}

			this.smtpSession = null;
			this.smtpTransport = null;

			this.createSession();
		}

		if (Thread.currentThread() != this.emailSender)
			this.emailSender.activate();

		if (log.isDebugEnabled())
			log.debug(this.protocol.toUpperCase() + "transport established");

		if (log.isDebugEnabled())
			log.debug("SMTP session opened");
	}

	/**
	 * The constructor creating and opening an SMTP session.
	 * 
	 * @param protocol
	 *            the SMTP protocol variant to use, either <code>smtp</code> or
	 *            <code>smtps</code>
	 * @param smtpServer
	 *            the SMTP server to use for sending mail messages
	 * @param serverPort
	 *            the port on which the SMTP server is listening, 25 is normal
	 * @param user
	 *            the user with whom to authenticate to the SMTP server, or
	 *            <code>null</code> in case authentication is not necessary.
	 * @param password
	 *            the password of the user or <code>null</code> if no password
	 *            is required.
	 * @param senderEmailAddress
	 *            the identifier to use for the sender of the email
	 */
	public SmtpConnection(String protocol, String smtpServer, int serverPort, String user, String password,
			String senderEmailAddress) {

		if ("smtp".equals(protocol) || "smtps".equals(protocol))
			this.protocol = protocol;
		else
			this.protocol = "smtp";

		this.smtpConnectionProperties.put("mail." + protocol + ".host", smtpServer);
		this.smtpConnectionProperties.put("mail." + protocol + ".port", serverPort);

		if (user != null) {
			this.user = user;
			this.password = password;
			this.smtpConnectionProperties.put("mail." + protocol + ".auth", "true");
			this.smtpConnectionProperties.put("mail." + protocol + ".user", user);
		}

		this.smtpConnectionProperties.put("mail.from", senderEmailAddress);

		this.emailSender.start();

		this.createSession();
	}
}
