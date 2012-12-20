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
package org.apache.axiom.om.impl.common;

import org.apache.axiom.om.NodeUnavailableException;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXBuilder;

public final class OMNodeHelper {
    private OMNodeHelper() {}
    
    public static OMNode getNextOMSibling(IChildNode node) throws OMException {
        OMNode nextSibling = node.getNextOMSiblingIfAvailable();
        if (nextSibling == null) {
            IParentNode parent = node.getIParentNode();
            if (parent != null && parent.getBuilder() != null) {
                switch (parent.getState()) {
                    case IParentNode.DISCARDED:
                        ((StAXBuilder)parent.getBuilder()).debugDiscarded(parent);
                        throw new NodeUnavailableException();
                    case IParentNode.INCOMPLETE:
                        do {
                            OMContainerHelper.buildNext(parent);
                        } while (parent.getState() == IParentNode.INCOMPLETE
                                && (nextSibling = node.getNextOMSiblingIfAvailable()) == null);
                }
            }
        }
        return nextSibling;
    }
}
