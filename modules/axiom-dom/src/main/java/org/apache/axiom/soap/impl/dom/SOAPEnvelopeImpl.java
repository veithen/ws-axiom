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

package org.apache.axiom.soap.impl.dom;

import org.apache.axiom.om.OMCloneOptions;
import org.apache.axiom.om.OMConstants;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.MTOMXMLStreamWriter;
import org.apache.axiom.om.impl.dom.DocumentImpl;
import org.apache.axiom.om.impl.dom.NodeImpl;
import org.apache.axiom.om.impl.util.OMSerializerUtil;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP11Version;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAP12Version;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.SOAPVersion;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class SOAPEnvelopeImpl extends SOAPElement implements SOAPEnvelope,
        OMConstants {

    private static final QName HEADER_QNAME = new QName(SOAPConstants.HEADER_LOCAL_NAME);

    /** @param builder  */
    public SOAPEnvelopeImpl(OMXMLParserWrapper builder, SOAPFactory factory) {
        super(null, SOAPConstants.SOAPENVELOPE_LOCAL_NAME, builder, factory);
    }

    /** @param ns  */
    public SOAPEnvelopeImpl(OMNamespace ns, SOAPFactory factory) {
        super(SOAPConstants.SOAPENVELOPE_LOCAL_NAME, ns, factory);
    }

    public SOAPVersion getVersion() {
        return ((SOAPFactory)factory).getSOAPVersion();
    }

    /**
     * Returns the <CODE>SOAPHeader</CODE> object for this <CODE> SOAPEnvelope</CODE> object.
     * <p/>
     * This SOAPHeader will just be a container for all the headers in the <CODE>OMMessage</CODE>
     * </P>
     *
     * @return the <CODE>SOAPHeader</CODE> object or <CODE> null</CODE> if there is none
     * @throws org.apache.axiom.om.OMException
     *                     if there is a problem obtaining the <CODE>SOAPHeader</CODE> object
     * @throws OMException
     */
    public SOAPHeader getHeader() throws OMException {
        // Header must be the first child
        OMElement header = getFirstElement();
        if (header == null || !(header instanceof SOAPHeader)) {
            return null;
        }

        return (SOAPHeader) header;
    }

    /**
     * Check that a node is allowed as a child of a SOAP envelope.
     * 
     * @param child
     */
    private void checkChild(OMNode child) {
        if ((child instanceof OMElement)
                && !(child instanceof SOAPHeader || child instanceof SOAPBody)) {
            throw new SOAPProcessingException(
                    "SOAP Envelope can not have children other than SOAP Header and Body",
                    SOAP12Constants.FAULT_CODE_SENDER);
        }
    }

    public void addChild(OMNode child) {
        // SOAP 1.1 allows for arbitrary elements after SOAPBody so do NOT check for
        // node types when appending to SOAP 1.1 envelope.
        if (getVersion() instanceof SOAP12Version) {
            checkChild(child);
        }

        if (child instanceof SOAPHeader) {
            // The SOAPHeader is added before the SOAPBody
            // We must be sensitive to the state of the parser.  It is possible that the
            // has not been processed yet.
            if (this.done) {
                // Parsing is complete, therefore it is safe to
                // call getBody.
                SOAPBody body = getBody();
                if (body != null) {
                    body.insertSiblingBefore(child);
                    return;
                }
            } else {
                // Flow to here indicates that we are still expanding the
                // envelope.  The body or body contents may not be
                // parsed yet.  We can't use getBody() yet...it will
                // cause a failure.  So instead, carefully find the
                // body and insert the header.  If the body is not found,
                // this indicates that it has not been parsed yet...and
                // the code will fall through to the super.addChild.
                OMNode node = (OMNode)this.lastChild;
                while (node != null) {
                    if (node instanceof SOAPBody) {
                        node.insertSiblingBefore(child);
                        return;
                    }
                    node = node.getPreviousOMSibling();
                }
            }
        }
        super.addChild(child);
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        // Check that the child to be added is valid in the context of a SOAP envelope.
        // Note that this also covers the appendChild case, since that method
        // calls insertBefore with refChild == null.
        
        // SOAP 1.1 allows for arbitrary elements after SOAPBody so do NOT check for
        // allowed node types when appending to SOAP 1.1 envelope.
        if (!(getVersion() instanceof SOAP11Version && refChild == null)) {
            checkChild((OMNode)newChild);
        }
        return super.insertBefore(newChild, refChild);
    }

    /**
     * Returns the <CODE>SOAPBody</CODE> object associated with this <CODE>SOAPEnvelope</CODE>
     * object.
     * <p/>
     * This SOAPBody will just be a container for all the BodyElements in the <CODE>OMMessage</CODE>
     * </P>
     *
     * @return the <CODE>SOAPBody</CODE> object for this <CODE> SOAPEnvelope</CODE> object or
     *         <CODE>null</CODE> if there is none
     * @throws org.apache.axiom.om.OMException
     *                     if there is a problem obtaining the <CODE>SOAPBody</CODE> object
     * @throws OMException
     */
    public SOAPBody getBody() throws OMException {
        // check for the first element
        OMElement element = getFirstElement();
        if (element != null) {
            if (SOAPConstants.BODY_LOCAL_NAME.equals(element.getLocalName())) {
                return (SOAPBody) element;
            } else { // if not second element SHOULD be the body
                OMNode node = element.getNextOMSibling();
                while (node != null && node.getType() != OMNode.ELEMENT_NODE) {
                    node = node.getNextOMSibling();
                }
                if (node == null) {
                    // The envelope only contains a header
                    return null;
                } else if (SOAPConstants.BODY_LOCAL_NAME.equals(((OMElement)node).getLocalName())) {
                    return (SOAPBody)node;
                } else {
                    throw new OMException("SOAPEnvelope must contain a body element " +
                            "which is either first or second child element of the SOAPEnvelope.");
                }
            }
        }
        return null;
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        // here do nothing as SOAPEnvelope doesn't have a parent !!!
    }

    public void internalSerialize(XMLStreamWriter writer2, boolean cache)
            throws XMLStreamException {

        MTOMXMLStreamWriter writer = (MTOMXMLStreamWriter) writer2;
        if (!writer.isIgnoreXMLDeclaration()) {
            String charSetEncoding = writer.getCharSetEncoding();
            String xmlVersion = writer.getXmlVersion();
            writer.getXmlStreamWriter().writeStartDocument(
                    charSetEncoding == null ? OMConstants.DEFAULT_CHAR_SET_ENCODING
                            : charSetEncoding,
                    xmlVersion == null ? OMConstants.DEFAULT_XML_VERSION
                            : xmlVersion);
        }
        if (cache) {
            //in this case we don't care whether the elements are built or not
            //we just call the serializeAndConsume methods
            OMSerializerUtil.serializeStartpart(this, writer);
            //serialize children
            SOAPHeader header = getHeader();
            if ((header != null) && (header.getFirstOMChild() != null)) {
                ((SOAPHeaderImpl) header).internalSerialize(writer, true);
            }
            SOAPBody body = getBody();
            //REVIEW: getBody has statements to return null..Can it be null in any case?
            if (body != null) {
                ((org.apache.axiom.soap.impl.dom.SOAPBodyImpl) body).internalSerialize(writer, true);
            }
            OMSerializerUtil.serializeEndpart(writer);

        } else {
            //Now the caching is supposed to be off. However caching been switched off
            //has nothing to do if the element is already built!
            if (this.done || (this.builder == null)) {
                OMSerializerUtil.serializeStartpart(this, writer);
                OMElement header = getHeader();
                if ((header != null) && (header.getFirstOMChild() != null)) {
                    serializeInternally((NodeImpl) header, writer);
                }
                SOAPBody body = getBody();
                if (body != null) {
                    serializeInternally((NodeImpl) body, writer);
                }
                OMSerializerUtil.serializeEndpart(writer);
            } else {
                OMSerializerUtil.serializeByPullStream(this, writer, cache);
            }
        }
    }

    private void serializeInternally(NodeImpl child, MTOMXMLStreamWriter writer)
            throws XMLStreamException {
        if ((!(child instanceof OMElement)) || child.isComplete() || child.getBuilder() == null) {
            child.internalSerialize(writer, false);
        } else {
            OMElement element = (OMElement) child;
            element.getBuilder().setCache(false);
            OMSerializerUtil.serializeByPullStream(element, writer, false);
        }
//        child = (NodeImpl) child.getNextOMSibling();
    }

    public OMNode getNextOMSibling() throws OMException {
        DocumentImpl ownerDocument = (DocumentImpl)getOwnerDocument();
        if (ownerDocument != null && !ownerDocument.isComplete()) {
            ownerDocument.setComplete(true);
        }
        return null;
    }

    public boolean hasFault() {      
        QName payloadQName = this.getPayloadQName_Optimized();
        if (payloadQName != null) {
            if (SOAPConstants.SOAPFAULT_LOCAL_NAME.equals(payloadQName.getLocalPart())) {
                String ns = payloadQName.getNamespaceURI();
                return SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(ns) ||
                       SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(ns);                                                         
            } 
        }
        
        // Fallback: Get the body and get the fault information from the body
        SOAPBody body = this.getBody();
        return (body == null) ? false : body.hasFault();
    }

    public String getSOAPBodyFirstElementLocalName() {
        QName payloadQName = this.getPayloadQName_Optimized();
        if (payloadQName != null) {
            return payloadQName.getLocalPart();
        }
        SOAPBody body = this.getBody();
        return (body == null) ? null : body.getFirstElementLocalName();
    }

    public OMNamespace getSOAPBodyFirstElementNS() {
        QName payloadQName = this.getPayloadQName_Optimized();
        if (payloadQName != null) {
            return this.factory.createOMNamespace(payloadQName.getNamespaceURI(), 
                                                  payloadQName.getPrefix());
        }
        SOAPBody body = this.getBody();
        return (body == null) ? null : body.getFirstElementNS();
    }
    
    /**
     * Use a parser property to fetch the first element in the body.
     * Returns null if this optimized property is not set or not available.
     * @return The qname of the first element in the body or null
     */
    private QName getPayloadQName_Optimized() {
        // The parser may expose a SOAPBODY_FIRST_CHILD_ELEMENT_QNAME property
        // Use that QName to determine if there is a fault
        OMXMLParserWrapper builder = this.getBuilder();
        if (builder instanceof StAXSOAPModelBuilder) {
            try {
                QName payloadQName = (QName) ((StAXSOAPModelBuilder) builder).
                    getReaderProperty(SOAPConstants.SOAPBODY_FIRST_CHILD_ELEMENT_QNAME);
                return payloadQName;
            } catch (Throwable e) {
                // The parser may not support this property. 
                // In such cases, processing continues below in the fallback approach
            }
            
        }
        return null;
    }

    protected OMElement createClone(OMCloneOptions options, OMContainer targetParent) {
        return ((SOAPFactory)factory).createSOAPEnvelope(getNamespace());
    }
}
