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
package com.mercatis.lighthouse3.security.ui.model;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignment;
import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.internal.Security;

/**
 * This class specializes {@link AbstractAccessor} as an GroupAccessor.
 * 
 * @see AbstractAccessor
 */
public class GroupAccessor extends AbstractAccessor {

	/**
	 * The {@link Group} this {@link GroupAccessor} represents
	 */
	private Group group;
	
	/**
	 * A neat {@link Image} shared over all instances
	 */
	private static Image image;
	
	/**
	 * Creates a new instance of {@link GroupAccessor}
	 * with given {@link LighthouseDomain} and {@link Group}
	 * 
	 * @param lighthouseDomain
	 * @param group
	 */
	public GroupAccessor(LighthouseDomain lighthouseDomain, Group group) {
		super(lighthouseDomain);
		this.group = group;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.model.AbstractAccessor#getLabel()
	 */
	@Override
	public String getLabel() {
		return LabelConverter.getLabel(group);
	}

	public Group getGroup() {
		return this.group;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.model.AbstractAccessor#getAssignments(java.lang.String)
	 */
	@Override
	protected List<ContextRoleAssignment> getAssignments(String context) {
		return Security.getService().findAssignmentsForGroupAndContext(lighthouseDomain.getProject(), getGroup(), context);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.model.AbstractAccessor#getImage()
	 */
	@Override
	public Image getImage() {
		if (image == null) {
			image = ImageDescriptor.createFromURL(getClass().getResource("/icons/group.png")).createImage();
		}
		return image;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.model.AbstractAccessor#setCodeOnAssignment(com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignment)
	 */
	@Override
	protected void setCodeOnAssignment(ContextRoleAssignment assignment) {
		assignment.setGroupCode(getGroup().getCode());
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.model.AbstractAccessor#loadContexts()
	 */
	@Override
	protected List<String> loadContexts() {
		return Security.getService().findContextsForGroup(lighthouseDomain.getProject(), getGroup());
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.model.AbstractAccessor#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof GroupAccessor) {
			GroupAccessor other = (GroupAccessor) obj;
			return group.equals(other.group)
			&& lighthouseDomain.equals(other.lighthouseDomain);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.model.AbstractAccessor#hashCode()
	 */
	@Override
	public int hashCode() {
		return getGroup().hashCode() * 17;
	}
}
