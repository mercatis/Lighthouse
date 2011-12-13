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
package com.mercatis.lighthouse3.base.ui.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;

/**
 * This editor input is able to provide the entity to be edited as well as
 * any kind of payload that may be interpreted elsewhere.
 * 
 */
public class GenericEditorInput<T> implements IEditorInput {

	/**
	 * The domain of the edited entity
	 */
	private LighthouseDomain lighthouseDomain;
	
	/**
	 * The edited entity
	 */
	private T entity;
	
	/**
	 * Optional multi-purpose-payload
	 */
	private Object payload = null;

	public GenericEditorInput(LighthouseDomain lighthouseDomain, T entity) {
		this.lighthouseDomain = lighthouseDomain;
		this.entity = entity;
	}

	public LighthouseDomain getDomain() {
		if (lighthouseDomain == null)
			lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(entity);
		return lighthouseDomain;
	}

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
	}

	public String getName() {
		return LabelConverter.getLabel(getEntity());
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return LabelConverter.getLabel(getEntity());
	}

	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((getDomain() == null) ? 0 : getDomain().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		GenericEditorInput<?> other = (GenericEditorInput<?>) obj;

		if (payload == null) {
			if (other.payload != null) {
				return false;
			}
		} else {
			if (!payload.equals(other.payload)) {
				return false;
			}
		}

		if (entity == null) {
			if (other.entity != null) {
				return false;
			}
		} else if (!entity.equals(other.entity)) {
			return false;
		}

		if (getDomain() == null) {
			if (other.getDomain() != null) {
				return false;
			}
		} else if (!getDomain().equals(other.getDomain())) {
			return false;
		}
		return true;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}

	public Object getPayload() {
		return payload;
	}
}
