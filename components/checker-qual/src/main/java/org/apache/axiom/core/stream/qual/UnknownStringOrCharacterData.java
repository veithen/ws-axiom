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
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Top qualifier for the {@code @StringOrCharacterData} hierarchy. This annotation is the default
 * for all unannotated types: it represents a value that is not known to be a {@link String} or
 * {@code CharacterData} instance. It is placed at the top of the qualifier hierarchy so that the
 * Checker Framework assigns it to every unannotated expression, and it is a supertype of
 * {@link StringOrCharacterData}.
 */
@Documented
@Retention(CLASS)
@Target(TYPE_USE)
@SubtypeOf({})
@DefaultQualifierInHierarchy
public @interface UnknownStringOrCharacterData {}
