package sdes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * S-DES 服务端 GUI。
 * 风格与 SDESGuiApp 一致；字体全部走 FontProvider，避免乱码。
 */
public class SDESServerGui extends JFrame {

    // ==================== 配色（与 SDESGuiApp 完全一致） ====================
    private static final Color CREAM_BG       = new Color(0xFAF7F0);
    private static final Color CREAM_CARD     = new Color(0xFFFCF6);
    private static final Color RETRO_GREEN    = new Color(0x5B7A5B);
    private static final Color RETRO_GREEN_DK = new Color(0x445C44);
    private static final Color RETRO_GREEN_LT = new Color(0xA8BCA8);
    private static final Color SOFT_LINE      = new Color(0xD8D2C4);
    private static final Color SOFT_GOLD      = new Color(0xD4BE8A);
    private static final Color TEXT_MAIN      = new Color(0x333333);
    private static final Color LOG_BG         = new Color(0x2F3D2F);
    private static final Color LOG_FG         = new Color(0xDCE4D4);

    // ==================== 字体（全部走 FontProvider，防乱码） ====================
    private static final Font FONT_APP_TITLE = FontProvider.appTitle();
    private static final Font FONT_SUBTITLE  = FontProvider.appSubtitle();
    private static final Font FONT_SECTION   = FontProvider.sectionTitle();
    private static final Font FONT_LABEL     = FontProvider.label();
    private static final Font FONT_BUTTON    = FontProvider.button();
    private static final Font FONT_INPUT     = FontProvider.input();
    private static final Font FONT_LOG       = FontProvider.log();
    private static final Font FONT_HINT      = FontProvider.hint();
    private static final Font FONT_STATUS    = FontProvider.status();

    // ==================== 业务常量 ====================
    private static final String SHARED_KEY = "1010000010";
    private static final int LISTEN_PORT = 8888;

    // ==================== 组件 ====================
    private final JTextArea logArea = new JTextArea(16, 60);
    private final JTextField targetHostField = new JTextField("127.0.0.1", 12);
    private final JTextField targetPortField = new JTextField("9999", 6);
    private final JTextField messageField = new JTextField(20);
    private final JTextField keyField = new JTextField(SHARED_KEY, 12);
    private final JLabel statusLabel = new JLabel("正在启动监听...");

    public SDESServerGui() {
        super("S-DES 服务端");

        // 设置窗口图标
        try {
            Image icon = javax.imageio.ImageIO.read(
                    Objects.requireNonNull(SDESServerGui.class.getResourceAsStream("/icon.png")));
            if (icon != null) setIconImage(icon);
        } catch (Exception ignored) { }

        initUi();
        startListenThread();
    }

