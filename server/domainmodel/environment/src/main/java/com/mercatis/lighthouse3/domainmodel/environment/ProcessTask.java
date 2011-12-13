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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.Tuple;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.ConstraintViolationException;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;

/**
 * This class captures business processes or tasks in a business process.
 * Deployments of software component may implement such tasks. Thereby, one can
 * be later able to infer business process-level monitoring states from
 * technical software component monitoring states.
 * 
 * A process may hierarchically consist of other sub processes or tasks.
 * Subprocesses or tasks can be connected to another in predecessor-successor
 * relationships. Moreover, subprocesses or tasks can be assigned to swimlanes /
 * roles.
 */
public class ProcessTask extends DeploymentCarryingDomainModelEntity<ProcessTask> {

	private static final long serialVersionUID = -8657277549653685373L;

	/**
	 * This property keeps a version designator string of the process or task in
	 * question.
	 */
	private String version = null;

	/**
	 * Returns a version designator string of the process or task in question.
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version designator string of the process or task in question
	 * 
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * This property captures a more readable name of the process or task in
	 * question compared to its unique code name.
	 */
	private String longName = null;

	/**
	 * Returns a more readable name of the process or task in question compared
	 * to its unique code name.
	 * 
	 * @return the long name
	 */
	public String getLongName() {
		return longName;
	}

	/**
	 * Sets a more readable name of the process or task in question compared to
	 * its unique code name.
	 * 
	 * @param longName
	 *            the long name to set
	 */
	public void setLongName(String longName) {
		this.longName = longName;
	}

	/**
	 * Stores a brief textual description of the process or task in question.
	 */
	private String description = null;

	/**
	 * Returns a brief textual description of the process or task in question.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets a brief textual description of the process or task in question.
	 * 
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * This property stores contact information for the responsible person for
	 * the process or task in question.
	 */
	private String contact = null;

	/**
	 * Returns contact information for the responsible person for the process or
	 * task in question.
	 * 
	 * @return the contact information
	 */
	public String getContact() {
		return contact;
	}

	/**
	 * Sets the contact information for the responsible person for the process
	 * or task in question.
	 * 
	 * @param contact
	 *            the contact information to set
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}

	/**
	 * This property stores an email address of the responsible person for the
	 * process or task in question.
	 */
	private String contactEmail = null;

	/**
	 * Returns an email address of the responsible person for the process or
	 * task in question.
	 * 
	 * @return the contact email
	 */
	public String getContactEmail() {
		return contactEmail;
	}

	/**
	 * Sets the email address of the responsible person for the process or task
	 * in question.
	 * 
	 * @param contactEmail
	 *            the contact email to set
	 */
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	/**
	 * This method returns the direct subprocess / task of the present process /
	 * task with the given code.
	 * 
	 * @param code
	 *            the code of the subprocess / task of interest
	 * @return the subprocess / task or <code>null</code> in case it could not
	 *         be found.
	 */
	public ProcessTask getDirectSubProcessTaskByCode(String code) {
		for (ProcessTask subProcess : this.getDirectSubEntities()) {
			if (subProcess.getCode().equals(code))
				return subProcess;
		}

		return null;
	}

	/**
	 * This map associates the subprocesses / subtasks of a process with a
	 * corresponding swim lane via their code.
	 */
	private Map<String, String> swimlanes = new HashMap<String, String>();

	/**
	 * This method assigns a direct subprocess / task of the present one to a
	 * swimlane.
	 * 
	 * @param subprocess
	 *            the subprocess to assign
	 * @param swimlane
	 *            the swimlane to which the subprocess is assigned
	 * @throws ConstraintViolationException
	 *             in case the process to assign to the swimlane is not a direct
	 *             subprocess.
	 */
	public void assignToSwimlane(ProcessTask subprocess, String swimlane) {
		this.assignToSwimlane(subprocess.getCode(), swimlane);
	}

	/**
	 * This method assigns a direct subprocess / task of the present one to a
	 * swimlane.
	 * 
	 * @param subprocess
	 *            the code of the subprocess to assign
	 * @param swimlane
	 *            the swimlane to which the subprocess is assigned
	 * @throws ConstraintViolationException
	 *             in case the process to assign to the swimlane is not a direct
	 *             subprocess.
	 */
	public void assignToSwimlane(String subprocess, String swimlane) {
		if (this.getDirectSubProcessTaskByCode(subprocess) == null)
			throw new ConstraintViolationException("Can only assign direct subprocesses / tasks to a swimlane.", null);
		this.swimlanes.put(subprocess, swimlane);
	}

