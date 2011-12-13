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

import java.util.Properties;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

/**
 * This class extends the InitialDirContext and adds some caching methods. The cache maps a search
 * request to the result.
 */
public class CachingDirContext extends InitialLdapContext {

    private JndiCache cache;

    public CachingDirContext(JndiCache cache, Properties config) throws NamingException {
        super(config, null);
        this.cache = cache;
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls controls) throws NamingException {
        SearchRequest request = new SearchRequest(name, filter, controls);
        NamingEnumeration<SearchResult> result = cache.get(request);
        if (result == null) {
            result = cache.put(request, super.search(name, filter, controls));
        }
        return result;
    }
}
