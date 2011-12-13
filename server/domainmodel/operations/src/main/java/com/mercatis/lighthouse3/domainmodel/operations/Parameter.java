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

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.ConstraintViolationException;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class describes named operation parameters. Values of those parameters
 * have to be passed when calling an operation.
 */
public class Parameter {

    final public static String STRING = "string";
    final public static String PASSWORD = "password";
    final public static String INTEGER = "integer";
    final public static String LONG = "long";
    final public static String FLOAT = "float";
    final public static String DOUBLE = "double";
    final public static String DATE = "dateTime";
    final public static String BINARY = "binary";
    final public static String CLOB = "clob";
    final public static String BOOLEAN = "boolean";

    /**
     * The name of the parameter.
     */
    private String name = null;

    /**
     * Returns the name of the parameter.
     *
     * @return the name of the parameter.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the parameter
     *
     * @param name the name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The optional operation call variant to which the parameter belongs. This is basically an option of parameters to choose from when calling the operation.
     */
    private String variant = null;

    /**
     * Returns the optional operation call variant to which the parameter belongs. This is basically an option of parameters to choose from when calling the operation.
     *
     * @return the operation call variant to which the parameter belongs
     */
    public String getVariant() {
        return this.variant;
    }

    /**
     * Sets the optional operation call variant to which the parameter belongs
     *
     * @param variant the operation call variant to which the parameter belongs.
     */
    public void setVariant(String variant) {
        this.variant = variant;
    }

    /**
     * The type of the parameter, one of
     * <ul>
     * <li><code>Parameter.STRING</code>
     * <li><code>Parameter.PASSWORD</code>
     * <li><code>Parameter.INTEGER</code>
     * <li><code>Parameter.LONG</code>
     * <li><code>Parameter.FLOAT</code>
     * <li><code>Parameter.DOUBLE</code>
     * <li><code>Parameter.DATE</code>
     * <li><code>Parameter.BINARY</code>
     * <li><code>Parameter.CLOB</code>
     * <li><code>Parameter.BOOLEAN</code>
     * </ul>
     * . Default is <code>Parameter.STRING</code>.
     */
    private String type = STRING;

    /**
     * Returns the type of the parameter, one of
     * <ul>
     * <li><code>Parameter.STRING</code>
     * <li><code>Parameter.PASSWORD</code>
     * <li><code>Parameter.INTEGER</code>
     * <li><code>Parameter.LONG</code>
     * <li><code>Parameter.FLOAT</code>
     * <li><code>Parameter.DOUBLE</code>
     * <li><code>Parameter.DATE</code>
     * <li><code>Parameter.BINARY</code>
     * <li><code>Parameter.CLOB</code>
     * <li><code>Parameter.BOOLEAN</code>
     * </ul>
     * . Default is <code>Parameter.STRING</code>.
     *
     * @return the type of the parameter
     */
    public String getType() {
        return this.type;
    }

    /**
     * This method set the type of the parameter, one of
     * <ul>
     * <li><code>Parameter.STRING</code>
     * <li><code>Parameter.PASSWORD</code>
     * <li><code>Parameter.INTEGER</code>
     * <li><code>Parameter.LONG</code>
     * <li><code>Parameter.FLOAT</code>
     * <li><code>Parameter.DOUBLE</code>
     * <li><code>Parameter.DATE</code>
     * <li><code>Parameter.BINARY</code>
     * <li><code>Parameter.CLOB</code>
     * <li><code>Parameter.BOOLEAN</code>
     * </ul>
     * . Default is <code>Parameter.STRING</code>.
     *
     * @param type the type to set
     * @throws ConstraintViolationException in case an invalid parameter has been passed.
     */
    public void setType(String type) {
        if (!(STRING.equals(type) || PASSWORD.equals(type) || INTEGER.equals(type) || LONG.equals(type) || FLOAT.equals(type)
                || DOUBLE.equals(type) || DATE.equals(type) || BINARY.equals(type) || CLOB.equals(type) || BOOLEAN.equals(type)))
            throw new ConstraintViolationException(
                    "Invalid parameter type passed. Must be one of string, integer, long, float, double, boolean, date, binary, clob",
                    null);

        this.type = type;
    }

    /**
     * Indicates whether the parameter is hidden, i.e., invisible.
     */
    private boolean hidden = false;

    /**
     * Predicate indicating whether the parameter is hidden or not. Default is
     * <code>false</code>.
     *
     * @return <code>true</code> iff the parameter is hidden
     */
    public boolean isHidden() {
        return this.hidden;
    }

