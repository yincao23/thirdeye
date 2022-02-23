/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * RcaMetadataApi contains all necessary info to perform RCA.
 * It is put in the AlertTemplate. This is a solution to implement RCA quickly.
 */
@JsonInclude(Include.NON_NULL)
@Deprecated // use AlertMetadataApi
public class RcaMetadataApi {

  private String datasource;
  private String dataset;
  private String metric;
  /**
   * For instance: sum.
   */
  private String aggregationFunction;
  /**
   * For instance: P1D. Recommendation: same as in __timeGroup macro.
   */
  private String granularity;
  /**
   * Custom properties to add features quickly if need be.
   */
  private Map<String, Object> properties;

  @Deprecated
  public String getDatasource() {
    return datasource;
  }

  @Deprecated
  public RcaMetadataApi setDatasource(final String datasource) {
    this.datasource = datasource;
    return this;
  }

  @Deprecated
  public String getDataset() {
    return dataset;
  }

  @Deprecated
  public RcaMetadataApi setDataset(final String dataset) {
    this.dataset = dataset;
    return this;
  }

  @Deprecated
  public String getMetric() {
    return metric;
  }

  @Deprecated
  public RcaMetadataApi setMetric(final String metric) {
    this.metric = metric;
    return this;
  }

  @Deprecated
  public String getAggregationFunction() {
    return aggregationFunction;
  }

  @Deprecated
  public RcaMetadataApi setAggregationFunction(final String aggregationFunction) {
    this.aggregationFunction = aggregationFunction;
    return this;
  }

  @Deprecated
  public String getGranularity() {
    return granularity;
  }

  @Deprecated
  public RcaMetadataApi setGranularity(final String granularity) {
    this.granularity = granularity;
    return this;
  }

  @Deprecated
  public Map<String, Object> getProperties() {
    return properties;
  }

  @Deprecated
  public RcaMetadataApi setProperties(final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  @Override
  public String toString() {
    try {
      return new ObjectMapper().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      return this.toString();
    }
  }
}