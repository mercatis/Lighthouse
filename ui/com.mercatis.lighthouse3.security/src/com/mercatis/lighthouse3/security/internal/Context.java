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
package com.mercatis.lighthouse3.security.internal;

public class Context {

	private final Context parentContext;
	
	private final String context;
	
	/**
	 * @param context
	 */
	public Context(String context) {
		if (!context.startsWith("//"))
			throw new IllegalArgumentException("Invalid context path format: "+context);
		
		while (context.substring(2).endsWith("/"))
			context = context.substring(0, context.length() - 1);
		
		if (context.substring(2).contains("/")) {
			this.parentContext = new Context(context.substring(0, context.lastIndexOf("/")));
			this.context = context.substring(context.lastIndexOf("/") + 1);
		} else {
			this.parentContext = null;
			this.context = context.substring(2);
		}
	}
	
	/**
	 * @param parentContext
	 * @param context
	 */
	public Context(Context parentContext, String context) {
		this.parentContext = parentContext;
		this.context = context;
	}
	
	/**
	 * @return
	 */
	public Context getParentContext() {
		return parentContext;
	}
	
	/**
	 * @return
	 */
	public String getContext() {
		return context;
	}
	
	public boolean isParentContext(Context childContext) {
		Context tmp = childContext;
		while (tmp != null) {
			if (this.equals(tmp))
				return true;
			
			tmp = tmp.parentContext;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		result = prime * result + ((parentContext == null) ? 0 : parentContext.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!getClass().equals(obj.getClass()))
			return false;
		Context other = (Context) obj;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		if (parentContext == null) {
			if (other.parentContext != null)
				return false;
		} else if (!parentContext.equals(other.parentContext))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		this.toString(builder);
		
		return builder.toString();
	}
	
	public void toString(StringBuilder builder) {
		if (this.parentContext != null) {
			this.parentContext.toString(builder);
			builder.append("/");
		} else {
			builder.append("//");
		}

		builder.append(context);
	}
	
}
