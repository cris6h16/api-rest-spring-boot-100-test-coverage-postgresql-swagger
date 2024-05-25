package org.cris6h16.apirestspringboot.Suites;

import org.cris6h16.apirestspringboot.Entities.NoteConstrainsValidationsTest;
import org.cris6h16.apirestspringboot.Entities.UserConstrainsValidationsTest;
import org.cris6h16.apirestspringboot.Repository.NoteRepositoryTest;
import org.cris6h16.apirestspringboot.Repository.RoleRepositoryTest;
import org.cris6h16.apirestspringboot.Repository.UserRepositoryTest;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

public class TestSuites {

    @Suite
    @SelectClasses({
            UserRepositoryTest.class,
            NoteRepositoryTest.class,
            RoleRepositoryTest.class,
            UserConstrainsValidationsTest.class,
            NoteConstrainsValidationsTest.class
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
}

