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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//-----------------------------------------------

/**
 * Mini YAML-like parser. <br/>
 * <em>This is NOT a YAML-compliant parser</em>, see the caveats below.<br/>
 * This parser only reads the following minimal subset of the YAML spec
 * (see {@code http://yaml.org}):
 * <pre>
 * ---  <-- start of document (mandatory)
 * #    <-- a comment line, ignored.
 *      <-- empty lines are ignored (except in multi-line string literals)
 * key:          <-- starts a key block in a mapping block (e.g. parent container.)
 * key: literal  <-- literal is not typed and internally kept as a string.
 * key: |        <-- '|' means everything that follows is a multi-type string till
 *                   another key of the same indentation OR LESSER is found.
 * - [entry]     <-- define an element in a sequence (e.g. an array).
 * ...  <-- end of document (mandatory)
 * </pre>
 * When a line "key:\n" is parsed, it creates a key-based entity in the container,
 * initially untyped. The container is then left untyped if nothing is defined later.
 * Otherwise it is transformed into a string literal or a sequence (array) or a map (key/values)
 * depending on the next line.
 * <p/>
 * This implementation does not support anything not mentioned explicitly above, e.g.
 * sequences of sequences, or mappings of mappings, (e.g. { } or [ ]), compact nested mapping,
 * tags or references, folded scalars, etc.
 * <p/>
 * Some obvious caveats:
 * <ul>
 * <li> This is NOT a YAML-compliant parser.
 *      It's a subset at best and claims only anecdotal compatibility with the YAML spec.
 * <li> Accepted line breaks are LF, CR or CR+LF.
 * <li> Parser uses a {@link Reader} interface so it's up to the caller to decide on the
 *      charset.
 * <li> Document directives are ignored. In fact anything before or after the document
 *      markers (--- and ...) are ignored.
 * <li> Only explicit documents are supported so --- and ... are mandatory.
 * <li> It's any error to try to mix a sequence (array) and a key mapping in the same block.
 * <li> A key can be anything except whitespace and the colon character (:).
 * <li> No reflection or Java bean support. Readers uses the underlying list/maps to retrieve values.
 * </ul>
 */
public final class MiniYamlParser {

    public MiniYamlParser() {
    }

    private static final Pattern RE_EMPTY_LINE = Pattern.compile("^\\s*(?:#.*)?$");

    static class Input {
        private final BufferedReader mReader;
        private String mUnreadLine = null;
        private int mLineCount = 0;

        public Input(BufferedReader reader) {
            mReader = reader;
        }

        /** Returns a "clean" document line, ignoring empty and comment lines. */
        public String readLine() throws IOException {
            String line = mUnreadLine;
            if (line != null) {
                mUnreadLine = null;
                if (!RE_EMPTY_LINE.matcher(line).matches()) {
                    return line;
                }
            }

            while ((line = mReader.readLine()) != null) {
                mLineCount++;
                if (!RE_EMPTY_LINE.matcher(line).matches()) {
                    break;
                }
            }

            return line;
        }

        /** Returns a literal line, including empty and comment lines. */
        public String readLiteralLine() throws IOException {
            String line = mUnreadLine;
            if (line != null) {
                mUnreadLine = null;
                return line;
            }

            line = mReader.readLine();
            if (line != null) {
                mLineCount++;
            }
            return line;
        }

        public int getLineCount() {
            return mLineCount;
        }

        public void unreadLine(String line) throws ParserException {
            if (mUnreadLine != null) {
                throw new ParserException(mLineCount, "Internal Error: can't only unread 1 line");
            }
            mUnreadLine = line;
        }
    }

    public Block parse(Reader reader)
                     throws IOException, ParserException {
        BufferedReader br = new BufferedReader(reader);
        try {
            Input input = new Input(br);

            // Skip lines till we match the beginning of a document.
            String line;
            while ((line = input.readLine()) != null) {
                if ("---".equals(line)) {
                    return parseDocument(input);
                }
            }

            throw new ParserException(input,
                            "Document marker not found (aka c-directives-end). " +
                            "Tip: start your document with '---'.");
        } finally {
            try { br.close(); } catch (IOException ignore) {}
        }
    }

    // Indent whitespace.                                      1=indent
    private static final Pattern RE_INDENT = Pattern.compile("^(\\s*)[^\\s].*$");

