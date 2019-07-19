/*
 * Copyright © 2019 The Authors (see NOTICE file)
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
package uk.ac.cam.cl.dtg.teaching.accessor;

interface TestActionListener {

  void constructingClass(String className, Iterable<Object> params);

  void createdObject(String instanceName, Object instance);

  void gettingStaticField(String className, String fieldName);

  void gettingInstanceField(Object instance, String fieldName);

  void invokingStaticMethod(String className, String methodName, Iterable<Object> params);

  void invokingInstanceMethod(Object instance, String methodName, Iterable<Object> params);
}
