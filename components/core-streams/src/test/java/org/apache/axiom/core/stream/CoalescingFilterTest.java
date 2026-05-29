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
package org.apache.axiom.core.stream;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

public class CoalescingFilterTest {
    /** Two consecutive processCharacterData calls are merged into one. */
    @Test
    public void testAdjacentTextNodesMerged() throws StreamException {
        XmlHandler parent = mock(XmlHandler.class);
        XmlHandler handler = CoalescingFilter.DEFAULT.createFilterHandler(parent);
        handler.processCharacterData("hello ", false);
        handler.processCharacterData("world", false);
        handler.endElement();
        InOrder order = inOrder(parent);
        order.verify(parent).processCharacterData("hello world", false);
        order.verify(parent).endElement();
        order.verifyNoMoreInteractions();
    }

    /** ignorable is true only when every accumulated chunk was ignorable. */
    @Test
    public void testIgnorableFlagAllIgnorable() throws StreamException {
        XmlHandler parent = mock(XmlHandler.class);
        XmlHandler handler = CoalescingFilter.DEFAULT.createFilterHandler(parent);
        handler.processCharacterData(" ", true);
        handler.processCharacterData("  ", true);
        handler.endElement();
        verify(parent).processCharacterData("   ", true);
    }

    @Test
    public void testIgnorableFlagMixed() throws StreamException {
        XmlHandler parent = mock(XmlHandler.class);
        XmlHandler handler = CoalescingFilter.DEFAULT.createFilterHandler(parent);
        handler.processCharacterData(" ", true);
        handler.processCharacterData("text", false);
        handler.endElement();
        verify(parent).processCharacterData(" text", false);
    }

    /** A non-character event causes any pending character data to be flushed first. */
    @Test
    public void testNonCharacterEventCausesFlush() throws StreamException {
        XmlHandler parent = mock(XmlHandler.class);
        XmlHandler handler = CoalescingFilter.DEFAULT.createFilterHandler(parent);
        handler.processCharacterData("a", false);
        handler.endElement();
        handler.processCharacterData("b", false);
        handler.endElement();
        InOrder order = inOrder(parent);
        order.verify(parent).processCharacterData("a", false);
        order.verify(parent).endElement();
        order.verify(parent).processCharacterData("b", false);
        order.verify(parent).endElement();
        order.verifyNoMoreInteractions();
    }

    /**
     * In non-CDATA mode, CDATA section boundaries are preserved. Text adjacent to a CDATA section
     * is flushed as a separate text event before or after the CDATA boundary.
     */
    @Test
    public void testCDATAPreservedInDefaultMode() throws StreamException {
        XmlHandler parent = mock(XmlHandler.class);
        XmlHandler handler = CoalescingFilter.DEFAULT.createFilterHandler(parent);
        handler.processCharacterData("before", false);
        handler.startCDATASection();
        handler.processCharacterData("inside", false);
        handler.endCDATASection();
        handler.processCharacterData("after", false);
        handler.endElement();
        InOrder order = inOrder(parent);
        order.verify(parent).processCharacterData("before", false);
        order.verify(parent).startCDATASection();
        order.verify(parent).processCharacterData("inside", false);
        order.verify(parent).endCDATASection();
        order.verify(parent).processCharacterData("after", false);
        order.verify(parent).endElement();
        order.verifyNoMoreInteractions();
    }

    /**
     * In CDATA-coalescing mode, CDATA section boundaries are removed and all adjacent character
     * data (text and CDATA content) is merged into a single text event.
     */
    @Test
    public void testCDATADissolvedInCoalesceCDATAMode() throws StreamException {
        XmlHandler parent = mock(XmlHandler.class);
        XmlHandler handler = CoalescingFilter.COALESCE_CDATA.createFilterHandler(parent);
        handler.processCharacterData("before", false);
        handler.startCDATASection();
        handler.processCharacterData("inside", false);
        handler.endCDATASection();
        handler.processCharacterData("after", false);
        handler.endElement();
        InOrder order = inOrder(parent);
        order.verify(parent).processCharacterData("beforeinsideafter", false);
        order.verify(parent).endElement();
        order.verifyNoMoreInteractions();
    }

    /** In CDATA-coalescing mode, a standalone CDATA section with no adjacent text is also merged. */
    @Test
    public void testStandaloneCDATADissolvedInCoalesceCDATAMode() throws StreamException {
        XmlHandler parent = mock(XmlHandler.class);
        XmlHandler handler = CoalescingFilter.COALESCE_CDATA.createFilterHandler(parent);
        handler.startCDATASection();
        handler.processCharacterData("data", false);
        handler.endCDATASection();
        handler.endElement();
        InOrder order = inOrder(parent);
        order.verify(parent).processCharacterData("data", false);
        order.verify(parent).endElement();
        order.verifyNoMoreInteractions();
    }

    /** Empty pending buffer: no processCharacterData call is forwarded. */
    @Test
    public void testNoCharacterDataNoFlush() throws StreamException {
        XmlHandler parent = mock(XmlHandler.class);
        XmlHandler handler = CoalescingFilter.DEFAULT.createFilterHandler(parent);
        handler.endElement();
        verify(parent).endElement();
        verifyNoMoreInteractions(parent);
    }
}
