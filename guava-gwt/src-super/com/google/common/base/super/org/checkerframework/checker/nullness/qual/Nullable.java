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
        // getAnnotatedType() method (https://github.com/google/guava/issues/5630).
        ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface Nullable {}
