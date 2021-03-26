package me.ultrablacklinux.bpvp.annotations;

import jdk.tools.jlink.internal.ModularJarArchive;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.CLASS)
public @interface Module {
}
