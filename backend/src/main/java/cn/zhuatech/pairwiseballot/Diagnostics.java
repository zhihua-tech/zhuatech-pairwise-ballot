package cn.zhuatech.pairwiseballot;

import java.util.*;

/**
 * 在去除平局且任务相互独立的假设下，以胜负各半为原假设计算双侧精确二项 p 值；逐票剔除观察胜率变化。全平局时不输出 p 值，不将显著性解释为业务收益。 上海如静知华信息科技有限公司 ·
 * https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
final class Diagnostics {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  private static Map<String, Object> map(Object... pairs) {
    var m = new LinkedHashMap<String, Object>();
    for (int i = 0; i < pairs.length; i += 2) m.put((String) pairs[i], pairs[i + 1]);
    return m;
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  private static double num(Map<String, Object> m, String k) {
    return ((Number) m.get(k)).doubleValue();
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  private static Double ratio(double a, double b) {
    return b == 0 ? null : a / b;
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @SuppressWarnings("unchecked")
  static Map<String, Object> evaluate(
      Map<String, Object> cfg, List<Map<String, Object>> rows, Map<String, Object> base) {
    var metrics = (Map<String, Object>) base.get("metrics");
    var evidence = (List<Map<String, Object>>) base.get("rows");
    var details = new ArrayList<Map<String, Object>>();
    int w = ((Number) metrics.get("wins")).intValue(),
        l = ((Number) metrics.get("losses")).intValue(),
        n = w + l;
    Double p = null;
    if (n > 0) {
      double mass = Math.scalb(1.0, -n), tail = mass;
      for (int i = 1; i <= Math.min(w, l); i++) {
        mass *= ((double) (n - i + 1)) / i;
        tail += mass;
      }
      p = Math.min(1.0, 2 * tail);
    }
    int empty = 0;
    Double max = null;
    for (var r : evidence) {
      String winner = (String) r.get("winner");
      int remain = n - (winner.isEmpty() ? 0 : 1);
      Double rate =
          remain == 0
              ? null
              : (double) (w - (cfg.get("candidate").equals(winner) ? 1 : 0)) / remain;
      Double diff = rate == null ? null : rate - (double) w / n;
      if (diff == null) empty++;
      else max = max == null ? Math.abs(diff) : Math.max(max, Math.abs(diff));
      details.add(map("id", r.get("id"), "winner", winner, "rateWithout", rate, "rateShift", diff));
    }
    return Checks.result(
        map(
            "decisiveVotes",
            n,
            "tieFraction",
            (double) (rows.size() - n) / rows.size(),
            "exactP",
            p,
            "maxAbsInfluence",
            max,
            "unestimableAfterDrop",
            empty),
        details);
  }
}
