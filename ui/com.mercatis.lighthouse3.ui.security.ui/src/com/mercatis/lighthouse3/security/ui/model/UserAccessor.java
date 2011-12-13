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
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.internal.Security;

/**
 * This class specializes {@link AbstractAccessor} as an UserAccessor.
 * 
 * @see AbstractAccessor
 */
public class UserAccessor extends AbstractAccessor {

	/**
	 * The {@link User} this {@link UserAccessor} represents
	 */
	private User user;
	
	/**
	 * A neat {@link Image} shared over all instances
	 */
	private static Image image;
	
	/**
	 * Creates a new instance of {@link UserAccessor}
	 * with given {@link LighthouseDomain} and {@link User}
	 * 
	 * @param lighthouseDomain
	 * @param user
	 */
	public UserAccessor(LighthouseDomain lighthouseDomain, User user) {
		super(lighthouseDomain);
		this.user = user;
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.model.AbstractAccessor#getLabel()
	 */
	@Override
	public String getLabel() {
		return LabelConverter.getLabel(user);
	}

	public User getUser() {
		return this.user;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.model.AbstractAccessor#getAssignments(java.lang.String)
	 */
	@Override
	protected List<ContextRoleAssignment> getAssignments(String context) {
		return Security.getService().findAssignmentsForUserAndContext(lighthouseDomain.getProject(), getUser(), context);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.model.AbstractAccessor#getImage()
	 */
	@Override
	public Image getImage() {
		if (image == null) {
			image = ImageDescriptor.createFromURL(getClass().getResource("/icons/user.png")).createImage();
		}
		return image;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.model.AbstractAccessor#setCodeOnAssignment(com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignment)
	 */
	@Override
	protected void setCodeOnAssignment(ContextRoleAssignment assignment) {
		assignment.setUserCode(getUser().getCode());
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.model.AbstractAccessor#loadContexts()
	 */
	@Override
	protected List<String> loadContexts() {
		return Security.getService().findContextsForUser(lighthouseDomain.getProject(), getUser());
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.model.AbstractAccessor#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof UserAccessor) {
			UserAccessor other = (UserAccessor) obj;
			return user.equals(other.user)
			&& lighthouseDomain.equals(other.lighthouseDomain);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.model.AbstractAccessor#hashCode()
	 */
	@Override
	public int hashCode() {
		return getUser().hashCode() * 17;
	}
}
