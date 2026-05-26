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
package org.apache.axiom.core.stream.sax.input;

import java.io.IOException;
import org.apache.axiom.core.stream.CharacterData;
import org.apache.axiom.core.stream.CharacterDataSink;

/**
 * A reusable {@link CharacterData} implementation backed by a {@code char[]} slice. Instances are
 * only valid for the duration of the {@link org.apache.axiom.core.stream.XmlHandler#processCharacterData}
 * invocation in which they are passed.
 */
final class CharArrayCharacterData implements CharacterData {
    char[] ch;
    int start;
    int length;

    @Override
    public String toString() {
        return new String(ch, start, length);
    }

    @Override
    public void writeTo(CharacterDataSink sink) throws IOException {
        sink.getWriter().write(ch, start, length);
    }

    @Override
    public void appendTo(StringBuilder buffer) {
        buffer.append(ch, start, length);
    }

    @Override
    public Object retain() {
        return new String(ch, start, length);
    }
}
