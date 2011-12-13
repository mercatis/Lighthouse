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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;

/**
 * This class represents a user and will be used for access control purposes.
 */
public class User extends CodedDomainModelEntity {

    private static final long serialVersionUID = 1261893486822283492L;

    /**
     * The password of the user hashed via SHA and encoded in Base64
     */
    private String password;

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * This method is reserved for serialization purposes only.
     * <br />To set a new password use <code>setAndHashPassword(String password)</code>
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets and hashes the given password.
     * <br />The resulting hash will be stored Base64 encoded.
     *
     * @param password to set and hash/encode
     */
    public void setAndHashPassword(String password) {
        this.password = getBase64CodedHashForPassword(password);
    }
    /**
     * This property stores the given name of an user.
     */
    private String givenName;

    /**
     * @return the givenName
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * @param givenName the givenName to set
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }
    /**
     * This property stores the surname of an user.
     */
    private String surName;

    /**
     * @return the surName
     */
    public String getSurName() {
        return surName;
    }

    /**
     * @param surName the surName to set
     */
    public void setSurName(String surName) {
        this.surName = surName;
    }
    /**
     * This property stores an email address of an user that can be used for inquiries.
     */
    private String contactEmail;

    /**
     * @return the contactEmail
     */
    public String getContactEmail() {
        return contactEmail;
    }

    /**
     * @param contactEmail the contactEmail to set
     */
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    /**
     * Hashes the given password and encodes the resulting byte array in a Base64 string.
     * <br />SHA is used for hashing.
     *
     * @param password to hash and encode
     * @return the Base64 encoded password hash
     */
    private String getBase64CodedHashForPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            password = XmlMuncher.byteArrayToXmlBinary(md.digest(password.getBytes()));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
        return password;
    }

    /**
     * Provide the unhashed password and compare against the existing hash.
     *
     * @param password to compare
     * @return true if hash of argument is equal to existing password
     */
    public boolean comparePassword(String password) {
        return this.password.equals(getBase64CodedHashForPassword(password));
    }

    @Override
    protected void fillRootElement(XmlWriter xml) throws IOException {
        super.fillRootElement(xml);
        if (this.getPassword() != null) {
            xml.writeEntityWithText("password", this.getPassword());
        }
        if (this.getGivenName() != null) {
            xml.writeEntityWithText("givenName", this.getGivenName());
        }
        if (this.getSurName() != null) {
            xml.writeEntityWithText("surName", this.getSurName());
        }
        if (this.getContactEmail() != null) {
            xml.writeEntityWithText("contactEmail", this.getContactEmail());
        }
    }

    @Override
    protected void readPropertiesFromXml(XmlMuncher xmlDocument) {
        super.readPropertiesFromXml(xmlDocument);
        this.setPassword(xmlDocument.readValueFromXml("/*/:password"));
        this.setGivenName(xmlDocument.readValueFromXml("/*/:givenName"));
        this.setSurName(xmlDocument.readValueFromXml("/*/:surName"));
        this.setContactEmail(xmlDocument.readValueFromXml("/*/:contactEmail"));
    }

    @Override
    public Map<String, String> toQueryParameters() {
        Map<String, String> parameters = super.toQueryParameters();

        if (getContactEmail() != null) {
            parameters.put("contactEmail", getContactEmail());
        }
        if (getSurName() != null) {
            parameters.put("surName", getSurName());
        }
        if (getGivenName() != null) {
            parameters.put("givenName", getGivenName());
        }
        if (getPassword() != null) {
            parameters.put("password", getPassword());
        }

        return parameters;
    }

    @SuppressWarnings("rawtypes")
	@Override
    protected void resolveEntityReferencesFromXml(XmlMuncher xmlDocument, DomainModelEntityDAO... resolversForEntityReferences) {
        super.resolveEntityReferencesFromXml(xmlDocument, resolversForEntityReferences);
    }
}