	/**
	 * This method removes a subprocess / task from the swimlane it is assigned
	 * to.
	 * 
	 * @param subprocess
	 *            the subprocess to remove.
	 */
	public void removeFromSwimlane(ProcessTask subprocess) {
		this.swimlanes.remove(subprocess.getCode());
	}

	/**
	 * This method returns all subprocesses / tasks assigned to a given
	 * swimlane.
	 * 
	 * @param swimlane
	 *            the swimlane of interest
	 * @return the set of subprocesses / tasks assigned to the swimlane.
	 */
	public Set<ProcessTask> getSwimlane(String swimlane) {
		HashSet<ProcessTask> result = new HashSet<ProcessTask>();

		for (String subProcessCode : this.swimlanes.keySet()) {
			if (this.swimlanes.get(subProcessCode).equals(swimlane))
				result.add(this.getDirectSubProcessTaskByCode(subProcessCode));
		}

		return result;
	}

	/**
	 * This method returns all swimlanes in which sub process / tasks have been
	 * placed.
	 * 
	 * @return the set of swimlanes
	 */
	public Set<String> getSwimlanes() {
		HashSet<String> result = new HashSet<String>();

		result.addAll(this.swimlanes.values());

		return result;
	}

	/**
	 * This predicate evaluates whether a given sub process / task is assigned
	 * to a given swimlane.
	 * 
	 * @param swimlane
	 *            the swimlane to check for
	 * @param subProcess
	 *            the process for which to check in which lane it is in.
	 * @return <code>true</code> iff the process is in the lane.
	 */
	public boolean isInSwimlane(String swimlane, ProcessTask subProcess) {
		return this.getSwimlane(swimlane).contains(subProcess);
	}

	/**
	 * This map puts the subprocesses / subtasks of a process into predecessor /
	 * successor relationships via the process codes.
	 */
	private Set<Tuple<String, String>> transitions = new HashSet<Tuple<String, String>>();

	/**
	 * This method creates a transition between two subprocesses / tasks of the
	 * given process / task, putting them into a predecessor / successor
	 * relationship. Note that it is only possible to do this with two direct
	 * subprocesses / tasks.
	 * 
	 * @param fromSubProcess
	 *            the predecessor process / task
	 * @param toSubProcess
	 *            the successor process / task
	 * @throws ConstraintViolationException
	 *             in case that one of the passed processes / tasks is not a
	 *             direct subentity.
	 */
	public void setTransition(ProcessTask fromSubProcess, ProcessTask toSubProcess) {
		this.setTransition(fromSubProcess.getCode(), toSubProcess.getCode());
	}

	/**
	 * This method creates a transition between two subprocesses / tasks of the
	 * given process / task, putting them into a predecessor / successor
	 * relationship. Note that it is only possible to do this with two direct
	 * subprocesses / tasks.
	 * 
	 * @param fromSubProcess
	 *            the code of the predecessor process / task
	 * @param toSubProcess
	 *            the code of the successor process / task
	 * @throws ConstraintViolationException
	 *             in case that one of the passed processes / tasks is not a
	 *             direct subentity.
	 */
	public void setTransition(String fromSubProcess, String toSubProcess) {
		if (this.getDirectSubProcessTaskByCode(fromSubProcess) == null
				|| this.getDirectSubProcessTaskByCode(toSubProcess) == null)
			throw new ConstraintViolationException("Can only create transitions between direct subprocesses / tasks.",
					null);

		this.transitions.add(new Tuple<String, String>(fromSubProcess, toSubProcess));
	}

	/**
	 * This method removes the transition between two subprocesses / tasks.
	 * 
	 * @param fromSubProcess
	 *            the start of the transition
	 * @param toSubProcess
	 *            the target of the transition
	 * 
	 */
	public void removeTransition(ProcessTask fromSubProcess, ProcessTask toSubProcess) {
		this.transitions.remove(new Tuple<String, String>(fromSubProcess.getCode(), toSubProcess.getCode()));
	}

	/**
	 * This method returns all subprocesses / tasks which constitute the origin
	 * of a transition to a given subprocess / task.
	 * 
	 * @param successor
	 *            the subprocess / task for whom to look up the predecessors.
	 * 
	 * @return the set of preceding subprocesses / tasks.
	 */
	public Set<ProcessTask> getPredecessors(ProcessTask successor) {
		HashSet<ProcessTask> result = new HashSet<ProcessTask>();

		for (Tuple<String, String> transition : this.transitions) {
			if (transition.getB().equals(successor.getCode()))
				result.add(this.getDirectSubProcessTaskByCode(transition.getA()));
		}
		return result;
	}

