package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensure;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureExists;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.api.ApplicationApi;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.util.ApiBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AnomalyResource {

  private static final Logger log = LoggerFactory.getLogger(AnomalyResource.class);

  private final AuthService authService;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;

  @Inject
  public AnomalyResource(
      final AuthService authService,
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.authService = authService;
  }

  @GET
  public Response getAll(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader
  ) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);

    final List<MergedAnomalyResultDTO> all = mergedAnomalyResultManager.findAll();
    return Response
        .ok(all.stream().map(ApiBeanMapper::toApi))
        .build();
  }

  @POST
  public Response createMultiple(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      List<ApplicationApi> applicationApiList) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);
    ensure(false, "Unsupported Operation.");
    return Response
        .ok()
        .build();
  }

  @PUT
  public Response editMultiple(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      List<ApplicationApi> applicationApiList) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);
    ensure(false, "Unsupported Operation.");
    return Response
        .ok()
        .build();
  }

  @GET
  @Path("{id}")
  public Response get(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);
    final MergedAnomalyResultDTO dto = mergedAnomalyResultManager.findById(id);
    ensureExists(dto, "Invalid id");

    return Response
        .ok(ApiBeanMapper.toApi(dto))
        .build();
  }

  @DELETE
  @Path("{id}")
  public Response delete(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);
    ensure(false, "Unsupported Operation.");
    return Response.ok().build();
  }
}