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
    "errors"
    "fmt"
    "sort"
    "strconv"
)

//----

// A block is either a literal or a mapping or a sequence.
// Note the API voluntarily maps the Java one.
type Block struct {
    mLiteral    *string
    mMapping    *map[string] *Block
    mSequence   *[]*Block
}

func (b *Block) IsEmpty() bool {
    return !b.IsLiteral() && !b.IsMapping() && !b.IsSequence()
}

func (b *Block) IsLiteral() bool {
    return b.mLiteral != nil
}

func (b *Block) IsMapping() bool {
    return b.mMapping != nil
}

func (b *Block) IsSequence() bool {
    return b.mSequence != nil
}

func (b *Block) GetType() string {
    if (b.IsLiteral()) {
        return "literal"
    } else if (b.IsMapping()) {
        return "mapping"
    } else if (b.IsSequence()) {
        return "sequence"
    }
    return "empty"
}

// --- Literal container

func (b *Block) SetLiteral(literal string) error {
    if b.mSequence != nil {
        return errors.New("Block of type 'sequence' can't be converted to type 'literal'")
    } else if b.mMapping != nil {
        return errors.New("Block of type 'mapping' can't be converted to type 'literal'")
    }

    b.mLiteral = &literal

    return nil
}

func (b *Block) GetString() string {
    return *b.mLiteral
}

func (b *Block) GetFloat64() (float64, error) {
    return strconv.ParseFloat(b.GetString(), 64)
}

func (b *Block) GetInt() (int64, error) {
    return strconv.ParseInt(b.GetString(), 10, 64)
}


// --- Mapping container

func (b *Block) SetKeyValue(key string, value *Block) error {
    if b.mLiteral != nil {
        return errors.New("Block of type 'literal' can't be converted to type 'mapping'")
    } else if b.mSequence != nil {
        return errors.New("Block of type 'sequence' can't be converted to type 'mapping'")
    }
    
    if b.mMapping == nil {
        b.mMapping = &map[string] *Block {}
    }
    (*b.mMapping)[key] = value

    return nil
}

func (b *Block) GetMapping() map[string] *Block {
    // Note the java version returns an unmodifiable collection.
    // However I don't really want to make a copy here.
    return *b.mMapping;
}

func (b *Block) GetKey(key string) *Block {
    return (*b.mMapping)[key];
}

// A shortcut for Block.GetMapping().GetKey(key).GetString()} for string literal values.
func (b *Block) GetKeyString(key string) (string, error) {
    value := b.GetKey(key)
    if value != nil {
        if ! value.IsLiteral() {
            return "", errors.New(fmt.Sprintf("Key '%s' is of type '%s', not literal",
                                               key, value.GetType()))
        }
        return value.GetString(), nil
    }
    return "", nil      // TODO return error to indicate lack of key?
}

func (b *Block) GetKeyDouble(key string, defaultValue float64) (float64, error) {
    value := b.GetKey(key);
    if value == nil {
        return defaultValue, nil
    }
    return value.GetFloat64()
}

func (b *Block) GetKeyInt(key string, defaultValue int64) (int64, error) {
    value := b.GetKey(key);
    if value == nil {
        return defaultValue, nil
    }
    return value.GetInt()
}

func (b *Block) GetKeys() []string {
    keys := make([]string, 0, len(*b.mMapping))
    for k := range *b.mMapping {
        keys = append(keys, k)
    }
    return keys;
}


// --- Sequence container

func (b *Block) AppendToSequence(block *Block) error {
    if b.mLiteral != nil {
        return errors.New("Block of type 'literal' can't be converted to type 'sequence'")
    } else if b.mMapping != nil {
        return errors.New("Block of type 'mapping' can't be converted to type 'sequence'")
    }
    
    if b.mSequence == nil {
        m := make([]*Block, 0)
        b.mSequence = &m
    }
    *b.mSequence = append(*b.mSequence, block)

    return nil
}

func (b *Block) GetSequence() []*Block {
    // Note the java version returns an unmodifiable collection.
    // However I don't really want to make a copy here.
    return *b.mSequence
}


// --

// Returns a Block representation suitable for debugging.
func (b *Block) ToString() string {
    if b.mLiteral != nil {
        return "'" + *b.mLiteral + "'"

    } else if b.mMapping != nil {
        keys := b.GetKeys()
        sort.Strings(keys)
        s := "{"
        first := true
        for _, k := range keys {
            if ! first {
                s += ", "
            }
            s += k + "=" + (*b.mMapping)[k].ToString()
            first = false
        }
        s += "}"
        return s

    } else if b.mSequence != nil {
        s := "["
        first := true
        for _, block := range *b.mSequence {
            if ! first {
                s += ", "
            }
            s += block.ToString()
            first = false
        }
        s += "]"
        return s
    }

    return "<empty container>";
}