	/**
	 * This method returns all subprocesses / tasks which constitute the end of
	 * a transition from the given subprocess / task.
	 * 
	 * @param predecessor
	 *            the subprocess / task for whom to look up the successors.
	 * 
	 * @return the set of succeeding subprocesses / tasks.
	 */
	public Set<ProcessTask> getSuccessors(ProcessTask predecessor) {
		HashSet<ProcessTask> result = new HashSet<ProcessTask>();

		for (Tuple<String, String> transition : this.transitions) {
			if (transition.getA().equals(predecessor.getCode()))
				result.add(this.getDirectSubProcessTaskByCode(transition.getB()));
		}
		return result;
	}

	/**
	 * The method returns all starter sub processes / tasks of the given
	 * process. A starter process is one with no predecessors.
	 * 
	 * @return the set of all starter processes
	 */
	public Set<ProcessTask> getStarters() {
		HashSet<ProcessTask> result = new HashSet<ProcessTask>();

		for (ProcessTask subProcess : this.getDirectSubEntities()) {
			if (this.getPredecessors(subProcess).isEmpty())
				result.add(subProcess);
		}

		return result;
	}

	/**
	 * The method returns all final sub processes / tasks of the given process.
	 * A final process is one with no successors.
	 * 
	 * @return the set of all final processes
	 */
	public Set<ProcessTask> getFinals() {
		HashSet<ProcessTask> result = new HashSet<ProcessTask>();

		for (ProcessTask subProcess : this.getDirectSubEntities()) {
			if (this.getSuccessors(subProcess).isEmpty())
				result.add(subProcess);
		}

		return result;
	}

	/**
	 * This method returns true iff a subprocess / task is the successor of
	 * another one.
	 * 
	 * @param predecessor
	 *            the process to which the other process is supposed to be
	 *            considered as a successor
	 * @param successor
	 *            the process to check for successor relationship.
	 * @return true iff <code>successor</code> is a successor of
	 *         <code>predecessor</code.
	 */
	public boolean isSuccessorOf(ProcessTask predecessor, ProcessTask successor) {
		return this.getSuccessors(predecessor).contains(successor);
	}

	/**
	 * This method returns true iff a subprocess / task is the predecessor of
	 * another one.
	 * 
	 * @param successor
	 *            the process to which the other process is supposed to be
	 *            considered as a predecessor
	 * @param predecessor
	 *            the process to which the process is supposed to be considered
	 *            as a successor
	 * @return true iff <code>predecessor</code> is a predecessor of
	 *         <code>successor</code>.
	 */
	public boolean isPredecessorOf(ProcessTask successor, ProcessTask predecessor) {
		return this.getPredecessors(successor).contains(predecessor);
	}

	/**
	 * This method checks whether a given sub process task is a started process
	 * / task.
	 * 
	 * @param subProcess
	 *            the subprocess to check
	 * @return true iff <code>subProcess</code> is a starter process / task with
	 *         no predecessors.
	 */
	public boolean isStarter(ProcessTask subProcess) {
		return this.getStarters().contains(subProcess);
	}

	/**
	 * This method checks whether a given sub process task is a final process /
	 * task.
	 * 
	 * @param subProcess
	 *            the subprocess to check
	 * @return true iff <code>subProcess</code> is a final process / task with
	 *         no predecessors.
	 */
	public boolean isFinal(ProcessTask subProcess) {
		return this.getFinals().contains(subProcess);
	}

	/**
	 * Returns all swimlane data in raw format. Do not modify the result!
	 * 
	 * @return the swimlane data
	 */
	public Map<String, String> getSwimlaneData() {
		return this.swimlanes;
	}

	/**
	 * Returns all transitions in raw format. Do not modify the result!
	 * 
	 * @return the transition data
	 */
	public Set<Tuple<String, String>> getTransitionData() {
		return this.transitions;
	}

	@Override
	public void removeSubEntity(ProcessTask subProcess) {
		this.removeFromSwimlane(subProcess);

		Set<Tuple<String, String>> transitionsToRemove = new HashSet<Tuple<String, String>>();

		for (Tuple<String, String> transition : this.transitions) {
			if (transition.getA().equals(subProcess.getCode()) || transition.getB().equals(subProcess.getCode()))
				transitionsToRemove.add(transition);
		}

		this.transitions.removeAll(transitionsToRemove);

		super.removeSubEntity(subProcess);
	}

