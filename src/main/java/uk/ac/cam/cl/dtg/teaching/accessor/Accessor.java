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

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Accessor {

  /** Load the class by searching the classpath and return it. */
  public Class<?> loadClass(String className) {
    try {
      return getClass().getClassLoader().loadClass(className);
    } catch (ClassNotFoundException e) {
      // ignore
    }

    ImmutableList<String> matchingClassNames = scanForMatchingClassNames(className);
    if (matchingClassNames.isEmpty()) {
      throw new AccessorException(
          new ClassNotFoundException("Failed to find a class matching name " + className));
    }
    if (matchingClassNames.size() > 1) {
      throw new AccessorException(
          new ClassNotFoundException(
              "Found multiple classes matching name "
                  + className
                  + ":"
                  + matchingClassNames.stream().collect(Collectors.joining(","))));
    }
    try {
      return getClass().getClassLoader().loadClass(matchingClassNames.get(0));
    } catch (ClassNotFoundException e) {
      throw new AccessorException(e);
    }
  }

  private ImmutableList<String> scanForMatchingClassNames(String className) {
    Pattern p = Pattern.compile(className);
    try {
      return ClassPath.from(getClass().getClassLoader()).getTopLevelClasses().stream()
          .map(ClassPath.ClassInfo::getName)
          .filter(p.asMatchPredicate())
          .collect(toImmutableList());
    } catch (IOException e) {
      throw new AccessorException("Failed to scan for matching class names", e);
    }
  }

  /** Create a new instance of the class given (from searching the classpath). */
  public Object construct(String className, Object... params) {
    Class<?> clazz = loadClass(className);
    for (Constructor<?> c : clazz.getDeclaredConstructors()) {
      if (paramMatch(c.getParameterTypes(), params)) {
        c.setAccessible(true);
        try {
          return c.newInstance(params);
        } catch (InstantiationException
            | IllegalAccessException
            | IllegalArgumentException
            | InvocationTargetException e) {
          throw new AccessorException(e);
        }
      }
    }

    String argList =
        Arrays.stream(params)
            .map(p -> p == null ? "(null)" : p.getClass().getSimpleName())
            .collect(Collectors.joining(","));
    throw new AccessorException(
        new NoSuchMethodException(
            String.format("Constructor %s(%s) not found", className, argList)));
  }

  /** Get the specified field from this object (overriding access controls). */
  public <T> T getField(Object instance, String field) {
    Class<?> c;
    if (instance instanceof String) {
      c = loadClass((String) instance);
      instance = null;
    } else {
      c = instance.getClass();
    }

    Set<Field> fields = new HashSet<>();
    addFieldList(c, fields);
    for (Field f : fields) {
      if (f.getName().equals(field)) {
        f.setAccessible(true);
        try {
          @SuppressWarnings("unchecked")
          T r = (T) f.get(instance);
          return r;
        } catch (IllegalArgumentException | IllegalAccessException e) {
          throw new AccessorException(e);
        }
      }
    }
    throw new AccessorException(
        new NoSuchFieldException("Field " + field + " not found in class " + c.getName()));
  }

  /** Invoke the specified method (overriding access controls). */
  public <T> T invoke(Object o, String methodName, Object... params) {
    try {
      return this.methodInvoker(o, methodName, params);
    } catch (InvocationTargetException e) {
      throw new AccessorException(e);
    }
  }

  /** Invoke the main method on this class. */
  public Object invokeMain(String className, String... params) {
    if (params == null) {
      params = new String[] {};
    }
    return invoke(className, "main", new Object[] {params});
  }

  /**
   * Invoke the given method, catching any exception and returning the name of the exception as a
   * string. If no exception is thrown returns null.
   */
  public String invokeCatch(String className, String methodName, Object... params) {
    try {
      methodInvoker(loadClass(className), null, methodName, params);
      return null;
    } catch (InvocationTargetException e) {
      return e.getTargetException().getClass().getName();
    }
  }

  private static boolean primitiveMatch(Class<?> primitive, Class<?> boxed, Object param) {
    return param.getClass().equals(primitive) || boxed.isInstance(param);
  }

  private static boolean paramMatch(Class<?>[] paramTypes, Object[] params) {
    if (paramTypes.length != params.length) {
      return false;
    }
    for (int i = 0; i < paramTypes.length; ++i) {
      if (!paramMatch(paramTypes[i], params[i])) {
        return false;
      }
    }
    return true;
  }

  private static boolean paramMatch(Class<?> clazz, Object param) {
    if (param == null) {
      return true;
    }
    if (clazz.isPrimitive()) {
      if (clazz.equals(Integer.TYPE)) {
        return primitiveMatch(Integer.TYPE, Integer.class, param);
      } else if (clazz.equals(Boolean.TYPE)) {
        return primitiveMatch(Boolean.TYPE, Boolean.class, param);
      } else if (clazz.equals(Float.TYPE)) {
        return primitiveMatch(Float.TYPE, Float.class, param);
      } else if (clazz.equals(Double.TYPE)) {
        return primitiveMatch(Double.TYPE, Double.class, param);
      } else if (clazz.equals(Character.TYPE)) {
        return primitiveMatch(Character.TYPE, Character.class, param);
      } else if (clazz.equals(Long.TYPE)) {
        return primitiveMatch(Long.TYPE, Long.class, param);
      } else if (clazz.equals(Short.TYPE)) {
        return primitiveMatch(Short.TYPE, Short.class, param);
      } else {
        throw new AccessorException("Unrecognised primitive type " + clazz.getName());
      }
    } else {
      return clazz.isInstance(param);
    }
  }

  private <T> T methodInvoker(Object o, String methodName, Object... params)
      throws InvocationTargetException {

    Class<?> c;
    if (o instanceof String) { // this is a class name
      c = loadClass((String) o);
      o = null;
    } else {
      c = o.getClass();
    }

    Set<Method> methods = new HashSet<>();
    addMethodList(c, methods);
    for (Method m : methods) {
      if (m.getName().equals(methodName)) {
        if (paramMatch(m.getParameterTypes(), params)) {
          m.setAccessible(true);
          if (o == null && !Modifier.isStatic(m.getModifiers())) {
            throw new AccessorException("Method " + m + " is not static");
          }
          try {
            @SuppressWarnings("unchecked")
            T res = (T) m.invoke(o, params);
            return res;
          } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new AccessorException(e);
          }
        }
      }
    }
    throw new AccessorException(
        new NoSuchMethodException(
            String.format(
                "%s method %s not found in class %s",
                o == null ? "Static" : "Instance", methodName, c.getName())));
  }

  private void addMethodList(Class<?> c, Set<Method> result) {
    if (c == null) {
      return;
    }
    result.addAll(Arrays.asList(c.getDeclaredMethods()));
    addMethodList(c.getSuperclass(), result);
  }

  private void addFieldList(Class<?> c, Set<Field> result) {
    if (c == null) {
      return;
    }
    result.addAll(Arrays.asList(c.getDeclaredFields()));
    addFieldList(c.getSuperclass(), result);
  }
}
