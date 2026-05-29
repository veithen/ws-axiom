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

import org.apache.axiom.core.stream.util.CharacterDataAccumulator;

/**
 * {@link XmlHandler} that performs coalescing, i.e. merges adjacent segments of character data
 * into a single {@link XmlHandler#processCharacterData(Object, boolean)} call. When {@code
 * mergeCDATA} is {@code true}, character data inside CDATA sections is merged with adjacent text
 * nodes and the {@link XmlHandler#startCDATASection()} / {@link XmlHandler#endCDATASection()}
 * delimiters are suppressed.
 */
final class CoalescingFilterHandler extends XmlHandlerWrapper {
    private final boolean mergeCDATA;
    private final CharacterDataAccumulator pending = new CharacterDataAccumulator();
    private boolean pendingIgnorable = true;

    CoalescingFilterHandler(XmlHandler parent, boolean mergeCDATA) {
        super(parent);
        this.mergeCDATA = mergeCDATA;
    }

    private void flushPending() throws StreamException {
        if (!pending.isEmpty()) {
            getParent().processCharacterData(pending.get(), pendingIgnorable);
            pending.clear();
            pendingIgnorable = true;
        }
    }

    @Override
    public void processCharacterData(Object data, boolean ignorable) throws StreamException {
        pending.append(data);
        if (!ignorable) {
            pendingIgnorable = false;
        }
    }

    @Override
    public void startDocument(String inputEncoding, String xmlVersion, String xmlEncoding, Boolean standalone)
            throws StreamException {
        flushPending();
        super.startDocument(inputEncoding, xmlVersion, xmlEncoding, standalone);
    }

    @Override
    public void startFragment() throws StreamException {
        flushPending();
        super.startFragment();
    }

    @Override
    public void processDocumentTypeDeclaration(String rootName, String publicId, String systemId, String internalSubset)
            throws StreamException {
        flushPending();
        super.processDocumentTypeDeclaration(rootName, publicId, systemId, internalSubset);
    }

    @Override
    public void startElement(String namespaceURI, String localName, String prefix) throws StreamException {
        flushPending();
        super.startElement(namespaceURI, localName, prefix);
    }

    @Override
    public void endElement() throws StreamException {
        flushPending();
        super.endElement();
    }

    @Override
    public void attributesCompleted() throws StreamException {
        flushPending();
        super.attributesCompleted();
    }

    @Override
    public void startProcessingInstruction(String target) throws StreamException {
        flushPending();
        super.startProcessingInstruction(target);
    }

    @Override
    public void endProcessingInstruction() throws StreamException {
        flushPending();
        super.endProcessingInstruction();
    }

    @Override
    public void startComment() throws StreamException {
        flushPending();
        super.startComment();
    }

    @Override
    public void endComment() throws StreamException {
        flushPending();
        super.endComment();
    }

    @Override
    public void startCDATASection() throws StreamException {
        if (!mergeCDATA) {
            flushPending();
            super.startCDATASection();
        }
        // In mergeCDATA mode, suppress the delimiter and let processCharacterData accumulate.
    }

    @Override
    public void endCDATASection() throws StreamException {
        if (!mergeCDATA) {
            flushPending();
            super.endCDATASection();
        }
        // In mergeCDATA mode, suppress the delimiter.
    }

    @Override
    public void processEntityReference(String name, String replacementText) throws StreamException {
        flushPending();
        super.processEntityReference(name, replacementText);
    }

    @Override
    public void completed() throws StreamException {
        flushPending();
        super.completed();
    }
}
