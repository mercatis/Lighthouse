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

import org.apache.log4j.Logger;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Message;

/**
 * This class provides a convenient encapsulation of the over engineered JMS
 * connection factory, connection, session, message producer cascade as a simple
 * connection, that also supports reconnection in case of errors.
 */
public class JmsConnection {

    /**
     * This constant defines the Thread.sleep period between retries (open connection, open session, send messages)
     */
    private static final long SLEEP_BETWEEN_RETRIES_MILLIS = 10000l;

    /**
     * This property keeps a logger.
     */
    protected Logger log = Logger.getLogger(this.getClass());

    /**
     * This property keeps a reference to the open JMS connection.
     */
    private Connection jmsConnection = null;

    /**
     * This property keeps a reference to the JmsProvider where the jmsConnection has been obtained from.
     */
    private JmsProvider jmsProvider = null;

    /**
     * This property keeps a JMS session open.
     */
    private Session jmsSession = null;

    /**
     * This property controls exception handling while session creation.
     */
    private boolean exceptOnError = false;

    /**
     * This method opens a JMS connection and retries upon failure.
     * @throws JMSException 
     */
    private void openConnection() throws JMSException {
        if (log.isDebugEnabled()) {
            log.debug("Opening JMS connection");
        }

        while (this.jmsConnection == null) {
            try {
                this.jmsConnection = null;
                this.jmsConnection = jmsProvider.getConnectionFactory().createConnection();

                if (this.jmsConnection == null) {
                    throw new JMSException("JmsProvider returned <null> connection.");
                }
            } catch (JMSException exception) {

                this.jmsConnection = null;

                log.error("Failed to create JMS connection (ClientID: " + this.jmsProvider.getClientId() + "). Retrying in " + SLEEP_BETWEEN_RETRIES_MILLIS / 1000l + " secs", exception);

                if (exceptOnError)
                	throw exception;
                
                try {
                    Thread.sleep(SLEEP_BETWEEN_RETRIES_MILLIS);
                } catch (InterruptedException e) {
                }

            }
        }

        if (log.isDebugEnabled()) {
            log.debug("JMS connection opened");
        }
    }

    /**
     * This method creates a session and retries upon failure.
     * @throws JMSException 
     */
    private void createSession() throws JMSException {
        if (log.isDebugEnabled()) {
            log.debug("Creating JMS session.");
        }

        while (this.jmsSession == null) {
            // obtain jmsConnection..
            if (this.jmsConnection == null) {
                this.openConnection();
            }

            try {
                // obtain jmsSession..
                this.jmsSession = this.jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                if (this.jmsSession == null) {
                    throw new JMSException("JmsConnection returned <null> session.");
                }
            } catch (JMSException e) {
                log.error("Failed to create JMS session. Closing JMS connection.", e);

                // close and clear underlying jmsConnection
                try {
                    this.jmsConnection.close();
                } catch (JMSException e1) {
                }
                this.jmsConnection = null;

                if (exceptOnError)
                	throw e;
                
                // wait 10 seconds
                log.error("Retrying in " + SLEEP_BETWEEN_RETRIES_MILLIS / 1000l + " secs.");
                try {
                    Thread.sleep(SLEEP_BETWEEN_RETRIES_MILLIS);
                } catch (InterruptedException e2) {
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("JMS session created.");
        }
    }

    /**
     * Use this method to send a JMS message to the given connection. Sending is
     * retried in case of exceptions.
     *
     * @param topicOrQueue   the topic or queue onto which to send the message.
     * @param messageCreator a message creator implementation that creates the message.
     */
    public void sendToDestination(Destination topicOrQueue, JmsMessageCreator messageCreator) {
        this.sendToDestination(topicOrQueue, messageCreator, Message.DEFAULT_TIME_TO_LIVE);
    }

    /**
     * Use this method to send a JMS message to the given connection. Sending is
     * retried in case of exceptions.
     *
     * @param topicOrQueue   the topic or queue onto which to send the message.
     * @param messageCreator a message creator implementation that creates the message.
     * @param timeToLive     The message producer's default time to live is unlimited; the message never expires.
     */
    synchronized public void sendToDestination(Destination topicOrQueue, JmsMessageCreator messageCreator, long timeToLive) {
        boolean successfulSent = false;

        if (log.isDebugEnabled()) {
            log.debug("Sending JMS message.");
        }

        while (!successfulSent) {
        	MessageProducer messageProducer = null;
            try {
            	if (this.jmsSession == null) {
            		this.createSession();
            	}
            	
                if (log.isDebugEnabled()) {
                    log.debug("Creating JMS message producer.");
                }
                messageProducer = this.jmsSession.createProducer(topicOrQueue);
                messageProducer.setTimeToLive(timeToLive);

                if (log.isDebugEnabled()) {
                    log.debug("Sending JMS message via producer.");
                }
                messageProducer.send(messageCreator.createMessage(this.jmsSession));
                successfulSent = true;
                if (log.isDebugEnabled()) {
                    log.debug("JMS message sent.");
                }

                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Closing JMS message producer.");
                    }
                    messageProducer.close();
                } catch (JMSException e) {
                    log.error("Message producer failed to close.", e);
                }

            } catch (JMSException e) {
                log.error("Failed to send message. Closing session and producer.", e);

                try {
                    if (messageProducer != null) {
                        messageProducer.close();
                    }
                } catch (JMSException e1) {
                }
                try {
                    if (this.jmsConnection != null) {
                        this.jmsSession.close();
                    }
                } catch (JMSException e1) {
                }

                this.jmsSession = null;

                log.error("Retrying after " + SLEEP_BETWEEN_RETRIES_MILLIS / 1000l + " secs.");
                try {
                    Thread.sleep(SLEEP_BETWEEN_RETRIES_MILLIS);
                } catch (InterruptedException e2) {
                }

            }
        }

        if (log.isDebugEnabled()) {
            log.debug("JMS message sent.");
        }
    }

