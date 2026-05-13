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
package org.apache.axiom.ts.soap12.builder;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultNode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.ts.AxiomTestCase;

import com.google.inject.Inject;

public class TestBuilder extends AxiomTestCase {
    @Inject
    public TestBuilder(OMMetaFactory metaFactory) {
        super(metaFactory);
    }

    @Override
    protected void runTest() throws Throwable {
        String soap12Message =
                "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">\n"
                        + "   <env:Header>\n"
                        + "       <test:echoOk xmlns:test=\"http://example.org/ts-tests\"\n"
                        + "                    env:role=\"http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver\"\n"
                        + "                    env:mustUnderstand=\"true\">\n"
                        + "                       foo\n"
                        + "       </test:echoOk>\n"
                        + "   </env:Header>\n"
                        + "   <env:Body>\n"
                        + "       <env:Fault>\n"
                        + "           <env:Code>\n"
                        + "               <env:Value>env:Sender</env:Value>\n"
                        + "               <env:Subcode>\n"
                        + "                   <env:Value>m:MessageTimeout</env:Value>\n"
                        + "                   <env:Subcode>\n"
                        + "                       <env:Value>m:MessageTimeout</env:Value>\n"
                        + "                   </env:Subcode>\n"
                        + "               </env:Subcode>\n"
                        + "           </env:Code>\n"
                        + "           <env:Reason>\n"
                        + "               <env:Text>Sender Timeout</env:Text>\n"
                        + "           </env:Reason>\n"
                        + "           <env:Node>\n"
                        + "               http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver\n"
                        + "           </env:Node>\n"
                        + "           <env:Role>\n"
                        + "               ultimateReceiver\n"
                        + "           </env:Role>\n"
                        + "           <env:Detail xmlns:m=\"http:www.sample.org\">\n"
                        + "               Details of error\n"
                        + "               <m:MaxTime m:detail=\"This is only a test\">\n"
                        + "                   P5M\n"
                        + "               </m:MaxTime>\n"
                        + "               <m:AveTime>\n"
                        + "                   <m:Time>\n"
                        + "                       P3M\n"
                        + "                   </m:Time>\n"
                        + "               </m:AveTime>\n"
                        + "           </env:Detail>\n"
                        + "       </env:Fault>\n"
                        + "   </env:Body>\n"
                        + "</env:Envelope>";

        OMXMLParserWrapper soap12Builder =
                OMXMLBuilderFactory.createSOAPModelBuilder(
                        metaFactory, new StringReader(soap12Message));
        SOAPEnvelope soap12Envelope = (SOAPEnvelope) soap12Builder.getDocumentElement();

        assertThat(soap12Envelope.getLocalName().equals(SOAPConstants.SOAPENVELOPE_LOCAL_NAME)).isTrue();
        assertThat(soap12Envelope .getNamespace() .getNamespaceURI() .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)).isTrue();

