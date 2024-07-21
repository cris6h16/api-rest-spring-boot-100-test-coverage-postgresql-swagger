package org.cris6h16.apirestspringboot.DTOs.Interfaces;

public interface CleanableAttributes {
    void trimNotNullAttributes();
    void toLowerCaseNotNullAttributes();
    default void cleanAttributes(){
        trimNotNullAttributes();
        toLowerCaseNotNullAttributes();
    }
}