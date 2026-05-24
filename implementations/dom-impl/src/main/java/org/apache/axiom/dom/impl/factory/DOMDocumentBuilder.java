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
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

final class DOMDocumentBuilder extends DocumentBuilder {
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
        SAXSource saxSource = new SAXSource(xmlReader, is);
        BuilderImpl builder = new BuilderImpl(new SAXInput(saxSource, false), nodeFactory, PlainXMLModel.INSTANCE, null);
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
