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

import com.generationjava.io.xml.XmlEncXmlWriter;
import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.commons.messaging.JmsConnection;
import com.mercatis.lighthouse3.commons.messaging.JmsMessageCreator;
import com.mercatis.lighthouse3.domainmodel.commons.ConstraintViolationException;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;

import javax.jms.*;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class captures invocations - calls - of operations. A call always goes
 * against an operation installation and carries the parameter values that are
 * passed to the operation.
 */
public class OperationCall {

    /**
     * This carries a reference to the target of the call.
     */
    private OperationInstallation target = null;
    
    /**
     * The variant of the operation call.
     */
    private String variant;

    /**
     * This method returns the target of the operation call.
     *
     * @return the operation installation receiving the call
     */
    public OperationInstallation getTarget() {
        return this.target;
    }

    /**
     * This property carries the parameters that are being passed to the
     * operation installation.
     */
    private List<ParameterValue> parameterValues = new ArrayList<ParameterValue>();

    /**
     * This method passes a parameter value to the call.
     *
     * @param parameterValue the ParameterValue value to pass.
     */
    public void addParameterValue(ParameterValue parameterValue) {
        this.parameterValues.add(parameterValue);
    }

    /**
     * A more convenient method to pass parameter values by means of setter
     * chaining.
     *
     * @param parameterValue the parameter value to pass
     * @return the operation call itself.
     */
    public OperationCall pass(ParameterValue parameterValue) {
        this.addParameterValue(parameterValue);
        return this;
    }

    /**
     * This method returns all parameter values of the call in the order they
     * were passed.
     *
     * @return the parameter values.
     */
    public List<ParameterValue> getParameterValues() {
        return this.parameterValues;
    }

    /**
     * This method returns all values passed for a given parameter in the order
     * they were passed.
     *
     * @param parameterName the name of the parameter whose values are of interest.
     * @return the parameter values.
     */
    public List<Object> getValuesForParameter(String parameterName) {
        List<Object> result = new ArrayList<Object>();

        for (ParameterValue parameterValue : this.parameterValues) {
            if (parameterName.equals(parameterValue.getName()))
                result.add(parameterValue.getValue());
        }

        return result;
    }

    /**
     * This method returns the value passed for a given parameter. If there were
     * more values passed than one, the first one passed is returned.
     *
     * @param parameterName the name of the parameter whose values are of interest
     * @return the value passed for the parameter or <code>null</code> in case
     *         no such value was passed.
     */
    public Object getValueForParameter(String parameterName) {
        for (ParameterValue parameterValue : this.parameterValues) {
            if (parameterName.equals(parameterValue.getName()))
                return parameterValue.getValue();
        }

        return null;
    }

    /**
     * This predicate determines whether a given value was passed to the
     * operation call.
     *
     * @param parameterName the name of the parameter of interest.
     * @return <code>true</code> iff a parameter with the given name was passed.
     */
    public boolean hasValueForParameter(String parameterName) {
        return this.getValueForParameter(parameterName) != null;
    }


    /**
     * This method returns which variant of operation parameters of the operation has been set.
     *
     * @param operation the operation called
     * @return the name of the variant  or <code>null</code> if the default variant has been called
     */
    private String appliedVariant(Operation operation) {
    	if (this.variant != null && this.variant.length() > 0)
        	return this.variant;
    	
        for (ParameterValue value : this.getParameterValues()) {
            Parameter parameter = operation.getParameter(value.getName());

            if (parameter != null && parameter.getVariant() != null)
            	this.variant = parameter.getVariant();
                return parameter.getVariant();
        }

        return null;
    }

    /**
     * Checks whether a parameter of a given operation is relevant to the call.
     *
     * @param operation the operation
     * @param parameter the parameter
     * @return the relevancy.
     */
    private boolean isRelevant(Operation operation, Parameter parameter) {
        String variantUsed = this.appliedVariant(operation);

        if (variantUsed != null) {
            return variantUsed.equals(parameter.getVariant());
        } else {
            return parameter.getVariant() == null;
        }
    }

