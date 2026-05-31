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
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.QualifierForLiterals;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Qualifier annotation for the Checker Framework {@code SubtypingChecker}. A value annotated with
 * {@code @StringOrCharacterData} is a {@link String} or a {@code CharacterData} instance. This
 * annotation is used to enforce correct usage of APIs that accept either type as {@link Object}.
 *
 * <p>{@link QualifierForLiterals} ensures that string literals are automatically
 * {@code @StringOrCharacterData} without requiring an explicit annotation at each call site.
 * Expressions of type {@code CharacterData} are covered by {@code @DefaultQualifierForUse} placed
 * on the {@code CharacterData} interface.
 */
@Documented
@Retention(CLASS)
@Target(TYPE_USE)
@SubtypeOf(UnknownStringOrCharacterData.class)
@QualifierForLiterals(LiteralKind.STRING)
public @interface StringOrCharacterData {}
