/*
 * Project: MiniYamlParser
 *
 * Copyright 2012 ralfoide gmail com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alfray.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

//-----------------------------------------------

public class Block {

    private String mLiteral = null;
    private Map<String, Block> mMapping = null;
    private List<Block> mSequence = null;

    public boolean isEmpty() {
        return !isLiteral() && !isMapping() && !isSequence();
    }

    public boolean isLiteral() {
        return mLiteral != null;
    }

    public boolean isMapping() {
        return mMapping != null;
    }

    public boolean isSequence() {
        return mSequence != null;
    }

    public String getType() {
        if (isLiteral()) {
            return "literal";
        } else if (isMapping()) {
            return "mapping";
        } else if (isSequence()) {
            return "sequence";
        }
        return "empty";
    }

    // --- Literal container

    Block setLiteral(String literal) throws ParserException {
        if (mSequence != null) {
            throw new ParserException("Block of type 'sequence' can't be converted to type 'literal'");
        } else if (mMapping != null) {
            throw new ParserException("Block of type 'mapping' can't be converted to type 'literal'");
        }

        mLiteral = literal;

        return this;
    }

    String getString() {
        return mLiteral;
    }

    double getDouble() throws NumberFormatException {
        return Double.parseDouble(getString());
    }

    int getInt() throws NumberFormatException {
        return Integer.parseInt(getString());
    }


    // --- Mapping container

    Block setKeyValue(String key, Block value) throws ParserException {
        if (mLiteral != null) {
            throw new ParserException("Block of type 'literal' can't be converted to type 'mapping'");
        } else if (mSequence != null) {
            throw new ParserException("Block of type 'sequence' can't be converted to type 'mapping'");
        }
        if (mMapping == null) {
            mMapping = new TreeMap<String, Block>();
        }
        mMapping.put(key, value);

        return this;
    }

    public Map<String, Block> getMapping() {
        return Collections.unmodifiableMap(mMapping);
    }

    public Block getKey(String key) {
        return mMapping.get(key);
    }

    /** A shortcut for {@code getMap().get(key).getLiteral()} for string literal values. */
    public String getKeyString(String key) throws ParserException {
        Block value = mMapping.get(key);
        if (value != null) {
            if (!value.isLiteral()) {
                throw new ParserException(
                            String.format("Key '%s' is of type '%s', not literal",
                                            key, value.getType()));
            }
            return value.getString();
        }
        return null;
    }

    public double getKeyDouble(String key, double defaultValue)
                  throws ParserException, NumberFormatException {
        Block value = mMapping.get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return value.getDouble();
        }
    }

    public int getKeyInt(String key, int defaultValue)
               throws ParserException, NumberFormatException {
        Block value = mMapping.get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return value.getInt();
        }
    }

    public Set<String> getKeys() {
        return mMapping.keySet();
    }

    // --- Sequence container

    Block appendToSequence(Block block) throws ParserException {
        if (mLiteral != null) {
            throw new ParserException("Block of type 'literal' can't be converted to type 'sequence'");
        } else if (mMapping != null) {
            throw new ParserException("Block of type 'mapping' can't be converted to type 'sequence'");
        }
        if (mSequence == null) {
            mSequence = new ArrayList<Block>();
        }
        mSequence.add(block);

        return this;
    }

    public List<Block> getSequence() {
        return Collections.unmodifiableList(mSequence);
    }

    // --

    /** Returns a Block representation suitable for debugging. */
    @Override
    public String toString() {
        if (mLiteral != null) {
            return "'" + mLiteral + "'";

        } else if (mMapping != null) {
            return mMapping.toString();

        } else if (mSequence != null) {
            return mSequence.toString();
        }

        return "<empty container>";
    }
}


