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
 * The user registry interface abstractly defines the contract for a
 * persistent repository of users. It provides basic CRUD functionality.
 * It can be implemented using different persistence technologies.
 *
 * With regard to persistence, the following rules must apply for
 * implementations:
 *
 * <ul>
 * <li>users are uniquely identified by their <code>id</code>.
 * <li>users are uniquely identified by their <code>code</code>.
 * </ul>
 */
public interface UserRegistry extends CodedDomainModelEntityDAO<User> {

    /**
     * Get a list of all available user codes.
     *
     * @return a list of all user codes available.
     */
    public List<String> findAllUserCodes();

    /**
     * Try to authenticate a user with given code and password.
     * <br />On success the user object will be returned, otherwise <code>null</code>.
     *
     * @param userCode
     * @param password
     * @return user on success, otherwise <code>null</code>
     */
    public User authenticate(String userCode, String password);
}
