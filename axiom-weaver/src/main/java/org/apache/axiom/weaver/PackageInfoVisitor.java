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
package org.apache.axiom.weaver;

import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

final class PackageInfoVisitor extends ClassVisitor {
    private final List<String> mixins;
    private boolean weavablePackage;

    PackageInfoVisitor(List<String> mixins) {
        super(Opcodes.ASM9);
        this.mixins = mixins;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (descriptor.equals("Lorg/apache/axiom/weaver/annotation/WeavablePackage;")) {
            weavablePackage = true;
            return new AnnotationVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitArray(String name) {
                    if (name.equals("mixins")) {
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override
                            public void visit(String name, Object value) {
                                mixins.add(((Type)value).getClassName());
                            }
                        };
                    } else {
                        return null;
                    }
                }
            };
        } else {
            return null;
        }
    }

    boolean isWeavablePackage() {
        return weavablePackage;
    }
}
