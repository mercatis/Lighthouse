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
package com.mercatis.lighthouse3.ui.service;

import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.domainmodel.users.User;

/**
 * Implementations of this interface may register as listener in the {@link SecurityService}.
 * 
 */
public interface SecurityModelChangedListener {
	
	/**
	 * A {@link User} was created.
	 * 
	 * @param user The new {@link User}
	 */
	public void userCreated(User user);
	
	/**
	 * A {@link User} was deleted.
	 * 
	 * @param user The deleted {@link User}
	 */
	public void userDeleted(User user);
	
	/**
	 * A {@link Group} was created.
	 * 
	 * @param group The new {@link Group}
	 */
	public void groupCreated(Group group);

	/**
	 * A {@link Group} was deleted.
	 * 
	 * @param group The deleted {@link Group}
	 */
	public void groupDeleted(Group group);
}
