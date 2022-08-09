/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.notification.performanceEvaluation;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import java.util.List;

/**
 * The f1 score of the cloned function with regarding to the labeled anomalies in original function.
 * The calculation is based on the time overlapped with the labeled anomalies.
 */
public class F1ScoreByTimePerformanceEvaluation extends BasePerformanceEvaluate {

  private final List<MergedAnomalyResultDTO> knownAnomalies;      // The merged anomalies which are labeled by user
  private final List<MergedAnomalyResultDTO> detectedAnomalies;        // The merged anomalies which are generated by anomaly function

  public F1ScoreByTimePerformanceEvaluation(List<MergedAnomalyResultDTO> knownAnomalies,
      List<MergedAnomalyResultDTO> detectedAnomalies) {
    this.knownAnomalies = knownAnomalies;
    this.detectedAnomalies = detectedAnomalies;
  }

  @Override
  public double evaluate() {
    double precision = new PrecisionByTimePerformanceEvaluation(knownAnomalies, detectedAnomalies)
        .evaluate();
    double recall = new RecallByTimePreformanceEvaluation(knownAnomalies, detectedAnomalies)
        .evaluate();
    return 2.0 / (1.0 / precision + 1.0 / recall);
  }
}