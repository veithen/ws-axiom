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
package org.apache.axiom.dom.impl.factory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.axiom.dom.DOMNodeFactory;

public final class DOMDocumentBuilderFactory extends DocumentBuilderFactory {
    private static final DOMNodeFactory NODE_FACTORY = loadNodeFactory();

    private static DOMNodeFactory loadNodeFactory() {
        try {
            return (DOMNodeFactory) DOMDocumentBuilderFactory.class
                    .getClassLoader()
                    .loadClass("org.apache.axiom.dom.impl.DOMNodeFactoryImpl")
                    .getField("INSTANCE")
                    .get(null);
        } catch (ReflectiveOperationException ex) {
            throw new Error(ex);
        }
    }

    @Override
    public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        return new DOMDocumentBuilder(NODE_FACTORY);
    }

    @Override
    public Object getAttribute(String name) throws IllegalArgumentException {
        throw new IllegalArgumentException("Attribute not recognized: " + name);
    }

    @Override
    public void setAttribute(String name, Object value) throws IllegalArgumentException {
        throw new IllegalArgumentException("Attribute not recognized: " + name);
    }

    @Override
    public void setFeature(String name, boolean value) throws ParserConfigurationException {
        // TODO
    }

    @Override
    public boolean getFeature(String name) throws ParserConfigurationException {
        throw new UnsupportedOperationException("TODO");
    }
}