    /**
     * This method registers a JMS message consumer on a topic or queue using
     * the given connection. This consumer receives all messages.
     *
     * @param topicOrQueue the topic or queue on which to listen
     * @param consumer     the consumer listening.
     */
    public DestinationConsumerRegistration registerDestinationConsumer(final Destination topicOrQueue, final MessageListener consumer) {
        return this.registerDestinationConsumer(topicOrQueue, null, consumer);
    }

    /**
     * This method registers a JMS message consumer on a topic or queue using
     * the given connection using a given message selector.
     *
     * @param topicOrQueue the topic or queue on which to listen
     * @param selector     an optional message selector, can be null when all messages
     *                     should be received.
     * @param listener     the consumer listening.
     */
    public DestinationConsumerRegistration registerDestinationConsumer(final Destination topicOrQueue, final String selector, final MessageListener listener) {
        boolean successfulRegistered = false;

        if (log.isDebugEnabled()) {
            log.debug("Registering JMS consumer on topic or queue.");
        }

        MessageConsumer messageConsumer = null;
        while (!successfulRegistered) {
            // create message consumer, add listener and set exception listener
            try {
            	if (this.jmsSession == null) {
            		this.createSession();
            	}
                if (selector != null) {
                    messageConsumer = this.jmsSession.createConsumer(topicOrQueue, selector);
                } else {
                    messageConsumer = this.jmsSession.createConsumer(topicOrQueue);
                }
                messageConsumer.setMessageListener(listener);

                if (log.isDebugEnabled()) {
                    log.debug("Attaching exception listener to connection: " + this.jmsConnection.toString());
                }
                this.jmsConnection.setExceptionListener(new ExceptionListener() {
                    public void onException(JMSException jmsException) {
                        log.error("JMS Exception caught while consuming destination - closing down and trying to reconnect.", jmsException);
                        close();
                        registerDestinationConsumer(topicOrQueue, selector, listener);
                    }
                });
                this.jmsConnection.start();
                successfulRegistered = true;

            } catch (Exception e) {
                log.error("Failed to create message consumer on destination. Closing consumer, session, and stopping connection.", e);
                try {
                    if (messageConsumer != null) {
                        messageConsumer.close();
                    }
                } catch (JMSException e1) {
                }

                messageConsumer = null;

                try {
                    if (jmsSession != null) {
                        jmsSession.close();
                    }
                } catch (JMSException e1) {
                }

                this.jmsSession = null;

                try {
                    if (jmsConnection != null) {
                        jmsConnection.stop();
                    }
                } catch (JMSException e0) {
                }
                jmsConnection = null;


                log.error("Retrying after " + SLEEP_BETWEEN_RETRIES_MILLIS / 1000l + " secs.");
                try {
                    Thread.sleep(SLEEP_BETWEEN_RETRIES_MILLIS);
                } catch (InterruptedException e2) {
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Registered JMS consumer on topic or queue.");
        }
        
        return new DestinationConsumerRegistration(messageConsumer);
    }

    /**
     * This method closes the given JMS connection and session cascade. It can
     * be reopened simply by sending a message again.
     */
    public void close() {
        try {
            if (jmsConnection != null) {
                this.jmsConnection.stop();
            }
        } catch (Exception e) {
            log.error("Failed to stop JMS connection.", e);
        }

        try {
            if (jmsSession != null) {
                this.jmsSession.close();
            }
        } catch (Exception e) {
            log.error("Failed to close JMS session.", e);
        } finally {
            this.jmsSession = null;
        }

        try {
            if (jmsConnection != null) {
                this.jmsConnection.close();
            }
        } catch (Exception e) {
            log.error("Failed to close JMS connection.", e);
        } finally {
            this.jmsConnection = null;
        }
    }

    /**
     * The JMS connection has to be configured with a JMS connection factory in
     * order to be able to create connections to JMS brokers and the like. Upon
     * calling the constructor, a JMS connection/session cascade is established.
     *
     * @param jmsProvider the jms provider we want to use
     */
    public JmsConnection(JmsProvider jmsProvider) {
        this.jmsProvider = (JmsProvider) jmsProvider.clone();
		try {
			this.createSession();
		} catch (JMSException e) {
			// should be unreachable due to exceptOnError being false
			e.printStackTrace();
		}
	}
    
    /**
     * The JMS connection has to be configured with a JMS connection factory in
     * order to be able to create connections to JMS brokers and the like. Upon
     * calling the constructor, a JMS connection/session cascade is established.
     *
     * @param jmsProvider the jms provider we want to use
     * @param retryForever should getting a session return or retry forever
     * @throws JMSException 
     */
    public JmsConnection(JmsProvider jmsProvider, boolean retryForever) throws JMSException {
        this.jmsProvider = (JmsProvider) jmsProvider.clone();
        this.exceptOnError = !retryForever;
		this.createSession();
	}
}
