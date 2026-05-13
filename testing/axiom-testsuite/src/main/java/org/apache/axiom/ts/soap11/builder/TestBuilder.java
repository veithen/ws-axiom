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
package org.apache.axiom.ts.soap11.builder;

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
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultRole;
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
        String soap11Message =
                "<?xml version='1.0' ?>"
                        + "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                        + "   <env:Header>\n"
                        + "       <test:echoOk xmlns:test=\"http://example.org/ts-tests\"\n"
                        + "                    env:actor=\"http://schemas.xmlsoap.org/soap/actor/next\"\n"
                        + "                    env:mustUnderstand=\"1\""
                        + "       >\n"
                        + "                       foo\n"
                        + "       </test:echoOk>\n"
                        + "   </env:Header>\n"
                        + "   <env:Body>\n"
                        + "       <env:Fault>\n"
                        + "           <faultcode>\n"
                        + "               env:Sender\n"
                        + "           </faultcode>\n"
                        + "           <faultstring>\n"
                        + "               Sender Timeout\n"
                        + "           </faultstring>\n"
                        + "           <faultactor>\n"
                        + "               http://schemas.xmlsoap.org/soap/envelope/actor/ultimateReceiver\n"
                        + "           </faultactor>\n"
                        + "           <detail xmlns:m=\"http:www.sample.org\">\n"
                        + "               Details of error\n"
                        + "               <m:MaxTime m:detail=\"This is only a test\">\n"
                        + "                   P5M\n"
                        + "               </m:MaxTime>\n"
                        + "               <m:AveTime>\n"
                        + "                   <m:Time>\n"
                        + "                       P3M\n"
                        + "                   </m:Time>\n"
                        + "               </m:AveTime>\n"
                        + "           </detail>\n"
                        + "           <n:Test xmlns:n=\"http:www.Test.org\">\n"
                        + "               <n:TestElement>\n"
                        + "                   This is only a test\n"
                        + "               </n:TestElement>\n"
                        + "           </n:Test>\n"
                        + "       </env:Fault>\n"
                        + "   </env:Body>\n"
                        + "</env:Envelope>";

        OMXMLParserWrapper soap11Builder =
                OMXMLBuilderFactory.createSOAPModelBuilder(
                        metaFactory, new StringReader(soap11Message));
        SOAPEnvelope soap11Envelope = (SOAPEnvelope) soap11Builder.getDocumentElement();
        //            soap11Envelope.build();
        //            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
        //            soap11Envelope.internalSerializeAndConsume(writer);
        //          writer.flush();

        assertThat(soap11Envelope.getLocalName().equals(SOAPConstants.SOAPENVELOPE_LOCAL_NAME)).as("SOAP 1.1 :- envelope local name mismatch").isTrue();
        assertThat(soap11Envelope .getNamespace() .getNamespaceURI() .equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)).as("SOAP 1.1 :- envelope namespace uri mismatch").isTrue();

        SOAPHeader header = soap11Envelope.getHeader();
        assertThat(header.getLocalName().equals(SOAPConstants.HEADER_LOCAL_NAME)).as("SOAP 1.1 :- Header local name mismatch").isTrue();
        assertThat(header.getNamespace() .getNamespaceURI() .equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)).as("SOAP 1.1 :- Header namespace uri mismatch").isTrue();

        SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) header.getFirstElement();
        assertThat(headerBlock.getLocalName().equals("echoOk")).as("SOAP 1.1 :- Header block name mismatch").isTrue();
        assertThat(headerBlock.getNamespace().getNamespaceURI().equals("http://example.org/ts-tests")).as("SOAP 1.1 :- Header block name space uri mismatch").isTrue();
        assertThat(headerBlock.getText().trim().equals("foo")).as("SOAP 1.1 :- Headaer block text mismatch").isTrue();

        // Attribute iteration is not in any guaranteed order.
        // Use QNames to get the OMAttributes.
        QName actorQName =
                new QName(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI, SOAP11Constants.ATTR_ACTOR);
        QName mustUnderstandQName =
                new QName(
                        SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                        SOAP11Constants.ATTR_MUSTUNDERSTAND);

        OMAttribute actorAttribute = headerBlock.getAttribute(actorQName);
        OMAttribute mustUnderstandAttribute = headerBlock.getAttribute(mustUnderstandQName);

        assertThat(mustUnderstandAttribute != null).as("SOAP 1.1 :- Mustunderstand attribute not found").isTrue();
        assertThat(mustUnderstandAttribute .getAttributeValue() .equals(SOAPConstants.ATTR_MUSTUNDERSTAND_1)).as("SOAP 1.1 :- Mustunderstand value mismatch").isTrue();
        assertThat(mustUnderstandAttribute .getNamespace() .getNamespaceURI() .equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)).as("SOAP 1.1 :- Mustunderstand attribute namespace uri mismatch").isTrue();

        assertThat(actorAttribute != null).as("SOAP 1.1 :- Actor attribute name not found").isTrue();
        assertThat(actorAttribute .getAttributeValue() .trim() .equals( "http://schemas.xmlsoap.org/soap/" + SOAP11Constants.ATTR_ACTOR + "/" + "next")).as("SOAP 1.1 :- Actor value mismatch").isTrue();
        assertThat(actorAttribute .getNamespace() .getNamespaceURI() .equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)).as("SOAP 1.1 :- Actor attribute namespace uri mismatch").isTrue();

        SOAPBody body = soap11Envelope.getBody();
        assertThat(body.getLocalName().equals(SOAPConstants.BODY_LOCAL_NAME)).as("SOAP 1.1 :- Body local name mismatch").isTrue();
        assertThat(body.getNamespace() .getNamespaceURI() .equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)).as("SOAP 1.1 :- Body namespace uri mismatch").isTrue();

        SOAPFault fault = body.getFault();
        assertThat(fault.getNamespace() .getNamespaceURI() .equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)).as("SOAP 1.1 :- Fault namespace uri mismatch").isTrue();

        Iterator<OMNode> iteratorInFault = fault.getChildren();

        iteratorInFault.next();
        SOAPFaultCode code = (SOAPFaultCode) iteratorInFault.next();
        assertThat((SOAP11Constants.SOAP_FAULT_CODE_LOCAL_NAME)).as("SOAP Fault code local name mismatch").isEqualTo(code.getLocalName());

        assertThat("env:Sender").as("SOAP 1.1 :- Fault code value mismatch").isEqualTo(code.getText().trim());

        iteratorInFault.next();
        SOAPFaultReason reason = (SOAPFaultReason) iteratorInFault.next();
        assertThat(reason.getLocalName().equals(SOAP11Constants.SOAP_FAULT_STRING_LOCAL_NAME)).as("SOAP 1.1 :- Fault string local name mismatch").isTrue();
        assertThat(reason.getText().trim().equals("Sender Timeout")).as("SOAP 1.1 :- Fault string value mismatch").isTrue();

        iteratorInFault.next();
        SOAPFaultRole role = (SOAPFaultRole) iteratorInFault.next();
        assertThat(role.getLocalName().equals(SOAP11Constants.SOAP_FAULT_ACTOR_LOCAL_NAME)).as("SOAP 1.1 :- Fault actor local name mismatch").isTrue();
        assertThat(role.getText() .trim() .equals("http://schemas.xmlsoap.org/soap/envelope/actor/ultimateReceiver")).as("SOAP 1.1 :- Actor value mismatch").isTrue();

        iteratorInFault.next();
        SOAPFaultDetail detail = (SOAPFaultDetail) iteratorInFault.next();
        assertThat(detail.getLocalName().equals(SOAP11Constants.SOAP_FAULT_DETAIL_LOCAL_NAME)).as("SOAP 1.1 :- Fault detail local name mismatch").isTrue();
        assertThat(detail.getText().trim().equals("Details of error")).as("SOAP 1.2 :- Text in detail mismatch").isTrue();

        Iterator<OMNode> iteratorInDetail = detail.getChildren();

        iteratorInDetail.next();
        OMElement element1 = (OMElement) iteratorInDetail.next();
        assertThat(element1.getLocalName().equals("MaxTime")).as("SOAP 1.1 :- MaxTime element mismatch").isTrue();
        assertThat(element1.getNamespace().getNamespaceURI().equals("http:www.sample.org")).as("SOAP 1.1 :- MaxTime element namespace mismatch").isTrue();
        assertThat(element1.getText().trim().equals("P5M")).as("SOAP 1.1 :- Text value in MaxTime element mismatch").isTrue();

        Iterator<OMAttribute> attributeIterator = element1.getAllAttributes();
        OMAttribute attributeInMaxTime = attributeIterator.next();
        assertThat(attributeInMaxTime.getLocalName().equals("detail")).as("SOAP 1.1 :- Attribute local name mismatch").isTrue();
        assertThat(attributeInMaxTime.getNamespace().getNamespaceURI().equals("http:www.sample.org")).as("SOAP 1.1 :- Attribute namespace mismatch").isTrue();
        assertThat(attributeInMaxTime.getAttributeValue().equals("This is only a test")).as("SOAP 1.1 :- Attribute value mismatch").isTrue();

        iteratorInDetail.next();
        OMElement element2 = (OMElement) iteratorInDetail.next();
        assertThat(element2.getLocalName().equals("AveTime")).as("SOAP 1.1 :- AveTime element mismatch").isTrue();
        assertThat(element2.getNamespace().getNamespaceURI().equals("http:www.sample.org")).as("SOAP 1.1 :- AveTime element namespace mismatch").isTrue();

        Iterator<OMNode> iteratorInAveTimeElement = element2.getChildren();

        iteratorInAveTimeElement.next();
        OMElement element21 = (OMElement) iteratorInAveTimeElement.next();
        assertThat(element21.getLocalName().equals("Time")).as("SOAP 1.1 :- Time element mismatch").isTrue();
        assertThat(element21.getNamespace().getNamespaceURI().equals("http:www.sample.org")).as("SOAP 1.1 :- Time element namespace mismatch").isTrue();
        assertThat(element21.getText().trim().equals("P3M")).as("SOAP 1.1 :- Text value in Time element mismatch").isTrue();

        iteratorInFault.next();
        OMElement testElement = (OMElement) iteratorInFault.next();
        assertThat(testElement.getLocalName().equals("Test")).as("SOAP 1.1 :- Test element mismatch").isTrue();
        assertThat(testElement.getNamespace().getNamespaceURI().equals("http:www.Test.org")).as("SOAP 1.1 :- Test element namespace mismatch").isTrue();

        OMElement childOfTestElement = testElement.getFirstElement();
        assertThat(childOfTestElement.getLocalName().equals("TestElement")).as("SOAP 1.1 :- Test element child local name mismatch").isTrue();
        assertThat(childOfTestElement.getNamespace().getNamespaceURI().equals("http:www.Test.org")).as("SOAP 1.1 :- Test element child namespace mismatch").isTrue();
        assertThat(childOfTestElement.getText().trim().equals("This is only a test")).as("SOAP 1.1 :- Test element child value mismatch").isTrue();

        soap11Builder.close();
    }
}
