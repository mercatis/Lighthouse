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
package com.mercatis.lighthouse3.security.ui.providers;

import com.mercatis.lighthouse3.base.ui.provider.ILabelConverterHelper;
import com.mercatis.lighthouse3.domainmodel.users.Group;


public class GroupConverter implements ILabelConverterHelper {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.provider.ILabelConverterHelper#getLabelForObject(java.lang.Object)
	 */
	public String getLabelForObject(Object obj) {
		String label = ((Group)obj).getLongName();
		if (label == null || label.length() == 0)
			label = ((Group)obj).getCode();
		return label;
	}
}
