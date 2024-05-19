package org.cris6h16.apirestspringboot.Controllers.MetaAnnotations;


import org.springframework.security.core.annotation.CurrentSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@CurrentSecurityContext(expression = "authentication.principal.id")
// instead of: Long id = ((UserWithId) (SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getId();
public @interface MyId {
}
