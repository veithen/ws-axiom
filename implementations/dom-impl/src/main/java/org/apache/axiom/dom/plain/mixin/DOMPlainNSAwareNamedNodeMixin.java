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
package org.apache.axiom.dom.plain.mixin;

import org.apache.axiom.core.CoreNamedNode;
import org.apache.axiom.core.CoreNSAwareNamedNode;
import org.apache.axiom.weaver.annotation.Mixin;

@Mixin
public abstract class DOMPlainNSAwareNamedNodeMixin implements CoreNSAwareNamedNode {
    private String namespaceURI = "";
    private String localName;
    private String prefix = "";

    @Override
    public final void initName(String namespaceURI, String localName, String prefix, Object namespaceHelper) {
        this.namespaceURI = namespaceURI == null ? "" : namespaceURI;
        this.localName = localName;
        this.prefix = prefix == null ? "" : prefix;
    }

    @Override
    public final String coreGetNamespaceURI() {
        return namespaceURI;
    }

    @Override
    public final String coreGetLocalName() {
        return localName;
    }

    @Override
    public final String coreGetPrefix() {
        return prefix;
    }

    @Override
    public final void coreSetPrefix(String prefix) {
        this.prefix = prefix == null ? "" : prefix;
    }

    @Override
    public final void coreSetName(String namespaceURI, String localName, String prefix) {
        this.namespaceURI = namespaceURI == null ? "" : namespaceURI;
        this.localName = localName;
        this.prefix = prefix == null ? "" : prefix;
    }

    @Override
    public final void initName(CoreNamedNode other) {
        CoreNSAwareNamedNode o = (CoreNSAwareNamedNode) other;
        this.namespaceURI = o.coreGetNamespaceURI();
        this.localName = o.coreGetLocalName();
        this.prefix = o.coreGetPrefix();
    }
}
