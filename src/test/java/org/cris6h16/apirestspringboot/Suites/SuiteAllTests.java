package org.cris6h16.apirestspringboot.Suites;

import org.cris6h16.apirestspringboot.Repository.NoteRepositoryTest;
import org.cris6h16.apirestspringboot.Repository.UserRepositoryTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        UserRepositoryTest.class,
        NoteRepositoryTest.class
})
public class SuiteAllTests {
}
