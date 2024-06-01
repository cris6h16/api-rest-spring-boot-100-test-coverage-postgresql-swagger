package org.cris6h16.apirestspringboot.Controllers.CustomMockUser;

import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
