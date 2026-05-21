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
package org.apache.axiom.testutils.suite;

import com.google.inject.Injector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * A leaf node that instantiates a test class via Guice and executes its test logic.
 *
 * <p>The test class must have an injectable constructor (either a no-arg constructor or one
 * annotated with {@code @Inject}). Field injection is also supported. The injector received from
 * the ancestor {@link FanOutNode} chain will have bindings for all dimension types, plus any
 * implementation-level bindings from the root injector.
 *
 * <p>Two styles of test class are supported:
 *
 * <ul>
 *   <li><b>Single-method style:</b> The test class implements {@link Executable}. The node
 *       produces a single {@link DynamicTest} named after the class. Once the instance is created,
 *       {@link Executable#execute()} is invoked.
 *   <li><b>Multi-method style (JUnit 5 style):</b> The test class is a plain class with one or
 *       more methods annotated with {@link Test @Test}. The node produces a {@link DynamicContainer}
 *       named after the class, with one child {@link DynamicTest} per annotated method. A fresh
 *       instance of the test class is created for each method invocation.
 * </ul>
 */
public class MatrixTest extends MatrixTestNode {
    private final Class<?> testClass;

    public MatrixTest(Class<?> testClass) {
        this.testClass = testClass;
    }

    @Override
    protected Stream<DynamicNode> toDynamicNodes(
            Injector injector,
            Map<String, String> inheritedLabels,
            BiPredicate<Class<?>, Map<String, String>> excludes) {
        if (excludes.test(testClass, inheritedLabels)) {
            return Stream.empty();
        }
        if (Executable.class.isAssignableFrom(testClass)) {
            @SuppressWarnings("unchecked")
            Class<? extends Executable> executableClass = (Class<? extends Executable>) testClass;
            return Stream.of(DynamicTest.dynamicTest(testClass.getSimpleName(), () -> {
                Executable testInstance = injector.getInstance(executableClass);
                testInstance.execute();
            }));
        } else {
            List<Method> testMethods = Arrays.stream(testClass.getMethods())
                    .filter(m -> m.isAnnotationPresent(Test.class))
                    .sorted(Comparator.comparing(Method::getName))
                    .collect(Collectors.toList());
            return Stream.of(DynamicContainer.dynamicContainer(
                    testClass.getSimpleName(),
                    testMethods.stream()
                            .map(method -> DynamicTest.dynamicTest(method.getName(), () -> {
                                Object testInstance = injector.getInstance(testClass);
                                try {
                                    method.invoke(testInstance);
                                } catch (InvocationTargetException e) {
                                    throw e.getCause();
                                }
                            }))));
        }
    }
}
