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

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.apache.axiom.core.DeferredParsingException;
import org.apache.axiom.core.impl.builder.BuilderImpl;
import org.apache.axiom.core.impl.builder.PlainXMLModel;
import org.apache.axiom.core.stream.StreamException;
import org.apache.axiom.core.stream.sax.input.SAXInput;
import org.apache.axiom.dom.DOMNodeFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

final class DOMDocumentBuilder extends DocumentBuilder {
    /**
     * SAX filter that coalesces adjacent {@code characters()} calls into a single call, and
     * correctly sequences lexical events (CDATA sections, comments) with buffered character data.
     *
     * <p>The SAX specification allows parsers to split text content across multiple {@code
     * characters()} calls, but DOM requires text nodes to contain the full content. Without
     * coalescing, adjacent SAX character events would produce multiple DOM text nodes for what
     * should be a single node.
     *
     * <p>This filter also intercepts the {@code lexical-handler} SAX property so that CDATA/comment
     * events are delivered <em>after</em> any pending buffered characters have been flushed,
     * preserving the correct order of events to the downstream handler.
     */
    private static final class CoalescingXMLFilter extends XMLFilterImpl implements LexicalHandler {
        private static final String LEXICAL_HANDLER_PROPERTY = "http://xml.org/sax/properties/lexical-handler";

        private final StringBuilder buf = new StringBuilder();
        private LexicalHandler downstreamLexicalHandler;

        CoalescingXMLFilter(XMLReader parent) {
            super(parent);
        }

        @Override
        public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
            if (LEXICAL_HANDLER_PROPERTY.equals(name)) {
                downstreamLexicalHandler = (LexicalHandler) value;
                // Register ourselves as the lexical handler on the underlying parser so
                // that we can flush buffered characters before forwarding lexical events.
                try {
                    getParent().setProperty(name, this);
                } catch (SAXException ex) {
                    // Ignore if the underlying parser doesn't support lexical handler
                }
            } else {
                super.setProperty(name, value);
            }
        }

        private void flushCharacters() throws SAXException {
            if (buf.length() > 0) {
                ContentHandler handler = getContentHandler();
                if (handler != null) {
                    char[] chars = buf.toString().toCharArray();
                    handler.characters(chars, 0, chars.length);
                }
                buf.setLength(0);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            buf.append(ch, start, length);
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            flushCharacters();
            super.ignorableWhitespace(ch, start, length);
        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts)
                throws SAXException {
            flushCharacters();
            super.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            flushCharacters();
            super.endElement(uri, localName, qName);
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
            flushCharacters();
            super.processingInstruction(target, data);
        }

        @Override
        public void startDocument() throws SAXException {
            buf.setLength(0);
            super.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            flushCharacters();
            super.endDocument();
        }

        // LexicalHandler implementation: flush buffered characters before forwarding

        @Override
        public void startDTD(String name, String publicId, String systemId) throws SAXException {
            if (downstreamLexicalHandler != null) {
                downstreamLexicalHandler.startDTD(name, publicId, systemId);
            }
        }

        @Override
        public void endDTD() throws SAXException {
            if (downstreamLexicalHandler != null) {
                downstreamLexicalHandler.endDTD();
            }
        }

        @Override
        public void startEntity(String name) throws SAXException {
            flushCharacters();
            if (downstreamLexicalHandler != null) {
                downstreamLexicalHandler.startEntity(name);
            }
        }

        @Override
        public void endEntity(String name) throws SAXException {
            flushCharacters();
            if (downstreamLexicalHandler != null) {
                downstreamLexicalHandler.endEntity(name);
            }
        }

        @Override
        public void startCDATA() throws SAXException {
            flushCharacters();
            if (downstreamLexicalHandler != null) {
                downstreamLexicalHandler.startCDATA();
            }
        }

        @Override
        public void endCDATA() throws SAXException {
            flushCharacters();
            if (downstreamLexicalHandler != null) {
                downstreamLexicalHandler.endCDATA();
            }
        }

        @Override
        public void comment(char[] ch, int start, int length) throws SAXException {
            flushCharacters();
            if (downstreamLexicalHandler != null) {
                downstreamLexicalHandler.comment(ch, start, length);
            }
        }
    }

    private final DOMNodeFactory nodeFactory;

    DOMDocumentBuilder(DOMNodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    @Override
    public boolean isNamespaceAware() {
        return true;
    }

    @Override
    public boolean isValidating() {
        return false;
    }

    @Override
    public DOMImplementation getDOMImplementation() {
        return nodeFactory;
    }

    @Override
    public Document newDocument() {
        return nodeFactory.createDocument();
    }

    @Override
    public void setEntityResolver(EntityResolver er) {
        // TODO
    }

    @Override
    public void setErrorHandler(ErrorHandler eh) {
        // TODO
    }

    @Override
    public Document parse(InputSource is) throws SAXException, IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        XMLReader xmlReader;
        try {
            xmlReader = spf.newSAXParser().getXMLReader();
        } catch (ParserConfigurationException ex) {
            throw new SAXException(ex);
        }
        SAXSource saxSource = new SAXSource(new CoalescingXMLFilter(xmlReader), is);
        BuilderImpl builder =
                new BuilderImpl(new SAXInput(saxSource, false), nodeFactory, PlainXMLModel.INSTANCE, null);
        try {
            return (Document) builder.getDocument();
        } catch (DeferredParsingException ex) {
            StreamException se = ex.getStreamException();
            Throwable cause = se.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            if (cause instanceof SAXException saxException) {
                throw saxException;
            }
            throw new SAXException(se);
        } finally {
            builder.close();
        }
    }
}
