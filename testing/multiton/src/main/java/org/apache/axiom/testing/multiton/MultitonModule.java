/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axiom.testing.multiton;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public final class MultitonModule<T> extends AbstractModule {
    private final Class<T> multitonClass;

    public MultitonModule(Class<T> multitonClass) {
        this.multitonClass = multitonClass;
    }

    @Override
    protected void configure() {
        Multibinder<T> binder = Multibinder.newSetBinder(binder(), multitonClass);
        for (Field field : multitonClass.getDeclaredFields()) {
            int mod = field.getModifiers();
            if (Modifier.isPublic(mod)
                    && Modifier.isStatic(mod)
                    && Modifier.isFinal(mod)
                    && multitonClass.isAssignableFrom(field.getType())) {
                try {
                    binder.addBinding().toInstance(multitonClass.cast(field.get(null)));
                } catch (IllegalAccessException ex) {
                    throw new MultitonInstantiationException(ex);
                }
            }
        }
        for (Method method : multitonClass.getDeclaredMethods()) {
            if (method.getAnnotation(Instances.class) != null) {
                int mod = method.getModifiers();
                if (!Modifier.isPrivate(mod) || !Modifier.isStatic(mod)) {
                    throw new MultitonInstantiationException(
                            "Methods annotated with @Instances must be private static");
                }
                if (method.getParameterTypes().length > 0) {
                    throw new MultitonInstantiationException(
                            "Methods annotated with @Instances must not take any parameters");
                }
                Class<?> returnType = method.getReturnType();
                if (!returnType.isArray()
                        || !multitonClass.isAssignableFrom(returnType.getComponentType())) {
                    throw new MultitonInstantiationException(
                            "Invalid return type for method annotated with @Instances");
                }
                method.setAccessible(true);
                try {
                    for (Object instance : (Object[]) method.invoke(null)) {
                        binder.addBinding().toInstance(multitonClass.cast(instance));
                    }
                } catch (IllegalAccessException ex) {
                    throw new MultitonInstantiationException(ex);
                } catch (InvocationTargetException ex) {
                    throw new MultitonInstantiationException(ex.getCause());
                }
            }
        }
    }
}
