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

import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.util.ResourceUtils.respondOk;

import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.service.AlertService;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.api.AlertInsightsRequestApi;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "Alert", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AlertResource extends CrudResource<AlertApi, AlertDTO> {

  private static final Logger LOG = LoggerFactory.getLogger(AlertResource.class);

  private final AlertService alertService;

  @Inject
  public AlertResource(final AlertService alertService) {
    super(alertService);
    this.alertService = alertService;
  }

  @Path("{id}/insights")
  @GET
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated(forRemoval = true)
  public Response getInsights(@ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      @PathParam("id") final Long id) {
    return Response.ok(alertService.getInsightsById(principal, id)).build();
  }

  @Path("insights")
  @POST
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response getInsights(@ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      final AlertInsightsRequestApi request) {
    final AlertApi alert = request.getAlert();
    ensureExists(alert);
    return Response.ok(alertService.getInsights(request)).build();
  }

  @Path("{id}/run")
  @POST
  @Timed
  public Response runTask(
      @ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      @PathParam("id") final Long id,
      @FormParam("start") final Long startTime,
      @FormParam("end") final Long endTime
  ) {
    alertService.runTask(principal, id, startTime, endTime);
    return Response.ok().build();
  }

  @POST
  @Timed
  @Path("/validate")
  @Produces(MediaType.APPLICATION_JSON)
  // can be moved to CrudResource if /validate is needed for other entities.
  public Response validateMultiple(
      @ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      final List<AlertApi> list) {
    ensureExists(list, "Invalid request");

    alertService.validateMultiple(principal, list);
    return Response.ok().build();
  }

  @Path("evaluate")
  @POST
  @Timed
  public Response evaluate(
      @ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      final AlertEvaluationApi request
  ) throws ExecutionException {
    ensureExists(request.getStart(), "start");
    ensureExists(request.getEnd(), "end");
    ensureExists(request.getAlert(), "alert");

    return Response.ok(alertService.evaluate(principal, request)).build();
  }

  @ApiOperation(value = "Delete associated anomalies and rerun detection till present")
  @POST
  @Path("{id}/reset")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response reset(
      @ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      @PathParam("id") final Long id) {
    return respondOk(alertService.reset(principal, id));
  }

  @GET
  @Timed
  @Path("{id}/stats")
  @Produces(MediaType.APPLICATION_JSON)
  public Response stats(
      @ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      @PathParam("id") final Long id,
      @QueryParam("enumerationItem.id") final Long enumerationId,
      @QueryParam("startTime") final Long startTime,
      @QueryParam("endTime") final Long endTime
  ) {
    ensureExists(id);
    return respondOk(alertService.stats(id, enumerationId, startTime, endTime));
  }
}
