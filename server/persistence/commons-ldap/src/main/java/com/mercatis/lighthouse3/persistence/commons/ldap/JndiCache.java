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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
import org.apache.log4j.Logger;

/**
 * Cache implementation for CachingDirContext.
 */
public class JndiCache {

    private long expiry;
    private long lastRefresh = -1;
    private Map<SearchRequest, List<SearchResult>> cache = new ConcurrentHashMap<SearchRequest, List<SearchResult>>();
    private Logger log = Logger.getLogger(JndiCache.class.getName());

    public JndiCache(long expiry) {
        this.expiry = expiry;
        this.lastRefresh = System.currentTimeMillis();
        log.debug(String.format("Using JNDI cache with %d minutes expiry", expiry / 60000));
    }

    public NamingEnumeration<SearchResult> put(SearchRequest request, NamingEnumeration<SearchResult> result) {
        List<SearchResult> results = new LinkedList<SearchResult>();
        try {
            while (result.hasMore()) {
                results.add(result.next());
            }
        } catch (NamingException ex) {
            log.error(ex);
        }
        cache.put(request, results);
        log.debug(String.format("Cachsize now %d entries", cache.size()));
        return new EnumeratedSearchResult(results);
    }

    public synchronized NamingEnumeration<SearchResult> get(SearchRequest request) {
        if (System.currentTimeMillis() > lastRefresh + expiry) {
            cache.clear();
            lastRefresh = System.currentTimeMillis();
            log.debug("JNDI cache expired");
        }
        List<SearchResult> result = cache.get(request);
        return result == null ? null : new EnumeratedSearchResult(result);
    }
}