	@Override
	protected void fillRootElement(XmlWriter xml) throws IOException {
		super.fillRootElement(xml);

		if (!this.swimlanes.isEmpty()) {
			this.writeSwimlanes(xml);
		}

		if (!this.transitions.isEmpty()) {
			this.writeTransitions(xml);
		}

		if (this.getVersion() != null) {
			xml.writeEntityWithText("version", this.getVersion());
		}

		if (this.getLongName() != null) {
			xml.writeEntityWithText("longName", this.getLongName());
		}

		if (this.getDescription() != null) {
			xml.writeEntityWithText("description", this.getDescription());
		}

		if (this.getContact() != null) {
			xml.writeEntityWithText("contact", this.getContact());
		}

		if (this.getContactEmail() != null) {
			xml.writeEntityWithText("contactEmail", this.getContactEmail());
		}
	}

	protected void writeSwimlanes(XmlWriter xml) throws IOException {
		xml.writeEntity("swimlanes");

		for (String subProcessInLaneCode : this.swimlanes.keySet()) {

			xml.writeEntity("swimlane");

			xml.writeEntityWithText("lane", this.swimlanes.get(subProcessInLaneCode));
			xml.writeEntityWithText("processOrTask", subProcessInLaneCode);

			xml.endEntity();
		}

		xml.endEntity();
	}

	protected void writeTransitions(XmlWriter xml) throws IOException {
		xml.writeEntity("transitions");

		for (Tuple<String, String> transition : this.transitions) {
			xml.writeEntity("transition");
			xml.writeEntityWithText("fromProcessOrTask", transition.getA());
			xml.writeEntityWithText("toProcessOrTask", transition.getB());
			xml.endEntity();
		}

		xml.endEntity();
	}

	@Override
	protected void readPropertiesFromXml(XmlMuncher xmlDocument) {
		super.readPropertiesFromXml(xmlDocument);

		this.setVersion(xmlDocument.readValueFromXml("/*/:version"));
		this.setLongName(xmlDocument.readValueFromXml("/*/:longName"));
		this.setDescription(xmlDocument.readValueFromXml("/*/:description"));
		this.setContact(xmlDocument.readValueFromXml("/*/:contact"));
		this.setContactEmail(xmlDocument.readValueFromXml("/*/:contactEmail"));
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void resolveEntityReferencesFromXml(XmlMuncher xmlDocument,
			DomainModelEntityDAO... resolversForEntityReferences) {

		if ((resolversForEntityReferences.length == 0)
				|| !resolversForEntityReferences[0].getManagedType().equals(ProcessTask.class)) {
			throw new XMLSerializationException(
					"XML deserialization of process / task requires reference to ProcessTaskRegistry as 1st resolverForEntityReferences.",
					null);
		}

		super.resolveEntityReferencesFromXml(xmlDocument, resolversForEntityReferences);

		List<String> laneNames = xmlDocument.readValuesFromXml("/*/:swimlanes/:swimlane/:lane");
		List<String> processesInLaneCodes = xmlDocument.readValuesFromXml("/*/:swimlanes/:swimlane/:processOrTask");

		this.swimlanes = new HashMap<String, String>();

		for (int i = 0; i < processesInLaneCodes.size(); i++) {
			try {
				this.assignToSwimlane(processesInLaneCodes.get(i), laneNames.get(i));
			} catch (ConstraintViolationException exception) {
				throw new XMLSerializationException(
						"XML deserialization of process / task references process / task with unknown code in swimlanes.",
						exception);
			}
		}

		List<String> transitionStartProcessCodes = xmlDocument
				.readValuesFromXml("/*/:transitions/:transition/:fromProcessOrTask");

		List<String> transitionEndProcessCodes = xmlDocument
				.readValuesFromXml("/*/:transitions/:transition/:toProcessOrTask");

		this.transitions = new HashSet<Tuple<String, String>>();

		for (int i = 0; i < transitionStartProcessCodes.size(); i++) {
			try {
				this.setTransition(transitionStartProcessCodes.get(i), transitionEndProcessCodes.get(i));
			} catch (ConstraintViolationException exception) {
				throw new XMLSerializationException(
						"XML deserialization of process / task references process / task with unknown code in transitions.",
						exception);
			}
		}
	}

	@Override
	public Map<String, String> toQueryParameters() {
		Map<String, String> parameters = super.toQueryParameters();

		if (this.getVersion() != null) {
			parameters.put("version", this.getVersion());
		}

		if (this.getLongName() != null) {
			parameters.put("longName", this.getLongName());
		}

		if (this.getDescription() != null) {
			parameters.put("description", this.getDescription());
		}

		if (this.getContact() != null) {
			parameters.put("contact", this.getContact());
		}

		if (this.getContactEmail() != null) {
			parameters.put("contactEmail", this.getContactEmail());
		}

		return parameters;
	}

}
