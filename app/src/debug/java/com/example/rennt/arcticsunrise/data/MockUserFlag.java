package com.example.rennt.arcticsunrise.data;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.inject.Qualifier;

@Qualifier
@Retention(RUNTIME)
public @interface MockUserFlag {
}
