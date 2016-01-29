/*
 * Copyright (c) 2015, TypeZero Engine (game.developpers.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of TypeZero Engine nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.aionemu.commons.callbacks.enhancer;

import com.aionemu.commons.callbacks.CallbackResult;
import com.aionemu.commons.callbacks.metadata.GlobalCallback;
import com.aionemu.commons.callbacks.util.CallbacksUtil;
import com.aionemu.commons.callbacks.util.GlobalCallbackHelper;
import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author SoulKeeper
 */
public class GlobalCallbackEnhancer extends CallbackClassFileTransformer {

	private static final Logger log = LoggerFactory.getLogger(GlobalCallbackEnhancer.class);

	@Override
	protected byte[] transformClass(ClassLoader loader, byte[] clazzBytes) throws Exception {
		ClassPool cp = new ClassPool();
		cp.appendClassPath(new LoaderClassPath(loader));
		CtClass clazz = cp.makeClass(new ByteArrayInputStream(clazzBytes));

		Set<CtMethod> methdosToEnhance = new HashSet<CtMethod>();

		for (CtMethod method : clazz.getDeclaredMethods()) {
			if (!isEnhanceable(method)) {
				continue;
			}

			methdosToEnhance.add(method);
		}

		if (!methdosToEnhance.isEmpty()) {
			log.debug("Enhancing class: " + clazz.getName());
			for (CtMethod method : methdosToEnhance) {
				log.debug("Enhancing method: " + method.getLongName());
				enhanceMethod(method);
			}

			return clazz.toBytecode();
		} else {
			log.trace("Class " + clazz.getName() + " was not enhanced");
			return null;
		}
	}

	/**
	 * Responsible for method enhancing, writing service calls to method.
	 *
	 * @param method Method that has to be edited
	 * @throws javassist.CannotCompileException
	 *                                     if something went wrong
	 * @throws javassist.NotFoundException if something went wrong
	 * @throws ClassNotFoundException
	 */
	protected void enhanceMethod(CtMethod method) throws CannotCompileException, NotFoundException, ClassNotFoundException {
		ClassPool cp = method.getDeclaringClass().getClassPool();

		method.addLocalVariable("___globalCallbackResult", cp.get(CallbackResult.class.getName()));

		CtClass listenerClazz = cp.get(((GlobalCallback) method.getAnnotation(GlobalCallback.class)).value().getName());

		boolean isStatic = Modifier.isStatic(method.getModifiers());
		String listenerFieldName = "$$$" + (isStatic ? "Static" : "") + listenerClazz.getSimpleName();

		CtClass clazz = method.getDeclaringClass();
		try {
			clazz.getField(listenerFieldName);
		} catch (NotFoundException e) {
			clazz.addField(CtField.make((isStatic ? "static " : "") + "Class " + listenerFieldName + " = Class.forName(\"" + listenerClazz.getName() + "\");", clazz));
		}

		int paramLength = method.getParameterTypes().length;

		method.insertBefore(writeBeforeMethod(method, paramLength, listenerFieldName));
		method.insertAfter(writeAfterMethod(method, paramLength, listenerFieldName));
	}

	/**
	 * Code that is added in the begining of the method
	 *
	 * @param method            method that should be edited
	 * @param paramLength       Lenght of methods parameters
	 * @param listenerFieldName Listener class that is used for method
	 * @return code that will be inserted before method
	 * @throws javassist.NotFoundException if something went wrong
	 */
	protected String writeBeforeMethod(CtMethod method, int paramLength, String listenerFieldName)
			throws NotFoundException {
		StringBuilder sb = new StringBuilder();
		sb.append('{');

		sb.append(" ___globalCallbackResult = ");
		sb.append(GlobalCallbackHelper.class.getName()).append(".beforeCall(");

		// check if method is static or not
		if (Modifier.isStatic(method.getModifiers())) {
			sb.append(method.getDeclaringClass().getName()).append(".class, ").append(listenerFieldName);
			sb.append(", ");
		} else {
			sb.append("this, ").append(listenerFieldName);
			sb.append(", ");
		}

		if (paramLength > 0) {
			sb.append("new Object[]{");
			for (int i = 1; i <= paramLength; i++) {
				sb.append("($w)$").append(i);

				if (i < paramLength) {
					sb.append(',');
				}
			}
			sb.append("}");
		} else {
			sb.append("null");
		}
		sb.append(");");

		sb.append("if(___globalCallbackResult.isBlockingCaller()){");

		// Fake return due to javassist bug
		// $r is not available in "insertBefore"
		CtClass returnType = method.getReturnType();
		if (returnType.equals(CtClass.voidType)) {
			sb.append("return");
		} else if (returnType.equals(CtClass.booleanType)) {
			sb.append("return false");
		} else if (returnType.equals(CtClass.charType)) {
			sb.append("return 'a'");
		} else if (returnType.equals(CtClass.byteType) || returnType.equals(CtClass.shortType)
				|| returnType.equals(CtClass.intType) || returnType.equals(CtClass.floatType)
				|| returnType.equals(CtClass.longType) || returnType.equals(CtClass.longType)) {
			sb.append("return 0");
		}
		sb.append(";}}");
		return sb.toString();
	}

	/**
	 * Writes code that will be inserted after method
	 *
	 * @param method            method to edit
	 * @param paramLength       lenght of method paramenters
	 * @param listenerFieldName method listener
	 * @return actual code that should be inserted
	 * @throws NotFoundException if something went wrong
	 */
	protected String writeAfterMethod(CtMethod method, int paramLength, String listenerFieldName)
			throws NotFoundException {
		StringBuilder sb = new StringBuilder();
		sb.append('{');

		// workaround for javassist bug, $r is not available in "insertBefore"
		if (!method.getReturnType().equals(CtClass.voidType)) {
			sb.append("if(___globalCallbackResult.isBlockingCaller()){");
			sb.append("$_ = ($r)($w)___globalCallbackResult.getResult();");
			sb.append("}");
		}

		sb.append("___globalCallbackResult = ").append(GlobalCallbackHelper.class.getName()).append(".afterCall(");

		// check if method is static or not
		if (Modifier.isStatic(method.getModifiers())) {
			sb.append(method.getDeclaringClass().getName()).append(".class, ").append(listenerFieldName);
			sb.append(", ");
		} else {
			sb.append("this, ");
			sb.append(listenerFieldName).append(", ");
		}

		if (paramLength > 0) {
			sb.append("new Object[]{");
			for (int i = 1; i <= paramLength; i++) {
				sb.append("($w)$").append(i);

				if (i < paramLength) {
					sb.append(',');
				}
			}
			sb.append("}");
		} else {
			sb.append("null");
		}
		sb.append(", ($w)$_);");
		sb.append("if(___globalCallbackResult.isBlockingCaller()){");
		if (method.getReturnType().equals(CtClass.voidType)) {
			sb.append("return;");
		} else {
			sb.append("return ($r)($w)___globalCallbackResult.getResult();");
		}
		sb.append("}");
		sb.append("else {return $_;}");
		sb.append("}");

		return sb.toString();
	}

	/**
	 * Checks if method is enhanceable. It should be marked with
	 * {@link com.aionemu.commons.callbacks.metadata.GlobalCallback} annotation, be not native and not abstract
	 *
	 * @param method method to check
	 * @return check result
	 */
	protected boolean isEnhanceable(CtMethod method) {
		int modifiers = method.getModifiers();
		return !(Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers))
				&& CallbacksUtil.isAnnotationPresent(method, GlobalCallback.class);
	}
}