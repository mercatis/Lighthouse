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
package com.mercatis.lighthouse3.domainmodel.commons;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.generationjava.io.xml.XmlEncXmlWriter;
import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;

/**
 * This base class subsumes basis characteristics of persistent domain model
 * entity classes.
 * 
 * Domain model entities offer support for entity properties, which are
 * basically objects that are considered particularly descriptive of an entity.
 * 
 * It is the task of the concrete implementations of this base class to define
 * what these properties are. For this purpose, the method
 * <code>getProperties()</code> needs to be overridden to return the objects
 * capturing the properties.
 */
public abstract class DomainModelEntity implements Serializable {

	private static final long serialVersionUID = -3151745860492320228L;

	/**
	 * Represents the unique surrogate id of the domain model entity in the
	 * database.
	 */
	private long id = 0L;

	/**
	 * Returns the unique surrogate id of the domain model entity in the
	 * database.
	 * 
	 * @return the unique id
	 */
	public long getId() {
		return id;
	}

	/**
	 * This method sets the unique surrogate id of the domain model entity in
	 * the database.
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * This static property keeps the default setting for the lighthouse domain.
	 * If not set manually, it is initialized to the local IP address. This
	 * property is not to be persisted.
	 */
	static public String lighthouseDomainDefault = null;

	/**
	 * This property keeps a string identifying the lighthouse domain. If not
	 * set manually, it is initialized to the lighthouse domain default.
	 */
	transient private String lighthouseDomain = null;

	/**
	 * Call this method to obtain the lighthouse domain from which the present
	 * domain model entity originates. If not set manually, it is initialized to
	 * the lighthouse domain default.
	 * 
	 * @return the domain
	 */
	public String getLighthouseDomain() {
		if (lighthouseDomain == null && lighthouseDomainDefault == null) {
			try {
				Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();

				while (netInterfaces.hasMoreElements() && (lighthouseDomain == null)) {
					NetworkInterface netInterface = netInterfaces.nextElement();

					Enumeration<InetAddress> inetAddresses = netInterface.getInetAddresses();

					while (inetAddresses.hasMoreElements() && (lighthouseDomain == null)) {
						InetAddress inetAddress = inetAddresses.nextElement();

						if (!inetAddress.getHostAddress().startsWith("127")) {
							lighthouseDomain = inetAddress.getHostAddress();
							lighthouseDomainDefault = lighthouseDomain;
						}
					}
				}
			} catch (SocketException e) {
			}
		} else if (lighthouseDomain == null) {
			lighthouseDomain = lighthouseDomainDefault;
		}

		return lighthouseDomain;
	}

	/**
	 * Call this to explicitly set the lighthouse domain on the present domain
	 * model entity.
	 * 
	 * @param lighthouseDomain
	 *            the lighthouse domain.
	 */
	public void setLighthouseDomain(String lighthouseDomain) {
		this.lighthouseDomain = lighthouseDomain;
	}

	/**
	 * This method returns a path identifier for the given domain model entity.
	 * It has a URL-like form: <code>entityClassInLowerCase://a/path</code>. For
	 * plain domain model entities the path is just the id of the entity.
	 * 
	 * @return the path for the entity.
	 */
	public String getPath() {
		return getClass().getSimpleName().toLowerCase() + "://" + getId();
	}

