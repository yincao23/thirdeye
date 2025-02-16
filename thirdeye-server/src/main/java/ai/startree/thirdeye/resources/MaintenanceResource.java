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

package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.datalayer.bao.EnumerationItemManagerImpl.eiRef;
import static ai.startree.thirdeye.datalayer.bao.EnumerationItemManagerImpl.toAlertDTO;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.json.ThirdEyeSerialization;
import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "zzz Maintenance zzz",
    authorizations = {
        @Authorization(value = "oauth")
    })
@SwaggerDefinition(securityDefinition = @SecurityDefinition(
    apiKeyAuthDefinitions = @ApiKeyAuthDefinition(
        name = HttpHeaders.AUTHORIZATION,
        in = ApiKeyLocation.HEADER,
        key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class MaintenanceResource {

  private static final Logger log = LoggerFactory.getLogger(EnumerationItemResource.class);

  private final EnumerationItemManager enumerationItemManager;
  private final AnomalyManager anomalyManager;
  private final SubscriptionGroupManager subscriptionGroupManager;
  private final AuthorizationManager authorizationManager;

  @Inject
  public MaintenanceResource(final EnumerationItemManager enumerationItemManager,
      final AnomalyManager anomalyManager,
      final SubscriptionGroupManager subscriptionGroupManager,
      final AuthorizationManager authorizationManager) {
    this.enumerationItemManager = enumerationItemManager;
    this.anomalyManager = anomalyManager;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.authorizationManager = authorizationManager;
  }

  @DELETE
  @Path("/enumeration-items/purge")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation("Remove enumeration items that are not associated with any alert and have no anomalies")
  public Response purge(@ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      @ApiParam(defaultValue = "true") @FormParam("dryRun") final boolean dryRun) {
    enumerationItemManager.findAll().stream()
        .peek(ei -> authorizationManager.ensureCanDelete(principal, ei))
        .filter(ei -> ei.getAlert() == null)
        .filter(this::shouldPurge)
        .peek(ei -> logDeleteOperation(ei, principal, dryRun))
        .filter(ei -> !dryRun)
        .forEach(enumerationItemManager::delete);
    return Response.ok().build();
  }

  private void logDeleteOperation(final EnumerationItemDTO ei,
      final ThirdEyePrincipal principal,
      final boolean dryRun) {
    String eiString;
    try {
      eiString = ThirdEyeSerialization
          .getObjectMapper()
          .writeValueAsString(ApiBeanMapper.toApi(ei));
    } catch (final Exception e) {
      eiString = ei.toString();
    }
    log.warn("Deleting{} by {}. enumeration item(id: {}}) json: {}",
        dryRun ? "(dryRun)" : "",
        principal.getName(),
        ei.getId(),
        eiString);
  }

  private boolean shouldPurge(final EnumerationItemDTO ei) {
    final AnomalyFilter f = new AnomalyFilter().setEnumerationItemId(ei.getId());
    final long anomalyCount = anomalyManager.filter(f).size();
    if (anomalyCount > 0) {
      return false;
    }
    final long subscriptionGroupCount = subscriptionGroupManager.findAll().stream()
        .filter(Objects::nonNull)
        .filter(sg -> sg.getAlertAssociations() != null)
        .filter(sg -> sg.getAlertAssociations().stream()
            .filter(Objects::nonNull)
            .filter(aa -> aa.getEnumerationItem() != null)
            .anyMatch(aa -> ei.getId().equals(aa.getEnumerationItem().getId()))
        )
        .count();
    return subscriptionGroupCount == 0;
  }

  @POST
  @Path("/enumeration-items/fix-incorrect-anomaly-migrations")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation("Go through all anomalies and fix incorrect migrations")
  public Response fixIncorrectMigrations(
      @ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      @ApiParam(defaultValue = "true") @FormParam("dryRun") final boolean dryRun) {

    final List<EnumerationItemDTO> allEis = enumerationItemManager.findAll();

    final Map<Long, EnumerationItemDTO> idToEi = allEis.stream()
        .collect(Collectors.toMap(AbstractDTO::getId, ei -> ei));

    final Set<Long> alertIds = allEis.stream()
        .map(EnumerationItemDTO::getAlert)
        .filter(Objects::nonNull)
        .map(AbstractDTO::getId)
        .collect(Collectors.toSet());

    final Map<EiKey, EnumerationItemDTO> eiMap = new HashMap<>();
    int count = 0;
    for (final Long alertId : alertIds) {
      log.info("Processing alert(id: {}) {}/{}", alertId, count, alertIds.size());
      final AnomalyFilter f = new AnomalyFilter().setAlertId(alertId);
      final List<AnomalyDTO> anomalies = anomalyManager.filter(f);
      for (final AnomalyDTO anomaly : anomalies) {
        if (anomaly.getEnumerationItem() == null) {
          continue;
        }
        final EnumerationItemDTO ei = idToEi.get(anomaly.getEnumerationItem().getId());
        if (ei == null) {
          log.error("Anomaly(id: {}) has an enumeration item(id: {}) that does not exist",
              anomaly.getId(),
              anomaly.getEnumerationItem().getId());
          continue;
        }
        if (ei.getAlert() != null && alertId.equals(ei.getAlert().getId())) {
          continue;
        }
        log.error(
            "Enumeration item(id: {}) has an alert(id: {}) that does not match anomaly(id: {})'s alert(id: {})",
            ei.getId(),
            optional(ei.getAlert()).map(AbstractDTO::getId).orElse(null),
            anomaly.getId(),
            alertId);

        // This is the problem we are trying to fix
        final EnumerationItemDTO existingOrCreated = getExistingOrCreate(alertId, ei, eiMap);
        if (existingOrCreated == null) {
          log.error("can't fix enumeration item {} for alert(id: {})", ei.getId(), alertId);
          continue;
        }
        log.info("Moving anomaly {} to {} enumeration item(id: {}) from (id: {})",
            anomaly.getId(),
            idToEi.containsKey(existingOrCreated.getId()) ? "existing" : "new",
            existingOrCreated.getId(),
            ei.getId());

        if (!dryRun) {
          anomalyManager.update(
              anomaly.setEnumerationItem(eiRef(existingOrCreated.getId()))
          );
        }
      }
      count++;
    }

    return Response.ok(
        alertIds.stream().map(
            id -> new AlertApi().setId(id)
        ).collect(Collectors.toList())
    ).build();
  }

  @POST
  @Path("/enumeration-items/fix-incorrect-sg-migrations")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation("Go through all anomalies and fix incorrect migrations")
  public Response fixIncorrectSubscriptionGroupMigrations(
      @ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      @ApiParam(defaultValue = "true") @FormParam("dryRun") final boolean dryRun) {

    final Map<Long, EnumerationItemDTO> idToEi = enumerationItemManager.findAll().stream()
        .collect(Collectors.toMap(AbstractDTO::getId, ei -> ei));

    final Map<EiKey, EnumerationItemDTO> eiMap = new HashMap<>();

    final List<SubscriptionGroupDTO> allSgs = subscriptionGroupManager.findAll();
    for (var sg : allSgs) {
      if (sg.getAlertAssociations() == null) {
        continue;
      }

      boolean updateRequired = false;
      for (var aa : sg.getAlertAssociations()) {
        if (aa.getEnumerationItem() == null || aa.getAlert() == null) {
          continue;
        }
        final EnumerationItemDTO ei = idToEi.get(aa.getEnumerationItem().getId());
        if (ei == null) {
          log.error("Subscription group(id: {}) has an enumeration item(id: {}) that does not exist",
              sg.getId(),
              aa.getEnumerationItem().getId());
          continue;
        }
        if (ei.getAlert() != null && aa.getAlert().getId().equals(ei.getAlert().getId())) {
          continue;
        }

        // Case where the alert id in the alert association does not match the alert id in the enumeration item
        final EnumerationItemDTO existingOrCreated = getExistingOrCreate(aa.getAlert().getId(),
            ei,
            eiMap);
        if (existingOrCreated == null) {
          log.error("can't fix enumeration item {} for alert(id: {})",
              ei.getId(),
              aa.getAlert().getId());
          continue;
        }
        log.info("Moving subscription group {} to {} enumeration item(id: {}) from (id: {})",
            sg.getId(),
            idToEi.containsKey(existingOrCreated.getId()) ? "existing" : "new",
            existingOrCreated.getId(),
            ei.getId());

        aa.setEnumerationItem(eiRef(existingOrCreated.getId()));
        updateRequired = true;
      }
      if (!dryRun && updateRequired) {
        subscriptionGroupManager.update(sg);
      }
    }

    return Response.ok().build();
  }

  private EnumerationItemDTO getExistingOrCreate(final Long alertId, final EnumerationItemDTO ei,
      final Map<EiKey, EnumerationItemDTO> eiMap) {
    if (ei.getName() == null
        || ei.getName().trim().isEmpty()
        || ei.getParams() == null
        || ei.getParams().size() == 0) {
      log.error("Enumeration item(id: {}) has no name or params", ei.getId());
      return null;
    }

    final EiKey key = new EiKey(alertId, ei.getName(), ei.getParams());
    if (eiMap.containsKey(key)) {
      return eiMap.get(key);
    }
    final EnumerationItemDTO existingOrCreated = enumerationItemManager.findExistingOrCreate(
        new EnumerationItemDTO()
            .setAlert(toAlertDTO(alertId))
            .setName(ei.getName())
            .setParams(ei.getParams())
    );
    eiMap.put(key, existingOrCreated);
    return existingOrCreated;
  }

  @POST
  @Path("/anomaly/index-ignored")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation("Update the ignored index based on anomaly labels for historical anomalies")
  public Response updateIgnoreLabelIndex(
      @ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal
  ) {
    // skip already updated ignored index
    final DaoFilter filter = new DaoFilter().setPredicate(Predicate.NEQ("ignored", true));
    anomalyManager.filter(filter).stream()
        .peek(anomaly -> authorizationManager.ensureCanEdit(principal, anomaly, anomaly))
        .filter(this::isIgnored)
        .forEach(anomalyManager::update);
    return Response.ok().build();
  }

  private boolean isIgnored(final AnomalyDTO anomaly) {
    final List<AnomalyLabelDTO> labels = anomaly.getAnomalyLabels();
    return labels != null && labels.stream().anyMatch(AnomalyLabelDTO::isIgnore);
  }

  public static class EiKey {

    private final Long alertId;
    private final String name;
    private final Map<String, Object> params;

    public EiKey(final Long alertId, final String name, final Map<String, Object> params) {
      this.alertId = alertId;
      this.name = name;
      this.params = params;
    }

    public Long getAlertId() {
      return alertId;
    }

    public String getName() {
      return name;
    }

    public Map<String, Object> getParams() {
      return params;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      final EiKey eiKey = (EiKey) o;
      return Objects.equals(alertId, eiKey.alertId) &&
          Objects.equals(name, eiKey.name) &&
          Objects.equals(params, eiKey.params);
    }

    @Override
    public int hashCode() {
      return Objects.hash(alertId, name, params);
    }
  }
}
