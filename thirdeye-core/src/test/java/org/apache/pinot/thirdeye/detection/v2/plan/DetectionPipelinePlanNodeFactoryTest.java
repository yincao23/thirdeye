package org.apache.pinot.thirdeye.detection.v2.plan;

import static org.apache.pinot.thirdeye.detection.v2.plan.DetectionPipelinePlanNodeFactory.V2_DETECTION_PLAN_PACKAGE_NAME;

import java.lang.reflect.Modifier;
import java.util.Set;
import org.apache.pinot.thirdeye.detection.v2.PlanNode;
import org.reflections.Reflections;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DetectionPipelinePlanNodeFactoryTest {

  @Test
  public void testNoDuplicatedTypeKeyRegistration() {
    int numPlanNodes = DetectionPipelinePlanNodeFactory.getAllPlanNodes().size();
    Reflections reflections = new Reflections(V2_DETECTION_PLAN_PACKAGE_NAME);
    Set<Class<? extends PlanNode>> planNodeClasses = reflections.getSubTypesOf(PlanNode.class);
    int expectPlanNodeClassesNum = planNodeClasses.size();
    for (Class<? extends PlanNode> planNodeClass : planNodeClasses) {
      if (Modifier.isAbstract(planNodeClass.getModifiers())) {
        expectPlanNodeClassesNum--;
      }
    }
    Assert.assertEquals(numPlanNodes, expectPlanNodeClassesNum);
  }
}