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
package com.mercatis.lighthouse3.domainmodel.environment;

import java.io.IOException;
import java.util.Set;

import com.generationjava.io.xml.XmlWriter;

/**
 * This interface should be implemented by all environment entities that are
 * able to carry status. Status are traffic lights-like indicators of problems
 * in an environment.
 */
public interface StatusCarrier {

	/**
	 * Returns the unique surrogate id of the status carrier in the database.
	 * 
	 * @return the unique id
	 */
	public long getId();

	/**
	 * This method sets the unique surrogate id the status carrier in the
	 * database.
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(long id);

	/**
	 * Returns the URL-like path identifier of the status carrier.
	 * 
	 * @return the path identifier of the status carrier
	 */
	public String getPath();

	/**
	 * This method returns all deployments that are associated with a given
	 * status carrier.
	 * 
	 * @return the associated deployments
	 */
	public Set<Deployment> getAssociatedDeployments();

	/**
	 * Status carriers may be organized hierarchically. This method returns all
	 * direct and indirect subcarriers of the present one.
	 * 
	 * @return the set of sub carriers, which may well be empty.
	 */
	public Set<StatusCarrier> getSubCarriers();

	/**
	 * Status carriers may be organized hierarchically. This method returns all
	 * direct subcarriers of the present one.
	 * 
	 * @return the set of direct sub carriers, which may well be empty.
	 */
	public Set<StatusCarrier> getDirectSubCarriers();

	/**
	 * Implement this method to write a reference to the given status carrier in
	 * XML format.
	 * 
	 * @param referenceTagName
	 *            the name of the element surrounding the carrier reference.
	 * @param xml
	 *            the XML writer to write to.
	 * @throws IOException
	 *             in case of a problem
	 */
	public void writeEntityReference(String referenceTagName, XmlWriter xml)
			throws IOException;

	/**
	 * This method returns the root element name of the status carrier.
	 * 
	 * @return the root element name
	 */
	public String getRootElementName();
}
