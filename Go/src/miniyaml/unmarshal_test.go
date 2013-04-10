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
    "miniyaml"
    "testing"
)

//---

//---

func TestUnmarshal0(t *testing.T) {
    b, err := createStringParser(t, `---
- line 1
...`)
    expectNotNil(t, b)
    expectNotNil(t, err)

    expectEqual (t, true, b.ToString())
    expectEqual (t, "foo", b.ToString())
}


//----

func createStringParser(t *testing.T, content string) (*miniyaml.Block, error) {
    p := miniyaml.NewParser()
    return p.Parse([]byte(content))
}
