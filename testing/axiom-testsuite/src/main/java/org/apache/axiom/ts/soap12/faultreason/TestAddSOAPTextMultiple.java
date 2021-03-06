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
package org.apache.axiom.ts.soap12.faultreason;

import static com.google.common.truth.Truth.assertThat;

import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.ts.soap.SOAPSpec;
import org.apache.axiom.ts.soap.SOAPTestCase;

public class TestAddSOAPTextMultiple extends SOAPTestCase {
    public TestAddSOAPTextMultiple(OMMetaFactory metaFactory) {
        super(metaFactory, SOAPSpec.SOAP12);
    }

    @Override
    protected void runTest() throws Throwable {
        SOAPFaultReason reason = soapFactory.createSOAPFaultReason();
        SOAPFaultText text1 = soapFactory.createSOAPFaultText();
        text1.setLang("en");
        text1.setText("System error");
        reason.addSOAPText(text1);
        SOAPFaultText text2 = soapFactory.createSOAPFaultText();
        text2.setLang("de");
        text2.setText("Systemfehler");
        reason.addSOAPText(text2);
        OMNode child = reason.getFirstOMChild();
        assertThat(child).isSameInstanceAs(text1);
        child = child.getNextOMSibling();
        assertThat(child).isSameInstanceAs(text2);
    }
}
