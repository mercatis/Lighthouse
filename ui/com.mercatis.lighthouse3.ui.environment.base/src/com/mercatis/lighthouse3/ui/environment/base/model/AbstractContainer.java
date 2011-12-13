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
package com.mercatis.lighthouse3.ui.environment.base.model;

import org.eclipse.core.runtime.PlatformObject;


public abstract class AbstractContainer extends PlatformObject {

	private LighthouseDomain lighthouseDomain;

	/**
	 * @param lighthouseDomain
	 */
	public AbstractContainer(LighthouseDomain lighthouseDomain) {
		this.lighthouseDomain = lighthouseDomain;
	}

	/**
	 * @return
	 */
	public LighthouseDomain getLighthouseDomain() {
		return lighthouseDomain;
	}

	/**
	 * @param lighthouseDomain
	 */
	protected void setLighthouseDomain(LighthouseDomain lighthouseDomain) {
		this.lighthouseDomain = lighthouseDomain;
	}

}
