/*
 *
 * Copyright (C) 2016 Krishna Kuntala @ Mastek <krishna.kuntala@mastek.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.dev.ops.common.utils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.dev.ops.exceptions.impl.WrappedSystemException;

public class NumberUtil {

	public static Object calculateExpression(final String expression) {
		Object expressionResult = null;
		final ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");

		try {
			expressionResult = engine.eval(expression);
		} catch(final ScriptException e) {
			throw new WrappedSystemException(e);
		}
		return expressionResult;
	}

	public static String calculateExpressionReturnString(final String expression) {
		final Object expressionResult = calculateExpression(expression);
		return expressionResult.toString();
	}
}