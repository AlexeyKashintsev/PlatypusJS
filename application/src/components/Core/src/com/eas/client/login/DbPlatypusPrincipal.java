/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.login;

import java.util.Collections;
import java.util.Set;

/**
 *
 * @author mg
 */
public class DbPlatypusPrincipal extends PlatypusPrincipal {

    private final String context;
    private final String email;
    private final String phone;
    private final String startAppElement;
    private final Set<String> roles;

    public DbPlatypusPrincipal(String aUsername, String aContext, String aEmail, String aPhone, String aStartAppElement, Set<String> aRoles) {
        super(aUsername);
        context = aContext;
        email = aEmail;
        phone = aPhone;
        startAppElement = aStartAppElement;
        roles = aRoles;
    }

    @Override
    public boolean hasRole(String aRole) {
        return roles != null ? roles.contains(aRole) : false;
    }

    public Set<String> getRoles() {
        return roles != null ? Collections.unmodifiableSet(roles) : null;
    }

    public String getContext() {
        return context;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getStartAppElement() {
        return startAppElement;
    }
}