package org.cris6h16.apirestspringboot.Constants;

public class UserCons {
    public class Constrains {
        public static final String EMAIL_UNIQUE_NAME = "email_unique";
        public static final String EMAIL_ALREADY__MSG = "Email already exists"
        public static final String USERNAME_UNIQUE_MSG = "Username already exists";
        public static final String USERNAME_UNIQUE_NAME = "username_unique";
    }

    public class Validations {
        public static final String EMAIL_IS_BLANK_MSG = "Email is required";
        public static final String EMAIL_INVALID_MSG = "Email is invalid";
        public static final String USERNAME_MAX_LENGTH_MSG = "Username must be less than 20 characters";
        public static final String USERNAME_IS_BLANK_MSG = "Username mustn't be blank";
        public static final byte MAX_USERNAME_LENGTH = 20;
        public static final String PASS_IS_BLANK_MSG = "Password mustn't be blank";

        public class InService {
            public static final String PASS_IS_TOO_SHORT_MSG = "Password must be at least 8 characters";
        }


    }
}