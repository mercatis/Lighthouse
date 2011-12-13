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
import java.util.Map;
import java.util.Set;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.persistence.environment.rest.SoftwareComponentRegistryImplementation;


@SuppressWarnings("rawtypes")
public class EagerSoftwareComponentRegistryImplementation extends EagerHierarchicalDomainModelEntityDAOImplementation<SoftwareComponent>
	implements SoftwareComponentRegistry {
	
	@SuppressWarnings("serial")
	protected class LateBindingSoftwareComponent extends SoftwareComponent
		implements LateBindingDomainModelEntity<SoftwareComponent> {
		
		private SoftwareComponent delegateEntity = new SoftwareComponent();
		
		private String parentCode = null;
		
		/* (non-Javadoc)
		 * @see com.mercatis.lighthouse3.services.internal.registries2.LateBindingDomainModelEntity#bind()
		 */
		public LateBindingDomainModelEntity<SoftwareComponent> bind() {
			try {
				if (parentCode != null) {
					SoftwareComponent parent = EagerSoftwareComponentRegistryImplementation.this.findByCode(parentCode);

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
		public SoftwareComponent getDelegateEntity() {
			return delegateEntity;
		}

		/**
		 * @param subEntity
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#addSubEntity(com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity)
		 */
		public void addSubEntity(SoftwareComponent subEntity) {
			delegateEntity.addSubEntity(subEntity);
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
			} catch (Exception anythingElse) {
				throw new XMLSerializationException("Invalid XML representation of entity: ", anythingElse);
			}
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
		 * @see com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent#getContact()
		 */
		public String getContact() {
			return delegateEntity.getContact();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent#getContactEmail()
		 */
		public String getContactEmail() {
			return delegateEntity.getContactEmail();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent#getCopyright()
		 */
		public String getCopyright() {
			return delegateEntity.getCopyright();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent#getDescription()
		 */
		public String getDescription() {
			return delegateEntity.getDescription();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#getDirectSubEntities()
		 */
		public Set<SoftwareComponent> getDirectSubEntities() {
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
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#getLocalProperties()
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
		 * @see com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent#getLongName()
		 */
		public String getLongName() {
			return delegateEntity.getLongName();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#getParentEntity()
		 */
		public SoftwareComponent getParentEntity() {
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
		public SoftwareComponent getRootEntity() {
			return delegateEntity.getRootEntity();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#getSubEntities()
		 */
		public Set<SoftwareComponent> getSubEntities() {
			return delegateEntity.getSubEntities();
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent#getVersion()
		 */
		public String getVersion() {
			return delegateEntity.getVersion();
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
		public boolean isDirectSubEntity(SoftwareComponent entity) {
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
		public boolean isSubEntity(SoftwareComponent entity) {
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
		public void removeSubEntity(SoftwareComponent subEntity) {
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
		 * @see com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent#setContact(java.lang.String)
		 */
		public void setContact(String contact) {
			delegateEntity.setContact(contact);
		}

		/**
		 * @param contactEmail
		 * @see com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent#setContactEmail(java.lang.String)
		 */
		public void setContactEmail(String contactEmail) {
			delegateEntity.setContactEmail(contactEmail);
		}

		/**
		 * @param copyright
		 * @see com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent#setCopyright(java.lang.String)
		 */
		public void setCopyright(String copyright) {
			delegateEntity.setCopyright(copyright);
		}

		/**
		 * @param description
		 * @see com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent#setDescription(java.lang.String)
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
		 * @see com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent#setLongName(java.lang.String)
		 */
		public void setLongName(String longName) {
			delegateEntity.setLongName(longName);
		}

		/**
		 * @param parentEntity
		 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity#setParentEntity(com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity)
		 */
		public void setParentEntity(SoftwareComponent parentEntity) {
			delegateEntity.setParentEntity(parentEntity);
		}

		/**
		 * @param version
		 * @see com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent#setVersion(java.lang.String)
		 */
		public void setVersion(String version) {
			delegateEntity.setVersion(version);
		}

		/**
		 * @return
		 * @see com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent#toQueryParameters()
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

	private SoftwareComponentRegistryImplementation delegateRegistry = null;
	
	public EagerSoftwareComponentRegistryImplementation(String serverUrl, String user, String password) {
		this.delegateRegistry = new SoftwareComponentRegistryImplementation(serverUrl, user, password) {
			
			/* (non-Javadoc)
			 * @see com.mercatis.lighthouse3.persistence.commons.rest.DomainModelEntityDAOImplementation#newEntity()
			 */
			@Override
			protected SoftwareComponent newEntity() {
				return new LateBindingSoftwareComponent();
			}
			
			/* (non-Javadoc)
			 * @see com.mercatis.lighthouse3.persistence.commons.rest.DomainModelEntityDAOImplementation#getManagedType()
			 */
			@Override
			public Class getManagedType() {
				return SoftwareComponent.class;
			}
			
		};
		
		this.invalidate();
	}
	
	public EagerSoftwareComponentRegistryImplementation(String serverUrl) {
		this(serverUrl, null, null);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries2.EagerDomainModelEntityDAOImplementation#delegateRegistry()
	 */
	@Override
	protected DomainModelEntityDAO<SoftwareComponent> delegateRegistry() {
		return this.delegateRegistry;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#getManagedType()
	 */
	public Class getManagedType() {
		return SoftwareComponent.class;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry#register(com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent)
	 */
	public void register(SoftwareComponent softwareComponent) {
		this.persist(softwareComponent);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry#reRegister(com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent)
	 */
	public void reRegister(SoftwareComponent softwareComponent) {
		this.update(softwareComponent);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry#unregister(com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent)
	 */
	public void unregister(SoftwareComponent softwareComponent) {
		this.delete(softwareComponent);
	}
	
}
