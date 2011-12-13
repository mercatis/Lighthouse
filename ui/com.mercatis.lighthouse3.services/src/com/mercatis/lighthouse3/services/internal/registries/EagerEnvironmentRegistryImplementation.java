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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.persistence.environment.rest.EnvironmentRegistryImplementation;


@SuppressWarnings("rawtypes")
public class EagerEnvironmentRegistryImplementation extends EagerDeploymentCarryingDomainModelEntityDAOImplementation<Environment>
	implements EnvironmentRegistry {
	
	@SuppressWarnings("serial")
	protected class LateBindingEnvironment extends Environment
		implements LateBindingDomainModelEntity<Environment> {
		
		private Environment delegateEntity = new Environment();
		
		private String parentCode = null;

		/* (non-Javadoc)
		 * @see com.mercatis.lighthouse3.services.internal.registries2.LateBindingDomainModelEntity#bind()
		 */
		public LateBindingDomainModelEntity<Environment> bind() {
			try {
				if (parentCode != null) {
					Environment parent = EagerEnvironmentRegistryImplementation.this.findByCode(parentCode);

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

		/* (non-Javadoc)
		 * @see com.mercatis.lighthouse3.services.internal.registries2.LateBindingDomainModelEntity#getDelegateEntity()
		 */
		public Environment getDelegateEntity() {
			return delegateEntity;
		}

		/**
		 * @param subEntity
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#addSubEntity(com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity)
		 */
		public void addSubEntity(Environment subEntity) {
			delegateEntity.addSubEntity(subEntity);
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
		 * @see com.mercatis.lighthouse3.domainmodel.environment.Environment#getContact()
		 */
		public String getContact() {
			return delegateEntity.getContact();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.Environment#getContactEmail()
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
		 * @see com.mercatis.lighthouse3.domainmodel.environment.Environment#getDescription()
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
		public Set<Environment> getDirectSubEntities() {
			return delegateEntity.getDirectSubEntities();
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
		 * @see com.mercatis.lighthouse3.domainmodel.environment.Environment#getLongName()
		 */
		public String getLongName() {
			return delegateEntity.getLongName();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#getParentEntity()
		 */
		public Environment getParentEntity() {
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
		public Environment getRootEntity() {
			return delegateEntity.getRootEntity();
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
		public Set<Environment> getSubEntities() {
			return delegateEntity.getSubEntities();
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
		public boolean isDirectSubEntity(Environment entity) {
			return delegateEntity.isDirectSubEntity(entity);
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#isRootEntity()
		 */
		public boolean isRootEntity() {
			return delegateEntity.isRootEntity();
		}

		/**
		 * @param entity
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#isSubEntity(com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity)
		 */
		public boolean isSubEntity(Environment entity) {
			return delegateEntity.isSubEntity(entity);
		}

		/**
		 * 
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#removeAllSubEntities()
		 */
		public void removeAllSubEntities() {
			delegateEntity.removeAllSubEntities();
		}

		/**
		 * @param subEntity
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#removeSubEntity(com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity)
		 */
		public void removeSubEntity(Environment subEntity) {
			delegateEntity.removeSubEntity(subEntity);
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
		 * @see com.mercatis.lighthouse3.domainmodel.environment.Environment#setContact(java.lang.String)
		 */
		public void setContact(String contact) {
			delegateEntity.setContact(contact);
		}

		/**
		 * @param contactEmail
		 * @see com.mercatis.lighthouse3.domainmodel.environment.Environment#setContactEmail(java.lang.String)
		 */
		public void setContactEmail(String contactEmail) {
			delegateEntity.setContactEmail(contactEmail);
		}

		/**
		 * @param description
		 * @see com.mercatis.lighthouse3.domainmodel.environment.Environment#setDescription(java.lang.String)
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
		 * @see com.mercatis.lighthouse3.domainmodel.environment.Environment#setLongName(java.lang.String)
		 */
		public void setLongName(String longName) {
			delegateEntity.setLongName(longName);
		}

		/**
		 * @param parentEntity
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#setParentEntity(com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity)
		 */
		public void setParentEntity(Environment parentEntity) {
			delegateEntity.setParentEntity(parentEntity);
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.Environment#toQueryParameters()
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

	private EnvironmentRegistryImplementation delegateRegistry = null;
	
	public EagerEnvironmentRegistryImplementation(String serverUrl, DeploymentRegistry deploymentRegistry, String user, String password) {
		System.out.println("New EagerEnvironmentRegistry: " + user + ", " + password);
		this.delegateRegistry = new EnvironmentRegistryImplementation(serverUrl, deploymentRegistry, user, password) {
			
			/* (non-Javadoc)
			 * @see com.mercatis.lighthouse3.persistence.commons.rest.DomainModelEntityDAOImplementation#newEntity()
			 */
			@Override
			protected Environment newEntity() {
				return new LateBindingEnvironment();
			}
			
			/* (non-Javadoc)
			 * @see com.mercatis.lighthouse3.persistence.commons.rest.DomainModelEntityDAOImplementation#getManagedType()
			 */
			@Override
			public Class getManagedType() {
				return Environment.class;
			}
			
		};
		
		this.invalidate();
	}
	
	public EagerEnvironmentRegistryImplementation(String serverUrl, DeploymentRegistry deploymentRegistry) {
		this(serverUrl, deploymentRegistry, null, null);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries2.EagerDomainModelEntityDAOImplementation#delegateRegistry()
	 */
	@Override
	protected DomainModelEntityDAO<Environment> delegateRegistry() {
		return delegateRegistry;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry#register(com.mercatis.lighthouse3.domainmodel.environment.Environment)
	 */
	public void register(Environment environment) {
		this.persist(environment);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry#reRegister(com.mercatis.lighthouse3.domainmodel.environment.Environment)
	 */
	public void reRegister(Environment environment) {
		this.update(environment);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry#unregister(com.mercatis.lighthouse3.domainmodel.environment.Environment)
	 */
	public void unregister(Environment environment) {
		this.delete(environment);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#getManagedType()
	 */
	public Class getManagedType() {
		return Environment.class;
	}
}
