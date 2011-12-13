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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.PlatformUI;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.UnitOfWork;
import com.mercatis.lighthouse3.services.EagerRegistry;
import com.mercatis.lighthouse3.services.Services;


public abstract class EagerDomainModelEntityDAOImplementation<Entity extends DomainModelEntity>
	implements DomainModelEntityDAO<Entity>, EagerRegistry {

	protected Map<String, Entity> cache = new HashMap<String, Entity>();
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#alreadyPersisted(com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity)
	 */
	public boolean alreadyPersisted(Entity entity) {
		return this.cache.containsValue(entity);
	}
	
	protected abstract DomainModelEntityDAO<Entity> delegateRegistry();

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#delete(com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity)
	 */
	public void delete(Entity entityToDelete) {
		delegateRegistry().delete(entityToDelete);
		this.cache.remove(keyForEntity(entityToDelete));
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#find(long)
	 */
	public Entity find(long id) {
		for (Entity entity : this.cache.values()) {
			if (entity.getId() == id) {
				return entity;
			}
		}
		Entity entity = delegateRegistry().find(id);
		if (entity != null)
			cache.put(keyForEntity(entity), entity);
		
		return entity;
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#findAll()
	 */
	public List<Entity> findAll() {
		return new ArrayList<Entity>(this.cache.values());
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#findByTemplate(com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity)
	 */
	public List<Entity> findByTemplate(Entity template) {
		//TODO create equalsByTemplate(Entity template) or something similar in DomainModelEntity
		List<Entity> entities = delegateRegistry().findByTemplate(template);
		for (Entity entity : entities) {
			if (entity != null)
				this.cache.put(keyForEntity(entity), entity);
		}
		
		return entities;
	}
	
	public String getLighthouseDomain() {
		return delegateRegistry().getLighthouseDomain();
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#getUnitOfWork()
	 */
	public UnitOfWork getUnitOfWork() {
		return null;
	}

	/* (non-Javadoc)
	 * 	@see com.mercatis.lighthouse3.services.EagerRegistry#invalidate()
	 */
	public void invalidate() {
		this.cache.clear();
		try {
			List<Entity> entities = delegateRegistry().findAll();
			
			for (Entity entity: entities) {
				if (entity != null)
					this.cache.put(keyForEntity(entity), entity);
			}
		} catch (Throwable t) {
			final Status status = new Status(IStatus.ERROR, Services.PLUGIN_ID, "Caching error", t);
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Error", getLighthouseDomain() + " could not refresh cache.", status);
				}
			});
		}
	}

	protected abstract String keyForEntity(Entity entity);

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#persist(com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity)
	 */
	public void persist(Entity entityToPersist) {
		delegateRegistry().persist(entityToPersist);
		List<Entity> tmp = delegateRegistry().findByTemplate(entityToPersist);
		if (tmp != null) {
			for (Iterator<Entity> it = tmp.iterator(); it.hasNext();) {
				Entity entity = (Entity) it.next();
				if (entity != null)
					this.cache.put(keyForEntity(entity), entity);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#update(com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity)
	 */
	public void update(Entity entityToUpdate) {
		delegateRegistry().update(entityToUpdate);
		if (entityToUpdate != null)
			this.cache.put(keyForEntity(entityToUpdate), entityToUpdate);
	}

}
