/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.bootstrap.events;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import org.jboss.weld.resources.ReflectionCache;

public class SimpleAnnotationDiscovery implements AnnotationDiscovery {

    private final ReflectionCache cache;

    public SimpleAnnotationDiscovery(ReflectionCache cache) {
        this.cache = cache;
    }

    @Override
    public boolean containsAnnotations(Class<?> javaClass, Collection<Class<? extends Annotation>> requiredAnnotations) {
        for (Class<?> clazz = javaClass; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            // class level annotations
            if (containsAnnotations(cache.getAnnotations(clazz), requiredAnnotations)) {
                return true;
            }
            // fields
            for (Field field : clazz.getDeclaredFields()) {
                if (containsAnnotations(cache.getAnnotations(field), requiredAnnotations)) {
                    return true;
                }
            }
            // constructors
            for (Constructor<?> constructor : clazz.getConstructors()) {
                if (containsAnnotations(cache.getAnnotations(constructor), requiredAnnotations)) {
                    return true;
                }
                for (Annotation[] parameterAnnotations : constructor.getParameterAnnotations()) {
                    if (containsAnnotations(parameterAnnotations, requiredAnnotations)) {
                        return true;
                    }
                }
            }
            // methods
            for (Method method : clazz.getDeclaredMethods()) {
                if (containsAnnotations(cache.getAnnotations(method), requiredAnnotations)) {
                    return true;
                }
                for (Annotation[] parameterAnnotations : method.getParameterAnnotations()) {
                    if (containsAnnotations(parameterAnnotations, requiredAnnotations)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean containsAnnotations(Annotation[] annotations, Collection<Class<? extends Annotation>> requiredAnnotations) {
        return containsAnnotations(annotations, requiredAnnotations, true);
    }

    private boolean containsAnnotations(Annotation[] annotations, Collection<Class<? extends Annotation>> requiredAnnotations,
            boolean checkMetaAnnotations) {
        for (Class<? extends Annotation> requiredAnnotation : requiredAnnotations) {
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (requiredAnnotation.equals(annotationType)) {
                    return true;
                }
                if (checkMetaAnnotations
                        && containsAnnotations(cache.getAnnotations(annotationType), requiredAnnotations, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void cleanup() {
    }

}
