package sdes;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * S-DES TCP 服务端。
 * 接收客户端发来的密文，用固定密钥解密后输出。
 */
public class SDESServer {

    /** 与服务端共享的 10-bit 密钥 */
    private static final String SHARED_KEY = "1010000010";
    private static final int LISTEN_PORT = 8888;

    public static void main(String[] args) throws Exception {
        System.out.println("S-DES 服务端启动，监听端口 " + LISTEN_PORT);
        try (ServerSocket serverSocket = new ServerSocket(LISTEN_PORT)) {
            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    System.out.println("客户端已连接：" + socket.getInetAddress());
                    byte[] cipherBytes = readAll(socket.getInputStream());
                    String cipherText = new String(cipherBytes, "ISO-8859-1");
                    String plainText = SDESCore.decryptString(cipherText, SHARED_KEY);
                    System.out.println("密文长度：" + cipherText.length());
                    System.out.println("解密结果：" + plainText);
                }
            }
        }
    }

    /** 从输入流读取全部数据 */
    private static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        int n;
        while ((n = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, n);
        }
        return buffer.toByteArray();
    }
}