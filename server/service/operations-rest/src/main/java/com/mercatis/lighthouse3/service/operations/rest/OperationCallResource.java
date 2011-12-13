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
package com.mercatis.lighthouse3.service.operations.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.jms.Queue;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.mercatis.lighthouse3.commons.messaging.ActiveMQProvider;
import com.mercatis.lighthouse3.commons.messaging.JmsConnection;
import com.mercatis.lighthouse3.commons.messaging.JmsProvider;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.operations.OperationCall;
import com.mercatis.lighthouse3.domainmodel.operations.OperationCallException;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry;
import com.mercatis.lighthouse3.persistence.operations.hibernate.OperationInstallationRegistryImplementation;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityRestServiceContainer;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityRestServiceContainer.CreatingServiceContainer;
import com.mercatis.lighthouse3.service.jaas.util.LighthouseAuthorizator;

/**
 * This class provides a resource capturing operation calls in a RESTful way.
 * This establishes a REST web service to execute operation calls.
 */
@Path("/OperationCall")
public class OperationCallResource {

	public static final String JMS_PROVIDER = "com.mercatis.lighthouse3.service.operations.jms.JmsProvider";
	public static final String JMS_CONFIG_FILE_LOCATION = "com.mercatis.lighthouse3.service.operations.jms.JmsConfigFileLocation";
	public static final String JMS_CONFIG_RESOURCE = "com.mercatis.lighthouse3.service.operations.jms.JmsConfigResource";
	public static final String JMS_PROVIDER_URL = "JmsProvider.URL";
	public static final String JMS_PROVIDER_PASSWORD = "JmsProvider.Password";
	public static final String JMS_PROVIDER_USER = "JmsProvider.User";
	public static final String JMS_QUEUE_OPERATION_CALLS = "Queue.OperationCalls";

	/**
	 * This property keeps a logger.
	 */
	protected Logger log = Logger.getLogger(this.getClass());

	/**
	 * This property keeps a reference to the JMS provider to use.
	 */
	private static JmsProvider jmsProvider = null;

	/**
	 * This property keeps a reference to an open JMS connection for publishing
	 * operation call execution requests on a JMS queue.
	 */
	private static JmsConnection jmsConnection = null;

	/**
	 * This property refers to the queue for publishing operation call execution
	 * requests
	 */
	private static Queue queueOperationCalls = null;

	@CreatingServiceContainer
	protected DomainModelEntityRestServiceContainer creatingServiceContainer = null;

	/**
	 * This method retrieves an init parameter from the service container.
	 * 
	 * @param parameter
	 *            the init parameter to retrieve
	 * @return the value of the init parameter.
	 */
	private String getInitParameter(String parameter) {
		return this.creatingServiceContainer.getInitParameter(parameter);
	}

	/**
	 * This method sets up the JMS provider to use for publishing status
	 * updates.
	 */
	private void setUpJmsProvider() {
		if (this.getInitParameter(JMS_PROVIDER) != null) {
			try {
				jmsProvider = (JmsProvider) Class.forName(this.getInitParameter(JMS_PROVIDER)).newInstance();
			} catch (Exception e) {
				log.error(e);
			}
		} else {
			jmsProvider = new ActiveMQProvider();
		}
	}

