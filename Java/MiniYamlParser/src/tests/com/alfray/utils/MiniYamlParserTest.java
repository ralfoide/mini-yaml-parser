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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.*;

import org.junit.Test;


//-----------------------------------------------

public final class MiniYamlParserTest {

    @Test
    public void test10() throws IOException, ParserException {
        Block r = createParser();

        assertEquals(1.0, r.getMapping().get("format").getDouble(), 1e-3);
        assertEquals("intro", r.getMapping().get("items").getSequence().get(0).getKeyString("name"));
        assertEquals("intro", r.getMapping().get("items").getSequence().get(0).getMapping().get("name").getString());
        assertEquals(320, r.getMapping().get("items").getSequence().get(0).getMapping().get("dpi").getInt());

        assertEquals(
            "{description='A key/value set used to configure an app of mine. It contains a multi-line script.', " +
             "format='1.0', " +
             "items=[" +
               "{" +
                 "dpi='320', " +
                 "landscape='      resize width 100%\n      move image 50% 40% to screen 85% 5%\n', " +
                 "link='http://www.example.com/test1', " +
                 "name='intro', " +
                 "portrait='      resize height 75%\n      move image 50% 0% to screen 10% 5%\n', " +
                 "text={1='All inner space is    preserved. Rest is trimmed.', " +
                       "3='Interested?'}}, " +
               "{" +
                 "dpi='160', " +
                 "link='http://www.example.com/test2', " +
                 "name='family', " +
                 "text={1='All your', " +
                       "2='Bases', " +
                       "3='make', " +
                       "4='your time', " +
                       "5='belong to us.'}}, " +
               "{" +
                 "landscape='        text-color #AAAAAA\n        text 1 at  4%   68% size 12%\n', " +
                 "name='sleep'}, " +
               "{" +
                 "name='work', " +
                 "portrait='        font Serif\n        text-color #AAAAAA\n'}]}",
             r.toString());
    }

    @Test
    public void test00() throws IOException, ParserException {
        try {
            createParser();
        } catch (ParserException e) {
            assertEquals("[line 2] Document marker not found (aka c-directives-end). Tip: start your document with '---'.", e.getMessage());
            return;
        }
        fail("ParserException expected");
    }

    @Test
    public void test01() throws IOException, ParserException {
        Block r = createParser();
        assertTrue(r.isEmpty());
    }

    @Test
    public void test02() throws IOException, ParserException {
        Block r = createParser();
        assertEquals(
            "{key1='value 1', " +
             "key3='value3', " +
             "key4='  This is a multi-line\n  string literal.\n    The identation is preserved as-is.\n', " +
             "key5='Matching indentation indicates the end of the string literal.', " +
             "key_2='value    2'}", r.toString());
    }

    @Test
    public void test03() throws IOException, ParserException {
        Block r = createParser();
        assertEquals(
            "['The first string.', " +
             "'The second       value.', " +
             "'The third line.', " +
             "'  This is a multi-line\n  string literal.\n    The identation is preserved as-is.\n', " +
             "'Matching indentation indicates the end of the string literal.', " +
             "'2 following are a sequence of mappings (aka list of maps)', " +
             "{key1a='value 1a', " +
              "key1b='value 1b'}, " +
             "{key2a='value 2a', " +
              "key2b='value 2b'}, " +
             "'This value is a literal.']", r.toString());
    }

    @Test
    public void test04() throws IOException, ParserException {
        Block r = createParser();
        assertEquals(
            "{key1='value 1', " +
             "key2={key2a='value 2a', " +
                   "key2b='value 2b'}, " +
             "key3='value 3', " +
             "key4={key4a={key4a1='value 4a1'}, " +
                   "key4b={key4b1='value 4b1', " +
                          "key4b2='value 4b2'}}, " +
             "key5='value 5'}", r.toString());
    }

    @Test
    public void test05() throws IOException, ParserException {
        Block r = createParser();
        assertEquals(
            "{key1='value 1', " +
             "key2=['value 2a', " +
                   "'value 2b'], " +
             "key3='value 3', " +
             "key4=[{key4a={key4a1='value 4a1'}}, " +
                   "{key4b={key4b1='value 4b1', " +
                           "key4b2='value 4b2'}}, " +
                   "{key4c={key4c1='value 4c1', " +
                           "key4c2='value 4c2'}}], " +
             "key5='value 5'}", r.toString());
    }

    private Block createParser() throws IOException, ParserException {
        // Uses the caller's thread to get the filename.
        // E.g. if the caller method is "test12", this will load and parse
        // the data file "test12.yaml" in the class' package directory.

        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 0, n = stack.length; i < n; i++) {
            StackTraceElement caller = stack[i];
            String name = caller.getMethodName();
            if (name.startsWith("test")) {
                InputStream is = this.getClass().getResourceAsStream(name + ".yaml");

                MiniYamlParser parser = new MiniYamlParser();
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                try {
                    return parser.parse(isr);
                } finally {
                    is.close();
                }
            }
        }
        fail("createParser wasn't invoked from a method named test*");
        return null;
    }
}



