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
package org.apache.axiom.ts.soap.headerblock;

import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.ts.soap.SOAPSpec;
import org.apache.axiom.ts.soap.SOAPTestCase;

/**
 * Tests that {@link SOAPHeaderBlock#setMustUnderstand(String)} throws a
 * {@link SOAPProcessingException} if the argument is not a valid boolean literal.
 */
public class TestSetMustUnderstandWithInvalidValue extends SOAPTestCase {
    private final String value;
    
    public TestSetMustUnderstandWithInvalidValue(OMMetaFactory metaFactory, SOAPSpec spec, String value) {
        super(metaFactory, spec);
        this.value = value;
        addTestParameter("value", value);
    }

    protected void runTest() throws Throwable {
        SOAPHeaderBlock soapHeaderBlock = createSOAPHeaderBlock();
        try {
            soapHeaderBlock.setMustUnderstand(value);
            fail("Expected SOAPProcessingException");
        } catch (SOAPProcessingException ex) {
            // Expected
        }
    }
}
