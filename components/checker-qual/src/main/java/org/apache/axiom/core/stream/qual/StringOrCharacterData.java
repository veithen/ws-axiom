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
package org.apache.axiom.core.stream.qual;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.axiom.core.stream.CharacterData;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Qualifier annotation for the Checker Framework {@code SubtypingChecker}. A value annotated with
 * {@code @StringOrCharacterData} is guaranteed to be an instance of either {@link String} or
 * {@link CharacterData}.
 *
 * <p>The {@link ImplicitFor} annotation instructs the checker that any expression with static type
 * {@link String} or {@link CharacterData} is implicitly {@code @StringOrCharacterData}, so explicit
 * annotations at call sites are not required.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE_USE)
@SubtypeOf(Object.class)
@ImplicitFor(typeNames = {String.class, CharacterData.class})
public @interface StringOrCharacterData {}
