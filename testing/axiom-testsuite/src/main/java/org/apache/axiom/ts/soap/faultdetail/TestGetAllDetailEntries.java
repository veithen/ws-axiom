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
package org.apache.axiom.ts.soap.faultdetail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultDetail;

import com.google.inject.Inject;

import junit.framework.TestCase;

public class TestGetAllDetailEntries extends TestCase {
    @Inject private SOAPFactory soapFactory;

    @Override
    protected void runTest() throws Throwable {
        SOAPEnvelope envelope = soapFactory.createSOAPEnvelope();
        SOAPBody body = soapFactory.createSOAPBody(envelope);
        SOAPFault fault = soapFactory.createSOAPFault(body);
        SOAPFaultDetail soapFaultDetail = soapFactory.createSOAPFaultDetail(fault);
        OMNamespace omNamespace = soapFactory.createOMNamespace("http://www.test.org", "test");
        Iterator<OMElement> iterator = soapFaultDetail.getAllDetailEntries();
        assertThat(iterator.hasNext()).as("SOAP Fault Detail Test : - After creating SOAP11FaultDetail element, it has DetailEntries").isFalse();
        soapFaultDetail.addDetailEntry(soapFactory.createOMElement("DetailEntry", omNamespace));
        iterator = soapFaultDetail.getAllDetailEntries();
        OMElement detailEntry = iterator.next();
        assertThat(detailEntry).as("SOAP Fault Detail Test : - After calling addDetailEntry method, getAllDetailEntries method returns empty iterator").isNotNull();
        assertThat(detailEntry.getLocalName()).as("SOAP Fault Detail Test : - detailEntry local name mismatch").isEqualTo("DetailEntry");
        assertThat(detailEntry.getNamespace().getNamespaceURI()).as("SOAP Fault Detail Test : - detailEntry namespace uri mismatch").isEqualTo("http://www.test.org");
        assertThat(iterator.hasNext()).as("SOAP Fault Detail Test : - After calling addDetailEntry method once, getAllDetailEntries method returns an iterator with two objects").isFalse();
    }
}
