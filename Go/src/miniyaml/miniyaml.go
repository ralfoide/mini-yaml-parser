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

package miniyaml

import (
    "fmt"
    "io"
    "regexp"
    "strings"
)

func NewParser() *MiniYaml {
    return &MiniYaml{}
}

//----
type ParserError struct {
    Line    int
    Msg     string
}

func NewParserError(msg string) error {
    return NewLineError(-1, msg)
}

func NewInputError(in *input, msg string) error {
    return NewLineError(in.LineCount, msg)
}

func NewLineError(line int, msg string) error {
    return &ParserError{ line, msg }
}

func (e *ParserError) Error() string {
    return fmt.Sprintf("[line %d] %s", e.Line, e.Msg)
}


//----
type MiniYaml struct {
    mRoot    *Block
}

func (my *MiniYaml) Parse(data []byte) (*Block, error) {
    in := &input { mSrc: data }

    for {
        if line := in.ReadLine(); line != nil {
            if *line == "---" {
                if b, e := my.parseDocument(in); e != io.EOF {
                    return b, e
                }
                break
            }
        } else {
            break
        }
    }
    
    return nil, NewInputError(in, "Document marker not found (aka c-directives-end). " +
                                  "Tip: start your document with '---'.")
}

// Indent whitespace.                1=indent
var kReIndent = regexp.MustCompile("^(\\s*)[^\\s].*$")

func (my *MiniYaml) parseDocument(in *input) (*Block, error) {
    doc := &Block{}
    indent := ""
    
    line := in.ReadLine()
    if line == nil {
        return doc, io.EOF
    }
    in.UnreadLine(line)
    m := kReIndent.FindStringSubmatch(*line)
    if m != nil {
        indent = m[1]
    }
    
    e := my.parseIntoContainer(in, doc, indent)
    
    if e == nil {
        line = in.ReadLine()
        if line == nil || *line != "..." {
            e = io.EOF
        }
    }

    if e == io.EOF {
        e = NewInputError(in, "Document end marker not found (aka c-document-end). " +
                          "Tip: end your document with '...' or check indentation levels.")
    }
    
    return doc, e
}


// A key or sequence (list item). This covers 3 cases:
// 1- A new sequence  item:  ^ - optional_literal
// 2- A new key:value item:  ^ key: optional_value
// 3- A new sequence item containing a new key:value item:
//                           ^ - key: optional_value
// Option 3 is semantically equivalent to an empty sequence item followed by a key:value.
//                                     1=indent  2=seq 3=map key         4=literal (optional)
var kReSeqOrKey = regexp.MustCompile("^(\\s*)(?:(-)|([^\\s:]+)\\s*:)\\s*(.*)$");
//                                      1=indent 2=seq 3=map key         4=literal (optional)
var kReSeqAndKey = regexp.MustCompile("^(\\s*)(-)\\s*([^\\s:]+)\\s*:\\s*(.*)$");

func (my *MiniYaml) parseIntoContainer(in *input, block *Block, indent string) error {
    for {
        line := in.ReadLine()
        if line == nil {
            return io.EOF
        } else if *line == "..." {
            // end of document marker reached.
            in.UnreadLine(line)
            return nil
        }
        
        m := kReSeqAndKey.FindStringSubmatch(*line)
        if m == nil {
            m = kReSeqOrKey.FindStringSubmatch(*line)
        }
        if m != nil {
            // assert len(m) == 5, match groups are 1..4
            i2 := m[1]
            if len(i2) > len(indent) {
                return NewInputError(in,
                        fmt.Sprintf("Mismatched map indentation, expected %d but was %d'",
                                    len(indent), len(i2)))
            } else if len(i2) < len(indent) {
                in.UnreadLine(line)
                return nil
            }
            
            c := &Block{}
            
            parseLiteral := true
            if m[2] == "-" {
                // group 2 is the - for a pure sequence item
                block.AppendToSequence(c)
                
                if len(m[3]) > 0 {
                    // This is a combo sequence item + new key:value *inside* the
                    // new sequence. We simulate this by handling this as a new
                    // sequence item and then change the line by removing
                    // the - marker and recursively iterate to handle a key:value item.
                    s := *line
                    s  = string(s[:len(i2)] + " " + s[len(i2)+1:])
                    line = &s
                    in.UnreadLine(line)
                    parseLiteral = false
                }

            } else if len(m[3]) > 0 {
                // group 3 is the key for a key:value item
                block.SetKeyValue(m[3], c)

            } else {
                // This case should not happen.
                return NewInputError(in, 
                            fmt.Sprintf("Internal error; unmatched syntax: %s", *line))
            }
            
            if parseLiteral {
                value := strings.TrimSpace(m[4])
                if value == "|" {
                    // Parse literal string. The multi-line literal stops when
                    // we encounter a potential key:value or sequence item at the
                    // same or outer scope level.
                    sb := ""
                    for {
                        line = in.ReadLine()
                        if line == nil {
                            break
                        } else if *line == "..." {
                            // end of document marker reached.
                            in.UnreadLine(line)
                            break
                        }
                        m2 := kReSeqOrKey.FindStringSubmatch(*line)
                        if m2 != nil && len(m2[1]) <= len(indent) {
                            // potential key:value or sequence item found.
                            in.UnreadLine(line)
                            break
                        }
                        
                        sb = sb + *line + "\n"
                    }
                    c.SetLiteral(sb)
                    
                } else if len(value) > 0 {
                    c.SetLiteral(value)
                }
            }
            
            if c.IsEmpty() {
                line = in.ReadLine()
                in.UnreadLine(line)
                m2 := kReIndent.FindStringSubmatch(*line)
                if m2 != nil {
                    i2 = m2[1]
                    if len(i2) > len(indent) {
                        my.parseIntoContainer(in, c, i2)
                    }
                }
            }
        } else {
            return NewInputError(in, 
                        fmt.Sprintf("'key:' or '- sequence' expected, found: %s", *line))
        }
    }
    
    return nil
}
