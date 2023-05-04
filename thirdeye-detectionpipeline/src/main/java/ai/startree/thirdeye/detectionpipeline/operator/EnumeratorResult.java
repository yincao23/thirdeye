/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ai.startree.thirdeye.detectionpipeline.operator;

import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.List;

/**
 * TODO spyne - refactor the results as part of an interface. This is a temporary solution.
 */
public class EnumeratorResult implements OperatorResult {

  private final List<EnumerationItemDTO> results;

  public EnumeratorResult(final List<EnumerationItemDTO> results) {
    this.results = results;
  }

  public List<EnumerationItemDTO> getResults() {
    return results;
  }
}
