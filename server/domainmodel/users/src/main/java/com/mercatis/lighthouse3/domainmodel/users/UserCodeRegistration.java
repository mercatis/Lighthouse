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


/**
 * This mapping class is needed due to hibernate's inability to create subqueries via criterions that search in plain <code>Set&lt;String&gt;</code>
 */
public class UserCodeRegistration {

    private String userCode;
    
    public String getUserCode() {
        return userCode;
    }

    public UserCodeRegistration setUserCode(String userCode) {
        this.userCode = userCode;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserCodeRegistration other = (UserCodeRegistration) obj;
        if ((this.userCode == null) ? (other.userCode != null) : !this.userCode.equals(other.userCode)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (this.userCode != null ? this.userCode.hashCode() : 0);
        return hash;
    }
}
