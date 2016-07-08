package com.dev.ops.common.utils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.dev.ops.exceptions.impl.WrappedSystemException;

public class NumberUtil {
	/*public static void main(String[] args) {
		String expression = "4000000000000*50";
		Object result = calculateExpression(expression);
		System.out.println(expression + " = " + result);
	}*/

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