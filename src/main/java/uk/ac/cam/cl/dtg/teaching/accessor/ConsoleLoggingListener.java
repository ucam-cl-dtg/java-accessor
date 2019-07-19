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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.stream.Collectors;

public class ConsoleLoggingListener implements TestActionListener {

  private static final ImmutableSet<Class<?>> TO_STRING_CLASSES =
      ImmutableSet.of(
          Integer.class,
          Double.class,
          Float.class,
          String.class,
          Boolean.class,
          Double.class,
          Short.class,
          Character.class);
  private final IdentityHashMap<Object, String> objectNames;

  public ConsoleLoggingListener() {
    objectNames = new IdentityHashMap<>();
  }

  @Override
  public void constructingClass(String className, Iterable<Object> params) {
    System.out.printf(
        "- Creating new object using constructor %s(%s)%n",
        className, stringRepresentation(params));
  }

  @Override
  public void createdObject(String instanceName, Object instance) {
    objectNames.put(instance, instanceName);
    System.out.println("- New object will be referred to as '" + instanceName + "'");
  }

  @Override
  public void gettingStaticField(String className, String fieldName) {
    System.out.println("- Getting static field " + className + "." + fieldName);
  }

  @Override
  public void gettingInstanceField(Object instance, String fieldName) {
    System.out.println(
        "- Getting instance field " + stringRepresentation(instance) + "." + fieldName);
  }

  @Override
  public void invokingStaticMethod(String className, String methodName, Iterable<Object> params) {
    System.out.printf(
        "- Invoking static method %s.%s(%s)%n",
        className, methodName, stringRepresentation(params));
  }

  @Override
  public void invokingInstanceMethod(Object instance, String methodName, Iterable<Object> params) {
    System.out.printf(
        "- Invoking instance method %s.%s(%s)%n",
        stringRepresentation(instance), methodName, stringRepresentation(params));
  }

  private String stringRepresentation(Iterable<Object> objects) {
    return Streams.stream(objects).map(this::stringRepresentation).collect(Collectors.joining(","));
  }

  private String stringRepresentation(Object o) {
    if (o == null) {
      return "<null>";
    }
    if (objectNames.containsKey(o)) {
      return objectNames.get(o);
    }
    if (TO_STRING_CLASSES.contains(o.getClass())) {
      return o.toString();
    }
    return o.getClass().getName();
  }
}
