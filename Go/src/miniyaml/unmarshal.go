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
    "reflect"
    "strconv"
)

// Note: mirrored on encoding/json/decode.go for unmarshaling json data.

// An InvalidUnmarshalError describes an invalid argument passed to Unmarshal.
// (The argument to Unmarshal must be a non-nil pointer.)
type InvalidUnmarshalError struct {
    Type reflect.Type
}


func (e *InvalidUnmarshalError) Error() string {
    if e.Type == nil {
        return "yaml: Unmarshal(nil)"
    }
    if e.Type.Kind() != reflect.Ptr {
        return "yaml: Unmarshal(non-pointer " + e.Type.String() + ")"
    }
    return "yaml: Unmarshal(nil " + e.Type.String() + ")"
}

// An UnmarshalFieldError describes a yaml object key that
// led to an unexported (and therefore unwritable) struct field.
type UnmarshalFieldError struct {
    Key   string
    Type  reflect.Type
    Field reflect.StructField
}

func (e *UnmarshalFieldError) Error() string {
    return "YAML: cannot unmarshal object key " + strconv.Quote(e.Key) + " into unexported field " + e.Field.Name + " of type " + e.Type.String()
}

func (b *Block) Unmarshal(v interface{}) error {
    rv := reflect.ValueOf(v)
    
    if rv.Kind() != reflect.Ptr || rv.IsNil() {
        return &InvalidUnmarshalError { reflect.TypeOf(v) }
    }
    
    if b.IsLiteral() {
        return b.unLiteral(rv)
    }
    
    return nil
}

func (b *Block) unLiteral(v reflect.Value) error {
    
    return nil
}

