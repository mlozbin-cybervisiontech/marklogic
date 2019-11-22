/*
 * Copyright © 2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.plugin.marklogic;

import io.cdap.cdap.etl.api.validation.CauseAttributes;
import io.cdap.cdap.etl.api.validation.ValidationFailure;
import io.cdap.cdap.etl.mock.validation.MockFailureCollector;
import org.junit.Assert;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public final class ValidationAssertions {
  public static void assertOutputSchemaFieldFailed(MockFailureCollector failureCollector, String fieldName) {
    assertValidationFailed(failureCollector, CauseAttributes.OUTPUT_SCHEMA_FIELD, fieldName);
  }

  public static void assertPropertyValidationFailed(MockFailureCollector failureCollector, String paramName) {
    assertValidationFailed(failureCollector, CauseAttributes.STAGE_CONFIG, paramName);
  }

  public static void assertValidationFailed(MockFailureCollector failureCollector, String attribute, String paramName) {
    List<ValidationFailure> failureList = failureCollector.getValidationFailures();
    Assert.assertEquals(1, failureList.size());
    ValidationFailure failure = failureList.get(0);
    List<ValidationFailure.Cause> causeList = getCauses(failure, attribute);
    Assert.assertEquals(1, causeList.size());
    ValidationFailure.Cause cause = causeList.get(0);
    Assert.assertEquals(paramName, cause.getAttribute(attribute));
  }

  @Nonnull
  private static List<ValidationFailure.Cause> getCauses(ValidationFailure failure, String stacktrace) {
    return failure.getCauses()
      .stream()
      .filter(cause -> cause.getAttribute(stacktrace) != null)
      .collect(Collectors.toList());
  }
}
