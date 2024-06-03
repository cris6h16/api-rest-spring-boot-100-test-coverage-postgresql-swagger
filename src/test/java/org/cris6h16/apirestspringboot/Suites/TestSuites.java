package org.cris6h16.apirestspringboot.Suites;

import org.cris6h16.apirestspringboot.Entities.Integration.CascadingUserEntity;
import org.cris6h16.apirestspringboot.Entities.NoteConstrainsValidationsTest;
import org.cris6h16.apirestspringboot.Entities.UserConstrainsValidationsTest;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

public class TestSuites {

    @Suite
    @SelectPackages({
            "org.cris6h16.apirestspringboot.Controllers",
            "org.cris6h16.apirestspringboot.Controllers.Integration.Advice",
            "org.cris6h16.apirestspringboot.Entities",
            "org.cris6h16.apirestspringboot.Entities.Integration",
            "org.cris6h16.apirestspringboot.Repository",
            "org.cris6h16.apirestspringboot.Service",
            "org.cris6h16.apirestspringboot.Service.Integration.ServiceUtils"
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


    @Suite
    @IncludeTags({"correct"}) // insertion of a entity correctly ( doesn't violate any constraint )
    @SelectClasses({
            UserConstrainsValidationsTest.class,
            NoteConstrainsValidationsTest.class
    })
    public static class CorrectTaggedTests {
    }

    @Suite
    @SelectClasses({
            CascadingUserEntity.class
    })
    public static class EntitiesIntegrationTests {
    }
}

