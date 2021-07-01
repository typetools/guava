package org.checkerframework.checker.nullness.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER,
        // Added because Android's variant of
        // com.google.common.reflect.Parameter lacks the
        // getAnnotatedType() method.  However, this is not enough to make
        // Guava's NullPointerTester tests pass.
        ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface Nullable {}
