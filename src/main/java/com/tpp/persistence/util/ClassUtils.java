package com.tpp.persistence.util;

import com.tpp.event.MethodEventHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class ClassUtils {

    private static Log LOG = LogFactory.getLog(ClassUtils.class);

    private ClassUtils() {
    }

    /**
     * retrieve a list of method from a type with {@link MethodEventHandler} annotation
     *
     * @param annotation
     * @param type
     * @return
     */
    public static List<Method> getAnnotatedMethods(final Class<? extends Annotation> annotation, final Class<?> type) {
        assert (annotation != null && type != null);
        final List<Method> methods = new ArrayList<Method>();
        Class<?> clazz = type;
        while (clazz != Object.class && clazz
                != null) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
            // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
            final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(clazz.getDeclaredMethods()));
            for (final Method method : allMethods) {
                if (method.isAnnotationPresent(annotation)) {
                    if (!Modifier.isPublic(method.getModifiers())) {
                        LOG.error("inaccessible method detected in " + type.getName() + ": " + method.getName());
                        continue;
                    }
                    // Annotation annotInstance = method.getAnnotation(annotation);
                    // process annotInstance
                    methods.add(method);
                }
            }
            // move to the upper class in the hierarchy in search for more methods
            clazz = clazz.getSuperclass();
        }
        return methods;
    }

}
