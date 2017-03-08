/*******************************************************************************
 * Copyright 2015-16 AutoCognite Testing Research Pvt Ltd
 * 
 * Website: www.AutoCognite.com
 * Email: support [at] autocognite.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.autocognite.pvt.unitee.testobject.lib.loader;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class JavaTestClassDataMethodsHandler extends DefaultDataMethodsHandler {
	private Class<?> testClass = null;

	public JavaTestClassDataMethodsHandler(Class<?> testClass) throws Exception {
		this.testClass = testClass;
	}

	protected boolean shouldInclude(Method m){
		return Modifier.isPublic(m.getModifiers());
	}

	@Override
	protected Method[] getMethods() {
		return testClass.getDeclaredMethods();
	}

	@Override
	protected String getContainerName() {
		return this.testClass.getName();
	}
}