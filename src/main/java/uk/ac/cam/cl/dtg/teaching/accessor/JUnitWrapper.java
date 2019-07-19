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

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class JUnitWrapper {

  public static void runTestsAndExitVm(Class<?>... testClasses) {
    JUnitCore jUnitCore = new JUnitCore();
    jUnitCore.addListener(
        new RunListener() {
          @Override
          public void testRunFinished(Result result) {
            int passed = result.getRunCount() - result.getFailureCount();
            System.out.printf(
                "%s: %d out of %d tests passed%n",
                result.wasSuccessful() ? "Pass" : "Fail", passed, result.getRunCount());
          }

          @Override
          public void testStarted(Description description) {
            System.out.println("Test starting: " + description.getMethodName());
          }

          @Override
          public void testFailure(Failure failure) {
            System.out.println("Test failed!: " + failure.getMessage());
          }
        });
    Result r = jUnitCore.run(testClasses);
    System.exit(r.wasSuccessful() ? 0 : -1);
  }
}