    /**
     * This method checks whether the given operation call is valid wrt. the
     * operation's signature.
     *
     * @param operation the against which to match the call. Must be identical to the
     *                  operation referred to by the call target.
     * @throws ConstraintViolationException in case validation stumbled across inconsistencies.
     */
    public void isValid(Operation operation) {
        if (!operation.getCode().equals(this.getTarget().getInstalledOperationCode()))
            throw new ConstraintViolationException(
                    "Invalid operation call: operation code : must match operation installation", null);


        for (Parameter parameter : new ArrayList<Parameter>(operation.getParameters())) {
            if (this.isRelevant(operation, parameter) && !this.hasValueForParameter(parameter.getName()) && parameter.hasDefaultValue()) {
                this.addParameterValue(parameter.createValue(parameter.getDefaultValue()));
            }
        }

        for (Parameter parameter : operation.getParameters()) {
            if (this.isRelevant(operation, parameter) && !parameter.isOptional() && !this.hasValueForParameter(parameter.getName()))
                throw new ConstraintViolationException("Invalid operation call: " + parameter.getName()
                        + " : required parameter missing", null);
            if (this.isRelevant(operation, parameter) && !parameter.isRepeatable() && (this.getValuesForParameter(parameter.getName()).size() > 1))
                throw new ConstraintViolationException("Invalid operation call: " + parameter.getName()
                        + " : multiple values for non-repeatable parameter given", null);
        }

        for (ParameterValue parameterValue : this.getParameterValues()) {
            Parameter parameter = operation.getParameter(parameterValue.getName());

            if (parameter == null)
                throw new ConstraintViolationException("Invalid operation call: " + parameterValue.getName()
                        + " : value for unknown parameter passed", null);

            try {
                parameter.createValue(parameterValue.getValue());
            } catch (ConstraintViolationException e) {
                throw new ConstraintViolationException("Invalid operation call: " + parameterValue.getName()
                        + " : invalid value passed for parameter", e);
            }
        }
    }

    /**
     * This method writes an XML representation of the present operation call to
     * the given XML writer.
     *
     * @param xml the XML writer to write to.
     * @throws IOException
     */
    public void writeXml(XmlWriter xml) throws IOException {
        xml.writeEntity("OperationCall");
        xml.writeAttribute("xmlns", XmlMuncher.MERCATIS_NS);

        if (this.target != null)
            this.target.writeEntityReference("target", xml);
        
        if (this.variant != null)
        	xml.writeEntityWithText("variant", this.variant);

        xml.writeEntity("parameterValues");

        for (ParameterValue parameterValue : this.parameterValues)
            parameterValue.writeXml(xml);

        xml.endEntity();

        xml.endEntity();
    }

    /**
     * This method returns an XML representation for the present operation call.
     *
     * @return the XML representation
     * @throws XMLSerializationException in case of an error.
     */
    public String toXml() {
        StringWriter output = new StringWriter();
        XmlWriter xml = new XmlEncXmlWriter(output);
        try {
            this.writeXml(xml);
        } catch (IOException ex) {
            throw new XMLSerializationException("Caught IO exception while serializing operation call", ex);
        }
        return output.toString();
    }

    /**
     * This method parses the XML representation of an operation call and sets
     * the properties of the operation call appropriately.
     *
     * @param xml                           the XML representation of the operation call
     * @param softwareComponentRegistry     the software component registry to use to resolve the call
     *                                      target
     * @param deploymentRegistry            the deployment registry to use to resolve the call target
     * @param operationInstallationRegistry the operation installation registry to use for resolving the
     *                                      call target.
     * @throws XMLSerializationException in case of a parsing problem
     */
    public void fromXml(String xml, SoftwareComponentRegistry softwareComponentRegistry,
                        DeploymentRegistry deploymentRegistry, OperationInstallationRegistry operationInstallationRegistry) {
        this.fromXml(new XmlMuncher(xml), softwareComponentRegistry, deploymentRegistry, operationInstallationRegistry);
    }

