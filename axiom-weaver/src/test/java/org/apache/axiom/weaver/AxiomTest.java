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

import org.apache.axiom.om.impl.intf.AxiomAttribute;
import org.apache.axiom.om.impl.intf.AxiomCDATASection;
import org.apache.axiom.om.impl.intf.AxiomCharacterDataNode;
import org.apache.axiom.om.impl.intf.AxiomComment;
import org.apache.axiom.om.impl.intf.AxiomDocType;
import org.apache.axiom.om.impl.intf.AxiomDocument;
import org.apache.axiom.om.impl.intf.AxiomElement;
import org.apache.axiom.om.impl.intf.AxiomEntityReference;
import org.apache.axiom.om.impl.intf.AxiomNamespaceDeclaration;
import org.apache.axiom.om.impl.intf.AxiomProcessingInstruction;
import org.apache.axiom.om.impl.intf.AxiomSourcedElement;
import org.junit.Test;

public class AxiomTest {
    @Test
    public void test() throws Exception {
        Weaver weaver = new Weaver(new SimpleImplementationClassNameMapper("impl"));
        weaver.loadWeavablePackage(AxiomTest.class.getClassLoader(), "org.apache.axiom.core.impl.mixin");
        weaver.loadWeavablePackage(AxiomTest.class.getClassLoader(), "org.apache.axiom.shared");
        weaver.loadWeavablePackage(AxiomTest.class.getClassLoader(), "org.apache.axiom.om.impl.mixin");
        weaver.loadWeavablePackage(AxiomTest.class.getClassLoader(), "org.apache.axiom.soap.impl.mixin");
//        weaver.loadWeavablePackage(AxiomTest.class.getClassLoader(), "org.apache.axiom.om.impl.llom.mixin");
        weaver.addInterfaceToImplement(AxiomAttribute.class);
        weaver.addInterfaceToImplement(AxiomCDATASection.class);
        weaver.addInterfaceToImplement(AxiomCharacterDataNode.class);
        weaver.addInterfaceToImplement(AxiomComment.class);
        weaver.addInterfaceToImplement(AxiomDocType.class);
        weaver.addInterfaceToImplement(AxiomDocument.class);
        weaver.addInterfaceToImplement(AxiomElement.class);
        weaver.addInterfaceToImplement(AxiomEntityReference.class);
        weaver.addInterfaceToImplement(AxiomNamespaceDeclaration.class);
        weaver.addInterfaceToImplement(AxiomProcessingInstruction.class);
        weaver.addInterfaceToImplement(AxiomSourcedElement.class);
        ClassLoader cl = weaver.toClassLoader(AxiomTest.class.getClassLoader());
        cl.loadClass("impl.AxiomDocumentImpl").getConstructor().newInstance();
    }
}
