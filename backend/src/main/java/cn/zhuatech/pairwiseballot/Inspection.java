package cn.zhuatech.pairwiseballot;

import static cn.zhuatech.pairwiseballot.Checks.*;

import java.util.*;

/**
 * 对比候选模型在左侧、右侧的曝光数、有效票及胜率；两侧都有非平局票时才给出胜率差。位置差是观察值，不代表因果。 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/
 * · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
final class Inspection {
  /**
   * 有序结果，可表达无法估计的数值。上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
   * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
   */
  private static Map<String, Object> fields(Object... pairs) {
    var row = new LinkedHashMap<String, Object>();
    for (int i = 0; i < pairs.length; i += 2) row.put((String) pairs[i], pairs[i + 1]);
    return row;
  }

  /**
   * 对已校验的输入生成复核证据。上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
   * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
   */
  @SuppressWarnings("unchecked")
  static Map<String, Object> evaluate(
      Map<String, Object> cfg, List<Map<String, Object>> rows, Map<String, Object> base) {
    var details = new ArrayList<Map<String, Object>>();

    String candidate = (String) cfg.get("candidate");
    int[][] count = new int[2][3];
    for (var r : rows) {
      int side = candidate.equals(r.get("left")) ? 0 : 1;
      String v = (String) r.get("pick");
      int k = v.equals("tie") ? 2 : ((side == 0) == v.equals("left") ? 0 : 1);
      count[side][k]++;
    }
    Double[] rates = new Double[2];
    for (int i = 0; i < 2; i++) {
      int[] c = count[i];
      int n = c[0] + c[1];
      rates[i] = n == 0 ? null : (double) c[0] / n;
      details.add(
          fields(
              "side",
              i == 0 ? "left" : "right",
              "exposures",
              n + c[2],
              "decisive",
              n,
              "wins",
              c[0],
              "ties",
              c[2],
              "winRate",
              rates[i]));
    }
    return result(
        fields("positionGap", rates[0] == null || rates[1] == null ? null : rates[0] - rates[1]),
        details);
  }
}
