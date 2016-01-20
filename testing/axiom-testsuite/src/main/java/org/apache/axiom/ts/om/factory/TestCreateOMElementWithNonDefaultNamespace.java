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
package org.apache.axiom.ts.om.factory;

import java.util.Iterator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNamespace;

public class TestCreateOMElementWithNonDefaultNamespace extends CreateOMElementTestCase {
    public TestCreateOMElementWithNonDefaultNamespace(OMMetaFactory metaFactory, CreateOMElementVariant variant, CreateOMElementParentSupplier parentSupplier) {
        super(metaFactory, variant, parentSupplier);
    }

    protected void runTest() throws Throwable {
        OMFactory factory = metaFactory.getOMFactory();
        OMElement element = variant.createOMElement(factory, parentSupplier.createParent(factory), "test", "urn:ns", "ns");
        assertTrue(element.isComplete());
        assertEquals("test", element.getLocalName());
        OMNamespace ns = factory.createOMNamespace("urn:ns", "ns");
        assertEquals(ns, element.getNamespace());
        Iterator<OMNamespace> it = element.getAllDeclaredNamespaces();
        assertTrue(it.hasNext());
        assertEquals(ns, it.next());
        assertFalse(it.hasNext());
    }
}
