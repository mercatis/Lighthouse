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
package com.mercatis.lighthouse3.ui.event.wizards.pages;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import com.mercatis.lighthouse3.base.ui.wizards.AbstractWizardPage;
import com.mercatis.lighthouse3.commons.messaging.JmsProvider;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration;
import com.mercatis.lighthouse3.ui.event.base.services.EventService;


public class WizardNewLighthouseDomainJMSPage extends AbstractWizardPage {

	public WizardNewLighthouseDomainJMSPage() {
		super("JMSPage");
	}
	
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;

	private Text jmsProviderUrlField;
	private String jmsProviderUrlFieldValue;
	
	private Combo jmsProviderField;
	private String jmsProviderFieldValue;
	
	private Text jmsUpdateTopicField;
	private String jmsUpdateTopicFieldValue;

	private Text jmsUsernameField;
	private String jmsUsernameFieldValue;
	
	private Text jmsPasswordField;
	private String jmsPasswordFieldValue;
	
	private LighthouseDomain lighthouseDomain;

	private Listener modifyListener = new Listener() {
		public void handleEvent(Event e) {
			boolean valid = validatePage();
			setPageComplete(valid);
		}
	};
	
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		initializeDialogUnits(parent);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createLighthouseLocationGroup(composite);
		setValues();
		
