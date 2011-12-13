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

import java.util.List;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;

/**
 * Implementation of NamingEnumeration to fetch LDAP search results.
 */
public class EnumeratedSearchResult implements NamingEnumeration<SearchResult> {

    private boolean closed = false;
    private SearchResult[] results = new SearchResult[0];
    private int iterator = 0;

    public EnumeratedSearchResult(List<SearchResult> results) {
        if (results != null) {
            this.results = results.toArray(this.results);
        }
    }

    public SearchResult next() throws NamingException {
        return nextElement();
    }

    public boolean hasMore() throws NamingException {
        return hasMoreElements();
    }

    public void close() throws NamingException {
        for (int i = 0; i < results.length; i++) {
            results[i] = null;
        }
        closed = true;
    }

    public boolean hasMoreElements() {
        return !closed && results.length > iterator;
    }

    public SearchResult nextElement() {
        return results[iterator++];
    }
}
