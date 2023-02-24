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
package ai.startree.thirdeye.plugins.enumerator;

import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultEnumeratorParams {

  private List<EnumerationItemDTO> items;

  public List<EnumerationItemDTO> getItems() {
    return items;
  }

  public DefaultEnumeratorParams setItems(
      final List<EnumerationItemDTO> items) {
    this.items = items;
    return this;
  }
}
