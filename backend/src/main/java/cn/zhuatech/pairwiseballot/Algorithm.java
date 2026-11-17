package cn.zhuatech.pairwiseballot;

import static cn.zhuatech.pairwiseballot.Checks.*;

import java.util.*;

/**
 * 还原随机左右展示顺序，逐票归因，计算候选模型去平局胜率及 Wilson 95% 区间。 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
final class Algorithm {
  /**
   * 接收已匿名化的双模型投票，不负责生成答案；每条任务一张独立票，不将平局计作负票。 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
   * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
   */
  static Map<String, Object> compute(Map<String, Object> cfg, List<Map<String, Object>> rows) {
    var base = calculate(cfg, rows);
    var enriched = new LinkedHashMap<String, Object>(base);
    enriched.put("inspection", Inspection.evaluate(cfg, rows, base));
    enriched.put("diagnostics", Diagnostics.evaluate(cfg, rows, base));
    return enriched;
  }

  /**
   * 保留原始计算口径。上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
   */
  private static Map<String, Object> calculate(
      Map<String, Object> cfg, List<Map<String, Object>> rows) {
    must(rows != null && !rows.isEmpty() && rows.size() <= 200, "需要 1–200 行");
    unique(rows);
    var evidence = new ArrayList<Map<String, Object>>();
    String c = str(cfg, "candidate", false), b = str(cfg, "baseline", false);
    must(!c.equals(b), "两个模型不能相同");
    int wins = 0, loss = 0, ties = 0;
    for (var r : rows) {
      String l = str(r, "left", false), rt = str(r, "right", false), vote = str(r, "pick", false);
      must(!l.equals(rt) && Set.of(c, b).containsAll(List.of(l, rt)), "展示模型与本次对照不符");
      must(Set.of("left", "right", "tie").contains(vote), "pick 应为 left/right/tie");
      String winner = vote.equals("left") ? l : vote.equals("right") ? rt : "";
      if (winner.isEmpty()) ties++;
      else if (winner.equals(c)) wins++;
      else loss++;
      evidence.add(Map.of("id", r.get("id"), "winner", winner, "vote", vote));
    }
    int n = wins + loss;
    var m = new LinkedHashMap<String, Object>();
    m.put("wins", wins);
    m.put("losses", loss);
    m.put("ties", ties);
    double p = n == 0 ? 0 : (double) wins / n,
        z = 1.95996398454,
        z2 = z * z,
        den = n + z2,
        center = (wins + z2 / 2) / den,
        delta = z * Math.sqrt(n * p * (1 - p) + z2 / 4) / den;
    m.put("winRate", n == 0 ? null : p);
    m.put("lower95", n == 0 ? null : Math.max(0, center - delta));
    m.put("upper95", n == 0 ? null : Math.min(1, center + delta));
    return result(m, evidence);
  }
}