    private Block parseDocument(Input input)
                      throws IOException, ParserException {
        Block doc = new Block();

        String indent = "";

        String line = input.readLine();
        input.unreadLine(line);
        Matcher m = RE_INDENT.matcher(line);
        if (m.matches()) {
            indent = m.group(1);
        }

        parseIntoContainer(input, doc, indent);

        line = input.readLine();
        if (!"...".equals(line)) {
            // end of document marker NOT reached.
            throw new ParserException(input,
                            "Document end marker not found (aka c-document-end). " +
                            "Tip: end your document with '...' or check indentation levels.");
        }

        return doc;
    }

    // A key or sequence (list item). This covers 3 cases:
    // 1- A new sequence  item:  ^ - optional_literal
    // 2- A new key:value item:  ^ key: optional_value
    // 3- A new sequence item containing a new key:value item:
    //                           ^ - key: optional_value
    // Option 3 is semantically equivalent to an empty sequence item followed by a key:value.
    private static final Pattern RE_SEQ_OR_KEY =
        //               1=indent  2=seq 3=map key         4=literal (optional)
        Pattern.compile("^(\\s*)(?:(-)|([^\\s:]+)\\s*:)\\s*(.*)$");
    private static final Pattern RE_SEQ_AND_KEY =
        //               1=indent 2=seq 3=map key         4=literal (optional)
        Pattern.compile("^(\\s*)(-)\\s*([^\\s:]+)\\s*:\\s*(.*)$");

    private void parseIntoContainer(Input input, Block block, String indent)
                 throws IOException, ParserException {
        try {
            String line;
            while ((line = input.readLine()) != null) {
                if ("...".equals(line)) {
                    // end of document marker reached.
                    input.unreadLine(line);
                    return;
                }

                Matcher m = RE_SEQ_AND_KEY.matcher(line);
                if (!m.matches()) {
                    m = RE_SEQ_OR_KEY.matcher(line);
                }
                if (m.matches()) {
                    assert m.groupCount() == 4;
                    String i2 = m.group(1);
                    if (i2.length() > indent.length()) {
                        throw new ParserException(input,
                                    String.format("Mismatched map indentation, expected %d but was %d'",
                                                indent.length(), i2.length()));
                    } else if (i2.length() < indent.length()) {
                        input.unreadLine(line);
                        return;
                    }

                    Block c = new Block();

                    boolean parseLiteral = true;
                    if ("-".equals(m.group(2))) {
                        // group 2 is the - for a pure sequence item
                        block.appendToSequence(c);

                        if (m.group(3) != null) {
                            // This is a combo sequence item + new key:value *inside* the
                            // new sequence. We simulate this by handling this as a new
                            // sequence item and then change the line by removing
                            // the - marker and recursively iterate to handle a key:value item.
                            line = line.substring(0, i2.length()) + ' ' + line.substring(i2.length() + 1);
                            input.unreadLine(line);
                            parseLiteral = false;
                        }

                    } else if (m.group(3) != null) {
                        // group 3 is the key for a key:value item
                        block.setKeyValue(m.group(3), c);

                    } else {
                        // This case should not happen.
                        throw new ParserException(input, "Internal error; unmatched syntax: " + line);
                    }

                    if (parseLiteral) {
                        String value = m.group(4).trim();
                        if ("|".equals(value)) {
                            // Parse literal string. The multi-line literal stops when
                            // we encounter a potential key:value or sequence item at the
                            // same or outer scope level.
                            StringBuilder sb = new StringBuilder();
                            while ((line = input.readLine()) != null) {
                                if ("...".equals(line)) {
                                    // end of document marker reached.
                                    input.unreadLine(line);
                                    break;
                                }
                                Matcher m2 = RE_SEQ_OR_KEY.matcher(line);
                                if (m2.matches() && m2.group(1).length() <= indent.length()) {
                                    // potential key:value or sequence item found.
                                    input.unreadLine(line);
                                    break;
                                }

                                sb.append(line).append('\n');
                            }

                            c.setLiteral(sb.toString());

                        } else if (value.length() > 0) {
                            c.setLiteral(value);
                        }
                    }

                    if (c.isEmpty()) {
                        line = input.readLine();
                        input.unreadLine(line);
                        Matcher m2 = RE_INDENT.matcher(line);
                        if (m2.matches()) {
                            i2 = m2.group(1);
                            if (i2.length() > indent.length()) {
                                parseIntoContainer(input, c, i2);
                            }
                        }
                    }
                } else {
                    throw new ParserException(input,
                                    "'key:' or '- sequence' expected, found: " + line);
                }
            }
        } catch (ParserException e) {
            if (e.getLine() == -1) {
                throw new ParserException(input, e);
            } else {
                throw e;
            }
        }
    }
}


