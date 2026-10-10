package sdes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

/**
 * S-DES 启动器：选择启动客户端或服务端。
 * 可重复点击、多窗口并存；关闭单个子窗口不影响其他窗口。
 */
public class Launcher {

    // ========== 配色方案：简约奶白 + 复古绿 ==========
    static final Color BG_MAIN      = new Color(0xFAF7F0); // 主背景 奶白
    static final Color GREEN_MAIN   = new Color(0x4A6B4A); // 主色 复古绿
    static final Color GREEN_DARK   = new Color(0x3A5639); // 深绿 悬停
    static final Color GREEN_LIGHT  = new Color(0x8FA98F); // 浅绿 高亮
    static final Color GOLD         = new Color(0xC9A961); // 强调金黄
    static final Color TEXT_SUB     = new Color(0x6B6B6B); // 次文字

    public static void main(String[] args) {
        // 全局字体抗锯齿 + 统一 UI 风格
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(Launcher::createAndShow);
    }

    private static void createAndShow() {
        JFrame frame = new JFrame("S-DES 启动器");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 关选择器 = 全部结束

        // 设置窗口图标
        setFrameIcon(frame);
        frame.setSize(400, 320);

        frame.setSize(400, 320);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        // 主容器
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_MAIN);
        root.setBorder(new EmptyBorder(24, 28, 20, 28));

        // ---------- 顶部标题区 ----------
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("S-DES");
        title.setFont(FontProvider.appTitle());
        title.setForeground(GREEN_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("简化数据加密标准 · 加解密与破解工具");
        subtitle.setFont(FontProvider.appSubtitle());
        subtitle.setForeground(TEXT_SUB);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        // ---------- 金色分隔线 ----------
        JSeparator sep = new JSeparator();
        sep.setForeground(GOLD);
        sep.setBackground(BG_MAIN);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // ---------- 中部按钮区 ----------
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(22, 0, 18, 0));

        JButton clientBtn = createStyledButton("启动客户端  ( 加密/解密 )");
        clientBtn.addActionListener(e -> launchChild(SDESGuiApp::launchNewInstance));

        JButton serverBtn = createStyledButton("启动服务端  ( TCP传输 )");
        serverBtn.addActionListener(e -> launchChild(SDESServerGui::launchNewInstance));

        center.add(clientBtn);
        center.add(Box.createVerticalStrut(14));
        center.add(serverBtn);

        // ---------- 底部提示 ----------
        JLabel hint = new JLabel("可重复点击，多个窗口可同时运行", SwingConstants.CENTER);
        hint.setFont(FontProvider.hint());
        hint.setForeground(TEXT_SUB);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---------- 组装 ----------
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.add(header);
        top.add(Box.createVerticalStrut(14));
        top.add(sep);

        root.add(top, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(hint, BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.setVisible(true);
    }

    /** 统一设置窗口图标，读不到就忽略 */
    private static void setFrameIcon(JFrame frame) {
        try {
            Image icon = javax.imageio.ImageIO.read(
                    Objects.requireNonNull(Launcher.class.getResourceAsStream("/icon.png")));
            if (icon != null) frame.setIconImage(icon);
        } catch (Exception ignored) { }
    }

    /** 生成一个带圆角、悬停变色、复古绿风格的按钮 */
    private static JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isRollover() ? GREEN_DARK : GREEN_MAIN;
                if (!isEnabled()) fill = GREEN_LIGHT;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FontProvider.button());
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(300, 44));

        // 悬停时重绘以更新颜色
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.repaint(); }
        });
        return btn;
    }

    /** 在新线程启动子程序，互不阻塞；线程随窗口销毁自然结束 */
    private static void launchChild(Runnable launcher) {
        Thread t = new Thread(launcher, "sdes-child");
        t.setDaemon(true);   // 守护线程：不阻止 JVM 退出
        t.start();
    }
}