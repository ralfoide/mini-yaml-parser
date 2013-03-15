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
    "log"
    "regexp"
)

//----
type input struct {
    mSrc        []byte
    mUnreadLine *string
    LineCount   int
}

var kReEmptyLine = regexp.MustCompile("^\\s*(?:#.*)?$")

// Returns a "clean" document line, ignoring empty and comment lines.
func (in *input) ReadLine() (line *string) {
    line = in.mUnreadLine
    
    if line != nil {
        in.mUnreadLine = nil
        if !kReEmptyLine.MatchString(*line) {
            return line
        }
    }
    
    for len(in.mSrc) > 0 {
        line = in.readEOL()
        in.LineCount++
        if !kReEmptyLine.MatchString(*line) {
            break
        }
    }
    return line
}

// Returns a literal line, including empty and comment lines.
func (in *input) ReadLiteralLine() (line *string) {
    line = in.mUnreadLine
    
    if line != nil {
        in.mUnreadLine = nil
        return line
    }
    
    line = in.readEOL()
    if line != nil {
        in.LineCount++
    }
    return line
}

// Unreads a line (makes it available for the next ReadLine).
// line: string to unread. Can be null.
func (in *input) UnreadLine(line *string) {
    if in.mUnreadLine != nil {
        e := NewInputError(in, "Internal Error: can only unread 1 line");
        log.Fatalln(e.Error())
    }
    in.mUnreadLine = line
}


func (in *input) readEOL() *string {
    if len(in.mSrc) > 0 {
        for pos, c := range in.mSrc {
            if c == '\n' {
                pos2 := pos
                if pos2 > 0 && in.mSrc[pos-1] == '\r' {
                    pos2--
                }
                s := string(in.mSrc[:pos2])
                in.mSrc = in.mSrc[pos+1:]
                return &s
            }
        }
    }
    return nil
}