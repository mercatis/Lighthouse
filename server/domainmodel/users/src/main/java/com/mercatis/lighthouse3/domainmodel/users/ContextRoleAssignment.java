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

import java.io.IOException;
import java.util.Map;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;

/**
 * This class represents the permission/restriction to do something (<code>role</code>) on some <code>context</code.
 */
public class ContextRoleAssignment extends DomainModelEntity {

	private static final long serialVersionUID = 4925684877447696549L;
	public static final int DENY = 0;
    public static final int GRANT = 1;

    /**
     * This property is used to assign this context/role to a group via the groups code.
     */
    private String groupCode;

    /**
     * @return the groupCode
     */
    public String getGroupCode() {
        return groupCode;
    }

    /**
     * @param groupCode the groupCode to set
     */
    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    /**
     * This property is used to assign this context/role to a user via the users code.
     */
    private String userCode;

    /**
     * @return the userCode
     */
    public String getUserCode() {
        return userCode;
    }

    /**
     * @param userCode the userCode to set
     */
    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    /**
     * This property defines what should be allowed/denied.
     */
    private String role;

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * This property defines in which context this role is allowed/denied.
     */
    private String context;

    /**
     * @return the context
     */
    public String getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * This property defines whether this a restrictive or permissive role context assignment.
     * <br />Possible values are:
     * <ul>
     * <li><code>ContextRoleAssignment.DENY</code></li>
     * <li><code>ContextRoleAssignment.GRANT</code></li>
     * </ul>
     * Default value is ContextRoleAssignment.GRANT
     */
    private int permissionType = GRANT;

    /**
     * @return the permissionType
     */
    public int getPermissionType() {
        return permissionType;
    }

    /**
     * @param permissionType the permissionType to set
     */
    public void setPermissionType(int permissionType) {
        if (permissionType != DENY && permissionType != GRANT)
            throw new IllegalArgumentException("permissionType should be either DENY==0 or GRANT==1, not " + permissionType);
        this.permissionType = permissionType;
    }

    @Override
    protected void readPropertiesFromXml(XmlMuncher xmlDocument) {
        super.readPropertiesFromXml(xmlDocument);
        this.setGroupCode(xmlDocument.readValueFromXml("/*/:groupCode"));
        this.setUserCode(xmlDocument.readValueFromXml("/*/:userCode"));
        this.setRole(xmlDocument.readValueFromXml("/*/:role"));
        this.setContext(xmlDocument.readValueFromXml("/*/:context"));
        this.setPermissionType(Integer.parseInt(xmlDocument.readValueFromXml("/*/:permissionType")));
        String id = xmlDocument.readValueFromXml("/*/:id");
        if (id != null) {
            this.setId(Long.parseLong(id));
        }
    }

    @Override
    public Map<String, String> toQueryParameters() {
        Map<String, String> parameters = super.toQueryParameters();

        if (this.getGroupCode() != null) {
            parameters.put("groupCode", this.getGroupCode());
        }
        if (this.getUserCode() != null) {
            parameters.put("userCode", this.getUserCode());
        }
        if (this.getRole() != null) {
            parameters.put("role", this.getRole());
        }
        if (this.getContext() != null) {
            parameters.put("context", this.getContext());
        }
        parameters.put("permissionType", Integer.toString(this.getPermissionType()));
        parameters.put("id", Long.toString(this.getId()));
        return parameters;
    }

    @Override
    protected void fillRootElement(XmlWriter xml) throws IOException {
        super.fillRootElement(xml);
        if (this.getGroupCode() != null) {
            xml.writeEntityWithText("groupCode", this.getGroupCode());
        }
        if (this.getUserCode() != null) {
            xml.writeEntityWithText("userCode", this.getUserCode());
        }
        if (this.getRole() != null) {
            xml.writeEntityWithText("role", this.getRole());
        }
        if (this.getContext() != null) {
            xml.writeEntityWithText("context", this.getContext());
        }
        xml.writeEntityWithText("permissionType", this.getPermissionType());
        xml.writeEntityWithText("id", this.getId());
    }

    @Override
    public void fromQueryParameters(Map<String, String> queryParameters) {
        String value = queryParameters.remove("permissionType");
        if (value != null)
            this.setPermissionType(Integer.parseInt(value));
        super.fromQueryParameters(queryParameters);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ContextRoleAssignment other = (ContextRoleAssignment) obj;
        if ((this.getGroupCode() == null) ? (other.getGroupCode() != null) : !this.groupCode.equals(other.groupCode)) {
            return false;
        }
        if ((this.getUserCode() == null) ? (other.getUserCode() != null) : !this.userCode.equals(other.userCode)) {
            return false;
        }
        if ((this.getRole() == null) ? (other.getRole() != null) : !this.role.equals(other.role)) {
            return false;
        }
        if ((this.getContext() == null) ? (other.getContext() != null) : !this.context.equals(other.context)) {
            return false;
        }
        if (this.getPermissionType() != other.getPermissionType()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.getGroupCode() != null ? this.getGroupCode().hashCode() : 0);
        hash = 73 * hash + (this.getUserCode() != null ? this.getUserCode().hashCode() : 0);
        hash = 73 * hash + (this.getRole() != null ? this.getRole().hashCode() : 0);
        hash = 73 * hash + (this.getContext() != null ? this.getContext().hashCode() : 0);
        hash = 73 * hash + this.getPermissionType();
        return hash;
    }
}
