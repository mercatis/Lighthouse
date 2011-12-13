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
package com.mercatis.lighthouse3.ui.swimlaneeditor.gef.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import com.mercatis.lighthouse3.base.ui.handlers.AbstractDeleteHandler;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.ProcessTaskContainer;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts.ProcessTaskModelEditPart;

public class DeleteProcessTaskModelHandler extends AbstractDeleteHandler {

	private Set<ProcessTaskContainer> containers;

	@Override
	protected void execute(Object element) throws ExecutionException {
		ProcessTaskModelEditPart model = (ProcessTaskModelEditPart) element;
		ProcessTask processTask = model.getProcessTask();
		LighthouseDomain lighthouseDomain = domainService.getLighthouseDomainByEntity(processTask);
		ProcessTask parentEntity = processTask.getParentEntity();
		if (parentEntity != null) {
			parentEntity.removeSubEntity(processTask);
			domainService.updateProcessTask(parentEntity);
		}
		domainService.deleteProcessTask(processTask);
		containers.add(lighthouseDomain.getProcessTaskContainer());
	}

	@Override
	protected void postExecution(ExecutionEvent event) throws ExecutionException {
		for (ProcessTaskContainer container : containers) {
			CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(container);
		}
	}

	@Override
	protected void preExecution(ExecutionEvent event) throws ExecutionException {
		containers = new HashSet<ProcessTaskContainer>();
	}

	@Override
	protected boolean showDialog(Object[] elements) {
		ArrayList<ProcessTask> items = new ArrayList<ProcessTask>(elements.length);
		for (Object element : elements) {
			ProcessTaskModelEditPart model = (ProcessTaskModelEditPart) element;
			items.add(model.getProcessTask());
		}
		return super.showDialog(items.toArray());
	}
}
