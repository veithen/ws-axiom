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
package org.apache.axiom.spring.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class for implementations of
 * {@link AxiomWebServiceMessage#pushSourceExtractionStrategy(SourceExtractionStrategy, Object)} and
 * {@link AxiomWebServiceMessage#popSourceExtractionStrategy(Object)}.
 * <p>
 * Note: this class is used internally; it is not expected to be used by application code.
 */
public final class SourceExtractionStrategyStack {
    private static final Log log = LogFactory.getLog(SourceExtractionStrategyStack.class);
    
    private SourceExtractionStrategy[] strategies = new SourceExtractionStrategy[4];
    private Object[] beans = new Object[4];
    private int top = -1;
    
    public void push(SourceExtractionStrategy strategy, Object bean) {
        if (log.isDebugEnabled()) {
            log.debug("Set Source extraction strategy " + strategy + " for bean " + bean);
        }
        top++;
        int capacity = strategies.length;
        if (top == capacity) {
            SourceExtractionStrategy[] newStrategies = new SourceExtractionStrategy[capacity*2];
            System.arraycopy(strategies, 0, newStrategies, 0, capacity);
            strategies = newStrategies;
            Object[] newBeans = new Object[capacity*2];
            System.arraycopy(beans, 0, newBeans, 0, capacity);
            beans = newBeans;
        }
        strategies[top] = strategy;
        beans[top] = bean;
    }
    
    public void pop(Object bean) {
        if (top == -1 || beans[top] != bean) {
            throw new IllegalStateException();
        }
        top--;
        if (log.isDebugEnabled()) {
            if (top == -1) {
                log.debug("Restored default strategy");
            } else {
                log.debug("Restored strategy " + strategies[top] + " for bean " + beans[top]);
            }
        }
    }
    
    public SourceExtractionStrategy getCurrent() {
        if (top == -1) {
            log.debug("Stack is empty; returning default strategy");
            return SourceExtractionStrategy.DEFAULT;
        } else {
            return strategies[top];
        }
    }
}