        SOAPHeader header = soap12Envelope.getHeader();
        assertThat(header.getLocalName().equals(SOAPConstants.HEADER_LOCAL_NAME)).isTrue();
        assertThat(header.getNamespace() .getNamespaceURI() .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)).isTrue();

        SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) header.getFirstElement();
        assertThat(headerBlock.getLocalName().equals("echoOk")).isTrue();
        assertThat(headerBlock.getNamespace().getNamespaceURI().equals("http://example.org/ts-tests")).isTrue();
        assertThat("foo").isEqualTo(headerBlock.getText().trim());

        // Attribute iteration is not in any guaranteed order.
        // Use QNames to get the OMAttributes.
        QName roleQName =
                new QName(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI, SOAP12Constants.SOAP_ROLE);
        QName mustUnderstandQName =
                new QName(
                        SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                        SOAP12Constants.ATTR_MUSTUNDERSTAND);

        OMAttribute roleAttribute = headerBlock.getAttribute(roleQName);
        OMAttribute mustUnderstandAttribute = headerBlock.getAttribute(mustUnderstandQName);

        assertThat(roleAttribute != null).isTrue();

        assertThat(roleAttribute .getAttributeValue() .trim() .equals( SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI + "/" + SOAP12Constants.SOAP_ROLE + "/" + "ultimateReceiver")).isTrue();

        assertThat(mustUnderstandAttribute != null).isTrue();

        assertThat(mustUnderstandAttribute .getAttributeValue() .equals(SOAPConstants.ATTR_MUSTUNDERSTAND_TRUE)).isTrue();

        SOAPBody body = soap12Envelope.getBody();
        assertThat(body.getLocalName().equals(SOAPConstants.BODY_LOCAL_NAME)).isTrue();
        assertThat(body.getNamespace() .getNamespaceURI() .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)).isTrue();

        SOAPFault fault = body.getFault();
        assertThat(fault.getLocalName().equals(SOAPConstants.SOAPFAULT_LOCAL_NAME)).isTrue();
        assertThat(fault.getNamespace() .getNamespaceURI() .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)).isTrue();

        Iterator<OMNode> iteratorInFault = fault.getChildren();

        iteratorInFault.next();
        SOAPFaultCode code = (SOAPFaultCode) iteratorInFault.next();
        assertThat(code.getLocalName().equals(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME)).isTrue();
        assertThat(code.getNamespace() .getNamespaceURI() .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)).isTrue();

        Iterator<OMNode> iteratorInCode = code.getChildren();

        iteratorInCode.next();
        SOAPFaultValue value1 = (SOAPFaultValue) iteratorInCode.next();
        assertThat(value1.getLocalName().equals(SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME)).isTrue();
        assertThat(value1.getNamespace() .getNamespaceURI() .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)).isTrue();
        assertThat(value1.getText().equals("env:Sender")).isTrue();

        QName valueQName = value1.getTextAsQName();
        assertThat(valueQName.getLocalPart().equals("Sender")).isTrue();

        assertThat(valueQName.getNamespaceURI().equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)).isTrue();

        iteratorInCode.next();
        SOAPFaultSubCode subCode1 = (SOAPFaultSubCode) iteratorInCode.next();
        assertThat(subCode1.getLocalName().equals(SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME)).isTrue();
        assertThat(subCode1.getNamespace() .getNamespaceURI() .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)).isTrue();

        Iterator<OMNode> iteratorInSubCode1 = subCode1.getChildren();

        iteratorInSubCode1.next();
        SOAPFaultValue value2 = (SOAPFaultValue) iteratorInSubCode1.next();
        assertThat(value2.getLocalName().equals(SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME)).isTrue();
        assertThat(value2.getNamespace() .getNamespaceURI() .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)).isTrue();
        assertThat(value2.getText().equals("m:MessageTimeout")).isTrue();

        iteratorInSubCode1.next();
        SOAPFaultSubCode subCode2 = (SOAPFaultSubCode) iteratorInSubCode1.next();
        assertThat(subCode2.getLocalName().equals(SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME)).isTrue();
        assertThat(subCode2.getNamespace() .getNamespaceURI() .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)).isTrue();

        Iterator<OMNode> iteratorInSubCode2 = subCode2.getChildren();

        iteratorInSubCode2.next();
        SOAPFaultValue value3 = (SOAPFaultValue) iteratorInSubCode2.next();
        assertThat(value3.getLocalName().equals(SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME)).isTrue();
        assertThat(value3.getNamespace() .getNamespaceURI() .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)).isTrue();
        assertThat(value3.getText().equals("m:MessageTimeout")).isTrue();

        iteratorInFault.next();
        SOAPFaultReason reason = (SOAPFaultReason) iteratorInFault.next();
        assertThat(reason.getLocalName().equals(SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME)).isTrue();
        assertThat(reason.getNamespace() .getNamespaceURI() .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)).isTrue();

        Iterator<OMNode> iteratorInReason = reason.getChildren();

        iteratorInReason.next();
        SOAPFaultText text = (SOAPFaultText) iteratorInReason.next();
        assertThat(text.getLocalName().equals(SOAP12Constants.SOAP_FAULT_TEXT_LOCAL_NAME)).isTrue();
        assertThat(text.getNamespace() .getNamespaceURI() .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)).isTrue();
        assertThat(text.getText().equals("Sender Timeout")).isTrue();

        iteratorInFault.next();
        SOAPFaultNode node = (SOAPFaultNode) iteratorInFault.next();
        assertThat(node.getLocalName().equals(SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME)).isTrue();
        assertThat(node.getNamespace() .getNamespaceURI() .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)).isTrue();
        assertThat(node.getText() .trim() .equals("http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver")).isTrue();

        iteratorInFault.next();
        SOAPFaultRole role = (SOAPFaultRole) iteratorInFault.next();
        assertThat(role.getLocalName().equals(SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME)).isTrue();
        assertThat(role.getNamespace() .getNamespaceURI() .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)).isTrue();
        assertThat(role.getText().trim().equals("ultimateReceiver")).isTrue();

        iteratorInFault.next();
        SOAPFaultDetail detail = (SOAPFaultDetail) iteratorInFault.next();
        assertThat(detail.getLocalName().equals(SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME)).isTrue();
        assertThat(detail.getNamespace() .getNamespaceURI() .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)).isTrue();

        assertThat(detail.getText().trim().equals("Details of error")).isTrue();

        Iterator<OMNode> iteratorInDetail = detail.getChildren();

        iteratorInDetail.next();
        OMElement element1 = (OMElement) iteratorInDetail.next();
        assertThat(element1.getLocalName().equals("MaxTime")).isTrue();
        assertThat(element1.getNamespace().getNamespaceURI().equals("http:www.sample.org")).isTrue();
        assertThat(element1.getText().trim().equals("P5M")).isTrue();

        Iterator<OMAttribute> attributeIterator = element1.getAllAttributes();
        OMAttribute attributeInMaxTime = attributeIterator.next();
        assertThat(attributeInMaxTime.getLocalName().equals("detail")).isTrue();
        assertThat(attributeInMaxTime.getNamespace().getNamespaceURI().equals("http:www.sample.org")).isTrue();
        assertThat(attributeInMaxTime.getAttributeValue().trim().equals("This is only a test")).isTrue();

        iteratorInDetail.next();
        OMElement element2 = (OMElement) iteratorInDetail.next();
        assertThat(element2.getLocalName().equals("AveTime")).isTrue();
        assertThat(element2.getNamespace().getNamespaceURI().equals("http:www.sample.org")).isTrue();

        Iterator<OMNode> iteratorInAveTimeElement = element2.getChildren();

        iteratorInAveTimeElement.next();
        OMElement element21 = (OMElement) iteratorInAveTimeElement.next();
        assertThat(element21.getLocalName().equals("Time")).isTrue();
        assertThat(element21.getNamespace().getNamespaceURI().equals("http:www.sample.org")).isTrue();
        assertThat(element21.getText().trim().equals("P3M")).isTrue();

        soap12Builder.close();
    }
}
