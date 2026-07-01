package com.heavyequip.rental.entity;

/**
 * Defines the three user roles in the system.
 * Used by Spring Security for role-based authorization (e.g. hasRole("OWNER")).
 */
public enum Role {
    CUSTOMER,
    OWNER,
    ADMIN
}