	/**
	 * This method sets up the JMS connection and queue to use for publishing
	 * operation call execution requests.
	 */
	private void setUpJmsConnectionAndQueue() {
		Properties jmsConfig = new Properties();
		String initParameter = this.getInitParameter(JMS_CONFIG_RESOURCE);
		if (initParameter != null) {
			try {
				InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(initParameter);
				jmsConfig.load(resourceAsStream);
			} catch (IOException e) {
				log.error(e);
			}
		}

		if (this.getInitParameter(JMS_CONFIG_FILE_LOCATION) != null) {
			try {
				jmsConfig.load(new FileInputStream(this.getInitParameter(JMS_CONFIG_FILE_LOCATION)));
			} catch (Exception e) {
				log.error(e);
			}
		}

		if (jmsConfig.get(JMS_PROVIDER_URL) != null) {
			jmsProvider.setProviderUrl((String) jmsConfig.get(JMS_PROVIDER_URL));
		}

		if (jmsConfig.get(JMS_PROVIDER_USER) != null) {
			jmsProvider.setProviderUser((String) jmsConfig.get(JMS_PROVIDER_USER));
		}

		if (jmsConfig.get(JMS_PROVIDER_PASSWORD) != null) {
			jmsProvider.setProviderUserPassword((String) jmsConfig.get(JMS_PROVIDER_PASSWORD));
		}

		String queueOperationCall = "com.mercatis.lighthouse3.service.operations.calls";

		if (jmsConfig.get(JMS_QUEUE_OPERATION_CALLS) != null)
			queueOperationCall = (String) jmsConfig.get(JMS_QUEUE_OPERATION_CALLS);

		String clientId = null;
		try {
			clientId = InetAddress.getLocalHost().getHostAddress() + "#service-operations-calls#"
					+ System.currentTimeMillis();
			jmsProvider.setClientId(clientId);
		} catch (UnknownHostException e) {
			log.error(e);
		}

		jmsConnection = new JmsConnection(jmsProvider);
		queueOperationCalls = jmsProvider.getQueue(queueOperationCall);
	}

	private OperationInstallationRegistry getOperationInstallationRegistry() {
		OperationInstallationRegistryImplementation operationInstallationRegistry = (OperationInstallationRegistryImplementation) this.creatingServiceContainer
				.getDAO(OperationInstallationRegistry.class);

		synchronized (OperationCall.class) {
			if (jmsConnection == null) {
				this.setUpJmsProvider();
				this.setUpJmsConnectionAndQueue();
			}
		}

		if (jmsConnection != null)
			operationInstallationRegistry.setOperationExecutionConnection(jmsConnection);

		if (queueOperationCalls != null)
			operationInstallationRegistry.setOperationExecutionQueue(queueOperationCalls);

		return operationInstallationRegistry;
	}

	private SoftwareComponentRegistry getSoftwareComponentRegistry() {
		return this.creatingServiceContainer.getDAO(SoftwareComponentRegistry.class);
	}

	private DeploymentRegistry getDeploymentRegistry() {
		return this.creatingServiceContainer.getDAO(DeploymentRegistry.class);
	}

	/**
	 * This method implements the HTTP <code>POST</code> method of the operation
	 * call resource. It facilitates the execution of operation calls. It
	 * expects that the XML representation of the operation call to be executed
	 * is passed as the post request payload to the following URL pattern
	 * <code>/OperationCall</code>.
	 * 
	 * @param payload
	 *            receives the XML representation of the operation call to be
	 *            executed.
	 * @return a 415 code if the payload could not be parsed, a 500 code if the
	 *         opercation call could not be executed, a 200 code otherwise.
	 */
	@POST
	@Consumes("application/xml")
	@Produces("text/plain")
	public Response execute(String payload, @Context HttpServletRequest servletRequest) {
		if (log.isDebugEnabled())
			log.debug("Executing OperationCall using " + payload);

		OperationInstallationRegistry operationInstallationRegistry = this.getOperationInstallationRegistry();

		OperationCall operationCall = new OperationCall();

		try {
			operationCall.fromXml(payload, this.getSoftwareComponentRegistry(), this.getDeploymentRegistry(),
					operationInstallationRegistry);
		} catch (XMLSerializationException exception) {
			log.error("XML serialization exception caught while executing OperationCall", exception);

			return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).entity(exception.toString()).build();
		}

		try {
			Deployment d = operationCall.getTarget().getInstallationLocation(); 
			String context = "/Deployment/Deployment("+d.getDeployedComponent().getCode()+','+d.getLocation()+')';

	    	if (LighthouseAuthorizator.denyAccess(creatingServiceContainer, servletRequest.getRemoteUser(), "executeOperation", context))
	    		return Response.status(Status.UNAUTHORIZED).build();

			operationInstallationRegistry.execute(operationCall);

			if (log.isDebugEnabled())
				log.debug("OperationCall executed.");
		} catch (OperationCallException exception) {
			log.error("Operation call exception caught while executing OperationCall", exception);

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.toString()).build();
		}

		return Response.status(Status.OK).build();
	}
}
