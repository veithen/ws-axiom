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
package org.apache.axiom.ts.soap.envelope;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.soap.SOAPCloneOptions;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.ts.soap.SOAPSpec;
import org.apache.axiom.ts.soap.SOAPTestCase;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;

import java.util.Iterator;

public abstract class CloneOMElementTestCase extends SOAPTestCase {
    public CloneOMElementTestCase(OMMetaFactory metaFactory, SOAPSpec spec) {
        super(metaFactory, spec);
    }

    /**
     * Make a copy of the source envelope and validate the target tree
     * @param sourceEnv
     * @throws Exception
     */
    protected void copyAndCheck(SOAPEnvelope sourceEnv) throws Exception {
       
        SOAPCloneOptions options = new SOAPCloneOptions();
        options.setFetchDataHandlers(true);
        options.setPreserveModel(true);
        options.setCopyOMDataSources(true);
        SOAPEnvelope targetEnv = (SOAPEnvelope)sourceEnv.cloneOMElement(options);
        
        identityCheck(sourceEnv, targetEnv, "");
        
        String sourceText = sourceEnv.toString();
        String targetText = targetEnv.toString();
        
        XMLAssert.assertXMLIdentical(XMLUnit.compareXML(sourceText, targetText), true);
        
        sourceEnv.close(false);
    }
    
    /**
     * Check the identity of each object in the tree
     * @param source
     * @param target
     * @param depth
     */
    protected void identityCheck(OMNode source, OMNode target, String depth) {
        // System.out.println(depth + source.getClass().getName());
        if (source instanceof OMElement) {
            
            if (source instanceof OMSourcedElement) {
                assertTrue("Source = " + source.getClass().getName() + 
                           "Target = " + target.getClass().getName(),
                           target instanceof OMSourcedElement);
                assertEquals(((OMSourcedElement)source).isExpanded(), ((OMSourcedElement)target).isExpanded());
                if (((OMSourcedElement)source).isExpanded()) {
                    Iterator i = ((OMElement) source).getChildren();
                    Iterator j = ((OMElement) target).getChildren();
                    while(i.hasNext() && j.hasNext()) {
                        OMNode sourceChild = (OMNode) i.next();
                        OMNode targetChild = (OMNode) j.next();
                        identityCheck(sourceChild, targetChild, depth + "  ");
                    }
                    assertEquals("Source and Target have different number of children",
                               i.hasNext(), j.hasNext());
                }
            } else {
                assertEquals(source.getClass(), target.getClass());
                Iterator i = ((OMElement) source).getChildren();
                Iterator j = ((OMElement) target).getChildren();
                while(i.hasNext() && j.hasNext()) {
                    OMNode sourceChild = (OMNode) i.next();
                    OMNode targetChild = (OMNode) j.next();
                    identityCheck(sourceChild, targetChild, depth + "  ");
                }
                assertEquals("Source and Target have different number of children",
                           i.hasNext(), j.hasNext());
            }
        } else {
            assertEquals(source.getClass(), target.getClass());
        }
    }
}