		setPageComplete(validatePage());
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		Dialog.applyDialogFont(composite);
	}

	@Override
	public void prefillProperty(Object key, Object value) {
		if (key instanceof String && value != null) {
			String property = (String) key;
			if (property.equals("DOMAIN_JMS_USER")) {
				jmsUsernameFieldValue = (String)value;
			} else if (property.equals("DOMAIN_EVENTS_PUBLICATION_TOPIC")) {
				jmsUpdateTopicFieldValue = (String)value;
			} else if (key.equals("DOMAIN_JMS_PASSWORD")) {
				jmsPasswordFieldValue = (String)value;
			} else if (key.equals("DOMAIN_JMS_BROKER_URL")) {
				jmsProviderUrlFieldValue = (String)value;
			} else if (key.equals("DOMAIN_JMS_PROVIDER_CLASS")) {
				jmsProviderFieldValue = (String)value;
			}
		}
	}

	private void setValues()  {
		if(lighthouseDomain != null) {
			jmsProviderUrlField.setText(CommonBaseActivator.getPlugin().getEventService().getEventConfiguration(lighthouseDomain).getJmsBrokerUrl());
			jmsProviderField.setText(CommonBaseActivator.getPlugin().getEventService().getEventConfiguration(lighthouseDomain).getJmsProviderClass());
			jmsUpdateTopicField.setText(CommonBaseActivator.getPlugin().getEventService().getEventConfiguration(lighthouseDomain).getEventsPublicationTopic());
			jmsUsernameField.setText(CommonBaseActivator.getPlugin().getEventService().getEventConfiguration(lighthouseDomain).getJmsUser());
			jmsPasswordField.setText(CommonBaseActivator.getPlugin().getEventService().getEventConfiguration(lighthouseDomain).getJmsPassword());
		} else {
			if (jmsProviderUrlFieldValue != null)
				jmsProviderUrlField.setText(jmsProviderUrlFieldValue);
			if (jmsProviderFieldValue != null)
				jmsProviderField.setText(jmsProviderFieldValue);
			if (jmsUpdateTopicFieldValue != null)
				jmsUpdateTopicField.setText(jmsUpdateTopicFieldValue);
			if (jmsUsernameFieldValue != null)
				jmsUsernameField.setText(jmsUsernameFieldValue);
			if (jmsPasswordFieldValue != null)
				jmsPasswordField.setText(jmsPasswordFieldValue);
		}
	}
	
	private void createLighthouseLocationGroup(Composite parent) {

		// common layout data
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;

		// lighthouse service group
		Group jmsGroup = new Group(parent, SWT.NONE);
		jmsGroup.setText("JMS settings");
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		jmsGroup.setLayout(layout);
		jmsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new provider url label
		Label jmsProviderUrlLabel = new Label(jmsGroup, SWT.NONE);
		jmsProviderUrlLabel.setText("URL");
		jmsProviderUrlLabel.setFont(parent.getFont());

		// new jms provider url text field
		jmsProviderUrlField = new Text(jmsGroup, SWT.BORDER);
		jmsProviderUrlField.setLayoutData(data);
		jmsProviderUrlField.setFont(parent.getFont());
		jmsProviderUrlField.setText("tcp://localhost:61616");
		jmsProviderUrlField.addListener(SWT.Modify, modifyListener);
		jmsProviderUrlField.setFocus();

		// new jms provider
		Label jmsProviderLabel = new Label(jmsGroup, SWT.NONE);
		jmsProviderLabel.setText("Provider Class");
		jmsProviderLabel.setFont(parent.getFont());

		// new jms provider
		jmsProviderField = new Combo(jmsGroup, SWT.BORDER | SWT.DROP_DOWN);
		jmsProviderField.setLayoutData(data);
		jmsProviderField.setFont(parent.getFont());
		jmsProviderField.add("com.mercatis.lighthouse3.commons.messaging.ActiveMQProvider");
		jmsProviderField.add("com.mercatis.lighthouse3.commons.messaging.EMSProvider");
		jmsProviderField.add("com.mercatis.lighthouse3.commons.messaging.HornetQMessagingProvider");
		jmsProviderField.add("com.mercatis.lighthouse3.commons.messaging.JBossClusteredMessagingProvider");
		jmsProviderField.add("com.mercatis.lighthouse3.commons.messaging.JBossMessagingProvider");
		jmsProviderField.select(0);
		jmsProviderField.addListener(SWT.Modify, modifyListener);

		// new username label
		Label usernameLabel = new Label(jmsGroup, SWT.NONE);
		usernameLabel.setText("Username");
		usernameLabel.setFont(parent.getFont());

		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;

		// new username text field
		jmsUsernameField = new Text(jmsGroup, SWT.BORDER);
		jmsUsernameField.setLayoutData(data);
		jmsUsernameField.setFont(parent.getFont());
		jmsUsernameField.addListener(SWT.Modify, modifyListener);

		// new password label
		Label passwordLabel = new Label(jmsGroup, SWT.NONE);
		passwordLabel.setText("Password");
		passwordLabel.setFont(parent.getFont());

		// new password text field
		jmsPasswordField = new Text(jmsGroup, SWT.BORDER | SWT.PASSWORD);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		jmsPasswordField.setLayoutData(data);
		jmsPasswordField.setFont(parent.getFont());
		jmsPasswordField.addListener(SWT.Modify, modifyListener);

		// new jms provider label
		Label jmsUpdateTopicLabel = new Label(jmsGroup, SWT.NONE);
		jmsUpdateTopicLabel.setText("Event Notification Topic");
		jmsUpdateTopicLabel.setFont(parent.getFont());

		// new jms provider field
		jmsUpdateTopicField = new Text(jmsGroup, SWT.BORDER);
		jmsUpdateTopicField.setLayoutData(data);
		jmsUpdateTopicField.setFont(parent.getFont());
		jmsUpdateTopicField.setText("com.mercatis.lighthouse3.service.eventlogger.events.notification");
		jmsUpdateTopicField.addListener(SWT.Modify, modifyListener);

		Button testB = new Button(parent, SWT.PUSH);
		testB.setText("Test Connection");
		testB.setLayoutData(new GridData(SWT.END, SWT.TOP, true, false));
		testB.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				Connection conn = null;
				try {
					Class<?> connClass = Class.forName(getJmsProvider());
					JmsProvider prov = (JmsProvider) connClass.newInstance();
					prov.setProviderUrl(getJmsProviderUrl());
					String user = getUsername();
					String pw = getPassword();
					if (user!=null && user.length()>0)
						prov.setProviderUser(user);
					if (pw!=null && pw.length()>0)
						prov.setProviderUserPassword(pw);
					ConnectionFactory cf = prov.getConnectionFactory();
					conn = cf.createConnection();
					MessageDialog.openInformation(getShell(), "Connection Test", "Connection test successful!");
				} catch (Exception e1) {
					MessageDialog.openError(getShell(), "Connection Test", "Connection test failed!\n\n"+e1.getMessage());
				} finally {
					if (conn!=null)
						try { conn.close(); } catch (JMSException e1) { /* ignore */ }
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) { /* ignore */ }
		});
	}

	public String getJmsProviderUrl() {
		return jmsProviderUrlField.getText();
	}

	public String getJmsProvider() {
		return jmsProviderField.getText();
	}

	public String getPassword() {
		return jmsPasswordField.getText();
	}

	public String getUsername() {
		return jmsUsernameField.getText();
	}

	public String getJmsUpdateTopic() {
		return jmsUpdateTopicField.getText();
	}

	protected boolean validatePage() {
		if (jmsProviderUrlField.getText().equals("")) {
			setErrorMessage(null);
			setMessage("Please enter URL");
			return false;
		}
		if (jmsProviderField.getText().equals("")) {
			setErrorMessage(null);
			setMessage("Please enter a provider class");
			return false;
		}
		if (jmsUpdateTopicField.getText().equals("")) {
			setErrorMessage(null);
			setMessage("Please enter a topic");
			return false;
		}
		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	public void setPropertiesFor(Object o) {
		if (o instanceof LighthouseDomain) {
			LighthouseDomain lighthouseDomain = (LighthouseDomain)o;
			EventService eventService = CommonBaseActivator.getPlugin().getEventService();
			eventService.addEventNature(lighthouseDomain);
			EventConfiguration eventConfiguration = eventService.getEventConfiguration(lighthouseDomain);
			eventConfiguration.setJmsProviderClass(getJmsProvider());
			eventConfiguration.setJmsBrokerUrl(getJmsProviderUrl());
			eventConfiguration.setEventsPublicationTopic(getJmsUpdateTopic());
			eventConfiguration.setJmsUser(getUsername());
			eventConfiguration.setJmsPassword(getPassword());
		}
	}
}
