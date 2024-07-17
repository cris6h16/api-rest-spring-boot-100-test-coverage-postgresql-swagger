package org.cris6h16.apirestspringboot.Constants;

import org.cris6h16.apirestspringboot.DTOs.Creation.CreateNoteDTO;

/**
 * This class contains all the constants used in the project
 * used to avoid hardcoding strings and centralize it.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public class Cons {

    public class User {

        public class Controller {

            public class Path {
                public static final String USER_PATH = "/api/v1/users";
                public static final String COMPLEMENT_PATCH_USERNAME = "/patch/username";
                public static final String COMPLEMENT_PATCH_EMAIL = "/patch/email";
                public static final String COMPLEMENT_PATCH_PASSWORD = "/patch/password";
            }
        }

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
            public static final byte MIN_PASSWORD_LENGTH = 8;

            public class InService {
                public static final String PASS_IS_TOO_SHORT_MSG = "Password must be at least 8 characters";
            }
        }

        public static class Fails {
            public static final String NOT_FOUND = "User not found";
        }

        public static class DTO {
            /**
             * message for provide errors when any dto related to the user entity is
             * null like the patch DTOs, create user DTO, and so on.
             */
            public static final String ANY_RELATED_DTO_WITH_USER_NULL = "User data to update/create cannot be null";
        }

        public static class Page {
            public static final byte DEFAULT_PAGE = 0;
            public static final byte DEFAULT_SIZE = 10;
            public static final String DEFAULT_SORT = "id";
        }
    }

    public class Note {
        public class Controller {
            public class Path {
                public static final String NOTE_PATH = "/api/v1/notes";
            }
        }

        public class Validations {
            public static final String TITLE_MAX_LENGTH_MSG = "Title must be less than 255 characters";
            public static final String TITLE_IS_BLANK_MSG = "Title is required";
            public static final String CONTENT_IS_NULL_MSG = "Content can be empty but not null";
            public static final short MAX_TITLE_LENGTH = 255;
        }

        public class Fails {
            public static final String NOT_FOUND = "Note not found";
        }

        /**
         * {@link CreateNoteDTO}
         */
        public static class DTO {
            public static final String NULL = "Note to update/create cannot be null";
        }

        public static class Page {
            public static final byte DEFAULT_PAGE = 0;
            public static final byte DEFAULT_SIZE = 10;
            public static final String DEFAULT_SORT = "id";
        }

    }


    public class CommonInEntity {
        public static final String ID_INVALID = "Invalid id";
    }


    public class Response {
        public static class ForClient {
            public static final String GENERIC_ERROR = "An error occurred, please try again later or contact the us for support";
        }
    }


    public static class TESTING {
        /**
         * Constant for testing purposes
         * <p>
         * this is the pattern
         * used for identify if an unhandled exceptions was thrown for testing purposes
         *
         * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
         * @since 1.0
         */
        public static final String UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES = "cris6h16's";
    }

    /**
     * path of the log files
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    public static class Logs {
        public static final String HiddenExceptionsOfUsers = "logs/exceptions/unhandled.log";
        public static final String SUCCESS_AUTHENTICATION_FILE = "logs/auth/success.log";
        public static final String FAIL_AUTHENTICATION_FILE = "logs/auth/failures.log";
    }
}