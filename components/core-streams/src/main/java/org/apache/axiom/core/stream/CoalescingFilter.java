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

/**
 * {@link XmlFilter} that performs coalescing, i.e. merges adjacent segments of character data into
 * a single {@link XmlHandler#processCharacterData(Object, boolean)} call.
 *
 * <p>When {@code mergeCDATA} is {@code false} (the default singleton {@link #TEXT_ONLY}), adjacent
 * text nodes are merged but CDATA sections are kept separate: a flush occurs before and after each
 * CDATA section boundary, and the {@link XmlHandler#startCDATASection()} / {@link
 * XmlHandler#endCDATASection()} events are forwarded unchanged.
 *
 * <p>When {@code mergeCDATA} is {@code true} (the singleton {@link #MERGE_CDATA}), character data
 * inside CDATA sections is also merged with adjacent text nodes and the {@link
 * XmlHandler#startCDATASection()} / {@link XmlHandler#endCDATASection()} delimiters are
 * suppressed.
 */
public final class CoalescingFilter implements XmlFilter {
    /** Singleton that coalesces adjacent text nodes only; CDATA section boundaries are preserved. */
    public static final CoalescingFilter TEXT_ONLY = new CoalescingFilter(false);

    /**
     * Singleton that coalesces adjacent text nodes and merges CDATA section content with adjacent
     * text, suppressing the CDATA section delimiters.
     */
    public static final CoalescingFilter MERGE_CDATA = new CoalescingFilter(true);

    private final boolean mergeCDATA;

    public CoalescingFilter(boolean mergeCDATA) {
        this.mergeCDATA = mergeCDATA;
    }

    @Override
    public XmlHandler createFilterHandler(XmlHandler parent) {
        return new CoalescingFilterHandler(parent, mergeCDATA);
    }
}
