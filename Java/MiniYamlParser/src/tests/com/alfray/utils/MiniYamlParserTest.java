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

    @Test
    public void test10() throws IOException, ParserException {
        Block r = createParser();
        assertEquals(
            "{description='An key/value set used to configure an app of mine. It contains a multi-line script.', " +
             "format='1.0', " +
             "items=[" +
             "{" +
              "dpi='320', " +
              "landscape='                load image1.png\n                    resize width 100%\n                    move image 50% 40% to screen 85% 5%\n                load image2.png\n                    resize width 10%\n                    move image 0% 100% to screen 3% 88%\n                font Sans\n                text 1 at 17% 75% size 10%\n                text 2 at 17% 85% size 10%\n                text 3 at  3% 95% size 5%\n', " +
              "link='http://www.example.com/test1', " +
              "name='intro', " +
              "portrait='                load image1.png\n                    resize height 75%\n                    move image 50% 0% to screen 10% 5%\n                load image2.png\n                    resize height 20%\n                    move image 100% 100% to screen 95% 94%\n                font Sans\n                text 1 at 3% 88% size 5%\n                text 2 at 3% 93% size 5%\n                text 3 at 3% 98% size 2.9%\n', " +
              "text={1='this is the', 2='All inner space is    preserved. Rest is trimmed.', 3='Interested?'}}, " +
             "{" +
              "dpi='320', landscape='                load image3.jpg\n                    resize width 82%\n                    move image 0% 0% to screen 0% 0%\n                load image2.png\n                    resize height 55%\n                    move image 100% 100% to screen 82% 100%\n                font Serif\n                text-color #AAAAAA\n                text 1 at     3% 70% size 10%\n                text 4 right 65% 87.5% size 20%\n                text 5 right 65% 97% size  6%\n', " +
              "link='http://www.example.com/test2', " +
              "name='family', " +
              "portrait='                load image3.jpg\n                    resize width 100%\n                    move image 0% 0% to screen 0% 68%\n                load image2.png\n                    resize height 30%\n                    move image 50% 50% to screen 2% 22%\n                font Serif\n                text-color #AAAAAA\n                text 2 at  1% 45% size 8%\n                text 3 at  6% 55% size 8%\n                text 4 at -3% 68% size 12%\n                text 5 right 85% 97% size 3.4%\n                load image2.png\n                    resize height 10%\n                    move image 100% 100% to screen 95% 98%\n', " +
              "text={1='All your', 2='Bases', 3='make', 4='your time', 5='belong to us.'}}, " +
             "{dpi='320', " +
              "landscape='                load image4.jpg\n                    resize width 100%\n                    move image 50% 72% to screen 50% 50%\n                load image2.png\n                    resize height 55%\n                    move image 50% 100% to screen 80% 100%\n                font Sans\n                text-color #AAAAAA\n                text 1 at  4%   68% size 12%\n                text 4 at  0.5% 88% size 22%\n                text 7 right 72% 95% size  6%\n', " +
              "link='http://www.example.com/test3', " +
              "name='sleep', " +
              "portrait='                load image4.jpg\n                    resize max 100%\n                    move image 50% 50% to screen 50% 50%\n                load image2.png\n                    resize height 30%\n                    move image 100% 100% to screen 102% 98%\n                font Sans\n                text-color #AAAAAA\n                text 2 at  1% 10% size 6%\n                text 3 at 20% 15% size 6%\n                text 5 at -2% 26% size 13%\n                text 6 at 10% 37% size 13%\n                text 8 right 75% 90% size 3%\n                text 9 right 75% 93% size 3%\n', " +
              "text={1='Go', 2='is the', 3='new python', 8='Love new features?'}}, " +
             "{" +
              "dpi='320', " +
              "landscape='                load image5.jpg\n                    resize width 100%\n                    move image 0% 0% to screen 0% 0%\n                load image2.png\n                    resize height 55%\n                    move image 100% 100% to screen 105% 105%\n                font Serif\n                text-color #AAAAAA\n                text 1 at 10% 70% size 10%\n                text 2 at  0% 89% size 20%\n                text 5 center 40% 98% size 5%\n', " +
              "link='http://www.example.com/test4', " +
              "name='work', " +
              "portrait='                load image5.jpg\n                    resize width 220%\n                    move image 91% 0% to screen 100% 0%\n                load image2.png\n                    resize height 30%\n                    move image 100% 100% to screen 110% 95%\n                font Serif\n                text-color #AAAAAA\n                text 1 at  0% 70% size 6%\n                text 3 at -2% 81.5% size 14%\n                text 4 at 25% 93% size 14%\n                text 5 center 50% 98% size 2.5%\n', " +
              "text={1='This FOR', 2='WORK TIME', 3='WORK', 4='TIME', 5='Continue.'}}], " +
             "script_syntax='        - empty lines are ignored.\n        - lines starting with a # are comments.\n        Images:\n          load image.jpg (or image.png) =| looks for it in assets/welcome or sdcard.\n          resize width|height|max nn%  => resize image width or height to x% of screen.\n          move image x% y% to screen x% y% => translation to match image point to screen point.\n                Note: 0% is top or left, 100% is bottom or right.\n        Text:\n          font Serif|Sans|Sans-Bold => applies to all following text\n          text-color #RRGGBB        => in hex, no alpha, applies to all following text\n          text index at|center|right|left x% y% size h%\n                x/y% are in screen coordinates.\n                size is % of the screen height (e.g screen 1000 pixels x 1% => font 10 point)   \n'}",
            r.toString());

        assertEquals(1.0, r.getMapping().get("format").getDouble(), 1e-3);
        assertEquals("intro", r.getMapping().get("items").getSequence().get(0).getKeyString("name"));
        assertEquals("intro", r.getMapping().get("items").getSequence().get(0).getMapping().get("name").getString());
        assertEquals(320, r.getMapping().get("items").getSequence().get(0).getMapping().get("dpi").getInt());
    }

}



