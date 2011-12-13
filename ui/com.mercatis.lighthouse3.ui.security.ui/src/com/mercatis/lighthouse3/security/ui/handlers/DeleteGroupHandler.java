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
import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.security.ui.model.GroupAccessor;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.internal.Security;


public class DeleteGroupHandler extends AbstractDeleteHandler {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler#execute(java.lang.Object)
	 */
	@Override
	protected void execute(Object element) throws ExecutionException {
		Group group = null;
		
		if (element instanceof GroupAccessor) {
			group = ((GroupAccessor)element).getGroup();
		} else if (element instanceof Group) {
			group = (Group) element;
		}
		
		if (group != null) {
			LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomain(group.getLighthouseDomain());
			Security.getService().deleteGroup(lighthouseDomain.getProject(), group);
		}
	}
}
