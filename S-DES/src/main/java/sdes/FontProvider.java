package sdes;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 字体提供者：探测系统中真正可用的中英文字体，
 * 并提供一套分层级的字体常量，用于界面美化。
 *
 * 字体性格：
 *   - TITLE 衬线（宋体系）：标题、状态栏
 *   - UI    无衬线（雅黑系）：标签、按钮、Tab
 *   - MONO  等宽（Monospaced）：输入框、日志
 *
 * 字号层级（从大到小）：
 *   24  应用主标题
 *   16  区块标题
 *   14  输入框内容
 *   13  字段标签、按钮、Tab、日志
 *   12  提示文字
 *   11  底部状态
 */
public final class FontProvider {

    private FontProvider() {}

    private static final Set<String> AVAILABLE = new HashSet<>();
    static {
        String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        AVAILABLE.addAll(Arrays.asList(names));
    }

    public static boolean has(String fontName) {
        return AVAILABLE.contains(fontName);
    }

    private static String firstAvailable(String fallback, String... candidates) {
        for (String name : candidates) {
            if (AVAILABLE.contains(name)) return name;
        }
        return fallback;
    }

    // ============ 三套字体族 ============

    /** 标题族：衬线，优雅 */
    private static final String TITLE_FAMILY = firstAvailable(
            "Serif",
            "Songti SC", "STSong",          // macOS
            "SimSun", "宋体",                // Windows
            "Noto Serif CJK SC",            // Linux
            "Source Han Serif SC"
    );

    /** 正文族：无衬线，现代 */
    private static final String UI_FAMILY = firstAvailable(
            "Dialog",
            "Microsoft YaHei UI",
            "Microsoft YaHei", "微软雅黑",
            "PingFang SC", "苹方-简",
            "Noto Sans CJK SC",
            "Source Han Sans SC",
            "WenQuanYi Micro Hei",
            "SimHei", "黑体"
    );

    /** 等宽族：日志与二进制位串，等宽更整齐 */
    private static final String MONO_FAMILY = firstAvailable(
            "Monospaced",
            "Sarasa Mono SC",               // 更纱黑体，中英等宽
            "Sarasa Term SC",
            "Microsoft YaHei Mono",
            "Noto Sans Mono CJK SC",
            "WenQuanYi Zen Hei Mono"
    );

    // ============ 基础生成器 ============
    public static Font title(int style, int size) { return new Font(TITLE_FAMILY, style, size); }
    public static Font ui(int style, int size)    { return new Font(UI_FAMILY, style, size); }
    public static Font mono(int style, int size)  { return new Font(MONO_FAMILY, style, size); }

    // ============ 界面专用字体（直接调用，无需记字号） ============

    /** 应用主标题：衬线 24 Bold */
    public static Font appTitle()      { return title(Font.BOLD, 24); }

    /** 副标题/装饰字：衬线 12 Italic */
    public static Font appSubtitle()   { return title(Font.ITALIC, 12); }

    /** 区块标题：雅黑 15 Bold */
    public static Font sectionTitle()  { return ui(Font.BOLD, 15); }

    /** 字段标签：雅黑 13 Bold */
    public static Font label()         { return ui(Font.BOLD, 13); }

    /** 普通正文：雅黑 13 Plain */
    public static Font body()          { return ui(Font.PLAIN, 13); }

    /** 按钮：雅黑 13 Bold */
    public static Font button()        { return ui(Font.BOLD, 13); }

    /** Tab 标签：雅黑 13 Bold */
    public static Font tab()           { return ui(Font.BOLD, 13); }

    /** 输入框内容：等宽 14 Plain */
    public static Font input()         { return mono(Font.PLAIN, 14); }

    /** 日志内容：等宽 13 Plain */
    public static Font log()           { return mono(Font.PLAIN, 13); }

    /** 破解结果：等宽 14 Plain（略大，便于阅读） */
    public static Font resultMono()    { return mono(Font.PLAIN, 14); }

    /** 提示文字：雅黑 12 Plain */
    public static Font hint()          { return ui(Font.PLAIN, 12); }

    /** 底部状态：衬线 11 Italic */
    public static Font status()        { return title(Font.ITALIC, 11); }

    /** 诊断输出 */
    public static void printDiagnostics() {
        System.out.println("[FontProvider] 标题族 = " + TITLE_FAMILY);
        System.out.println("[FontProvider] 正文族 = " + UI_FAMILY);
        System.out.println("[FontProvider] 等宽族 = " + MONO_FAMILY);
    }
}