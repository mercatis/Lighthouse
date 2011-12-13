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
package com.mercatis.lighthouse3.ui.status.base.service;

import java.util.List;

import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusChange;
import com.mercatis.lighthouse3.domainmodel.status.StatusHistogram;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.status.base.StatusModelChangedListener;

public interface StatusService {

	/**
	 * @param lighthouseDomain
	 * @return
	 */
	public StatusConfiguration getStatusConfiguration(LighthouseDomain lighthouseDomain);

	public List<Status> getPagedStatusesForCarrier(StatusCarrier statusCarrier, int pageSize, int pageNo);

	public StatusHistogram getStatusHistogramForObject(LighthouseDomain lighthouseDomain, Object object);
	
	public Status refresh(Status status, int pageSize, int pageNo);
	
	public void persistStatus(Status status);

	public void updateStatus(Status status);

	public void deleteStatus(Status status);

	public void clearStatusManually(Status status, String reason, String clearer);
	
	public LighthouseDomain getLighthouseDomainForEntity(Object entity);
	
	public StatusChange getLastChangeForStatus(Status status);
	
	public Status findStatusByCode(LighthouseDomain lighthouseDomain, String code);
	
	public void addStatusModelChangedListener(StatusModelChangedListener listener);

	public void removeStatusModelChangedListener(StatusModelChangedListener listener);
	
	public void openLighthouseDomain(LighthouseDomain lighthouseDomain);
	
	public void closeLighthouseDomain(LighthouseDomain lighthouseDomain);
	
	public void closeAllLighthouseDomains();
}