	/**
	 * This helper method provides a generic accessor for setting attributes of
	 * the present domain model entity. When the attribute does not exist
	 * nothing happens.
	 * 
	 * @param attribute
	 *            the name of the attribute
	 * @param value
	 *            the value of the attribute
	 */
	public void setAttributeByName(String attribute, Object value) {
		String setterName = "set" + String.valueOf(attribute.charAt(0)).toUpperCase() + attribute.substring(1);

		try {
			Method setter = null;
			Method[] methods = getClass().getMethods();
			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];

				if (method.getName().equals(setterName)) {
					setter = method;
					break;
				}
			}
			if (setter != null) {
				setter.setAccessible(true);
				setter.invoke(this, value);
			}
		} catch (SecurityException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
	}

	/**
	 * This method returns the properties of the present domain model entity,
	 * filtered by their membership to a specific class.
	 * 
	 * This method can be overridden by concrete implementations of this domain
	 * model entities.
	 * 
	 * @return the properties of the present domain model entity.
	 */
	@SuppressWarnings("rawtypes")
	public Set getProperties() {
		return new HashSet();
	}

	/**
	 * This method returns the properties of the present domain model entity,
	 * filtered by their class.
	 * 
	 * @param filterByClass
	 *            the class to which the returned properties must belong.
	 * @return the properties of the present domain model entity.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set getProperties(Class filterByClass) {
		Set result = new HashSet();

		for (Object property : getProperties()) {
			if (filterByClass.isInstance(property))
				result.add(property);
		}

		return result;
	}

	/**
	 * This method returns an XML representation of the given domain entity.
	 * 
	 * Internally, the method calls <code>writeRootElement()</code> and
	 * <code>fillRootElementContent()</code> which may be overridden by
	 * subclasses.
	 * 
	 * @return the XML representation of the entity.
	 * @throws XMLSerializationException
	 *             the method may throw an unchecked XMLSerialization exception.
	 */
	public String toXml() {
		StringWriter result = new StringWriter();
		XmlWriter xml = new XmlEncXmlWriter(result);

		writeRootElement(xml);

		return result.toString();
	}

	public void toXml(XmlWriter xml) {
		writeRootElement(xml);
	}

	/**
	 * This method knows how to write an XML reference to the present domain
	 * model entity. Should be overridden by subclasses. Note that in case the
	 * reference cannot be written because the identifying data for the
	 * reference is incomplete, nothing should be written.
	 * 
	 * @param referenceTagName
	 *            the name of the tag to be used for wrapping the reference
	 * @param reference
	 *            the domain model entity referenced
	 * @param xml
	 *            the XML writer used for XML serialization.
	 * @throws IOException
	 *             the method may throw an IOException
	 */
	public void writeEntityReference(String referenceTagName, XmlWriter xml) throws IOException {
	}

	/**
	 * This method fills the root element representing the domain entity with
	 * content for capturing the domain entity's characteristics.
	 * 
	 * @param xml
	 *            the XML writer used for XML serialization.
	 * @throws IOException
	 *             the method may throw an IOException exception.
	 */
	protected void fillRootElement(XmlWriter xml) throws IOException {
		if (getId() != 0L)
			xml.writeEntityWithText("id", getId());

		if (getLighthouseDomain() != null)
			xml.writeEntityWithText("lighthouseDomain", getLighthouseDomain());
	}

	/**
	 * This method sets the root XML element for the serialization of the
	 * present domain entity. This method calls <code>fillRootElement()</code>
	 * to fill in the properties of the entity.
	 * 
	 * Defaults to the class name. May be overridden by subclasses.
	 * 
	 * @param xml
	 *            the XML writer used for XML serialization.
	 * @throws XMLSerializationException
	 *             the method may throw an unchecked XMLSerialization exception.
	 */
	protected void writeRootElement(XmlWriter xml) {
		try {
			xml.writeEntity(getRootElementName());
			xml.writeAttribute("xmlns", XmlMuncher.MERCATIS_NS);
			fillRootElement(xml);
			xml.endEntity();
		} catch (IOException e) {
			throw new XMLSerializationException(e.getMessage(), e);
		}
	}

	/**
	 * This method returns the root element name of the present domain model
	 * entity class.
	 * 
	 * @return the root element name
	 */
	public String getRootElementName() {
		return getClass().getSimpleName();
	}

	/**
	 * This method checks whether the root element of the XML representation to
	 * parse is correct.
	 * 
	 * @param xmlDocument
	 *            the XML representation of the entity
	 * 
	 */
	protected void checkRootElement(XmlMuncher xmlDocument) {
		String rootElementName = xmlDocument.getRootElementName();

		if (!rootElementName.equals(getClass().getSimpleName()))
			throw new XMLSerializationException("Invalid XML representation of entity: root element wrong.", null);
	}

	/**
	 * This method reads the primitive attributes of the present entity during
	 * XML deserialization. Should be overridden by subclasses.
	 * 
	 * @param xmlDocument
	 *            the XML representation of the entity
	 */
	protected void readPropertiesFromXml(XmlMuncher xmlDocument) {
		String id = xmlDocument.readValueFromXml("/*/:id");
		if (id != null)
			setId(Long.parseLong(id));

		String lighthouseDomain = xmlDocument.readValueFromXml("/*/:lighthouseDomain");
		if (lighthouseDomain != null)
			setLighthouseDomain(lighthouseDomain);
	}

	/**
	 * This method resolves the references to other entities of the present
	 * entity during XML deserialization. Should be overridden by subclasses.
	 * 
	 * @param xmlDocument
	 *            the XML representation of the entity
	 * @param resolversForEntityReferences
	 *            an entity-specific number of DAOs (including 0 DAOs) as
	 *            required for the resolution of references to other entities.
	 */
	@SuppressWarnings("rawtypes")
	protected void resolveEntityReferencesFromXml(XmlMuncher xmlDocument, DomainModelEntityDAO... resolversForEntityReferences) {
	}

	/**
	 * This method deserializes the given entity from its XML representation.
	 * 
	 * @param xml
	 *            the XML representation as an XML Muncher.
	 * 
	 * @param resolversForEntityReferences
	 *            an entity-specific number of DAOs (including 0 DAOs) as
	 *            required for the resolution of references to other entities.
	 * 
	 * @throws XMLSerializationException
	 *             an unchecked exception is thrown in case of an error in the
	 *             XML representation
	 */
	@SuppressWarnings("rawtypes")
	public void fromXml(XmlMuncher xml, DomainModelEntityDAO... resolversForEntityReferences) {
		try {
			readPropertiesFromXml(xml);
			resolveEntityReferencesFromXml(xml, resolversForEntityReferences);
		} catch (XMLSerializationException xmlSerializationException) {
			throw xmlSerializationException;
		} catch (UnknownContextException unknownContextException) {
			throw unknownContextException;
		} catch (Exception anythingElse) {
			throw new XMLSerializationException("Invalid XML representation of entity: ", anythingElse);
		}
	}

	/**
	 * This method deserializes the given entity from its XML representation.
	 * 
	 * @param xml
	 *            the XML representation as a string.
	 * 
	 * @param resolversForEntityReferences
	 *            an entity-specific number of DAOs (including 0 DAOs) as
	 *            required for the resolution of references to other entities.
	 * 
	 * @throws XMLSerializationException
	 *             an unchecked exception is thrown in case of an error in the
	 *             XML representation
	 */
	@SuppressWarnings("rawtypes")
	public void fromXml(String xml, DomainModelEntityDAO... resolversForEntityReferences) {
		try {
			XmlMuncher xmlDocument = new XmlMuncher(xml);
			checkRootElement(xmlDocument);
			fromXml(xmlDocument, resolversForEntityReferences);
		} catch (XMLSerializationException xmlSerializationException) {
			throw xmlSerializationException;
		} catch (UnknownContextException unknownContextException) {
			throw unknownContextException;
		} catch (Exception anythingElse) {
			throw new XMLSerializationException("Invalid XML representation of entity: ", anythingElse);
		}
	}

	/**
	 * This method interprets the current domain model entity as a template and
	 * transforms its set properties into a map of CGI query parameters.
	 * 
	 * @return the query parameters
	 */
	public Map<String, String> toQueryParameters() {
		Map<String, String> queryParameters = new HashMap<String, String>();

		if (getId() != 0l)
			queryParameters.put("id", "" + getId());

		return queryParameters;
	}

	/**
	 * This method fills in the basic properties of the present domain model
	 * entity from a bunch of CGI parameters.
	 * 
	 * @param queryParameters
	 *            the CGI parameters
	 */
	public void fromQueryParameters(Map<String, String> queryParameters) {
		try {
			for (String attributeName : queryParameters.keySet()) {
				String value = queryParameters.get(attributeName);
				setAttributeByName(attributeName, value);
			}
		} catch (Exception anyProblem) {
			throw new ConstraintViolationException("Could not create template from query parameters", anyProblem);
		}
	}
}