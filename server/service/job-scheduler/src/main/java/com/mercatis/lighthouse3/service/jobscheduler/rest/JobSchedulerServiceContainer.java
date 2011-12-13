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
package com.mercatis.lighthouse3.service.jobscheduler.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import javax.jms.Queue;

import com.mercatis.lighthouse3.commons.messaging.ActiveMQProvider;
import com.mercatis.lighthouse3.commons.messaging.JmsProvider;
import com.mercatis.lighthouse3.domainmodel.operations.JobRegistry;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry;
import com.mercatis.lighthouse3.service.commons.rest.HibernateDomainModelEntityRestServiceContainer;
import com.mercatis.lighthouse3.service.jobscheduler.JobScheduler;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;

/**
 * This class provides a servlet container around the job scheduler service.
 */
public class JobSchedulerServiceContainer extends HibernateDomainModelEntityRestServiceContainer {
	private static final long serialVersionUID = 526853771461931229L;

	public static final String JMS_PROVIDER = "com.mercatis.lighthouse3.service.jobscheduler.jms.JmsProvider";
	public static final String JMS_CONFIG_FILE_LOCATION = "com.mercatis.lighthouse3.service.jobscheduler.jms.JmsConfigFileLocation";
	public static final String JMS_CONFIG_RESOURCE = "com.mercatis.lighthouse3.service.jobscheduler.jms.JmsConfigResource";
	public static final String JMS_PROVIDER_URL = "JmsProvider.URL";
	public static final String JMS_PROVIDER_PASSWORD = "JmsProvider.Password";
	public static final String JMS_PROVIDER_USER = "JmsProvider.User";
	public static final String QUEUE_OPERATION_CALLS = "Queue.OperationCalls";
	public static final String JOB_SCHEDULER_INSTANCES_NUMBER = "com.mercatis.lighthouse3.service.jobscheduler.instances.number";
	public static final String JOB_SCHEDULER_INSTANCES_CURRENT = "com.mercatis.lighthouse3.service.jobscheduler.instances.current";

	/**
	 * This property keeps a reference to the JMS provider to use.
	 */
	private JmsProvider jmsProvider = null;

	/**
	 * This property refers to the queue for issuing operation calls.
	 */
	private Queue queueOperationCalls = null;

	/**
	 * This method sets up the JMS provider to use.
	 */
	private void setUpJmsProvider() {
		if (this.getInitParameter(JMS_PROVIDER) != null) {
			try {
				this.jmsProvider = (JmsProvider) Class.forName(this.getInitParameter(JMS_PROVIDER)).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			this.jmsProvider = new ActiveMQProvider();
		}
	}

	/**
	 * This method sets up the JMS connection and destinations to use for
	 * monitoring job update notifications and executing operation calls.
	 */
	private void setUpJmsProviderAndDestinations() {
		Properties jmsConfig = new Properties();
		if (configuration != null) {
			jmsConfig = this.configuration;
		} else if (this.getInitParameter(JMS_CONFIG_RESOURCE) != null) {
			try {
				InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(this.getInitParameter(JMS_CONFIG_RESOURCE));
				jmsConfig.load(resourceAsStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (this.getInitParameter(JMS_CONFIG_FILE_LOCATION) != null) {
			try {
				jmsConfig.load(new FileInputStream(this.getInitParameter(JMS_CONFIG_FILE_LOCATION)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (jmsConfig.get(JMS_PROVIDER_URL) != null) {
			this.jmsProvider.setProviderUrl((String) jmsConfig.get(JMS_PROVIDER_URL));
		}

		if (jmsConfig.get(JMS_PROVIDER_USER) != null) {
			this.jmsProvider.setProviderUser((String) jmsConfig.get(JMS_PROVIDER_USER));
		}

		if (jmsConfig.get(JMS_PROVIDER_PASSWORD) != null) {
			this.jmsProvider.setProviderUserPassword((String) jmsConfig.get(JMS_PROVIDER_PASSWORD));
		}

		String queueOperationCalls = "com.mercatis.lighthouse3.service.operations.calls";
		if (jmsConfig.get(QUEUE_OPERATION_CALLS) != null)
			queueOperationCalls = (String) jmsConfig.get(QUEUE_OPERATION_CALLS);

		this.queueOperationCalls = this.jmsProvider.getQueue(queueOperationCalls);
	}

	/**
	 * This property keeps a reference to the job scheduler service.
	 */
	private JobScheduler jobSchedulerService = null;

	/**
	 * This method returns the job scheduler service instance.
	 * 
	 * @return the job service instance.
	 */
	public JobScheduler getJobScheduler() {
		return this.jobSchedulerService;
	}

	/**
	 * This method sets up the status monitor service.
	 */
	private void setUpJobSchedulerService() {
		int jobSchedulerInstancesNumber = 1;
		int jobSchedulerInstancesCurrent = 1;

		if (this.getInitParameter(JOB_SCHEDULER_INSTANCES_NUMBER) != null)
			jobSchedulerInstancesNumber = Integer.parseInt(this.getInitParameter(JOB_SCHEDULER_INSTANCES_NUMBER));

		if (this.getInitParameter(JOB_SCHEDULER_INSTANCES_CURRENT) != null)
			jobSchedulerInstancesCurrent = Integer.parseInt(this.getInitParameter(JOB_SCHEDULER_INSTANCES_CURRENT));

		this.jobSchedulerService = new JobScheduler(jobSchedulerInstancesCurrent, jobSchedulerInstancesNumber, this,
				this.getDAO(JobRegistry.class), this.getDAO(OperationInstallationRegistry.class), this.jmsProvider,
				this.queueOperationCalls);
	}

	@Override
	protected void configure(Map<String,String> initParams, ResourceConfig rc, WebApplication wa) {
		super.configure(initParams, rc, wa);

		this.setUpJmsProvider();
		this.setUpJmsProviderAndDestinations();
		this.setUpJobSchedulerService();
	}

	@Override
	public void destroy() {
		if (this.jobSchedulerService != null) {
			this.jobSchedulerService.stop();
		}
		super.destroy();
	}
	
	public String getInitParameter(String parameter) {
		if(this.configuration != null) {
			return configuration.getProperty(parameter);
		}
		return super.getInitParameter(parameter);
	}
}