    /**
     * Sets whether the given parameter is hidden or not.
     *
     * @param hidden the hidden value.
     * @throws ConstraintViolationException in case the parameter is to be hidden but does not have a default value or is set as optional or repeatable.
     */
    public void setHidden(boolean hidden) {
        if (hidden && !this.hasDefaultValue())
            throw new ConstraintViolationException("A parameter without a default value cannot be hidden. ", null);

        if (hidden && this.isOptional())
            throw new ConstraintViolationException("An optional parameter cannot be hidden. ", null);

        if (hidden && this.isRepeatable())
            throw new ConstraintViolationException("An repeatable parameter cannot be hidden. ", null);

        this.hidden = hidden;
    }

    /**
     * Indicates whether the parameter is optional or not. Default is
     * <code>false</code>.
     */
    private boolean optional = false;

    /**
     * Predicate indicating whether the parameter is optional or not. Default is
     * <code>false</code>.
     *
     * @return <code>true</code> iff the parameter is optional
     */
    public boolean isOptional() {
        return this.optional;
    }

    /**
     * Sets whether the given parameter is optional or not.
     *
     * @param optional the optionality value.
     * @throws ConstraintViolationException in case the parameter has a default value or is hidden and one tries to mark it as optional
     */
    public void setOptional(boolean optional) {
        if (optional && this.hasDefaultValue())
            throw new ConstraintViolationException("A parameter with a default value cannot be marked optional. ", null);

        if (optional && this.isHidden())
            throw new ConstraintViolationException("A hidden parameter cannot be marked optional. ", null);

        this.optional = optional;
    }

    /**
     * Indicates whether the parameter can be repeated, i.e., values for it can
     * be passed more than once to the operation during a call.
     */
    private boolean repeatable = false;

    /**
     * Predicate indicating whether the parameter can be repeated, ie., values
     * for it can be passed more than once to the operation during a call. If
     * repeatable, an implicit ordering is established by the order in which the
     * parameter values are passed. Default is <code>false</code>.
     *
     * @return <code>true</code> iff the parameter is repeatable.
     */
    public boolean isRepeatable() {
        return this.repeatable;
    }

    /**
     * Sets whether the given parameter is optional or not.
     *
     * @param repeatable the repeatability value.
     * @throws ConstraintViolationException in case the parameter is hidden and one tries to mark it as repeatable
     */
    public void setRepeatable(boolean repeatable) {
        if (repeatable && this.isHidden())
            throw new ConstraintViolationException("A hidden parameter cannot be marked repeatable. ", null);

        this.repeatable = repeatable;
    }

    /**
     * An optional regular expression to which all values have to conform.
     */
    private String regularExpression = null;

    /**
     * Returns an optional regular expression to which all values have to
     * conform.
     *
     * @return the regular expression or <code>null</code> if no restriction
     *         exists.
     */
    public String getRegularExpression() {
        return this.regularExpression;
    }

    /**
     * Sets the regular expression to which the values of the present parameter
     * has to conform.
     *
     * @param regularExpression the regular expression.
     * @throws ConstraintViolationException in case an invalid regular expression is given.
     */
    public void setRegularExpression(String regularExpression) {
        if (regularExpression != null)
            try {
                Pattern.compile(regularExpression);
            } catch (Exception e) {
                throw new ConstraintViolationException("Invalid regular expression passed", e);
            }
        this.regularExpression = regularExpression;
    }

    /**
     * An optional default value for the parameter.
     */
    private String defaultValue = null;

    /**
     * This method sets the default value for the parameter.
     *
     * @param defaultValue the default value passed in string representation
     * @throws ConstraintViolationException in case the default value does not match the type constraints of the parameter or the parameter is optional or hidden and the default value is to be reset.
     */
    public void setDefaultValue(String defaultValue) {
        if (defaultValue != null && this.isOptional())
            throw new ConstraintViolationException("An optional parameter cannot be given a default value", null);

        if (defaultValue == null && this.isHidden())
            throw new ConstraintViolationException("A hidden parameter must have a default value", null);


        if (defaultValue != null)
            this.typedValueFromString(defaultValue);

        this.defaultValue = defaultValue;
    }

    /**
     * Returns the default value of the parameter.
     *
     * @return the default value or <code>null</code> in case no such value has been defined.
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Predicate determining whether a default value has been set.
     *
     * @return <code>true</code> iff a default value has been set.
     */
    public boolean hasDefaultValue() {
        return this.defaultValue != null;
    }

    /**
     * An optional list of permissible values for the parameter.
     */
    private List<String> choice = null;

