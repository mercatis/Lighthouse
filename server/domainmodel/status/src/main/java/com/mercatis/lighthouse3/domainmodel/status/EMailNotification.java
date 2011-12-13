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

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.messaging.EmailMessageCreator;
import com.mercatis.lighthouse3.domainmodel.events.Event;

/**
 * This class provides an e-mail notification channel about changes of a status.
 */
public class EMailNotification extends StatusChangeNotificationChannel<EMailNotification, EMailNotificationChannelConfiguration> {
	
	protected Logger log = Logger.getLogger(this.getClass());

	/**
	 * This property stores the recipients of email notifications about changes
	 * to a status. Multiple recipients are separated by comma.
	 */
	private String recipients = null;

	/**
	 * The method returns the recipients of email notifications about changes to
	 * a status. Multiple recipients are separated by comma.
	 * 
	 * @return the email recipients.
	 */
	public String getRecipients() {
		return recipients;
	}

	/**
	 * The method sets the recipients of email notifications about changes to a
	 * status. Multiple recipients are separated by comma.
	 * 
	 * @param recipients
	 *            the email recipients to set.
	 */
	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}

	/**
	 * This property keeps the email body mime type. The default is "text/html".
	 */
	private String bodyMimeType = "text/html";

	/**
	 * This method returns the mime type of the email body.
	 * 
	 * @return the body mime type
	 */
	public String getBodyMimeType() {
		return this.bodyMimeType;
	}

	/**
	 * This method sets the mime type of the email body.
	 * 
	 * @param mimeType
	 *            the mime type of the email body.
	 */
	public void setBodyMimeType(String mimeType) {
		this.bodyMimeType = mimeType;
	}

	/**
	 * This property keeps a velocity template that will create a title out of
	 * the changed status passed in the variable <code>$status</code>. The
	 * potential event that triggered the status change is passed in the
	 * variable <code>$event</code>.
	 */
	private String titleTemplate = null;

	/**
	 * This method returns the velocity template used to create a title out of
	 * the changed status passed in the variable <code>$status</code>. The
	 * potential event that triggered the status change is passed in the
	 * variable <code>$event</code>.
	 * 
	 * @return the template for the title
	 */
	public String getTitleTemplate() {
		return titleTemplate;
	}

	/**
	 * This method sets the velocity template used to create a title out of the
	 * changed status passed in the variable <code>$status</code>. The potential
	 * event that triggered the status change is passed in the variable
	 * <code>$event</code>.
	 * 
	 * @param titleTemplate
	 *            the template for the mail title.
	 */
	public void setTitleTemplate(String titleTemplate) {
		this.titleTemplate = titleTemplate;
	}

	/**
	 * This property keeps a velocity template that will create a mail body out
	 * of the changed status passed in the variable <code>$status</code>. The
	 * potential event that triggered the status change is passed in the
	 * variable <code>$event</code>.
	 */
	private String bodyTemplate = null;

	/**
	 * This method returns the velocity template used to create the mail body
	 * out of the changed status passed in the variable <code>$status</code>.
	 * The potential event that triggered the status change is passed in the
	 * variable <code>$event</code>.
	 * 
	 * @return the template for the mail body.
	 */
	public String getBodyTemplate() {
		return bodyTemplate;
	}

	/**
	 * This method sets the velocity template used to create the mail body out
	 * of the changed status passed in the variable <code>$status</code>. The
	 * potential event that triggered the status change is passed in the
	 * variable <code>$event</code>.
	 * 
	 * @param bodyTemplate
	 *            the template for the mail body
	 */
	public void setBodyTemplate(String bodyTemplate) {
		this.bodyTemplate = bodyTemplate;
	}

	/**
	 * This static property keeps a reference to the single velocity engine to
	 * use.
	 */
	static private VelocityEngine velocityEngine = null;

	/**
	 * This method renders a given Velocity template against a status passed in
	 * the variable <code>$status</code>. The potential event that triggered the
	 * status change is passed in the variable <code>$event</code>.
	 * 
	 * @param template
	 *            the template to render
	 * @param status
	 *            the status to pass to the template
	 * @param event
	 *            the event that triggered the status change, in case there was
	 *            one. <code>null</code> in case the status change was not
	 *            triggered by an event.
	 * @return the rendition
	 * @throws IllegalArgumentException
	 *             in case of a Velocity error.
	 */
	@SuppressWarnings("deprecation")
	private String renderVelocityTemplateForStatus(String template, Status status, Event event) {
		if (status == null) {
			throw new IllegalArgumentException("Stopped rendering template with null status.");
		}
		StringWriter rendition = new StringWriter();

		try {
			if (velocityEngine == null) {
				velocityEngine = new VelocityEngine();
				velocityEngine.init();
			}

			VelocityContext context = new VelocityContext();
			
			// Ensure that the complete status change history is loaded. otherwise we might run
        	// into an exception because the current hibernate session is already closed when accessed.
			status.getChangeHistory();
			context.put("status", status);
			String name = status.getLongName() != null && status.getLongName().length() > 0
				? status.getLongName()
				: status.getCode();
			context.put("name", name);
			context.put("contextPath", status.getContext().getPath());
			StatusChange current = (StatusChange) status.getCurrent().clone();
			context.put("current", current);
			context.put("changeType", current.getClass().getSimpleName());
                        if (event == null && current instanceof EventTriggeredStatusChange) {
                            log.error("Trying to render notification template without event.");
			}
			context.put("event", event);

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.mmm");
			context.put("dateFormat", dateFormat);

			velocityEngine.evaluate(context, rendition, "email-template-generation", template);

		} catch (Exception e) {
			throw new IllegalArgumentException("Velocity error occurred while rendering template", e);
		}

		return rendition.toString();
	}

	/**
	 * This method returns the e-mail title created by applying the title
	 * velocity template to the status that changed passed in the variable
	 * <code>$status</code>. The potential event that triggered the status
	 * change is passed in the variable <code>$event</code>.
	 * 
	 * @param status
	 *            the status that changed
	 * @param event
	 *            the event that triggered the status change, in case there was
	 *            one. <code>null</code> in case the status change was not
	 *            triggered by an event.
	 * @return the e-mail title, or <code>null</code> if no title template has
	 *         been defined.
	 * @throws IllegalArgumentException
	 *             in case that the rendition of the velocity template yielded
	 *             an error.
	 */
	public String getTitle(Status status, Event event) {
		if (log.isDebugEnabled()) {
			log.debug("Rendering EMail Title - Status: " + status);
			log.debug("Rendering EMail Title - Event:  " + event);
		}
		if (this.getTitleTemplate() != null)
			return this.renderVelocityTemplateForStatus(this.getTitleTemplate(), status, event);
		else
			return null;
	}

	/**
	 * This method returns the e-mail body created by applying the body velocity
	 * template to the status that changed passed in the variable
	 * <code>$status</code>.
	 * 
	 * @param status
	 *            the status that changed.
	 * @param event
	 *            the event that triggered the status change, in case there was
	 *            one. <code>null</code> in case the status change was not
	 *            triggered by an event.
	 * @return the e-mail body, or <code>null</code> if no title template has
	 *         been defined.
	 * @throws IllegalArgumentException
	 *             in case that the rendition of the velocity template yielded
	 *             an error.
	 */
	public String getBody(Status status, Event event) {
		if (log.isDebugEnabled()) {
			log.debug("Rendering EMail Body - Status: " + status);
			log.debug("Rendering EMail Body - Event:  " + event);
		}
		if (this.getBodyTemplate() != null)
			return this.renderVelocityTemplateForStatus(this.getBodyTemplate(), status, event);
		else
			return null;
	}

	@Override
	protected void fillStatusChangeNotificationChannelElement(XmlWriter xml) throws IOException {
		super.fillStatusChangeNotificationChannelElement(xml);

		if (this.getRecipients() != null)
			xml.writeEntityWithText("recipients", this.getRecipients());

		if (this.getBodyMimeType() != null)
			xml.writeEntityWithText("bodyMimeType", this.getBodyMimeType());

		if (this.getTitleTemplate() != null)
			xml.writeEntityWithText("titleTemplate", this.getTitleTemplate());

		if (this.getBodyTemplate() != null)
			xml.writeEntityWithText("bodyTemplate", this.getBodyTemplate());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bodyMimeType == null) ? 0 : bodyMimeType.hashCode());
		result = prime * result + ((bodyTemplate == null) ? 0 : bodyTemplate.hashCode());
		result = prime * result + ((recipients == null) ? 0 : recipients.hashCode());
		result = prime * result + ((titleTemplate == null) ? 0 : titleTemplate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EMailNotification other = (EMailNotification) obj;
		if (bodyMimeType == null) {
			if (other.bodyMimeType != null)
				return false;
		} else if (!bodyMimeType.equals(other.bodyMimeType))
			return false;
		if (bodyTemplate == null) {
			if (other.bodyTemplate != null)
				return false;
		} else if (!bodyTemplate.equals(other.bodyTemplate))
			return false;
		if (recipients == null) {
			if (other.recipients != null)
				return false;
		} else if (!recipients.equals(other.recipients))
			return false;
		if (titleTemplate == null) {
			if (other.titleTemplate != null)
				return false;
		} else if (!titleTemplate.equals(other.titleTemplate))
			return false;
		return true;
	}

	@Override
	public void statusChanged(final EMailNotificationChannelConfiguration channelConfiguration, final StatusChange statusChange) {
		Event event = null;
		try {
		    if (statusChange.clone() instanceof EventTriggeredStatusChange) {
			    event = ((EventTriggeredStatusChange) statusChange.clone()).getTriggeringEvent();
			    if (event == null) {
				log.error("EventTriggeredStatusChanges does not provide an Event");
			    }
		    }
		} catch (CloneNotSupportedException ex) {
		    log.error("Could not clone status change", ex);
		}
		
		String title = null;
		String body = null;
		try {
			title = getTitle(statusChange.getStatus(), event);
		} catch (Exception ex) {
                        String statusCode = statusChange.getStatus().getCode();
                        String errorMessage = String.format("Error occurred while rendering email title template for Status %s", statusCode);
			log.error(errorMessage, ex);
			title = errorMessage;
		}
		try {
			body = getBody(statusChange.getStatus(), event);
		} catch (Exception ex) {
                        String statusCode = statusChange.getStatus().getCode();
                        String errorMessage = String.format("Error occurred while rendering email body template for Status %s", statusCode);
			log.error(errorMessage, ex);
			body = errorMessage;
		}
		
		final String titleText = title;
		final String bodyText = body;
		
		channelConfiguration.sendToRecipients(this.getRecipients(), new EmailMessageCreator() {

				public MimeMessage createEmailMessage(Session smtpSession) throws MessagingException {
					MimeMessage message = new MimeMessage(smtpSession);
					message.setSubject(titleText);
					message.setText(bodyText);
					message.setHeader("Content-Type", getBodyMimeType());

					return message;
				}
			});
	}
}
