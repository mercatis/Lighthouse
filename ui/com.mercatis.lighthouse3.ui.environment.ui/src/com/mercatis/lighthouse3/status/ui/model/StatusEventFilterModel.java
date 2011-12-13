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
package com.mercatis.lighthouse3.status.ui.model;

import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.event.providers.EventFilterModel;


public class StatusEventFilterModel extends EventFilterModel {

	/**
	 * @param lighthouseDomain
	 * @param template
	 */
	public StatusEventFilterModel(LighthouseDomain lighthouseDomain, Event template) {
		super(lighthouseDomain, template);
	}

	/**
	 * @param lighthouseDomain
	 */
	public StatusEventFilterModel(LighthouseDomain lighthouseDomain) {
		super(lighthouseDomain);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.widgets.eventfilter.EventFilterModel#isMandatory(int)
	 */
	@Override
	public boolean isMandatory(int propertyIndex) {
		// for the status event filtering, no property is mandatory
		return false;
	}
	
}
