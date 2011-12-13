/**
 * (c) Copyright 2010 mercatis technologies AG
 *
 * All rights reserved.
 *
 * Part of Lighthouse 3
 *
 * This source code is proprietary trade secret information of
 * mercatis information systems GmbH. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * mercatis information systems GmbH.
 */
package com.mercatis.lighthouse3.services.internal.registries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.Tuple;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.ConstraintViolationException;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.persistence.environment.rest.ProcessTaskRegistryImplementation;


@SuppressWarnings("rawtypes")
public class EagerProcessTaskRegistryImplementation extends EagerDeploymentCarryingDomainModelEntityDAOImplementation<ProcessTask>
	implements ProcessTaskRegistry {
	
	@SuppressWarnings("serial")
	protected class LateBindingProcessTask extends ProcessTask
		implements LateBindingDomainModelEntity<ProcessTask> {
		
		private ProcessTask delegateEntity = new ProcessTask();
		
		private String parentCode = null;

		private List<String> laneNames;

		private List<String> processesInLaneCodes;

		private List<String> transitionStartProcessCodes;

		private List<String> transitionEndProcessCodes;

		/* (non-Javadoc)
		 * @see com.mercatis.lighthouse3.services.internal.registries2.LateBindingDomainModelEntity#bind()
		 */
		public LateBindingDomainModelEntity<ProcessTask> bind() {
			try {
				if (parentCode != null) {
					ProcessTask parent = EagerProcessTaskRegistryImplementation.this.findByCode(parentCode);

					if (parent == null)
						throw new XMLSerializationException("XML serialization of hierarchical domain model entity references parent entity with unknown code.", null);

					if (parent.getCode() != null) {
						delegateEntity.setParentEntity(parent);
						parent.addSubEntity(delegateEntity);
					}
				}
				
				return this;
			} catch (XMLSerializationException xmlSerializationException) {
				throw xmlSerializationException;
			} catch (Exception anythingElse) {
				throw new XMLSerializationException("Invalid XML representation of entity: ", anythingElse);
			}
		}
		
		public LateBindingProcessTask bindLanesAndTransitions() {
			try {
				if (processesInLaneCodes != null && laneNames != null) {
					for (int i = 0; i < processesInLaneCodes.size(); i++) {
						try {
							delegateEntity.assignToSwimlane(processesInLaneCodes.get(i), laneNames.get(i));
						} catch (ConstraintViolationException exception) {
							throw new XMLSerializationException(
								"XML deserialization of process / task references process / task with unknown code in swimlanes.",
								exception);
						}
					}
				}
				
				if (transitionStartProcessCodes != null && transitionEndProcessCodes != null) {
					for (int i = 0; i < transitionStartProcessCodes.size(); i++) {
						try {
							delegateEntity.setTransition(transitionStartProcessCodes.get(i), transitionEndProcessCodes.get(i));
						} catch (ConstraintViolationException exception) {
							throw new XMLSerializationException(
									"XML deserialization of process / task references process / task with unknown code in transitions.",
									exception);
						}
					}
				}
				
				return this;
			} catch (XMLSerializationException xmlSerializationException) {
				throw xmlSerializationException;
			} catch (Exception anythingElse) {
				throw new XMLSerializationException("Invalid XML representation of entity: ", anythingElse);
			}
		}

		/* (non-Javadoc)
		 * @see com.mercatis.lighthouse3.services.internal.registries2.LateBindingDomainModelEntity#getDelegateEntity()
		 */
		public ProcessTask getDelegateEntity() {
			return delegateEntity;
		}

		/**
		 * @param subEntity
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#addSubEntity(com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity)
		 */
		public void addSubEntity(ProcessTask subEntity) {
			delegateEntity.addSubEntity(subEntity);
		}

		/**
		 * @param subprocess
		 * @param swimlane
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#assignToSwimlane(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask, java.lang.String)
		 */
		public void assignToSwimlane(ProcessTask subprocess, String swimlane) {
			delegateEntity.assignToSwimlane(subprocess, swimlane);
		}

		/**
		 * @param subprocess
		 * @param swimlane
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#assignToSwimlane(java.lang.String, java.lang.String)
		 */
		public void assignToSwimlane(String subprocess, String swimlane) {
			delegateEntity.assignToSwimlane(subprocess, swimlane);
		}

		/**
		 * @param deployment
		 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity#attachDeployment(com.mercatis.lighthouse3.domainmodel.environment.Deployment)
		 */
		public void attachDeployment(Deployment deployment) {
			delegateEntity.attachDeployment(deployment);
		}

		/**
		 * @param deployment
		 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity#detachDeployment(com.mercatis.lighthouse3.domainmodel.environment.Deployment)
		 */
		public void detachDeployment(Deployment deployment) {
			delegateEntity.detachDeployment(deployment);
		}

		/**
		 * 
		 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity#detachDeployments()
		 */
		public void detachDeployments() {
			delegateEntity.detachDeployments();
		}

		/**
		 * @param arg0
		 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity#fromQueryParameters(java.util.Map)
		 */
		public void fromQueryParameters(Map<String, String> arg0) {
			delegateEntity.fromQueryParameters(arg0);
		}

		/**
		 * @param arg0
		 * @param arg1
		 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity#fromXml(java.lang.String, com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO[])
		 */
		public void fromXml(String arg0, DomainModelEntityDAO... arg1) {
			delegateEntity.fromXml(arg0, arg1);
		}

		/**
		 * @param arg0
		 * @param arg1
		 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity#fromXml(com.mercatis.lighthouse3.commons.commons.XmlMuncher, com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO[])
		 */
		public void fromXml(XmlMuncher xml, DomainModelEntityDAO... resolversForEntityReferences) {
			try {
				this.readPropertiesFromXml(xml);
				this.parentCode = xml.readValueFromXml("/*/:parent" + delegateEntity.getClass().getSimpleName() + "/:code");
				this.laneNames = xml.readValuesFromXml("/*/:swimlanes/:swimlane/:lane");
				this.processesInLaneCodes = xml.readValuesFromXml("/*/:swimlanes/:swimlane/:processOrTask");
				this.transitionStartProcessCodes = xml.readValuesFromXml("/*/:transitions/:transition/:fromProcessOrTask");
				this.transitionEndProcessCodes = xml.readValuesFromXml("/*/:transitions/:transition/:toProcessOrTask");
				
				// resolve attached deployments
				DeploymentRegistry deploymentRegistry = (DeploymentRegistry) resolversForEntityReferences[1];
				List<String> deployedComponentCodes = xml.readValuesFromXml("/*/:attachedDeployments/:attachedDeployment/:deployedComponentCode");
				List<String> deploymentLocations = xml.readValuesFromXml("/*/:attachedDeployments/:attachedDeployment/:deploymentLocation");
				for (int i = 0; i < deployedComponentCodes.size(); i++) {
					String deployedComponentCode = deployedComponentCodes.get(i);
					String deploymentLocation = deploymentLocations.get(i);
					
					List<Deployment> deployments = deploymentRegistry.findAtLocation(deploymentLocation);
					for (Deployment deployment : deployments) {
						if (deployment.getDeployedComponent().getCode().equals(deployedComponentCode)) {
							((DeploymentCarryingDomainModelEntity) getDelegateEntity()).attachDeployment(deployment);
							break;
						}
					}
				}
			} catch (Exception anythingElse) {
				throw new XMLSerializationException("Invalid XML representation of entity: ", anythingElse);
			}
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity#getAllDeployments()
		 */
		public Set<Deployment> getAllDeployments() {
			return delegateEntity.getAllDeployments();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity#getAssociatedDeployments()
		 */
		public Set<Deployment> getAssociatedDeployments() {
			return delegateEntity.getAssociatedDeployments();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity#getCode()
		 */
		public String getCode() {
			return delegateEntity.getCode();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#getContact()
		 */
		public String getContact() {
			return delegateEntity.getContact();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#getContactEmail()
		 */
		public String getContactEmail() {
			return delegateEntity.getContactEmail();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity#getDeployments()
		 */
		public Set<Deployment> getDeployments() {
			return delegateEntity.getDeployments();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#getDescription()
		 */
		public String getDescription() {
			return delegateEntity.getDescription();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity#getDirectSubCarriers()
		 */
		public Set<StatusCarrier> getDirectSubCarriers() {
			return delegateEntity.getDirectSubCarriers();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#getDirectSubEntities()
		 */
		public Set<ProcessTask> getDirectSubEntities() {
			return delegateEntity.getDirectSubEntities();
		}

		/**
		 * @param arg0
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#getDirectSubProcessTaskByCode(java.lang.String)
		 */
		public ProcessTask getDirectSubProcessTaskByCode(String arg0) {
			return delegateEntity.getDirectSubProcessTaskByCode(arg0);
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#getFinals()
		 */
		public Set<ProcessTask> getFinals() {
			return delegateEntity.getFinals();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity#getId()
		 */
		public long getId() {
			return delegateEntity.getId();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity#getLighthouseDomain()
		 */
		public String getLighthouseDomain() {
			return delegateEntity.getLighthouseDomain();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity#getLocalProperties()
		 */
		public Set getLocalProperties() {
			return delegateEntity.getLocalProperties();
		}

		/**
		 * @param arg0
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#getLocalProperties(java.lang.Class)
		 */
		public Set getLocalProperties(Class arg0) {
			return delegateEntity.getLocalProperties(arg0);
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#getLongName()
		 */
		public String getLongName() {
			return delegateEntity.getLongName();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#getParentEntity()
		 */
		public ProcessTask getParentEntity() {
			return delegateEntity.getParentEntity();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#getPath()
		 */
		public String getPath() {
			return delegateEntity.getPath();
		}

		/**
		 * @param arg0
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#getPredecessors(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
		 */
		public Set<ProcessTask> getPredecessors(ProcessTask arg0) {
			return delegateEntity.getPredecessors(arg0);
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#getProperties()
		 */
		public Set getProperties() {
			return delegateEntity.getProperties();
		}

		/**
		 * @param filterByClass
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#getProperties(java.lang.Class)
		 */
		public Set getProperties(Class filterByClass) {
			return delegateEntity.getProperties(filterByClass);
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity#getRootElementName()
		 */
		public String getRootElementName() {
			return delegateEntity.getRootElementName();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#getRootEntity()
		 */
		public ProcessTask getRootEntity() {
			return delegateEntity.getRootEntity();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#getStarters()
		 */
		public Set<ProcessTask> getStarters() {
			return delegateEntity.getStarters();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity#getSubCarriers()
		 */
		public Set<StatusCarrier> getSubCarriers() {
			return delegateEntity.getSubCarriers();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#getSubEntities()
		 */
		public Set<ProcessTask> getSubEntities() {
			return delegateEntity.getSubEntities();
		}

		/**
		 * @param arg0
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#getSuccessors(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
		 */
		public Set<ProcessTask> getSuccessors(ProcessTask arg0) {
			return delegateEntity.getSuccessors(arg0);
		}

		/**
		 * @param arg0
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#getSwimlane(java.lang.String)
		 */
		public Set<ProcessTask> getSwimlane(String arg0) {
			return delegateEntity.getSwimlane(arg0);
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#getSwimlaneData()
		 */
		public Map<String, String> getSwimlaneData() {
			return delegateEntity.getSwimlaneData();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#getSwimlanes()
		 */
		public Set<String> getSwimlanes() {
			return delegateEntity.getSwimlanes();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#getTransitionData()
		 */
		public Set<Tuple<String, String>> getTransitionData() {
			return delegateEntity.getTransitionData();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#getVersion()
		 */
		public String getVersion() {
			return delegateEntity.getVersion();
		}

		/**
		 * @param deployment
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity#hasAttachedDeployment(com.mercatis.lighthouse3.domainmodel.environment.Deployment)
		 */
		public boolean hasAttachedDeployment(Deployment deployment) {
			return delegateEntity.hasAttachedDeployment(deployment);
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity#hasDeployments()
		 */
		public boolean hasDeployments() {
			return delegateEntity.hasDeployments();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#isComplexEntity()
		 */
		public boolean isComplexEntity() {
			return delegateEntity.isComplexEntity();
		}

		/**
		 * @param entity
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#isDirectSubEntity(com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity)
		 */
		public boolean isDirectSubEntity(ProcessTask entity) {
			return delegateEntity.isDirectSubEntity(entity);
		}

		/**
		 * @param subProcess
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#isFinal(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
		 */
		public boolean isFinal(ProcessTask subProcess) {
			return delegateEntity.isFinal(subProcess);
		}

		/**
		 * @param swimlane
		 * @param subProcess
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#isInSwimlane(java.lang.String, com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
		 */
		public boolean isInSwimlane(String swimlane, ProcessTask subProcess) {
			return delegateEntity.isInSwimlane(swimlane, subProcess);
		}

		/**
		 * @param successor
		 * @param predecessor
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#isPredecessorOf(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask, com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
		 */
		public boolean isPredecessorOf(ProcessTask successor, ProcessTask predecessor) {
			return delegateEntity.isPredecessorOf(successor, predecessor);
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#isRootEntity()
		 */
		public boolean isRootEntity() {
			return delegateEntity.isRootEntity();
		}

		/**
		 * @param subProcess
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#isStarter(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
		 */
		public boolean isStarter(ProcessTask subProcess) {
			return delegateEntity.isStarter(subProcess);
		}

		/**
		 * @param entity
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#isSubEntity(com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity)
		 */
		public boolean isSubEntity(ProcessTask entity) {
			return delegateEntity.isSubEntity(entity);
		}

		/**
		 * @param predecessor
		 * @param successor
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#isSuccessorOf(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask, com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
		 */
		public boolean isSuccessorOf(ProcessTask predecessor, ProcessTask successor) {
			return delegateEntity.isSuccessorOf(predecessor, successor);
		}

		/**
		 * 
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#removeAllSubEntities()
		 */
		public void removeAllSubEntities() {
			delegateEntity.removeAllSubEntities();
		}

		/**
		 * @param subprocess
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#removeFromSwimlane(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
		 */
		public void removeFromSwimlane(ProcessTask subprocess) {
			delegateEntity.removeFromSwimlane(subprocess);
		}

		/**
		 * @param arg0
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#removeSubEntity(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
		 */
		public void removeSubEntity(ProcessTask arg0) {
			delegateEntity.removeSubEntity(arg0);
		}

		/**
		 * @param fromSubProcess
		 * @param toSubProcess
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#removeTransition(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask, com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
		 */
		public void removeTransition(ProcessTask fromSubProcess, ProcessTask toSubProcess) {
			delegateEntity.removeTransition(fromSubProcess, toSubProcess);
		}

		/**
		 * @param arg0
		 * @param arg1
		 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity#setAttributeByName(java.lang.String, java.lang.Object)
		 */
		public void setAttributeByName(String arg0, Object arg1) {
			delegateEntity.setAttributeByName(arg0, arg1);
		}

		/**
		 * @param code
		 * @see com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity#setCode(java.lang.String)
		 */
		public void setCode(String code) {
			delegateEntity.setCode(code);
		}

		/**
		 * @param contact
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#setContact(java.lang.String)
		 */
		public void setContact(String contact) {
			delegateEntity.setContact(contact);
		}

		/**
		 * @param contactEmail
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#setContactEmail(java.lang.String)
		 */
		public void setContactEmail(String contactEmail) {
			delegateEntity.setContactEmail(contactEmail);
		}

		/**
		 * @param description
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#setDescription(java.lang.String)
		 */
		public void setDescription(String description) {
			delegateEntity.setDescription(description);
		}

		/**
		 * @param id
		 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity#setId(long)
		 */
		public void setId(long id) {
			delegateEntity.setId(id);
		}

		/**
		 * @param lighthouseDomain
		 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity#setLighthouseDomain(java.lang.String)
		 */
		public void setLighthouseDomain(String lighthouseDomain) {
			delegateEntity.setLighthouseDomain(lighthouseDomain);
		}

		/**
		 * @param longName
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#setLongName(java.lang.String)
		 */
		public void setLongName(String longName) {
			delegateEntity.setLongName(longName);
		}

		/**
		 * @param parentEntity
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#setParentEntity(com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity)
		 */
		public void setParentEntity(ProcessTask parentEntity) {
			delegateEntity.setParentEntity(parentEntity);
		}

		/**
		 * @param fromSubProcess
		 * @param toSubProcess
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#setTransition(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask, com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
		 */
		public void setTransition(ProcessTask fromSubProcess, ProcessTask toSubProcess) {
			delegateEntity.setTransition(fromSubProcess, toSubProcess);
		}

		/**
		 * @param fromSubProcess
		 * @param toSubProcess
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#setTransition(java.lang.String, java.lang.String)
		 */
		public void setTransition(String fromSubProcess, String toSubProcess) {
			delegateEntity.setTransition(fromSubProcess, toSubProcess);
		}

		/**
		 * @param version
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#setVersion(java.lang.String)
		 */
		public void setVersion(String version) {
			delegateEntity.setVersion(version);
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTask#toQueryParameters()
		 */
		public Map<String, String> toQueryParameters() {
			return delegateEntity.toQueryParameters();
		}

		/**
		 * @return
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return delegateEntity.toString();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity#toXml()
		 */
		public String toXml() {
			return delegateEntity.toXml();
		}

		/**
		 * @param xml
		 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity#toXml(com.generationjava.io.xml.XmlWriter)
		 */
		public void toXml(XmlWriter xml) {
			delegateEntity.toXml(xml);
		}

		/**
		 * @param referenceTagName
		 * @param xml
		 * @throws IOException
		 * @see com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity#writeEntityReference(java.lang.String, com.generationjava.io.xml.XmlWriter)
		 */
		public void writeEntityReference(String referenceTagName, XmlWriter xml) throws IOException {
			delegateEntity.writeEntityReference(referenceTagName, xml);
		}
		
		
	}

	private ProcessTaskRegistryImplementation delegateRegistry = null;

	public EagerProcessTaskRegistryImplementation(String serverUrl, DeploymentRegistry deploymentRegistry) {
		this(serverUrl, deploymentRegistry, null, null);
	}
	
	public EagerProcessTaskRegistryImplementation(String serverUrl, DeploymentRegistry deploymentRegistry, String user, String password) {
		this.delegateRegistry = new ProcessTaskRegistryImplementation(serverUrl, deploymentRegistry, user, password) {
			
			/* (non-Javadoc)
			 * @see com.mercatis.lighthouse3.persistence.commons.rest.DomainModelEntityDAOImplementation#newEntity()
			 */
			@Override
			protected ProcessTask newEntity() {
				return new LateBindingProcessTask();
			}
			
			/* (non-Javadoc)
			 * @see com.mercatis.lighthouse3.persistence.commons.rest.DomainModelEntityDAOImplementation#getManagedType()
			 */
			@Override
			public Class getManagedType() {
				return ProcessTask.class;
			}
			
		};
		
		this.invalidate();
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry#register(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
	 */
	public void register(ProcessTask processTask) {
		this.persist(processTask);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry#reRegister(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
	 */
	public void reRegister(ProcessTask processTask) {
		this.update(processTask);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry#unregister(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
	 */
	public void unregister(ProcessTask processTask) {
		this.delete(processTask);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries2.EagerDomainModelEntityDAOImplementation#delegateRegistry()
	 */
	@Override
	protected DomainModelEntityDAO<ProcessTask> delegateRegistry() {
		return delegateRegistry;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#getManagedType()
	 */
	public Class getManagedType() {
		return ProcessTask.class;
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries2.EagerHierarchicalDomainModelEntityDAOImplementation#invalidate()
	 */
	@Override
	public void invalidate() {
		cache.clear();
		
		List<ProcessTask> entities = delegateRegistry().findAll();
		for (ProcessTask entity : entities) {
			if (entity != null)
				cache.put(entity.getCode(), entity);
		}
		
		// bind parent - child relationships
		List<ProcessTask> unboundEntities = new ArrayList<ProcessTask>(cache.values());
		for (ProcessTask unboundEntity : unboundEntities) {
			ProcessTask boundEntity = (ProcessTask) ((LateBindingProcessTask) unboundEntity).bind();
			if (boundEntity != null)
				cache.put(boundEntity.getCode(), boundEntity);
		}
		
		// bind lane / transition relationships
		unboundEntities = new ArrayList<ProcessTask>(cache.values());
		for (ProcessTask unboundEntity : unboundEntities) {
			ProcessTask boundEntity = (ProcessTask) ((LateBindingProcessTask) unboundEntity).bindLanesAndTransitions().getDelegateEntity();
			if (boundEntity != null)
				cache.put(boundEntity.getCode(), boundEntity);
		}
	}
}
