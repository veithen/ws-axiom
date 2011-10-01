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
package org.apache.axiom.ts.dom.document;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axiom.ts.dom.DOMTestCase;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;

public class TestCreateAttribute extends DOMTestCase {
    public TestCreateAttribute(DocumentBuilderFactory dbf) {
        super(dbf);
    }

    protected void runTest() throws Throwable {
        String attrName = "attrIdentifier";
        String attrValue = "attrValue";
        String attrNs = "http://ws.apache.org/axis2/ns";
        String attrNsPrefix = "axis2";

        Document doc = dbf.newDocumentBuilder().newDocument();
        Attr attr = doc.createAttribute(attrName);

        assertEquals("Attr name mismatch", attrName, attr.getName());
        assertNull("Namespace value should be null", attr.getNamespaceURI());


        attr = doc.createAttributeNS(attrNs, attrNsPrefix + ":" + attrName);
        assertEquals("Attr name mismatch", attrName, attr.getLocalName());
        assertNotNull("Namespace value should not be null", attr.getNamespaceURI());
        assertEquals("NamsspaceURI mismatch", attrNs, attr.getNamespaceURI());
        assertEquals("namespace prefix mismatch", attrNsPrefix, attr.getPrefix());

        attr.setValue(attrValue);
    }
}
