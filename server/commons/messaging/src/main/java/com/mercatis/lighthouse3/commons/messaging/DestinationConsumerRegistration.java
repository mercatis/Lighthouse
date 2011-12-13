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
import javax.jms.MessageConsumer;

import org.apache.log4j.Logger;

/**
 * <p>The DestinationConsumerRegistration represents a Handle that allows to close a registered DestinationConsumer.</p>
 * 
 * @see com.mercatis.lighthouse3.commons.messaging.JmsConnection#registerDestinationConsumer(javax.jms.Destination, javax.jms.MessageListener)
 * @see com.mercatis.lighthouse3.commons.messaging.JmsConnection#registerDestinationConsumer(javax.jms.Destination, String, javax.jms.MessageListener)
 */
public class DestinationConsumerRegistration {

	private Logger log = Logger.getLogger(this.getClass());
	
	private MessageConsumer messageConsumer;
	
	/*package*/ DestinationConsumerRegistration(MessageConsumer messageConsumer) {
		this.messageConsumer = messageConsumer;
	}
	
	/**
	 * <p>Closes the underlying MessageConsumer.</p>
	 */
	public void close() {
		if (this.messageConsumer != null) {
			try {
				this.messageConsumer.close();
			} catch (JMSException ex) {
				log.error("Failed to close MessageConsumer.", ex);
			} finally {
				this.messageConsumer = null;
			}
		}
	}
	
}
