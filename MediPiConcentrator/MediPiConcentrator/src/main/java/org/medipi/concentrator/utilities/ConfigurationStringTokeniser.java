/*
 Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.concentrator.utilities;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
 /**
 * Implementation of StringTokenizer behaviour where tokens are whitespace
 * delimited, but the tokeniser is aware of, and honours, double- and single-
 * quotes, square brackets and parentheses.
 * 
 * @author Damian Murphy <murff@warlock.org>
 */
public class ConfigurationStringTokeniser {

    private final ArrayList<String> tokens = new ArrayList<>();
    private Iterator<String> tokenSet = null;

    public ConfigurationStringTokeniser(String s) {
        char[] characters = s.trim().toCharArray();
        ArrayDeque<Character> encaps = new ArrayDeque<>();
        StringBuilder sb = null;
        char in_force_quote = 0;

        for (char c : characters) {
            if (Character.isWhitespace(c)) {
                if ((in_force_quote == 0) && (encaps.isEmpty())) {
                    if (sb != null) {
                        tokens.add(sb.toString());
                        sb = null;
                    }
                } else {
                    if(sb!=null){
                        sb.append(c);
                    }
                }
            } else {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append(c);
                // Handle encapsulation
                // If c is a quote, the "turn off" all encapsulation processing
                if ((c == '\'') || (c == '"')) {
                    if (c == in_force_quote) {
                        in_force_quote = 0;
                    } else {
                        in_force_quote = c;
                    }
                } else {
                    // Handle nested quotes
                    // This *will* end up in a horrible mess if the input is badly-formed,
                    // but we don't need to try to analyse why there is a problem, just
                    // detect it.
                    if ((c == '[') || (c == '(')) {
                        encaps.addFirst(c);
                    } else {
                        if (!encaps.isEmpty()) {
                            if (c == ']') {
                                if (encaps.getFirst() == '[') {
                                    encaps.removeFirst();
                                }
                            } else {
                                if (c == ')') {
                                    if (encaps.getFirst() == '(') {
                                        encaps.removeFirst();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (sb != null) {
            tokens.add(sb.toString());
        }
        tokenSet = tokens.iterator();
    }

    public int countTokens() { return tokens.size(); }
    
    public String nextToken()
        throws Exception
    {
        return tokenSet.next();
    }

    public boolean hasMoreTokens() {
        return tokenSet.hasNext();
    }
}
