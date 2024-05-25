package org.cris6h16.apirestspringboot.Suites;

import org.cris6h16.apirestspringboot.Entities.Integration.CascadingUserEntity;
import org.cris6h16.apirestspringboot.Entities.NoteConstrainsValidationsTest;
import org.cris6h16.apirestspringboot.Entities.UserConstrainsValidationsTest;
import org.cris6h16.apirestspringboot.Repository.NoteRepositoryTest;
import org.cris6h16.apirestspringboot.Repository.RoleRepositoryTest;
import org.cris6h16.apirestspringboot.Repository.UserRepositoryTest;
import org.junit.platform.suite.api.*;

public class TestSuites {

    @Suite
    @SelectPackages({
            "org.cris6h16.apirestspringboot.Repository",
            "org.cris6h16.apirestspringboot.Entities",
            "org.cris6h16.apirestspringboot.Entities.Integration"
    })
    public static class AllTests {
    }


    @Suite
    @IncludeTags({"ConstraintViolationException", "DataIntegrityViolationException"})
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

