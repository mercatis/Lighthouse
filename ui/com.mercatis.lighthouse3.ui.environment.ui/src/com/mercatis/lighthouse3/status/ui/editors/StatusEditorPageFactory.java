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
package com.mercatis.lighthouse3.status.ui.editors;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.ui.forms.editor.FormEditor;
import com.mercatis.lighthouse3.base.ui.editors.AbstractEditorPageFactory;
import com.mercatis.lighthouse3.base.ui.editors.AbstractLighthouseEditorPage;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.status.ui.editors.pages.StatusOverviewEditorPage;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.status.base.model.StatusEditingObject;


public class StatusEditorPageFactory extends AbstractEditorPageFactory {

	public List<AbstractLighthouseEditorPage> getPages(FormEditor editor) {
		List<AbstractLighthouseEditorPage> pages = new ArrayList<AbstractLighthouseEditorPage>();
		if (editor.getEditorInput() instanceof GenericEditorInput<?>) {
			GenericEditorInput<?> input = (GenericEditorInput<?>) editor.getEditorInput();
			if (input.getEntity() instanceof StatusCarrier) {
				StatusCarrier carrier = (StatusCarrier) input.getEntity();
				LighthouseDomain lighthouseDomain = input.getDomain();
				int pageSize = CommonBaseActivator.getPlugin().getStatusService().getStatusConfiguration(lighthouseDomain).getStatusPageSize();
				int pageNo = CommonBaseActivator.getPlugin().getStatusService().getStatusConfiguration(lighthouseDomain).getStatusPageNo();
				List<Status> statuus = CommonBaseActivator.getPlugin().getStatusService().getPagedStatusesForCarrier(carrier, pageSize, pageNo);
				List<StatusEditingObject> seo = new LinkedList<StatusEditingObject>();
				for (Status status : statuus) {
					if (status == null) {
						System.err.println("StatusEditorPageFactory received list of status with a null element");
						continue;
					}
					seo.add(new StatusEditingObject(status));
				}
				pages.add(new StatusOverviewEditorPage(editor, StatusOverviewEditorPage.class.getName(), "Status", seo));
			}
		}
		return pages;
	}

	@Override
	public FactoryPosition getFactoryPosition() {
		return FactoryPosition.BEFORE_STATIC;
	}
}
