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

import com.alfray.utils.MiniYamlParser.Input;

//-----------------------------------------------

public class ParserException extends Exception {

    private static final long serialVersionUID = 7179119799044003095L;
    private final int mLine;

    public ParserException(String message) {
        super(message);
        mLine = -1;
    }

    public ParserException(int line, String message) {
        super("[line " + line + "] " + message);
        mLine = line;
    }

    public ParserException(Input context, String message) {
        this(context.getLineCount(), message);
    }

    public ParserException(Input context, ParserException e) {
        this(e.getLine() == -1 ? context.getLineCount() : e.getLine(), e.getMessage());
    }

    public int getLine() {
        return mLine;
    }

}
