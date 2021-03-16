package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.common.constants.rca.MultiDimensionalSummaryConstants.CUBE_DEPTH;
import static org.apache.pinot.thirdeye.common.constants.rca.MultiDimensionalSummaryConstants.CUBE_DIM_HIERARCHIES;
import static org.apache.pinot.thirdeye.common.constants.rca.MultiDimensionalSummaryConstants.CUBE_EXCLUDED_DIMENSIONS;
import static org.apache.pinot.thirdeye.common.constants.rca.MultiDimensionalSummaryConstants.CUBE_ONE_SIDE_ERROR;
import static org.apache.pinot.thirdeye.common.constants.rca.MultiDimensionalSummaryConstants.CUBE_SUMMARY_SIZE;
import static org.apache.pinot.thirdeye.common.constants.rca.RootCauseResourceConstants.BASELINE_END;
import static org.apache.pinot.thirdeye.common.constants.rca.RootCauseResourceConstants.BASELINE_START;
import static org.apache.pinot.thirdeye.common.constants.rca.RootCauseResourceConstants.CURRENT_END;
import static org.apache.pinot.thirdeye.common.constants.rca.RootCauseResourceConstants.CURRENT_START;
import static org.apache.pinot.thirdeye.common.constants.rca.RootCauseResourceConstants.METRIC_URN;
import static org.apache.pinot.thirdeye.common.constants.rca.RootCauseResourceConstants.TIME_ZONE;
import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_DEPTH;
import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_EXCLUDED_DIMENSIONS;
import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_HIERARCHIES;
import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_ONE_SIDE_ERROR;
import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_TIMEZONE_ID;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensure;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureExists;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.parseListParams;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.api.RootCauseEntity;
import org.apache.pinot.thirdeye.api.DataCubeSummaryApi;
import org.apache.pinot.thirdeye.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator;
import org.apache.pinot.thirdeye.rca.RootCauseAnalysisService;
import org.apache.pinot.thirdeye.rca.RootCauseEntityFormatter;
import org.apache.pinot.thirdeye.rootcause.Entity;
import org.apache.pinot.thirdeye.rootcause.RCAFramework;
import org.apache.pinot.thirdeye.rootcause.RCAFrameworkExecutionResult;
import org.apache.pinot.thirdeye.rootcause.impl.TimeRangeEntity;
import org.apache.pinot.thirdeye.rootcause.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "Root Cause Analysis")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RcaResource {

  private static final Logger LOG = LoggerFactory.getLogger(RcaResource.class);

  private static final int DEFAULT_FORMATTER_DEPTH = 1;

  private static final long ANALYSIS_RANGE_MAX = TimeUnit.DAYS.toMillis(32);
  private static final long ANOMALY_RANGE_MAX = TimeUnit.DAYS.toMillis(32);
  private static final long BASELINE_RANGE_MAX = ANOMALY_RANGE_MAX;

  private final List<RootCauseEntityFormatter> formatters;
  private final Map<String, RCAFramework> frameworks;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final DataCubeSummaryCalculator dataCubeSummaryCalculator;
  private final RootCauseTemplateResource rootCauseTemplateResource;
  private final RootCauseSessionResource rootCauseSessionResource;
  private final RootCauseMetricResource rootCauseMetricResource;

  @Inject
  public RcaResource(
      final RootCauseAnalysisService rootCauseAnalysisService,
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final DataCubeSummaryCalculator dataCubeSummaryCalculator,
      final RootCauseTemplateResource rootCauseTemplateResource,
      final RootCauseSessionResource rootCauseSessionResource,
      final RootCauseMetricResource rootCauseMetricResource) {
    this.frameworks = rootCauseAnalysisService.getFrameworks();
    this.formatters = rootCauseAnalysisService.getFormatters();
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.dataCubeSummaryCalculator = dataCubeSummaryCalculator;
    this.rootCauseTemplateResource = rootCauseTemplateResource;
    this.rootCauseSessionResource = rootCauseSessionResource;
    this.rootCauseMetricResource = rootCauseMetricResource;
  }

  @Path(value = "/template")
  public RootCauseTemplateResource getRootCauseTemplateResource() {
    return rootCauseTemplateResource;
  }

  @Path(value = "/sessions")
  public RootCauseSessionResource getRootCauseSessionResource() {
    return rootCauseSessionResource;
  }

  @Path(value = "/metrics")
  public RootCauseMetricResource getRootCauseMetricResource() {
    return rootCauseMetricResource;
  }

  @GET
  @Path("/cube/anomaly")
  @ApiOperation("Retrieve the likely root causes behind an anomaly")
  public Response dataCubeSummary(
      @ApiParam(value = "internal id of the anomaly")
      @QueryParam("anomalyId") long anomalyId) {
    final MergedAnomalyResultDTO anomalyDTO = ensureExists(
        mergedAnomalyResultManager.findById(anomalyId), String.format("Anomaly ID: %d", anomalyId));

    // In the highlights api we retrieve only the top 3 results across 3 dimensions.
    // TODO: polish the results to make it more meaningful
    return Response.ok(dataCubeSummaryCalculator.compute(anomalyDTO)).build();
  }

  @GET
  @Path("cube")
  @Produces(MediaType.APPLICATION_JSON)
  public Response buildSummary(
      @QueryParam(METRIC_URN) String metricUrn,
      @QueryParam("dataset") String dataset,
      @QueryParam("metric") String metric,
      @QueryParam(CURRENT_START) long currentStartInclusive,
      @QueryParam(CURRENT_END) long currentEndExclusive,
      @QueryParam(BASELINE_START) long baselineStartInclusive,
      @QueryParam(BASELINE_END) long baselineEndExclusive,
      @QueryParam("dimensions") String groupByDimensions,
      @QueryParam("filters") String filterJsonPayload,
      @QueryParam(CUBE_SUMMARY_SIZE) int summarySize,
      @QueryParam(CUBE_DEPTH) @DefaultValue(DEFAULT_DEPTH) int depth,
      @QueryParam(CUBE_DIM_HIERARCHIES) @DefaultValue(DEFAULT_HIERARCHIES) String hierarchiesPayload,
      @QueryParam(CUBE_ONE_SIDE_ERROR) @DefaultValue(DEFAULT_ONE_SIDE_ERROR) boolean doOneSideError,
      @QueryParam(CUBE_EXCLUDED_DIMENSIONS) @DefaultValue(DEFAULT_EXCLUDED_DIMENSIONS) String excludedDimensions,
      @QueryParam(TIME_ZONE) @DefaultValue(DEFAULT_TIMEZONE_ID) String timeZone) throws Exception {
    final DataCubeSummaryApi response = dataCubeSummaryCalculator.compute(metricUrn,
        metric,
        dataset,
        currentStartInclusive,
        currentEndExclusive,
        baselineStartInclusive,
        baselineEndExclusive,
        groupByDimensions,
        filterJsonPayload,
        summarySize,
        depth,
        hierarchiesPayload,
        doOneSideError,
        excludedDimensions,
        timeZone);
    return Response.ok(response).build();
  }

  @GET
  @Path("/query")
  @ApiOperation(value = "Send query")
  public List<RootCauseEntity> query(
      @ApiParam(value = "framework name")
      @QueryParam("framework") String framework,
      @ApiParam(value = "start overall time window to consider for events")
      @QueryParam("analysisStart") Long analysisStart,
      @ApiParam(value = "end of overall time window to consider for events")
      @QueryParam("analysisEnd") Long analysisEnd,
      @ApiParam(value = "start time of the anomalous time period for the metric under analysis")
      @QueryParam("anomalyStart") Long anomalyStart,
      @ApiParam(value = "end time of the anomalous time period for the metric under analysis")
      @QueryParam("anomalyEnd") Long anomalyEnd,
      @ApiParam(value = "baseline start time, e.g. anomaly start time offset by 1 week")
      @QueryParam("baselineStart") Long baselineStart,
      @ApiParam(value = "baseline end time, e.g. typically anomaly start time offset by 1 week")
      @QueryParam("baselineEnd") Long baselineEnd,
      @QueryParam("formatterDepth") Integer formatterDepth,
      @ApiParam(value = "URNs of metrics to analyze")
      @QueryParam("urns") List<String> urns) throws Exception {

    // configuration validation
    ensure(frameworks.containsKey(framework),
        String.format("Could not resolve framework '%s'. Allowed values: %s",
            framework,
            frameworks.keySet()));

    // input validation
    ensureExists(analysisStart,
        "Must provide analysis start timestamp (in milliseconds)");

    ensureExists(analysisEnd,
        "Must provide analysis end timestamp (in milliseconds)");

    if (anomalyStart == null) {
      anomalyStart = analysisStart;
    }

    if (anomalyEnd == null) {
      anomalyEnd = analysisEnd;
    }

    if (baselineStart == null) {
      baselineStart = anomalyStart - TimeUnit.DAYS.toMillis(7);
    }

    if (baselineEnd == null) {
      baselineEnd = anomalyEnd - TimeUnit.DAYS.toMillis(7);
    }

    if (formatterDepth == null) {
      formatterDepth = DEFAULT_FORMATTER_DEPTH;
    }

    ensure(analysisEnd - analysisStart <= ANALYSIS_RANGE_MAX,
        String.format("Analysis range cannot be longer than %d", ANALYSIS_RANGE_MAX));

    ensure(anomalyEnd - anomalyStart <= ANOMALY_RANGE_MAX,
        String.format("Anomaly range cannot be longer than %d", ANOMALY_RANGE_MAX));

    ensure(baselineEnd - baselineStart <= BASELINE_RANGE_MAX,
        String.format("Baseline range cannot be longer than %d", BASELINE_RANGE_MAX));

    urns = parseListParams(urns);

    // validate window size
    long anomalyWindow = anomalyEnd - anomalyStart;
    long baselineWindow = baselineEnd - baselineStart;
    ensure(anomalyWindow == baselineWindow,
        "Must provide equal-sized anomaly and baseline periods");

    // format inputs
    Set<Entity> inputs = new HashSet<>(Arrays.asList(
        TimeRangeEntity.fromRange(1.0, TimeRangeEntity.TYPE_ANOMALY, anomalyStart, anomalyEnd),
        TimeRangeEntity.fromRange(0.8, TimeRangeEntity.TYPE_BASELINE, baselineStart, baselineEnd),
        TimeRangeEntity.fromRange(1.0, TimeRangeEntity.TYPE_ANALYSIS, analysisStart, analysisEnd)
    ));

    for (String urn : urns) {
      inputs.add(EntityUtils.parseURN(urn, 1.0));
    }

    // run root-cause analysis
    RCAFrameworkExecutionResult result = frameworks.get(framework).run(inputs);

    // apply formatters
    return applyFormatters(result.getResultsSorted(), formatterDepth);
  }

  @GET
  @Path("/raw")
  @ApiOperation(value = "Raw")
  public List<RootCauseEntity> raw(
      @QueryParam("framework") String framework,
      @QueryParam("formatterDepth") Integer formatterDepth,
      @QueryParam("urns") List<String> urns) throws Exception {

    // configuration validation
    ensure(frameworks.containsKey(framework),
        String.format("Could not resolve framework '%s'. Allowed values: %s",
            framework,
            frameworks.keySet()));

    if (formatterDepth == null) {
      formatterDepth = DEFAULT_FORMATTER_DEPTH;
    }

    // parse urns arg
    urns = parseListParams(urns);

    // format input
    Set<Entity> input = new HashSet<>();
    for (String urn : urns) {
      input.add(EntityUtils.parseURNRaw(urn, 1.0));
    }

    // run root-cause analysis
    RCAFrameworkExecutionResult result = this.frameworks.get(framework).run(input);

    // apply formatters
    return applyFormatters(result.getResultsSorted(), formatterDepth);
  }

  private List<RootCauseEntity> applyFormatters(Iterable<Entity> entities, int maxDepth) {
    List<RootCauseEntity> output = new ArrayList<>();
    for (Entity e : entities) {
      output.add(this.applyFormatters(e, maxDepth));
    }
    return output;
  }

  private RootCauseEntity applyFormatters(Entity e, int remainingDepth) {
    for (RootCauseEntityFormatter formatter : this.formatters) {
      if (formatter.applies(e)) {
        try {
          RootCauseEntity rce = formatter.format(e);

          if (remainingDepth > 1) {
            for (Entity re : e.getRelated()) {
              rce.addRelatedEntity(this.applyFormatters(re, remainingDepth - 1));
            }
          } else {
            // clear out any related entities added by the formatter by default
            rce.setRelatedEntities(Collections.emptyList());
          }

          return rce;
        } catch (Exception ex) {
          LOG.warn("Error applying formatter '{}'. Skipping.", formatter.getClass().getName(), ex);
        }
      }
    }
    throw new IllegalArgumentException(String.format("No formatter for Entity '%s'", e.getUrn()));
  }
}