    /**
     * This method processes the XML representation of an operation call and
     * sets the properties of the operation call appropriately.
     *
     * @param xmlDocument                   the parsed XML document with the XML representation of the
     *                                      operation call
     * @param softwareComponentRegistry     the software component registry to use to resolve the call
     *                                      target
     * @param deploymentRegistry            the deployment registry to use to resolve the call target
     * @param operationInstallationRegistry the operation installation registry to use for resolving the
     *                                      call target.
     * @throws XMLSerializationException in case of a parsing problem
     */
    public void fromXml(XmlMuncher xmlDocument, SoftwareComponentRegistry softwareComponentRegistry,
                        DeploymentRegistry deploymentRegistry, OperationInstallationRegistry operationInstallationRegistry) {
        String operationCode = xmlDocument.readValueFromXml("//:target/:installedOperationCode");
        if (operationCode == null)
            throw new XMLSerializationException("Operation code for call not given", null);

        String deploymentLocation = xmlDocument.readValueFromXml("//:target/:installationLocation/:deploymentLocation");
        if (deploymentLocation == null)
            throw new XMLSerializationException(
                    "Location of deployment where the operation being called is installed not given", null);

        String deploymentCode = xmlDocument.readValueFromXml("//:target/:installationLocation/:deployedComponentCode");
        if (deploymentCode == null)
            throw new XMLSerializationException(
                    "Component code of deployment where the operation being called is installed not given", null);

        SoftwareComponent softwareComponent = softwareComponentRegistry.findByCode(deploymentCode);
        if (softwareComponent == null)
            throw new XMLSerializationException("Operation call refers to unknown software component '" + deploymentCode + "' as call target",
                    null);

        Deployment deployment = deploymentRegistry.findByComponentAndLocation(softwareComponent, deploymentLocation);
        if (deployment == null)
            throw new XMLSerializationException("Operation call refers to unknown deployment '" + softwareComponent + "@" + deploymentLocation + "' as call target", null);

        OperationInstallation operationInstallation = null;

        for (OperationInstallation installationAtDeployment : operationInstallationRegistry
                .findAtDeployment(deployment))
            if (installationAtDeployment.getInstalledOperationCode().equals(operationCode)) {
                operationInstallation = installationAtDeployment;
                break;
            }

        if (operationInstallation == null)
            throw new XMLSerializationException(
                    "Operation call refers to unknown operation installation as call target", null);

        this.target = operationInstallation;
        
        this.variant = xmlDocument.readValueFromXml("//:variant");

        for (XmlMuncher parameterValueFragment : xmlDocument
                .getSubMunchersForContext("//:parameterValues/:ParameterValue")) {
            ParameterValue parameterValue = new ParameterValue();

            parameterValue.fromXml(parameterValueFragment);

            this.addParameterValue(parameterValue);
        }
    }

    /**
     * This method schedules the present operation call for asynchronous
     * execution, by publishing it on the operation execution queue. There the
     * call will be picked up by operation executor services and executed.
     *
     * @param jmsConnection           the JMS connection to use for operation execution.
     * @param operationExecutionQueue the queue on which to publish the execution of the operation
     *                                call.
     * @throws OperationCallException in case that the publication failed.
     */
    public void execute(JmsConnection jmsConnection, Queue operationExecutionQueue) {
        try {
            jmsConnection.sendToDestination(operationExecutionQueue, new JmsMessageCreator() {

                public Message createMessage(Session jmsSession) throws JMSException {
                    TextMessage operationExecutionMessage = jmsSession.createTextMessage();

                    operationExecutionMessage.setText(toXml());

                    return operationExecutionMessage;
                }
            });
        } catch (Exception e) {
            throw new OperationCallException("Could not execute operation call", e);
        }
    }

    /**
	 * @param variant the variant to set
	 */
	public void setVariant(String variant) {
		this.variant = variant;
	}
	
	/**
	 * @return the variant
	 */
	public String getVariant() {
		return variant;
	}
	
    /**
     * The constructor for operation calls.
     *
     * @param target the operation installation being called.
     */
    public OperationCall(OperationInstallation target) {
        this.target = target;
    }
    
    public OperationCall(OperationInstallation target, String variant) {
    	this.target = target;
    	this.variant = variant;
    }

    /**
     * Default constructor. Not to be used except by reflection.
     */
    public OperationCall() {
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parameterValues == null) ? 0 : parameterValues.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		result = prime * result + ((variant == null) ? 0 : variant.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OperationCall other = (OperationCall) obj;
		if (parameterValues == null) {
			if (other.parameterValues != null)
				return false;
		} else if (!parameterValues.equals(other.parameterValues))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		if (variant == null) {
			if (other.variant != null)
				return false;
		} else if (!variant.equals(other.variant))
			return false;
		return true;
	}

}
