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
package org.apache.axiom.core.stream;

final class CoalescingFilterHandler extends XmlHandlerWrapper {
    private final boolean coalesceCDATASections;
    private final StringBuilder pendingData = new StringBuilder();
    private boolean pendingIgnorable = true;

    CoalescingFilterHandler(XmlHandler parent, boolean coalesceCDATASections) {
        super(parent);
        this.coalesceCDATASections = coalesceCDATASections;
    }

    private void flush() throws StreamException {
        if (pendingData.length() > 0) {
            getParent().processCharacterData(pendingData.toString(), pendingIgnorable);
            pendingData.setLength(0);
            pendingIgnorable = true;
        }
    }

    @Override
    public void processCharacterData(Object data, boolean ignorable) throws StreamException {
        if (data instanceof CharacterData cd) {
            cd.appendTo(pendingData);
        } else {
            pendingData.append(data.toString());
        }
        pendingIgnorable = pendingIgnorable && ignorable;
    }

    @Override
    public void startDocument(String inputEncoding, String xmlVersion, String xmlEncoding, Boolean standalone)
            throws StreamException {
        flush();
        super.startDocument(inputEncoding, xmlVersion, xmlEncoding, standalone);
    }

    @Override
    public void startFragment() throws StreamException {
        flush();
        super.startFragment();
    }

    @Override
    public void processDocumentTypeDeclaration(String rootName, String publicId, String systemId, String internalSubset)
            throws StreamException {
        flush();
        super.processDocumentTypeDeclaration(rootName, publicId, systemId, internalSubset);
    }

    @Override
    public void startElement(String namespaceURI, String localName, String prefix) throws StreamException {
        flush();
        super.startElement(namespaceURI, localName, prefix);
    }

    @Override
    public void endElement() throws StreamException {
        flush();
        super.endElement();
    }

    @Override
    public void processAttribute(
            String namespaceURI, String localName, String prefix, String value, String type, boolean specified)
            throws StreamException {
        flush();
        super.processAttribute(namespaceURI, localName, prefix, value, type, specified);
    }

    @Override
    public void processAttribute(String name, String value, String type, boolean specified) throws StreamException {
        flush();
        super.processAttribute(name, value, type, specified);
    }

    @Override
    public void processNamespaceDeclaration(String prefix, String namespaceURI) throws StreamException {
        flush();
        super.processNamespaceDeclaration(prefix, namespaceURI);
    }

    @Override
    public void attributesCompleted() throws StreamException {
        flush();
        super.attributesCompleted();
    }

    @Override
    public void startCDATASection() throws StreamException {
        if (!coalesceCDATASections) {
            flush();
            super.startCDATASection();
        }
    }

    @Override
    public void endCDATASection() throws StreamException {
        if (!coalesceCDATASections) {
            flush();
            super.endCDATASection();
        }
    }

    @Override
    public void startComment() throws StreamException {
        flush();
        super.startComment();
    }

    @Override
    public void endComment() throws StreamException {
        flush();
        super.endComment();
    }

    @Override
    public void startProcessingInstruction(String target) throws StreamException {
        flush();
        super.startProcessingInstruction(target);
    }

    @Override
    public void endProcessingInstruction() throws StreamException {
        flush();
        super.endProcessingInstruction();
    }

    @Override
    public void processEntityReference(String name, String replacementText) throws StreamException {
        flush();
        super.processEntityReference(name, replacementText);
    }

    @Override
    public void completed() throws StreamException {
        flush();
        super.completed();
    }
}