    /**
     * Returns an optional list of permissible values to which the parameter is
     * restricted.
     *
     * @return the list of permissible values or <code>null</code> if no
     *         restriction exists.
     */
    public List<String> getChoice() {
        return this.choice;
    }

    /**
     * Sets an optional list of permissible values to which the parameter is
     * restricted.
     *
     * @param choice the list of permissible values in string representation
     * @throws ConstraintViolationException in case a value or more in the list do not match the
     *                                      parameter type.
     */
    public void setChoice(List<String> choice) {
        if (choice != null)
            for (String choiceString : choice)
                this.typedValueFromString(choiceString);

        this.choice = choice;
    }

    /**
     * This method takes a given string representation of a value for the given
     * parameter and transforms it to an appropriately typed Java object given
     * the parameter type.
     * <p/>
     * <ul>
     * <li><code>Parameter.STRING</code> becomes a <code>java.lang.String</code>
     * <li><code>Parameter.PASSWORD</code> becomes a <code>java.lang.String</code>
     * <li><code>Parameter.INTEGER</code> becomes a
     * <code>java.lang.Integer</code>
     * <li><code>Parameter.LONG</code> becomes a <code>java.lang.Long</code>
     * <li><code>Parameter.FLOAT</code> becomes a <code>java.lang.Float</code>
     * <li><code>Parameter.DOUBLE</code> becomes a <code>java.lang.Double</code>
     * <li><code>Parameter.DATE</code> (XML Schema dateTime representation)
     * becomes a <code>java.util.Date</code>
     * <li><code>Parameter.BINARY</code> (Base64 representation) becomes a
     * <code>byte[]</code>
     * <li><code>Parameter.BLOB</code> (Base64 representation) becomes a
     * <code>char[]</code>
     * <li><code>Parameter.BOOLEAN</code> becomes a
     * <code>java.lang.Boolean</code>
     * </ul>
     *
     * @param stringRepresentation the string representation of the value.
     * @return the Java object capturing the type.
     * @throws ConstraintViolationException in case a typed value could not be created.
     */
    protected Object typedValueFromString(String stringRepresentation) {
        Object result = null;

        try {
            if (this.getType().equals(STRING))
                result = stringRepresentation;
            if (this.getType().equals(PASSWORD))
                result = stringRepresentation;
            if (this.getType().equals(INTEGER))
                result = Integer.parseInt(stringRepresentation);
            if (this.getType().equals(LONG))
                result = Long.parseLong(stringRepresentation);
            if (this.getType().equals(FLOAT))
                result = Float.parseFloat(stringRepresentation);
            if (this.getType().equals(DOUBLE))
                result = Double.parseDouble(stringRepresentation);
            if (this.getType().equals(BOOLEAN))
                result = Boolean.parseBoolean(stringRepresentation);
            if (this.getType().equals(DATE))
                result = XmlMuncher.xmlDateTimeToJavaDate(stringRepresentation);

            if (this.getType().equals(CLOB))
                result = XmlMuncher.xmlBinaryToCharArray(stringRepresentation);

            if (this.getType().equals(BINARY))
                result = XmlMuncher.xmlBinaryToByteArray(stringRepresentation);

        } catch (Exception e) {
            throw new ConstraintViolationException("Could not create " + this.getType() + " value from string "
                    + stringRepresentation, e);
        }

        if (result == null)
            throw new ConstraintViolationException("Could not create " + this.getType() + " value from string "
                    + stringRepresentation, null);
        return result;
    }

