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
package com.mercatis.lighthouse3.domainmodel.operations;

import java.io.IOException;
import java.util.Date;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;

/**
 * This class captures a value for an operation parameter.
 */
public class ParameterValue {
	/**
	 * The operation parameter name for which a value is provided.
	 */
	private String name = null;

	/**
	 * Returns the operation parameter name to which the value belongs.
	 *
	 * @return the parameter name.
	 */
	public String getName() {
		return this.name;
	}

        /**
         * Possibility to put some additional information to a parameter value like a filename
         * for binary data
         */
        private String valueLabel = null;

        public String getValueLabel() {
            return valueLabel;
        }

        public void setValueLabel(String valueLabel) {
            this.valueLabel = valueLabel;
        }

	/**
	 * The value for the parameter, in typed representation.
	 */
	private Object value = null;

	/**
	 * The value for the parameter.
	 * 
	 * @return the value, appropriately typed.
	 */
	public Object getValue() {
		return this.value;
	}

	/**
	 * Constructor for creating a parameter value
	 * 
	 * @param parameter
	 *            the parameter for which to create a value
	 * @param valueInTypedOrStringRepresentation
	 *            the value in typed or string representation.
	 * @throws ConstraintViolationException
	 *             in case the passed value is invalid for the parameter.
	 */
	protected ParameterValue(Parameter parameter, Object valueInTypedOrStringRepresentation) {
		this.name = parameter.getName();
		parameter.validateValue(valueInTypedOrStringRepresentation);
		if (valueInTypedOrStringRepresentation instanceof String)
			this.value = parameter.typedValueFromString((String) valueInTypedOrStringRepresentation);
		else
			this.value = valueInTypedOrStringRepresentation;
	}

	/**
	 * Default constructor. Not to be used except by reflection.
	 */
	protected ParameterValue() {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		ParameterValue other = (ParameterValue) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	/**
	 * This method writes a parameter value XML representation onto an XML
	 * writer.
	 * 
	 * @param xml
	 *            the XML writer
	 * @throws IOException
	 *             in case of an error.
	 */
	public void writeXml(XmlWriter xml) throws IOException {
		xml.writeEntity("ParameterValue");
		xml.writeEntityWithText("name", this.name);
                xml.writeEntityWithText("valueLabel", this.valueLabel);
		String parameterValue = null;
		String parameterType = null;

		if (this.value instanceof Date) {
			parameterValue = XmlMuncher.javaDateToXmlDateTime((Date) this.value);
			parameterType = Parameter.DATE;
		} else if (this.value instanceof byte[]) {
			parameterValue = XmlMuncher.byteArrayToXmlBinary((byte[]) this.value);
			parameterType = Parameter.BINARY;
		} else if (this.value instanceof char[]) {
			parameterValue = XmlMuncher.charArrayToXmlBinary((char[]) this.value);
			parameterType = Parameter.CLOB;
		} else {
			parameterValue = this.value.toString();
			parameterType = this.value.getClass().getSimpleName().toLowerCase();
		}

		xml.writeEntityWithText("value", parameterValue);
		xml.writeEntityWithText("type", parameterType);

		xml.endEntity();
	}

	/**
	 * This method reassembles a parameter value from its XML representation.
	 * 
	 * @param xmlDocument
	 *            the XML representation of the parameter value in parsed form
	 * @throws XMLSerializationException
	 *             in case of trouble processing the XML representation
	 */
	public void fromXml(XmlMuncher xmlDocument) {
		this.name = xmlDocument.readValueFromXml("//:name");
		if (this.name == null)
			throw new XMLSerializationException("Name not given for parameter value", null);

		String type = xmlDocument.readValueFromXml("//:type");
		if (type == null)
			throw new XMLSerializationException("Type not given for parameter value " + this.name, null);

		String stringValue = xmlDocument.readValueFromXml("//:value");
		if (stringValue == null)
			throw new XMLSerializationException("Value not given for parameter " + this.name, null);

                this.valueLabel = xmlDocument.readValueFromXml("//:valueLabel");

		if (type.equals("string"))
			this.value = stringValue;
		else if (type.equals("boolean"))
			this.value = new Boolean(stringValue);
		else if (type.equals("integer"))
			this.value = new Integer(stringValue);
		else if (type.equals("long"))
			this.value = new Long(stringValue);
		else if (type.equals("double"))
			this.value = new Double(stringValue);
		else if (type.equals("float"))
			this.value = new Float(stringValue);
		else if (type.equals("binary"))
			this.value = XmlMuncher.xmlBinaryToByteArray(stringValue);
        else if (type.equals("clob"))
			this.value = XmlMuncher.xmlBinaryToCharArray(stringValue);
		else if (type.equals("dateTime"))
			this.value = XmlMuncher.xmlDateTimeToJavaDate(stringValue);

		if (this.value == null)
			throw new XMLSerializationException("Invalid value given for parameter " + this.name, null);
	}
}
