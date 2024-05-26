package org.cris6h16.apirestspringboot.Constants;

import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;

public class Cons {
    public class User {
        public class Constrains {
            public static final String EMAIL_UNIQUE_NAME = "email_unique";
            public static final String EMAIL_UNIQUE_MSG = "Email already exists";
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

        public static class Fails {
            public static final String NOT_FOUND = "User not found";
        }

        /**
         * {@link CreateUpdateUserDTO}
         */
        public static class DTO {
            public static final String NULL = "User to update/create cannot be null";
        }
    }

    public class Note {
        public class Validations {
            public static final String TITLE_MAX_LENGTH_MSG = "Title must be less than 255 characters";
            public static final String TITLE_IS_BLANK_MSG = "Title is required";
            public static final short MAX_TITLE_LENGTH = 255;
        }

        public class Fails {
            public static final String NOT_FOUND = "Note not found";
        }

    }

    public class Auth {
        public class Fails {
            public static final String IS_NOT_YOUR_ID_MSG = "You aren't the owner of this id";
        }
    }

    public class Response {
        public static class ForClient {
            public static final String GENERIC_ERROR = "An error occurred, please try again later or contact the us for support";
        }
    }

    public class Services {
        public static class User {
            public static final String ID_INVALID = "Invalid user id";
        }
    }

}