    /**
     * This method validates a value in either typed or string representation
     * against the parameter, checking whether it constitutes a valid value.
     *
     * @param valueInTypedOrStringRepresentation
     *         the value to validate
     * @throws ConstraintViolationException in case that it doesn't
     */
    public void validateValue(Object valueInTypedOrStringRepresentation) {
        String stringValue = null;
        Object typedValue = null;

        if (valueInTypedOrStringRepresentation instanceof String) {
            stringValue = (String) valueInTypedOrStringRepresentation;
            typedValue = this.typedValueFromString(stringValue);
        } else {
            typedValue = valueInTypedOrStringRepresentation;

            if (this.getType().equals(DATE)) {
                if (!(valueInTypedOrStringRepresentation instanceof Date))
                    throw new ConstraintViolationException(
                            "Expected Date instance as value for parameter of type date", null);

                stringValue = XmlMuncher.javaDateToXmlDateTime((Date) valueInTypedOrStringRepresentation);
            } else if (this.getType().equals(BINARY)) {
                if (!(valueInTypedOrStringRepresentation instanceof byte[]))
                    throw new ConstraintViolationException(
                            "Expected byte array  as value for parameter of type binary", null);

                stringValue = XmlMuncher.byteArrayToXmlBinary((byte[]) valueInTypedOrStringRepresentation);
            } else if (this.getType().equals(CLOB)) {
                if (!(valueInTypedOrStringRepresentation instanceof char[]))
                    throw new ConstraintViolationException(
                            "Expected byte array  as value for parameter of type clob", null);

                stringValue = XmlMuncher.charArrayToXmlBinary((char[]) valueInTypedOrStringRepresentation);

            } else {
                stringValue = valueInTypedOrStringRepresentation.toString();

                if (!typedValue.equals(this.typedValueFromString(stringValue)))
                    throw new ConstraintViolationException("Invalid value type for parameter", null);
            }
        }

        if ((this.getRegularExpression() != null) && !Pattern.matches(this.getRegularExpression(), stringValue))
            throw new ConstraintViolationException("Value does not match regular expression constraint", null);

        if (this.getChoice() != null) {

            boolean matchingChoiceFound = false;

            for (String choice : this.getChoice()) {
                if (this.typedValueFromString(choice).equals(typedValue))
                    matchingChoiceFound = true;
            }

            if (!matchingChoiceFound)
                throw new ConstraintViolationException("Value does not occur in choice list", null);
        }
    }

    /**
     * Creates a value for the given parameter.
     *
     * @param valueInTypedOrStringRepresentation
     *         the value in typed or string representation
     * @return the parameter value made out of the value.
     * @throws ConstraintViolationException in case the passed value is invalid for the parameter.
     */
    public ParameterValue createValue(Object valueInTypedOrStringRepresentation) {
        return new ParameterValue(this, valueInTypedOrStringRepresentation);
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((variant == null) ? 0 : variant.hashCode());
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
		Parameter other = (Parameter) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (variant == null) {
			if (other.variant != null)
				return false;
		} else if (!variant.equals(other.variant))
			return false;
		return true;
	}
    
    /**
     * Serializes the given parameter into XML.
     *
     * @param xml the XML writer onto which to serialize the parameter.
     * @throws IOException in case of trouble writing to the XML writer.
     */
    public void writeToXmlWriter(XmlWriter xml) throws IOException {
        xml.writeEntity("parameter");

        if (this.getName() != null)
            xml.writeEntityWithText("name", this.getName());
        if (this.getType() != null)
            xml.writeEntityWithText("type", this.getType());
        if (this.getVariant() != null) {
            xml.writeEntityWithText("variant", this.getVariant());
        }

        xml.writeEntityWithText("optional", this.isOptional());
        xml.writeEntityWithText("hidden", this.isHidden());
        xml.writeEntityWithText("repeatable", this.isRepeatable());

        if (this.hasDefaultValue())
            xml.writeEntityWithText("default", this.getDefaultValue());
        if (this.getRegularExpression() != null)
            xml.writeEntityWithText("regularExpression", this.getRegularExpression());
        if (this.getChoice() != null) {
            xml.writeEntity("choices");
            for (String choice : this.getChoice())
                xml.writeEntityWithText("choice", choice);
            xml.endEntity();
        }

        xml.endEntity();
    }

	/**
     * This method parses a parameter out of an XML document (part).
     *
     * @param xmlDocument the XML document from which to parse the
     * @throws XMLSerializationException in case of an error.
     */
    public void fromXml(XmlMuncher xmlDocument) {
        this.setName(xmlDocument.readValueFromXml("//:name"));
        this.setType(xmlDocument.readValueFromXml("//:type"));
        this.setVariant(xmlDocument.readValueFromXml("//:variant"));

        try {
            String defaultValue = xmlDocument.readValueFromXml("//:default");
            if (defaultValue != null)
                this.setDefaultValue(defaultValue);

            String hidden = xmlDocument.readValueFromXml("//:hidden");
            if ("true".equals(hidden))
                this.setHidden(true);

            String optional = xmlDocument.readValueFromXml("//:optional");
            if ("true".equals(optional))
                this.setOptional(true);

            String repeatable = xmlDocument.readValueFromXml("//:repeatable");
            if ("true".equals(repeatable))
                this.setRepeatable(true);
        } catch (ConstraintViolationException e) {
            throw new XMLSerializationException("Could not construct parameter from XML due to constraint violation", e);
        }

        this.setRegularExpression(xmlDocument.readValueFromXml("//:regularExpression"));
        List<String> choices = xmlDocument.readValuesFromXml("//:choice");

        if (choices.isEmpty())
            this.setChoice(null);
        else
            this.setChoice(choices);
    }
}
