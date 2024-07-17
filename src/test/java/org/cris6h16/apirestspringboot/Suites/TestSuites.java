package org.cris6h16.apirestspringboot.Suites;

import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

public class TestSuites {

    @Suite
    @SelectPackages({
            "org.cris6h16.apirestspringboot",

            "org.cris6h16.apirestspringboot.Config.Security",
            "org.cris6h16.apirestspringboot.Config.Security.CustomUser",
            "org.cris6h16.apirestspringboot.Config.Security.EventListener",
            "org.cris6h16.apirestspringboot.Config.Security.UserDetailsService",

            "org.cris6h16.apirestspringboot.Controllers",
            "org.cris6h16.apirestspringboot.Controllers.ExceptionHandler",
            "org.cris6h16.apirestspringboot.Controllers.UserController",

            "org.cris6h16.apirestspringboot.Entities",
            "org.cris6h16.apirestspringboot.Repositories",
            "org.cris6h16.apirestspringboot.Services",

            "org.cris6h16.apirestspringboot.Utils"
    })
    public static class AllTests {
    }


    @Suite
    @SelectPackages({
            "org.cris6h16.apirestspringboot",

            "org.cris6h16.apirestspringboot.Config.Security",
            "org.cris6h16.apirestspringboot.Config.Security.CustomUser",
            "org.cris6h16.apirestspringboot.Config.Security.EventListener",
            "org.cris6h16.apirestspringboot.Config.Security.UserDetailsService",

            "org.cris6h16.apirestspringboot.Controllers",
            "org.cris6h16.apirestspringboot.Controllers.ExceptionHandler",
            "org.cris6h16.apirestspringboot.Controllers.UserController",

            "org.cris6h16.apirestspringboot.Entities",
            "org.cris6h16.apirestspringboot.Repositories",
            "org.cris6h16.apirestspringboot.Services",

            "org.cris6h16.apirestspringboot.Utils"
    })
    @ExcludeTags("IntegrationTest")
    public static class AllTestsExceptIntegrationTests {
    }


}

