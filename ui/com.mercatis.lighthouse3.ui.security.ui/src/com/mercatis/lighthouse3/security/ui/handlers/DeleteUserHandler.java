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
/**
 * 
 */
package com.mercatis.lighthouse3.security.ui.handlers;

import org.eclipse.core.commands.ExecutionException;
import com.mercatis.lighthouse3.base.ui.handlers.AbstractDeleteHandler;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.security.ui.model.UserAccessor;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.internal.Security;


public class DeleteUserHandler extends AbstractDeleteHandler {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler#execute(java.lang.Object)
	 */
	@Override
	protected void execute(Object element) throws ExecutionException {
		User user = null;
		
		if (element instanceof UserAccessor) {
			user = ((UserAccessor)element).getUser();
		} else if (element instanceof User) {
			user = (User) element;
		}
		
		if (user != null) {
			LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomain(user.getLighthouseDomain());
			Security.getService().deleteUser(lighthouseDomain.getProject(), user);
		}
	}
}
