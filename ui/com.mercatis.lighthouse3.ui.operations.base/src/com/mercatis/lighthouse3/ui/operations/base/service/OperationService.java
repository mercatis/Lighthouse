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
package com.mercatis.lighthouse3.ui.operations.base.service;

import java.util.List;

import com.mercatis.lighthouse3.domainmodel.operations.Operation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.base.model.Category;


public interface OperationService {

	public List<String> findAllCodes(LighthouseDomain lighthosueDomain);
	
	public List<Category<Operation>> findAllCategories(LighthouseDomain lighthouseDomain);
	
	public List<String> findAllCategoryStrings(LighthouseDomain lighthouseDomain);
	
	public List<Operation> findByCategory(LighthouseDomain lighthouseDomain, String category);
	
	public Operation findByCode(LighthouseDomain lighthouseDomain, String code);
	
	public Operation findInstalled(LighthouseDomain lighthouseDomain, OperationInstallation operationInstallation);
	
	public void persist(LighthouseDomain lighthouseDomain, Operation operation);
	
	public void update(LighthouseDomain lighthouseDomain, Operation operation);
	
	public void delete(LighthouseDomain lighthouseDomain, Operation operation);
	
	public OperationConfiguration getOperationConfiguration(LighthouseDomain lighthouseDomain);
}
