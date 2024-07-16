package org.cris6h16.apirestspringboot.Suites;

import org.cris6h16.apirestspringboot.Entities.NoteConstrainsValidationsTest;
import org.cris6h16.apirestspringboot.Entities.UserConstrainsValidationsTest;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

public class TestSuites {

    @Suite
    @SelectPackages({
            "org.cris6h16.apirestspringboot.*"
    })
    public static class AllTests {
    }


    @Suite
    @IncludeTags({
            "ConstraintViolationException",
            "DataIntegrityViolationException",
            "MyValidations"
    })
    @SelectClasses({
            UserConstrainsValidationsTest.class,
            NoteConstrainsValidationsTest.class
    })
    public static class ViolationTaggedTests {
    }


}

