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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CoalescingFilterTest {
    /**
     * Minimal recording handler that captures {@code processCharacterData}, {@code
     * startCDATASection} and {@code endCDATASection} calls so tests can assert on them.
     */
    private static final class RecordingHandler extends XmlHandlerWrapper {
        RecordingHandler() {
            super(NullXmlHandler.INSTANCE);
        }

        sealed interface Event permits CharDataEvent, StartCDATAEvent, EndCDATAEvent {}

        record CharDataEvent(String data, boolean ignorable) implements Event {}

        record StartCDATAEvent() implements Event {}

        record EndCDATAEvent() implements Event {}

        final List<Event> events = new ArrayList<>();

        @Override
        public void processCharacterData(Object data, boolean ignorable) {
            events.add(new CharDataEvent(data.toString(), ignorable));
        }

        @Override
        public void startCDATASection() {
            events.add(new StartCDATAEvent());
        }

        @Override
        public void endCDATASection() {
            events.add(new EndCDATAEvent());
        }
    }

    /** Two adjacent text chunks are merged into one {@code processCharacterData} call. */
    @Test
    public void testAdjacentTextChunksMerged() throws StreamException {
        RecordingHandler sink = new RecordingHandler();
        XmlHandler handler = CoalescingFilter.TEXT_ONLY.createFilterHandler(sink);
        handler.startFragment();
        handler.startElement("", "root", "");
        handler.attributesCompleted();
        handler.processCharacterData("hello", false);
        handler.processCharacterData(" world", false);
        handler.endElement();
        handler.completed();

        assertThat(sink.events).hasSize(1);
        assertThat(sink.events.get(0)).isEqualTo(new RecordingHandler.CharDataEvent("hello world", false));
    }

    /**
     * Merged result is ignorable only if every individual chunk was ignorable; a single
     * non-ignorable chunk makes the whole result non-ignorable.
     */
    @Test
    public void testIgnorableFlagMerging() throws StreamException {
        RecordingHandler sink = new RecordingHandler();
        XmlHandler handler = CoalescingFilter.TEXT_ONLY.createFilterHandler(sink);
        handler.startFragment();
        handler.startElement("", "root", "");
        handler.attributesCompleted();
        handler.processCharacterData("  ", true); // ignorable whitespace
        handler.processCharacterData("text", false); // non-ignorable
        handler.processCharacterData("  ", true); // ignorable whitespace
        handler.endElement();
        handler.completed();

        assertThat(sink.events).hasSize(1);
        assertThat(sink.events.get(0)).isEqualTo(new RecordingHandler.CharDataEvent("  text  ", false));
    }

    /** All-ignorable chunks produce an ignorable merged event. */
    @Test
    public void testAllIgnorableStaysIgnorable() throws StreamException {
        RecordingHandler sink = new RecordingHandler();
        XmlHandler handler = CoalescingFilter.TEXT_ONLY.createFilterHandler(sink);
        handler.startFragment();
        handler.startElement("", "root", "");
        handler.attributesCompleted();
        handler.processCharacterData(" ", true);
        handler.processCharacterData("\n", true);
        handler.endElement();
        handler.completed();

        assertThat(sink.events).hasSize(1);
        assertThat(sink.events.get(0)).isEqualTo(new RecordingHandler.CharDataEvent(" \n", true));
    }

    /**
     * A non-character event (element boundary) flushes the buffer before being forwarded,
     * so text before and after the element are emitted as separate events.
     */
    @Test
    public void testFlushOnElementBoundary() throws StreamException {
        RecordingHandler sink = new RecordingHandler();
        XmlHandler handler = CoalescingFilter.TEXT_ONLY.createFilterHandler(sink);
        handler.startFragment();
        handler.startElement("", "root", "");
        handler.attributesCompleted();
        handler.processCharacterData("before", false);
        handler.startElement("", "child", "");
        handler.attributesCompleted();
        handler.endElement();
        handler.processCharacterData("after", false);
        handler.endElement();
        handler.completed();

        assertThat(sink.events).hasSize(2);
        assertThat(sink.events.get(0)).isEqualTo(new RecordingHandler.CharDataEvent("before", false));
        assertThat(sink.events.get(1)).isEqualTo(new RecordingHandler.CharDataEvent("after", false));
    }

    /**
     * A single chunk passes through unmodified — the same data object is forwarded (fast path).
     */
    @Test
    public void testSingleChunkPassthrough() throws StreamException {
        RecordingHandler sink = new RecordingHandler();
        XmlHandler handler = CoalescingFilter.TEXT_ONLY.createFilterHandler(sink);
        handler.startFragment();
        handler.startElement("", "root", "");
        handler.attributesCompleted();
        String data = "hello";
        handler.processCharacterData(data, false);
        handler.endElement();
        handler.completed();

        assertThat(sink.events).hasSize(1);
        // The exact same object should be forwarded when no merging was needed.
        assertThat(((RecordingHandler.CharDataEvent) sink.events.get(0)).data()).isSameAs(data);
    }

    /**
     * With {@code mergeCDATA=false}: CDATA delimiters are preserved and text before/inside/after a
     * CDATA section is flushed as separate events at each boundary.
     */
    @Test
    public void testCDATAPreservedWhenMergeCDATAFalse() throws StreamException {
        RecordingHandler sink = new RecordingHandler();
        XmlHandler handler = CoalescingFilter.TEXT_ONLY.createFilterHandler(sink);
        handler.startFragment();
        handler.startElement("", "root", "");
        handler.attributesCompleted();
        handler.processCharacterData("before", false);
        handler.startCDATASection();
        handler.processCharacterData("inside", false);
        handler.endCDATASection();
        handler.processCharacterData("after", false);
        handler.endElement();
        handler.completed();

        // Expected sequence: CharData("before"), StartCDATA, CharData("inside"), EndCDATA,
        // CharData("after")
        assertThat(sink.events).hasSize(5);
        assertThat(sink.events.get(0)).isEqualTo(new RecordingHandler.CharDataEvent("before", false));
        assertThat(sink.events.get(1)).isInstanceOf(RecordingHandler.StartCDATAEvent.class);
        assertThat(sink.events.get(2)).isEqualTo(new RecordingHandler.CharDataEvent("inside", false));
        assertThat(sink.events.get(3)).isInstanceOf(RecordingHandler.EndCDATAEvent.class);
        assertThat(sink.events.get(4)).isEqualTo(new RecordingHandler.CharDataEvent("after", false));
    }

    /**
     * With {@code mergeCDATA=true}: CDATA delimiters are suppressed and all adjacent character
     * data (including CDATA section content) is merged into a single event.
     */
    @Test
    public void testCDATAMergedWhenMergeCDATATrue() throws StreamException {
        RecordingHandler sink = new RecordingHandler();
        XmlHandler handler = CoalescingFilter.MERGE_CDATA.createFilterHandler(sink);
        handler.startFragment();
        handler.startElement("", "root", "");
        handler.attributesCompleted();
        handler.processCharacterData("before", false);
        handler.startCDATASection();
        handler.processCharacterData("inside", false);
        handler.endCDATASection();
        handler.processCharacterData("after", false);
        handler.endElement();
        handler.completed();

        // All three text segments should be merged into one; no CDATA delimiters.
        assertThat(sink.events).hasSize(1);
        assertThat(sink.events.get(0)).isEqualTo(new RecordingHandler.CharDataEvent("beforeinsideafter", false));
    }

    /**
     * With {@code mergeCDATA=true} and only CDATA content (no surrounding text): the CDATA content
     * is emitted as a plain text event.
     */
    @Test
    public void testCDATAOnlyMergedWhenMergeCDATATrue() throws StreamException {
        RecordingHandler sink = new RecordingHandler();
        XmlHandler handler = CoalescingFilter.MERGE_CDATA.createFilterHandler(sink);
        handler.startFragment();
        handler.startElement("", "root", "");
        handler.attributesCompleted();
        handler.startCDATASection();
        handler.processCharacterData("cdata content", false);
        handler.endCDATASection();
        handler.endElement();
        handler.completed();

        assertThat(sink.events).hasSize(1);
        assertThat(sink.events.get(0)).isEqualTo(new RecordingHandler.CharDataEvent("cdata content", false));
    }
}
