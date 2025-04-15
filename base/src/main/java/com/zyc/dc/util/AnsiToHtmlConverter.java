package com.zyc.dc.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnsiToHtmlConverter {

    // 更新正则表达式，匹配 ANSI 转义码
    private static final Pattern ANSI_COLOR_PATTERN = Pattern.compile("\033\\[([0-9;]*)m");

    // 将 ANSI 字符串转换为 HTML，每行作为一个 string 返回
    public static String[] convertToHtml(String ansiString) {
        if (ansiString == null || ansiString.isEmpty()) return new String[0];
        // 拆分多行字符串
        String[] lines = ansiString.split("\n");
        String[] htmlLines = new String[lines.length];

        // 遍历每一行并转换为 HTML
        for (int i = 0; i < lines.length; i++) {
        	htmlLines[i] = convertLineToHtml(lines[i]);
        }

        return htmlLines;
    }

    // 将每行 ANSI 字符串转换为 HTML
    private static String convertLineToHtml(String line) {
        StringBuilder lineHtml = new StringBuilder();
        Matcher matcher = ANSI_COLOR_PATTERN.matcher(line);
        int lastEnd = 0;
        boolean inSpan = false; // 标记是否处于 <span> 标签内
        // 遍历每一行中的 ANSI 转义码
        while (matcher.find()) {
            // 获取颜色和样式码，例如 "0;32" 或 "1;33"
            String colorCode = matcher.group(1);

            // 获取对应的 CSS 样式
            String cssStyle = getCssForAnsiCode(colorCode);

            // 将之前的非颜色部分添加到 HTML 字符串中
            lineHtml.append(line, lastEnd, matcher.start());

            // 如果没有在 <span> 标签内，则需要添加开头的 <span> 标签
            if (!inSpan) {
                lineHtml.append("<span style=\"").append(cssStyle).append("\">");
                inSpan = true; // 进入 <span> 标签内
            }

            // 继续匹配下一个内容
            lastEnd = matcher.end();
        }

        // 将剩余的非颜色部分添加到 HTML 字符串中
        lineHtml.append(line.substring(lastEnd));

        // 如果之前有匹配的转义码，需要关闭 <span> 标签
        if (inSpan) {
            lineHtml.append("</span>");
        }

        return lineHtml.toString();
    }

    // 根据 ANSI 颜色码返回相应的 CSS 样式
    private static String getCssForAnsiCode(String colorCode) {
        StringBuilder cssStyle = new StringBuilder();

        // 分割多个样式和颜色
        String[] codes = colorCode.split(";");

        for (String code : codes) {
            switch (code) {
                case "0":
                    break; // 无样式
                case "1":
                    cssStyle.append("font-weight: bold;");  // 加粗
                    break;
                case "2":
                    cssStyle.append("opacity: 0.7;");  // 暗色（变浅）
                    break;
                case "4":
                    cssStyle.append("text-decoration: underline;");  // 下划线
                    break;
                case "7":
                    cssStyle.append("color: white; background-color: black;");  // 反转颜色
                    break;
                case "30":
                    cssStyle.append("color: black;");  // 黑色前景
                    break;
                case "31":
                    cssStyle.append("color: red;");  // 红色前景
                    break;
                case "32":
                    cssStyle.append("color: green;");  // 绿色前景
                    break;
                case "33":
                    cssStyle.append("color: #DAA520;");  // 黄色前景
                    break;
                case "34":
                    cssStyle.append("color: blue;");  // 蓝色前景
                    break;
                case "35":
                    cssStyle.append("color: magenta;");  // 品红色前景
                    break;
                case "36":
                    cssStyle.append("color: cyan;");  // 青色前景
                    break;
                case "37":
                    cssStyle.append("color: white;");  // 白色前景
                    break;
                case "40":
                    cssStyle.append("background-color: black;");  // 黑色背景
                    break;
                case "41":
                    cssStyle.append("background-color: red;");  // 红色背景
                    break;
                case "42":
                    cssStyle.append("background-color: green;");  // 绿色背景
                    break;
                case "43":
                    cssStyle.append("background-color: yellow;");  // 黄色背景
                    break;
                case "44":
                    cssStyle.append("background-color: blue;");  // 蓝色背景
                    break;
                case "45":
                    cssStyle.append("background-color: magenta;");  // 品红色背景
                    break;
                case "46":
                    cssStyle.append("background-color: cyan;");  // 青色背景
                    break;
                case "47":
                    cssStyle.append("background-color: white;");  // 白色背景
                    break;
                default:
                    break;  // 其他的代码可以忽略
            }
        }

        return cssStyle.toString();
    }
}
