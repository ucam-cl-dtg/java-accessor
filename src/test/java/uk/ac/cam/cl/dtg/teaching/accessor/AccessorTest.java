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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AccessorTest {

  @Test
  public void resolveClassName_findsExactMatch() {
    // ARRANGE
    String className = "java.lang.String";
    Accessor accessor = new Accessor(new NoOpListener());

    // ACT
    String resolvedName = accessor.resolveClassName(className);

    // ASSERT
    assertThat(resolvedName).isEqualTo(className);
  }

  @Test
  public void loadClass_findsRegexMatch() {
    // ARRANGE
    Accessor accessor = new Accessor(new NoOpListener());
    String className = "uk.ac.cam.cl.dtg.teaching.*.AccessorTest";

    // ACT
    String resolvedName = accessor.resolveClassName(className);

    // ASSERT
    assertThat(resolvedName).isEqualTo(getClass().getName());
  }

  @Test
  public void loadClass_throws_withAmbiguousMatch() {
    // ARRANGE
    String className = getClass().getPackageName() + ".*";
    Accessor accessor = new Accessor(new NoOpListener());

    // ACT + ASSERT
    assertThrows(AccessorException.class, () -> accessor.resolveClassName(className));
  }
}
