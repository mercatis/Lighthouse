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
package com.mercatis.lighthouse3.persistence.commons.ldap;

import javax.naming.directory.SearchControls;

/**
 * Instances of this class represent a search request that is mapped to a result.
 * From SearchControls, only the searchScope will be considered.
 */
public class SearchRequest {

    private String name;
    private String filter;
    private int searchScope;

    public SearchRequest(String name, String filter, SearchControls controls) {
        this.name = name;
        this.filter = filter;
        this.searchScope = controls.getSearchScope();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SearchRequest other = (SearchRequest) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.filter == null) ? (other.filter != null) : !this.filter.equals(other.filter)) {
            return false;
        }
        if (this.searchScope != other.searchScope) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 89 * hash + (this.filter != null ? this.filter.hashCode() : 0);
        hash = 89 * hash + this.searchScope;
        return hash;
    }
}
