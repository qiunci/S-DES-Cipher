package sdes;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.Socket;
import java.util.List;

/**
 * S-DES GUI 主界面。
 */
public class SDESGuiApp extends JFrame {

    // ==================== 配色（奶白 + 复古绿，低饱和柔和） ====================
    private static final Color CREAM_BG       = new Color(0xFAF7F0);
    private static final Color CREAM_PANEL    = new Color(0xF3EEE3);
    private static final Color CREAM_CARD     = new Color(0xFFFCF6);
    private static final Color RETRO_GREEN    = new Color(0x5B7A5B);
    private static final Color RETRO_GREEN_DK = new Color(0x445C44);
    private static final Color RETRO_GREEN_LT = new Color(0xA8BCA8);
    private static final Color SOFT_LINE      = new Color(0xD8D2C4);
    private static final Color SOFT_GOLD      = new Color(0xD4BE8A);
    private static final Color TEXT_MAIN      = new Color(0x333333);
    private static final Color TEXT_SUB       = new Color(0x7A7A7A);
    private static final Color LOG_BG         = new Color(0x2F3D2F);
    private static final Color LOG_FG         = new Color(0xDCE4D4);

    // ==================== 字体（分层级，优雅衬线 + 现代无衬线 + 等宽） ====================
    private static final Font FONT_APP_TITLE  = FontProvider.appTitle();
    private static final Font FONT_SUBTITLE   = FontProvider.appSubtitle();
    private static final Font FONT_SECTION    = FontProvider.sectionTitle();
    private static final Font FONT_LABEL      = FontProvider.label();
    private static final Font FONT_BODY       = FontProvider.body();
    private static final Font FONT_BUTTON     = FontProvider.button();
    private static final Font FONT_INPUT      = FontProvider.input();
    private static final Font FONT_LOG        = FontProvider.log();
    private static final Font FONT_RESULT     = FontProvider.resultMono();
    private static final Font FONT_HINT       = FontProvider.hint();
    private static final Font FONT_TAB        = FontProvider.tab();
    private static final Font FONT_STATUS     = FontProvider.status();

    // ==================== 组件 ====================
    private final JTextField keyField = new JTextField("1010000010", 12);
    private final JTextField plainTextField = new JTextField(24);
    private final JTextField cipherTextField = new JTextField(24);

    private final JTextField bfPlainField = new JTextField(10);
    private final JTextField bfCipherField = new JTextField(10);
    private final JTextArea bfResultArea = new JTextArea(6, 50);

    private final JTextField hostField = new JTextField("127.0.0.1", 10);
    private final JTextField portField = new JTextField("8888", 6);
    private final JTextField networkMessageField = new JTextField(24);

    private final JTextArea logArea = new JTextArea(20, 70);

    private JTabbedPane tabbedPane;

    public SDESGuiApp() {
        super("S-DES 加解密工具");
        initUi();
    }

    // ============================================================
    //                        UI 构建
    // ============================================================
    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(CREAM_BG);
        setLayout(new BorderLayout());

        add(buildHeaderPanel(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        styleTabbedPane(tabbedPane);
        tabbedPane.addTab("  加解密  ", buildCipherTab());
        tabbedPane.addTab("  暴力破解  ", buildBruteForceTab());
        tabbedPane.addTab("  网络通信  ", buildNetworkTab());
        tabbedPane.addTab("  运算日志  ", buildLogTab());
        add(tabbedPane, BorderLayout.CENTER);

        add(buildStatusBar(), BorderLayout.SOUTH);

        setSize(1000, 740);
        setMinimumSize(new Dimension(860, 620));
        setLocationRelativeTo(null);
    }

    /** 顶部标题栏 */
    private JPanel buildHeaderPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CREAM_BG);

        // ★ 定义 titleRow（这一步不能少）
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(CREAM_BG);
        titleRow.setBorder(new EmptyBorder(18, 26, 14, 26));