    // ============================================================
    //                        UI 构建
    // ============================================================
    private void initUi() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(CREAM_BG);
        setLayout(new BorderLayout());

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildSendPanel(), BorderLayout.SOUTH);

        setSize(900, 640);
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(null);
    }

    /** 顶部标题栏 */
    private JPanel buildHeaderPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CREAM_BG);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(CREAM_BG);
        titleRow.setBorder(new EmptyBorder(18, 26, 14, 26));

        // 左侧标题
        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setBackground(CREAM_BG);

        JLabel title = new JLabel("S-DES  服务端");
        title.setFont(FONT_APP_TITLE);
        title.setForeground(RETRO_GREEN_DK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBox.add(title);

        JLabel subtitle = new JLabel("监听端口 " + LISTEN_PORT + "   ·   共享密钥 " + SHARED_KEY);
        subtitle.setFont(FONT_SUBTITLE);
        subtitle.setForeground(RETRO_GREEN_LT);
        subtitle.setBorder(new EmptyBorder(2, 2, 0, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBox.add(subtitle);

        titleRow.add(titleBox, BorderLayout.WEST);

        // 右侧状态
        statusLabel.setFont(FONT_STATUS);
        statusLabel.setForeground(RETRO_GREEN);
        titleRow.add(statusLabel, BorderLayout.EAST);

        wrapper.add(titleRow, BorderLayout.NORTH);

        // 金色分隔线
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

    /** 中部：日志区 */
    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CREAM_BG);
        panel.setBorder(new EmptyBorder(16, 26, 8, 26));

        JLabel sectionTitle = new JLabel("接收日志");
        sectionTitle.setFont(FONT_SECTION);
        sectionTitle.setForeground(RETRO_GREEN_DK);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        panel.add(sectionTitle, BorderLayout.NORTH);

        logArea.setEditable(false);
        logArea.setFont(FONT_LOG);
        logArea.setBackground(LOG_BG);
        logArea.setForeground(LOG_FG);
        logArea.setCaretColor(LOG_FG);
        logArea.setLineWrap(false);
        logArea.setBorder(new EmptyBorder(10, 12, 10, 12));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createLineBorder(SOFT_LINE));
        logScroll.getViewport().setBackground(LOG_BG);
        panel.add(logScroll, BorderLayout.CENTER);

        return panel;
    }

    /** 底部：主动发送区 */
    private JPanel buildSendPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CREAM_BG);
        wrapper.setBorder(new EmptyBorder(8, 26, 20, 26));

        JLabel sectionTitle = new JLabel("主动发送");
        sectionTitle.setFont(FONT_SECTION);
        sectionTitle.setForeground(RETRO_GREEN_DK);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        wrapper.add(sectionTitle, BorderLayout.NORTH);

        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(CREAM_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 18);   // 组与组之间留 18px，组内靠 FlowLayout 控制
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        // ============ 第一行：对方 IP | 端口 | 密钥 ============
        gbc.gridy = 0;

        gbc.gridx = 0; gbc.weightx = 0;
        row.add(makeFieldGroup("对方 IP", targetHostField, 140), gbc);

        gbc.gridx = 1; gbc.weightx = 0;
        row.add(makeFieldGroup("端口", targetPortField, 80), gbc);

        gbc.gridx = 2; gbc.weightx = 1.0;
        row.add(makeFieldGroup("密钥", keyField, 130), gbc);

        // ============ 第二行：消息 + 发送按钮 ============
        gbc.gridy = 1;

        gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        row.add(makeMessageGroup(messageField), gbc);

        gbc.gridx = 2; gbc.gridwidth = 1; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JButton sendBtn = makePrimaryButton();
        sendBtn.addActionListener(e -> onSend());
        row.add(sendBtn, gbc);

        wrapper.add(row, BorderLayout.CENTER);

        JLabel tip = new JLabel("<html><span style='color:#7A7A7A;'>"
                + "使用共享密钥对消息逐字符加密，通过 TCP 发送到目标地址；");
        tip.setFont(FONT_HINT);
        tip.setBorder(new EmptyBorder(10, 6, 0, 0));
        wrapper.add(tip, BorderLayout.SOUTH);

        return wrapper;
    }

    /** 把"标签 + 固定宽输入框"打包成一组，间距紧凑 */
    private JPanel makeFieldGroup(String labelText, JTextField field, int fieldWidth) {
        JPanel group = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); // hgap=5
        group.setOpaque(false);
        group.add(makeLabel(labelText));
        styleTextField(field);
        field.setPreferredSize(new Dimension(fieldWidth, 32));
        group.add(field);
        return group;
    }

    /** 把"标签 + 可拉伸输入框"打包成一组（用于消息行） */
    private JPanel makeMessageGroup(JTextField field) {
        JPanel group = new JPanel(new BorderLayout(5, 0)); // 水平间距 5
        group.setOpaque(false);
        group.add(makeLabel("消息"), BorderLayout.WEST);
        styleTextField(field);
        group.add(field, BorderLayout.CENTER);
        return group;
    }
    // ============================================================
    //                        样式工具
    // ============================================================

    /**
     * 统一输入框样式。
     * 关键：上下 EmptyBorder 只用 2px，配合 32px 高度，
     * 保证 14px 等宽字体行高（约 18px）完整显示，文字不会被挤。
     */
    private void styleTextField(JTextField tf) {
        tf.setFont(FONT_INPUT);                 // ← 走 FontProvider，防乱码
        tf.setForeground(TEXT_MAIN);
        tf.setBackground(CREAM_CARD);
        tf.setCaretColor(RETRO_GREEN_DK);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SOFT_LINE),
                new EmptyBorder(2, 8, 2, 8)));  // 上下 2px
        tf.setHorizontalAlignment(JTextField.LEFT);
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT_MAIN);
        return label;
    }

    private JButton makePrimaryButton() {
        JButton btn = new JButton("加密并发送") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? RETRO_GREEN_DK : RETRO_GREEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 32));
        return btn;
    }

    // ============================================================
    //                        业务逻辑
    // ============================================================

    private void startListenThread() {
        Thread listener = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(LISTEN_PORT)) {
                log("服务端启动，监听端口 " + LISTEN_PORT + " ...");
                SwingUtilities.invokeLater(() ->
                        statusLabel.setText("监听中 · 端口 " + LISTEN_PORT));
                while (true) {
                    try (Socket socket = serverSocket.accept()) {
                        // ★ 信息来源：客户端 IP + 端口
                        String clientAddress = socket.getInetAddress().getHostAddress();
                        int clientPort = socket.getPort();
                        String clientInfo = clientAddress + ":" + clientPort;

                        log("");
                        log("══════════════ 收到连接 ══════════════");
                        log("  信息来源        : " + clientInfo);
                        log("  连接时间        : " + java.time.LocalTime.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));

                        // 读取密文
                        byte[] cipherBytes = readAll(socket.getInputStream());
                        String cipherText = new String(cipherBytes, StandardCharsets.ISO_8859_1);

                        log("  收到密文长度    : " + cipherText.length() + " 字符");
                        log("  收到密文(Hex)   : " + toHex(cipherText));

                        // 解密并显示过程 + 明文
                        String plainText = SDESCore.decryptStringWithLog(
                                cipherText, SHARED_KEY, buildLogListener());
                        log("  解密明文        : \"" + plainText + "\"");
                        log("────────────── 回执发送 ──────────────");

                        // ★ 回执内容改为："收到" + 原密文
                        String reply = "收到" + cipherText;
                        OutputStream out = socket.getOutputStream();
                        out.write(reply.getBytes(StandardCharsets.ISO_8859_1));   // 用 ISO-8859-1，保证密文字节不被 UTF-8 改变
                        out.flush();

                        log("  已回执给        : " + clientInfo);
                        log("══════════════ 处理结束 ══════════════");
                    } catch (Exception ex) {
                        log("处理连接出错：" + ex.getMessage());
                    }
                }
            } catch (Exception ex) {
                log("监听启动失败：" + ex.getMessage());
                SwingUtilities.invokeLater(() ->
                        statusLabel.setText("监听失败：" + ex.getMessage()));
            }
        }, "sdes-listen-thread");
        listener.setDaemon(true);
        listener.start();
    }

    private void onSend() {
        try {
            String key = keyField.getText().trim();
            SDESCore.validateBinary(key, 10, "密钥");
            String host = targetHostField.getText().trim();
            int port = Integer.parseInt(targetPortField.getText().trim());
            String message = messageField.getText();

            log("========== 准备主动发送 ==========");
            String cipher = SDESCore.encryptStringWithLog(message, key, buildLogListener());
            try (Socket socket = new Socket(host, port)) {
                socket.getOutputStream().write(cipher.getBytes(StandardCharsets.ISO_8859_1));
                socket.getOutputStream().flush();
                log("已发送密文到 " + host + ":" + port);

                byte[] buf = new byte[1024];
                int n = socket.getInputStream().read(buf);
                if (n > 0) {
                    log("对方回执：" + new String(buf, 0, n, StandardCharsets.UTF_8));
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
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
                    log("── " + padRight(groupLabel, 10) + " ────────");
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
        sb.repeat(" ", Math.max(0, pad));
        return sb.toString();
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    /** 把字符串按字节转成十六进制，便于在日志里显示密文的真实字节 */
    private static String toHex(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            sb.append(String.format("%02X ", (int) s.charAt(i) & 0xFF));
        }
        return sb.toString().trim();
    }

    private static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        int n;
        while ((n = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, n);
            if (buffer.size() > 0 && in.available() == 0) break;
        }
        return buffer.toByteArray();
    }

    /** 供 Launcher 调用：每次创建一个全新实例 */
    public static void launchNewInstance() {
        SwingUtilities.invokeLater(() -> {
            SDESServerGui app = new SDESServerGui();
            app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 关键！
            app.setVisible(true);
        });
    }

    public static void main(String[] args) {
        // 可选：启动时打印字体探测结果，方便排查乱码
        FontProvider.printDiagnostics();
        launchNewInstance();
    }
}