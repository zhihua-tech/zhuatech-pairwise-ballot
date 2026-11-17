package cn.zhuatech.pairwiseballot;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.*;
import tools.jackson.databind.ObjectMapper;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@SuppressWarnings("unchecked")
class InspectionTest {
  final ObjectMapper json = new ObjectMapper();
  Map<String, Object> cfg;
  List<Map<String, Object>> data;

  @BeforeEach
  void load() throws Exception {
    var fixture = json.readValue(getClass().getResourceAsStream("/static/catalog.json"), Map.class);
    cfg = (Map) fixture.get("settings");
    data = (List) fixture.get("rows");
  }

  Map<String, Object> view(Map<String, Object> c, List<Map<String, Object>> r) {
    return (Map) Algorithm.compute(c, r).get("inspection");
  }

  Map<String, Object> summary(List<Map<String, Object>> r) {
    return (Map) view(cfg, r).get("metrics");
  }

  List<Map<String, Object>> detail(List<Map<String, Object>> r) {
    return (List) view(cfg, r).get("rows");
  }

  boolean matches(Map<?, ?> v, String fixture) {
    var actual = json.valueToTree(v);
    var expected = json.readTree(fixture);
    return expected.properties().stream()
        .allMatch(
            e -> {
              var a = actual.get(e.getKey());
              if (a == null) return false;
              return a.isNumber() && e.getValue().isNumber()
                  ? Math.abs(a.asDouble() - e.getValue().asDouble()) < 1e-9
                  : e.getValue().equals(a);
            });
  }

  @Test
  void exampleSummary() {
    assertTrue(matches(summary(data), "{\"positionGap\":null}"));
  }

  @Test
  void exampleEvidence() {
    assertTrue(
        detail(data).stream()
            .anyMatch(v -> matches(v, "{\"side\":\"right\",\"exposures\":1,\"winRate\":1}")));
  }

  @Test
  void operationalBoundary() {
    var a = new HashMap<>(data.getFirst());
    a.put("id", "extra");
    a.put("left", "B");
    a.put("right", "A");
    a.put("pick", "right");
    assertEquals(-1d, summary(List.of(data.getFirst(), a)).get("positionGap"));
  }
}