        // ---- 左侧：主标题 + 副标题 ----
        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setBackground(CREAM_BG);

        JLabel title = new JLabel("S-DES  加解密工具");
        title.setFont(FONT_APP_TITLE);
        title.setForeground(RETRO_GREEN_DK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBox.add(title);

        JLabel subtitle = new JLabel("Simplified  Data  Encryption  Standard");
        subtitle.setFont(FONT_SUBTITLE);
        subtitle.setForeground(RETRO_GREEN_LT);
        subtitle.setBorder(new EmptyBorder(2, 2, 0, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBox.add(subtitle);

        titleRow.add(titleBox, BorderLayout.WEST);   // ← 用到了 titleRow

        // ---- 右侧：密钥输入 ----
        JPanel keyRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        keyRow.setBackground(CREAM_BG);
        JLabel keyLbl = new JLabel("密钥 K (10 bit)");
        keyLbl.setFont(FONT_LABEL);
        keyLbl.setForeground(TEXT_MAIN);
        keyRow.add(keyLbl);

        styleTextField(keyField);
        keyField.setPreferredSize(new Dimension(150, 32));
        keyRow.add(keyField);

        JLabel hint = new JLabel("0/1 共 10 位");
        hint.setFont(FONT_HINT);
        hint.setForeground(TEXT_SUB);
        keyRow.add(hint);

        titleRow.add(keyRow, BorderLayout.EAST);     // ← 又用到了 titleRow

        wrapper.add(titleRow, BorderLayout.NORTH);   // ← 还用到了 titleRow

        // ---- 底部：柔和金色分隔线 ----
        JPanel dividerWrap = new JPanel(new BorderLayout());
        dividerWrap.setBackground(CREAM_BG);
        dividerWrap.setBorder(new EmptyBorder(0, 26, 0, 26));
        JPanel divider = new JPanel();
        divider.setBackground(SOFT_GOLD);
        divider.setPreferredSize(new Dimension(0, 1));
        dividerWrap.add(divider, BorderLayout.CENTER);
        wrapper.add(dividerWrap, BorderLayout.SOUTH);

        return wrapper;
    }

    // ============================================================
    //                    ① 加解密 标签页
    // ============================================================
    private JPanel buildCipherTab() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(CREAM_BG);
        outer.setBorder(new EmptyBorder(26, 40, 26, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        outer.add(makeLabel("明文"), gbc);

        styleTextField(plainTextField);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        outer.add(plainTextField, gbc);

        JButton encBitBtn = makePrimaryButton("单字符加密");
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.weightx = 0;
        outer.add(encBitBtn, gbc);

        JButton decBitBtn = makeSecondaryButton("单字符解密");
        gbc.gridx = 4;
        outer.add(decBitBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        outer.add(makeLabel("密文"), gbc);

        styleTextField(cipherTextField);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        outer.add(cipherTextField, gbc);

        JButton encStrBtn = makePrimaryButton("字符串加密");
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.weightx = 0;
        outer.add(encStrBtn, gbc);

        JButton decStrBtn = makeSecondaryButton("字符串解密");
        gbc.gridx = 4;
        outer.add(decStrBtn, gbc);

        JLabel tip = new JLabel("<html><span style='color:#7A7A7A;font-family:Microsoft YaHei;'>"
                + "单字符需 8 bit 二进制；字符串按 ASCII 每字符 1 Byte 分组");
        tip.setFont(FONT_HINT);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 5;
        gbc.insets = new Insets(24, 12, 8, 12);
        outer.add(tip, gbc);

        encBitBtn.addActionListener(e -> onEncryptBit());
        decBitBtn.addActionListener(e -> onDecryptBit());
        encStrBtn.addActionListener(e -> onEncryptString());
        decStrBtn.addActionListener(e -> onDecryptString());

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(CREAM_BG);
        container.add(outer, BorderLayout.NORTH);
        return container;
    }

    // ============================================================
    //                    ② 暴力破解 标签页
    // ============================================================
    private JPanel buildBruteForceTab() {
        JPanel outer = new JPanel(new BorderLayout(12, 12));
        outer.setBackground(CREAM_BG);
        outer.setBorder(new EmptyBorder(26, 40, 26, 40));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(CREAM_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(makeLabel("已知明文 P"), gbc);
        gbc.gridx = 1;
        styleTextField(bfPlainField);
        inputPanel.add(bfPlainField, gbc);

        gbc.gridx = 2;
        inputPanel.add(makeLabel("目标密文 C"), gbc);
        gbc.gridx = 3;
        styleTextField(bfCipherField);
        inputPanel.add(bfCipherField, gbc);

        gbc.gridx = 4;
        JButton bfBtn = makePrimaryButton("开始破解");
        inputPanel.add(bfBtn, gbc);

        outer.add(inputPanel, BorderLayout.NORTH);

        bfResultArea.setEditable(false);
        bfResultArea.setFont(FONT_LOG);
        bfResultArea.setBackground(CREAM_CARD);
        bfResultArea.setForeground(TEXT_MAIN);
        bfResultArea.setBorder(new EmptyBorder(14, 16, 14, 16));

        // 用自定义圆角边框包裹
        RoundedBorder roundedBorder = new RoundedBorder(SOFT_LINE, 10);
        JPanel resultWrapper = new JPanel(new BorderLayout());
        resultWrapper.setBackground(CREAM_CARD);
        resultWrapper.setBorder(BorderFactory.createCompoundBorder(
                roundedBorder, new EmptyBorder(2, 2, 2, 2)));

        JLabel resultTitle = new JLabel("破解结果");
        resultTitle.setFont(FONT_SECTION);
        resultTitle.setForeground(RETRO_GREEN_DK);
        resultTitle.setBorder(new EmptyBorder(10, 14, 4, 14));

        resultWrapper.add(resultTitle, BorderLayout.NORTH);
        resultWrapper.add(bfResultArea, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(resultWrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        outer.add(scroll, BorderLayout.CENTER);

        JLabel tip = new JLabel("<html><span style='color:#7A7A7A;font-family:Microsoft YaHei;'>"
                + "穷举1024 个密钥，"
                + "找出使 encrypt(P,K)=C 的所有候选，并统计耗时。</span></html>");
        tip.setFont(FONT_HINT);
        tip.setBorder(new EmptyBorder(6, 4, 0, 4));
        outer.add(tip, BorderLayout.SOUTH);

        bfBtn.addActionListener(e -> onBruteForce());
        return outer;
    }

    // ============================================================
    //                    ③ 网络通信 标签页
    // ============================================================
    private JPanel buildNetworkTab() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(CREAM_BG);
        outer.setBorder(new EmptyBorder(26, 40, 26, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ---------- 第 0 行：IP + 端口 打包成一个 panel ----------
        JPanel ipPortRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        ipPortRow.setOpaque(false);
        ipPortRow.add(makeLabel("服务器 IP"));
        styleTextField(hostField);
        hostField.setPreferredSize(new Dimension(220, 35));
        ipPortRow.add(hostField);
        ipPortRow.add(makeLabel("端口"));
        styleTextField(portField);
        portField.setPreferredSize(new Dimension(100, 35));
        ipPortRow.add(portField);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        outer.add(ipPortRow, gbc);

        // ---------- 第 1 行：消息标签 | 消息框 | 发送按钮 ----------
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        outer.add(makeLabel("要发送的消息"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        styleTextField(networkMessageField);
        outer.add(networkMessageField, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JButton sendBtn = makePrimaryButton("加密并发送");
        outer.add(sendBtn, gbc);

        // ---------- 第 2 行：提示 ----------
        JLabel tip = new JLabel("<html><span style='color:#7A7A7A;font-family:Microsoft YaHei;'>"
                + "使用当前密钥对消息逐字符加密，通过 TCP 发送到目标地址；");
        tip.setFont(FONT_HINT);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(24, 10, 8, 10);
        outer.add(tip, gbc);

        sendBtn.addActionListener(e -> onSendToServer());

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(CREAM_BG);
        container.add(outer, BorderLayout.NORTH);
        return container;
    }

    // ============================================================
    //                    ④ 运算日志 标签页
    // ============================================================
    private JPanel buildLogTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(CREAM_BG);
        panel.setBorder(new EmptyBorder(18, 22, 18, 22));

        logArea.setEditable(false);
        logArea.setFont(FONT_LOG);
        logArea.setLineWrap(false);
        logArea.setBackground(LOG_BG);
        logArea.setForeground(LOG_FG);
        logArea.setCaretColor(SOFT_GOLD);
        logArea.setBorder(new EmptyBorder(14, 16, 14, 16));

        // ★ 禁用自动滚动：写日志时不跟随光标，性能大幅提升
        if (logArea.getCaret() instanceof javax.swing.text.DefaultCaret) {
            ((javax.swing.text.DefaultCaret) logArea.getCaret())
                    .setUpdatePolicy(javax.swing.text.DefaultCaret.NEVER_UPDATE);
        }

        // 圆角日志容器
        JPanel logWrapper = new JPanel(new BorderLayout());
        logWrapper.setBackground(LOG_BG);
        logWrapper.setBorder(new RoundedBorder(RETRO_GREEN_DK, 12));
        logWrapper.add(logArea, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(logWrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        toolbar.setBackground(CREAM_BG);

        JButton clearBtn = makeSecondaryButton("清空日志");
        clearBtn.addActionListener(e -> logArea.setText(""));
        toolbar.add(clearBtn);

        JButton copyBtn = makeSecondaryButton("复制全部");
        copyBtn.addActionListener(e -> {
            logArea.selectAll();
            logArea.copy();
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
        toolbar.add(copyBtn);

        panel.add(toolbar, BorderLayout.SOUTH);
        return panel;
    }

    /** 底部状态栏 */
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(CREAM_PANEL);
        bar.setBorder(new EmptyBorder(8, 22, 8, 22));

        // 上方一条浅金细线
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CREAM_BG);
        JPanel line = new JPanel();
        line.setBackground(SOFT_LINE);
        line.setPreferredSize(new Dimension(0, 1));
        wrapper.add(line, BorderLayout.NORTH);
        wrapper.add(bar, BorderLayout.CENTER);

        JLabel left = new JLabel("S-DES  ·  分组 8-bit  ·  密钥 10-bit");
        left.setFont(FONT_STATUS);
        left.setForeground(TEXT_SUB);
        bar.add(left, BorderLayout.WEST);

        JLabel right = new JLabel("奶白 · 复古绿");
        right.setFont(FONT_STATUS);
        right.setForeground(RETRO_GREEN);
        bar.add(right, BorderLayout.EAST);
        return wrapper;
    }

    // ============================================================
    //                    控件样式工具
    // ============================================================

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(TEXT_MAIN);
        return lbl;
    }

    private void styleTextField(JTextField field) {
        field.setFont(FONT_INPUT);
        field.setForeground(RETRO_GREEN_DK);
        field.setBackground(CREAM_CARD);
        field.setCaretColor(RETRO_GREEN);
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(SOFT_LINE, 10),
                new EmptyBorder(6, 12, 6, 12)));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 34));
    }

    /** 主按钮：复古绿实心 */
    private JButton makePrimaryButton(String text) {
        return makeButton(text, RETRO_GREEN, Color.WHITE, RETRO_GREEN_DK, false);
    }

    /** 次按钮：奶白底 + 绿色边框 */
    private JButton makeSecondaryButton(String text) {
        return makeButton(text, CREAM_CARD, RETRO_GREEN_DK, RETRO_GREEN_LT, true);
    }

    private JButton makeButton(String text, Color bg, Color fg, Color border, boolean outlined) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);          // 关闭默认填充，用 paintComponent 画圆角
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);

        final Color baseBg = bg;
        final Color hoverBg = outlined ? new Color(0xE8E2D3) : RETRO_GREEN_DK;
        final Color[] curBg = { baseBg };
        final Color baseBorder = outlined ? border : bg;
        final Color[] curBorder = { baseBorder };

        // 自定义圆角按钮绘制
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int w = c.getWidth(), h = c.getHeight();
                g2.setColor(curBg[0]);
                g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 14, 14));
                g2.setColor(curBorder[0]);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 14, 14));
                g2.dispose();
                super.paint(g, c);
            }
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                curBg[0] = hoverBg;
                curBorder[0] = outlined ? RETRO_GREEN : RETRO_GREEN_DK;
                btn.repaint();
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                curBg[0] = baseBg;
                curBorder[0] = baseBorder;
                btn.repaint();
            }
        });
        return btn;
    }

    /** 自定义 TabbedPane 样式（柔和圆角 + 复古绿选中） */
    private void styleTabbedPane(JTabbedPane tp) {
        tp.setFont(FONT_TAB);
        tp.setBackground(CREAM_BG);
        tp.setForeground(TEXT_MAIN);
        tp.setBorder(new EmptyBorder(4, 12, 8, 12));
        tp.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement,
                                              int tabIndex, int x, int y, int w, int h,
                                              boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected ? RETRO_GREEN : CREAM_PANEL);
                g2.fill(new RoundRectangle2D.Float(x + 2, y + 4, w - 4, h - 4, 16, 16));
                g2.dispose();
            }
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement,
                                          int tabIndex, int x, int y, int w, int h,
                                          boolean isSelected) {
                // 不画边框，仅靠背景色区分
            }
            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement,
                                               Rectangle[] rects, int tabIndex,
                                               Rectangle iconRect, Rectangle textRect,
                                               boolean isSelected) {
                // 不画焦点虚线
            }
            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // 去掉内容区默认边框
            }
            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font,
                                     FontMetrics metrics, int tabIndex, String title,
                                     Rectangle textRect, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(font);
                g2.setColor(isSelected ? Color.WHITE : TEXT_MAIN);
                FontMetrics fm = g2.getFontMetrics();
                int textX = textRect.x + (textRect.width - fm.stringWidth(title)) / 2;
                int textY = textRect.y + fm.getAscent()
                        + (textRect.height - fm.getHeight()) / 2;
                g2.drawString(title, textX, textY);
                g2.dispose();
            }
            @Override
            protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                return 36;
            }
            @Override
            protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                return 120;
            }
            @Override
            protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
                return maxTabHeight + 8;
            }
        });
    }

    // ============================================================
    //                     事件处理（逻辑不变）
    // ============================================================

    private String readKey() {
        String key = keyField.getText().trim();
        SDESCore.validateBinary(key, 10, "密钥");
        return key;
    }

    private void onEncryptBit() {
        try {
            String plain = plainTextField.getText().trim();
            String key = readKey();
            logHeader("单字符加密运算过程");
            String cipher = SDESCore.encryptWithLog(plain, key, buildLogListener());
            cipherTextField.setText(cipher);
            logFooter("加密结束，密文 = " + cipher);
            scrollLogToBottom();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void onDecryptBit() {
        try {
            String cipher = cipherTextField.getText().trim();
            String key = readKey();
            logHeader("单字符解密运算过程");
            String plain = SDESCore.decryptWithLog(cipher, key, buildLogListener());
            plainTextField.setText(plain);
            logFooter("解密结束，明文 = " + plain);
            scrollLogToBottom();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void onEncryptString() {
        try {
            String key = readKey();
            String message = plainTextField.getText();
            logHeader("字符串加密开始");
            String cipher = SDESCore.encryptStringWithLog(message, key, buildLogListener());
            cipherTextField.setText(cipher);
            logFooter("字符串加密结束，密文长度 = " + cipher.length());
            scrollLogToBottom();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void onDecryptString() {
        try {
            String key = readKey();
            String cipher = cipherTextField.getText();
            logHeader("字符串解密开始");
            String message = SDESCore.decryptStringWithLog(cipher, key, buildLogListener());
            plainTextField.setText(message);
            logFooter("字符串解密结束");
            scrollLogToBottom();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void onSendToServer() {
        try {
            String key = readKey();
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            String message = networkMessageField.getText();

            logHeader("TCP 加密发送");
            log("  服务器          : " + host + ":" + port);
            log("  消息            : " + message);
            String cipher = SDESCore.encryptString(message, key);
            log("  密文长度        : " + cipher.length());

            try (Socket socket = new Socket(host, port)) {
                // 1. 发送密文
                socket.getOutputStream().write(cipher.getBytes("ISO-8859-1"));
                socket.getOutputStream().flush();

                log("  明文消息        : " + message);
                log("  发送密文(Hex)   : " + toHex(cipher));
                log("  已发送至        : " + host + ":" + port);

                // 2. 读取服务端回执
                byte[] buf = new byte[4096];
                int n = socket.getInputStream().read(buf);
                if (n > 0) {
                    String reply = new String(buf, 0, n, "ISO-8859-1");   // 用 ISO-8859-1 保持字节
                    log("  收到回执长度    : " + reply.length() + " 字符");
                    log("  收到回执(Hex)   : " + toHex(reply));

                    // ★ 回执形如 "收到<密文>"，把"收到"前缀剥掉，剩下的就是原密文
                    String prefix = "收到";
                    if (reply.startsWith(prefix)) {
                        String echoedCipher = reply.substring(prefix.length());
                        log("  回执前缀        : \"" + prefix + "\"");
                        log("  回执携带密文    : " + echoedCipher);
                        log("  与我发送的密文一致? " + cipher.equals(echoedCipher));
                    } else {
                        log("  回执内容        : " + reply);
                    }
                } else {
                    log("  未收到回执");
                }
            }catch (Exception io) {
                log("  发送失败        : " + io.getMessage());
                logFooter("发送结束");
                showError("发送失败：" + io.getMessage());
            }
            scrollLogToBottom();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void onBruteForce() {
        try {
            String knownPlain = bfPlainField.getText().trim();
            String targetCipher = bfCipherField.getText().trim();
            SDESCore.validateBinary(knownPlain, 8, "已知明文");
            SDESCore.validateBinary(targetCipher, 8, "目标密文");

            logHeader("暴力破解开始");
            log("  已知明文 P      : " + knownPlain);
            log("  目标密文 C      : " + targetCipher);
            log("  穷举范围        : 0000000000 ~ 1111111111 (共 1024)");

            SDESCore.BruteForceResult result =
                    SDESCore.bruteForce(knownPlain, targetCipher, buildLogListener());

            log("  ──────────────────────────────");
            log("  穷举密钥总数    : " + result.totalTried);
            log(String.format("  总耗时          : %.3f ms", result.elapsedMillis()));
            log(String.format("  平均每密钥      : %.1f ns", result.averageNanosPerKey()));
            log("  候选密钥数      : " + result.candidates.size());
            List<String> cs = result.candidates;
            for (int i = 0; i < cs.size(); i++) {
                log("    候选 " + (i + 1) + "        : " + cs.get(i));
            }
            logFooter("暴力破解结束");

            StringBuilder sb = new StringBuilder();
            sb.append("穷举密钥总数 : ").append(result.totalTried).append(" 个\n");
            sb.append(String.format("总耗时       : %.3f ms%n", result.elapsedMillis()));
            sb.append(String.format("平均每密钥   : %.1f ns%n", result.averageNanosPerKey()));
            sb.append("候选密钥数   : ").append(cs.size()).append("\n");
            for (int i = 0; i < cs.size(); i++) {
                sb.append("  候选 ").append(i + 1).append(" : ").append(cs.get(i)).append("\n");
            }
            if (cs.size() == 1) {
                sb.append("✅ 唯一密钥：" + cs.get(0));
            } else if (cs.size() > 1) {
                sb.append("⚠ 存在多个候选，需用第二组 (P,C) 进一步筛选。");
            } else {
                sb.append("❌ 未找到匹配密钥（请检查输入）。");
            }
            bfResultArea.setText(sb.toString());
            scrollLogToBottom();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    // ============================================================
    //                     日志与对齐
    // ============================================================

    private void log(String message) {
        logArea.append(message + "\n");
        // 不再每行都 setCaretPosition，避免频繁滚动卡顿
    }

    /** 手动滚到底部（在每个操作结束后调用一次） */
    private void scrollLogToBottom() {
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void logHeader(String title) {
        log("");
        log("══════════════ " + title + " ══════════════");
    }

    private void logFooter(String title) {
        log("────────────── " + title + " ──────────────");
        log("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }

    private SDESCore.ProcessListener buildLogListener() {
        final String[] currentGroup = { null };
        final int FIELD_WIDTH = 18;
        return (groupLabel, fieldName, value) -> {
            if (groupLabel == null || groupLabel.isEmpty()) {
                log("  " + padRight(fieldName, FIELD_WIDTH) + ": " + value);
            } else {
                if (!groupLabel.equals(currentGroup[0])) {
                    currentGroup[0] = groupLabel;
                    log("── " + padRight(groupLabel, 10) + " ──────────────");
                }
                log("  " + padRight(fieldName, FIELD_WIDTH) + ": " + value);
            }
        };
    }

    private static int displayWidth(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            width += (c >= 0x2E80 && c <= 0x9FFF) || (c >= 0xFF00 && c <= 0xFF60) ? 2 : 1;
        }
        return width;
    }

    private static String padRight(String text, int targetWidth) {
        StringBuilder sb = new StringBuilder(text);
        int pad = targetWidth - displayWidth(text);
        for (int i = 0; i < pad; i++) sb.append(' ');
        return sb.toString();
    }

    /** 把字符串按字节转成十六进制，便于在日志里显示密文的真实字节 */
    private static String toHex(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            sb.append(String.format("%02X ", (int) s.charAt(i) & 0xFF));
        }
        return sb.toString().trim();
    }

    // ============================================================
    //                  自定义圆角边框类
    // ============================================================

    /**
     * 圆角边框：用 Graphics2D 画圆角矩形，替代 Swing 默认的直角 LineBorder。
     * 让输入框、卡片等控件呈现柔和边缘。
     */
    private static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        private final int thickness;

        RoundedBorder(Color color, int radius) {
            this(color, radius, 1);
        }
        RoundedBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f,
                    width - 1, height - 1, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness + 2, thickness + 4, thickness + 2, thickness + 4);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = thickness + 4;
            insets.top = insets.bottom = thickness + 2;
            return insets;
        }
    }

    // ============================================================

    public static void main(String[] args) {
        FontProvider.printDiagnostics();

        UIManager.put("Label.font",              FontProvider.label());
        UIManager.put("Button.font",             FontProvider.button());
        UIManager.put("TextField.font",          FontProvider.input());
        UIManager.put("TextArea.font",           FontProvider.log());
        UIManager.put("TabbedPane.font",         FontProvider.tab());
        UIManager.put("OptionPane.messageFont",  FontProvider.body());
        UIManager.put("OptionPane.buttonFont",   FontProvider.button());
        UIManager.put("TitledBorder.font",       FontProvider.sectionTitle());

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> new SDESGuiApp().setVisible(true));
    }
}