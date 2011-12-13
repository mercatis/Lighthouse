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
package com.mercatis.lighthouse3.domainmodel.users;

import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntityDAO;
import java.util.List;

/**
 * The group registry interface abstractly defines the contract for a
 * persistent repository of groups. It provides basic CRUD functionality.
 * It can be implemented using different persistence technologies.
 *
 * With regard to persistence, the following rules must apply for
 * implementations:
 *
 * <ul>
 * <li>groups are uniquely identified by their <code>id</code>.
 * <li>groups are uniquely identified by their <code>code</code>.
 * <li>groups are always fully loaded including their users.
 * <li>groups are persisted along with their attached users.
 * </ul>
 */
public interface GroupRegistry extends CodedDomainModelEntityDAO<Group> {

    /**
     * Gather all groups of an user with the given userCode.
     *
     * @param userCode of an user
     * @return a <code>List<Group></code> found for this userCode.
     */
    public List<Group> getGroupsForUserCode(String userCode);

    /**
     * Get a list of all available group codes.
     *
     * @return a list of all group codes available.
     */
    public List<String> findAllGroupCodes();
    
    public UserCodeRegistration getUserCodeRegistration(String userCode);
}
