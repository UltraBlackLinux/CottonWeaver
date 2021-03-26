package me.ultrablacklinux.bpvp.annotations;

import me.ultrablacklinux.bpvp.modules.ModuleManager;

import java.util.Objects;

public class ModuleAnnotation {
    public void addModule(Object object) throws InvalidModuleException {
        if (Objects.isNull(object)) {
            throw new InvalidModuleException("Not a Module!");
        }

        System.out.println(object.getClass().getSimpleName());

        ModuleManager.modules.add(object.getClass());
        if (!object.getClass().isAnnotationPresent(Module.class)) {
            throw new InvalidModuleException(object.getClass().getSimpleName() + " Is not annotated with Module");
        }
    }

}
