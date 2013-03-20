/*
 * Project: MiniYamlParser
 *
 * Copyright 2013 ralfoide gmail com
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

package miniyaml_test

import (
    "io/ioutil"
    "miniyaml"
    "os"
    "path/filepath"
    "testing"
)

//---

// 'actual' should be nil, error if it's not.
func expectNil(t *testing.T, actual interface{}) {
    if actual != nil {
        t.Fatalf("Nil expected.\nExpected: nil\nActual  : %#v", actual)
    }
}

// 'actual' should NOT be nil, error if it is.
func expectNotNil(t *testing.T, actual interface{}) {
    if actual == nil {
        t.Fatalf("Nil not expected.\nExpected: not-nil\nActual  : %#v", actual)
    }
}

func expectEqual(t *testing.T, expected, actual interface{}) {
    if actual != expected {
        t.Fatalf("Equality mismatch.\nExpected: %#v\nActual  : %#v", expected, actual)
    }
}

// wrap a type+error multiple-value return type into a struct
type _w struct {
    a interface{}
    e error
}
func w(a interface{}, e error) _w { return _w { a, e } }

// Equality for methods returning a (value, error) tuple. Use the w(value, error) wrapper.
func expectEqual2(t *testing.T, expected _w, actual _w) {
    if actual.a != expected.a {
        t.Fatalf("Equality value mismatch.\nExpected: %#v\nActual  : %#v", expected.a, actual.a)
    }
    if actual.e != expected.e {
        t.Fatalf("Equality error mismatch.\nExpected: %#v\nActual  : %#v", expected.e, actual.e)
    }
}

//---

func TestNew(t *testing.T) {

    p := miniyaml.NewParser()   
    if p == nil {
        t.Error("miniyaml.NewParser returned nil ")
    }
}

func Test00(t *testing.T) {
    b, err := createParser(t, "test00.yaml")
    expectNotNil(t, b)
    expectNotNil(t, err)
    expectEqual(t, 
        "[line 2] Document marker not found (aka c-directives-end). Tip: start your document with '---'.",
        err.Error())
}

func Test01(t *testing.T) {
    b, err := createParser(t, "test01.yaml")
    expectNotNil(t, b)
    expectNil(t, err)
}

func Test02(t *testing.T) {
    b, err := createParser(t, "test02.yaml")
    expectNotNil(t, b)
    expectNil(t, err)

    expected := "{key1='value 1', " +
             "key3='value3', " +
             "key4='  This is a multi-line\n  string literal.\n    The identation is preserved as-is.\n', " +
             "key5='Matching indentation indicates the end of the string literal.', " +
             "key_2='value    2'}"
    expectEqual(t, expected, b.ToString())
}

func Test03(t *testing.T) {
    b, err := createParser(t, "test03.yaml")
    expectNotNil(t, b)
    expectNil(t, err)

    expected := "['The first string.', " +
             "'The second       value.', " +
             "'The third line.', " +
             "'  This is a multi-line\n  string literal.\n    The identation is preserved as-is.\n', " +
             "'Matching indentation indicates the end of the string literal.', " +
             "'2 following are a sequence of mappings (aka list of maps)', " +
             "{key1a='value 1a', " +
              "key1b='value 1b'}, " +
             "{key2a='value 2a', " +
              "key2b='value 2b'}, " +
             "'This value is a literal.']"
    expectEqual(t, expected, b.ToString())
}

func Test04(t *testing.T) {
    b, err := createParser(t, "test04.yaml")
    expectNotNil(t, b)
    expectNil(t, err)

    expected := "{key1='value 1', " +
             "key2={key2a='value 2a', " +
                   "key2b='value 2b'}, " +
             "key3='value 3', " +
             "key4={key4a={key4a1='value 4a1'}, " +
                   "key4b={key4b1='value 4b1', " +
                          "key4b2='value 4b2'}}, " +
             "key5='value 5'}"
    expectEqual(t, expected, b.ToString())
}

func Test05(t *testing.T) {
    b, err := createParser(t, "test05.yaml")
    expectNotNil(t, b)
    expectNil(t, err)

    expected := "{key1='value 1', " +
             "key2=['value 2a', " +
                   "'value 2b'], " +
             "key3='value 3', " +
             "key4=[{key4a={key4a1='value 4a1'}}, " +
                   "{key4b={key4b1='value 4b1', " +
                           "key4b2='value 4b2'}}, " +
                   "{key4c={key4c1='value 4c1', " +
                           "key4c2='value 4c2'}}], " +
             "key5='value 5'}"
    expectEqual(t, expected, b.ToString())
}

func Test10(t *testing.T) {
    b, err := createParser(t, "test10.yaml")
    expectNotNil(t, b)
    expectNil(t, err)

    expected := "{description='A key/value set used to configure an app of mine. It contains a multi-line script.', " +
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
                 "portrait='        font Serif\n        text-color #AAAAAA\n'}]}"
    expectEqual(t, expected, b.ToString())
}

func Test10_GetAPI(t *testing.T) {
    r, err := createParser(t, "test10.yaml")
    expectNotNil(t, r)
    expectNil(t, err)

    expectEqual (t, "intro", 
                    r.GetMapping()["items"].GetSequence()[0].GetMapping()["name"].GetString());
    expectEqual (t, w( "intro", nil ),
                    w( r.GetMapping()["items"].GetSequence()[0].GetKeyString("name") ));
    expectEqual2(t, w( 1.0, nil ), 
                    w( r.GetMapping()["format"].GetFloat64() ));
    expectEqual2(t, w( int64(320), nil ), 
                    w( r.GetMapping()["items"].GetSequence()[0].GetKeyInt("dpi", 0) ));
    expectEqual2(t, w( int64(320), nil ), 
                    w( r.GetMapping()["items"].GetSequence()[0].GetMapping()["dpi"].GetInt() ));
    expectEqual2(t, w( int64(321), nil ), 
                    w( r.GetMapping()["items"].GetSequence()[0].GetKeyInt("not a key", 321) ));

}

//----

func createParser(t *testing.T, fileName string) (*miniyaml.Block, error) {
    pwd, _ := os.Getwd()
    data_bin, err := ioutil.ReadFile(filepath.Join(pwd, "..", "..", "testdata", fileName))
    if err != nil || data_bin == nil {
        t.Fatal(err)
    }

    p := miniyaml.NewParser()
    return p.Parse(data_bin)
}
