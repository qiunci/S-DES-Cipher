package sdes;

/**
 * S-DES 算法核心实现类。
 * 分组长度 8 bit，密钥长度 10 bit。
 * 所有置换、S 盒、密钥扩展均在此类中以函数式方式组织。
 */
public final class SDESCore {

    // ============ 转换装置（常量表） ============
    /** P10 密钥置换表 */
    private static final int[] P10 = {3, 5, 2, 7, 4, 10, 1, 9, 8, 6};
    /** P8 密钥压缩置换表 */
    private static final int[] P8  = {6, 3, 7, 4, 8, 5, 10, 9};
    /** 初始置换 IP */
    private static final int[] IP  = {2, 6, 3, 1, 4, 8, 5, 7};
    /** 逆初始置换 IP^-1 */
    private static final int[] IP_INV = {4, 1, 3, 5, 7, 2, 8, 6};
    /** 扩展置换 EPBox */
    private static final int[] EP_BOX = {4, 1, 2, 3, 2, 3, 4, 1};
    /** SP 置换（S 盒后） */
    private static final int[] SP_BOX = {2, 4, 3, 1};

    /** S 盒 1 */
    private static final int[][] S_BOX_1 = {
            {1, 0, 3, 2},
            {3, 2, 1, 0},
            {0, 2, 1, 3},
            {3, 1, 0, 2}
    };
    /** S 盒 2 */
    private static final int[][] S_BOX_2 = {
            {0, 1, 2, 3},
            {2, 3, 1, 0},
            {3, 0, 1, 2},
            {2, 1, 0, 3}
    };

    // 私有构造，工具类不允许实例化
    private SDESCore() {}

    // ============ 通用置换函数 ============

    /**
     * 通用位置换函数。
     * @param input     输入的位串（如 "10101010"）
     * @param table     置换表（下标从 1 开始）
     * @return          置换后的位串
     */
    public static String permute(String input, int[] table) {
        StringBuilder output = new StringBuilder();
        for (int index : table) {
            output.append(input.charAt(index - 1));
        }
        return output.toString();
    }

    /**
     * 循环左移（对二进制串按位数左移，超出部分回卷）。
     * @param bits  输入位串
     * @param shift 左移位数
     * @return      左移后的位串
     */
    public static String leftShift(String bits, int shift) {
        int len = bits.length();
        int actualShift = shift % len;
        return bits.substring(actualShift) + bits.substring(0, actualShift);
    }

    // ============ 密钥扩展 ============

    /**
     * 密钥扩展：由 10-bit 主密钥生成两个 8-bit 子密钥 k1、k2。
     * k_i = P8(Shift^i(P10(K)))
     * @param key10 10 位二进制密钥
     * @return 长度为 2 的数组，[k1, k2]
     */
    public static String[] generateSubKeys(String key10) {
        if (key10.length() != 10) {
            throw new IllegalArgumentException("密钥长度必须为 10 bit");
        }
        // P10 置换
        String permutedKey = permute(key10, P10);
        String leftHalf  = permutedKey.substring(0, 5);
        String rightHalf = permutedKey.substring(5);

        String[] subKeys = new String[2];
        for (int round = 1; round <= 2; round++) {
            // 分别对左右两半做循环左移 i 位
            leftHalf  = leftShift(leftHalf, round);
            rightHalf = leftShift(rightHalf, round);
            // 合并后再做 P8 压缩，得到子密钥 k_i
            subKeys[round - 1] = permute(leftHalf + rightHalf, P8);
        }
        return subKeys;
    }

    // ============ S 盒查表 ============

    /**
     * 通过 S 盒查表。
     * @param bits   4 bit 输入
     * @param sBox   4x4 S 盒
     * @return       2 bit 输出
     */
    private static String sBoxLookup(String bits, int[][] sBox) {
        int row = Integer.parseInt("" + bits.charAt(0) + bits.charAt(3), 2);
        int col = Integer.parseInt("" + bits.charAt(1) + bits.charAt(2), 2);
        int value = sBox[row][col];
        return String.format("%2s", Integer.toBinaryString(value)).replace(' ', '0');
    }

    // ============ 轮函数 F ============

    /**
     * 轮函数 F(R, K) = SPBox( SBox( EPBox(R) XOR K ) )
     * @param rightHalf 4 bit 右半部分
     * @param subKey    8 bit 子密钥
     * @return          4 bit 结果
     */
    public static String roundFunction(String rightHalf, String subKey) {
        // 1. EP 扩展为 8 bit
        String expanded = permute(rightHalf, EP_BOX);
        // 2. 与子密钥异或
        String xored = xor(expanded, subKey);
        // 3. 分为左右各 4 bit，分别过 S 盒
        String leftNibble  = xored.substring(0, 4);
        String rightNibble = xored.substring(4);
        String s1Out = sBoxLookup(leftNibble, S_BOX_1);
        String s2Out = sBoxLookup(rightNibble, S_BOX_2);
        // 4. 合并为 4 bit，再过 SP 置换
        return permute(s1Out + s2Out, SP_BOX);
    }

    /**
     * 两个等长二进制串按位异或。
     */
    public static String xor(String a, String b) {
        if (a.length() != b.length()) {
            throw new IllegalArgumentException("异或的两个串长度必须相同");
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < a.length(); i++) {
            result.append(a.charAt(i) ^ b.charAt(i));
        }
        return result.toString();
    }

    // ============ 单轮 f_k 结构 ============

    /**
     * f_k 单轮结构：对 8-bit 数据进行一次轮函数处理。
     * 返回 L' || R'，其中 L' = L XOR F(R,K)，R' = R。
     * @param input8  8 bit 数据
     * @param subKey  8 bit 子密钥
     * @return        8 bit 结果
     */
    public static String fk(String input8, String subKey) {
        String leftHalf  = input8.substring(0, 4);
        String rightHalf = input8.substring(4);
        String fResult   = roundFunction(rightHalf, subKey);
        String newLeft   = xor(leftHalf, fResult);
        return newLeft + rightHalf;
    }

    /**
     * 交换左右 4 bit。
     */
    public static String swapHalves(String input8) {
        return input8.substring(4) + input8.substring(0, 4);
    }

    // ============ 加密 / 解密主流程 ============

    /**
     * S-DES 加密：C = IP^-1( f_k2( SW( f_k1( IP(P) ) ) ) )
     * @param plainText8 8 bit 明文
     * @param key10      10 bit 密钥
     * @return           8 bit 密文
     */
    public static String encrypt(String plainText8, String key10) {
        validateBinary(plainText8, 8, "明文");
        String[] subKeys = generateSubKeys(key10);

        String ipResult   = permute(plainText8, IP);      // 初始置换
        String round1     = fk(ipResult, subKeys[0]);     // 第一轮 f_k1
        String swapped    = swapHalves(round1);           // SW
        String round2     = fk(swapped, subKeys[1]);      // 第二轮 f_k2
        return permute(round2, IP_INV);                   // 逆初始置换
    }

    /**
     * S-DES 解密：P = IP^-1( f_k1( SW( f_k2( IP(C) ) ) ) )
     * @param cipherText8 8 bit 密文
     * @param key10       10 bit 密钥
     * @return            8 bit 明文
     */
    public static String decrypt(String cipherText8, String key10) {
        validateBinary(cipherText8, 8, "密文");
        String[] subKeys = generateSubKeys(key10);

        String ipResult   = permute(cipherText8, IP);
        String round1     = fk(ipResult, subKeys[1]);     // 先用 k2
        String swapped    = swapHalves(round1);
        String round2     = fk(swapped, subKeys[0]);      // 再用 k1
        return permute(round2, IP_INV);
    }

    // ============ 校验与编码辅助 ============

    /** 校验二进制串长度及字符合法性 */
    public static void validateBinary(String bits, int expectedLength, String name) {
        if (bits == null || bits.length() != expectedLength) {
            throw new IllegalArgumentException(name + "长度必须为 " + expectedLength + " bit");
        }
        for (int i = 0; i < bits.length(); i++) {
            char c = bits.charAt(i);
            if (c != '0' && c != '1') {
                throw new IllegalArgumentException(name + "只能包含 0 和 1");
            }
        }
    }

    /** 将字符串按 ASCII 逐字符加密，每字符 8 bit 明文，密钥 10 bit，输出 8 bit 密文按字符拼装 */
    public static String encryptString(String message, String key10) {
        StringBuilder sb = new StringBuilder();
        for (char c : message.toCharArray()) {
            String plainBits = toBinaryString(c, 8);
            String cipherBits = encrypt(plainBits, key10);
            sb.append((char) Integer.parseInt(cipherBits, 2));
        }
        return sb.toString();
    }

    /** 对字符串密文逐字符解密 */
    public static String decryptString(String cipherMessage, String key10) {
        StringBuilder sb = new StringBuilder();
        for (char c : cipherMessage.toCharArray()) {
            String cipherBits = toBinaryString(c, 8);
            String plainBits = decrypt(cipherBits, key10);
            sb.append((char) Integer.parseInt(plainBits, 2));
        }
        return sb.toString();
    }

    /**
     * 整数转固定长度二进制串（左侧补零，只保留最低 length 位）。
     * 注意：这里按 length 掩码，而不是固定 & 0xFF，
     * 避免 10-bit 密钥被错误截断为 8-bit。
     */
    public static String toBinaryString(int value, int length) {
        int mask = (length >= 32) ? -1 : ((1 << length) - 1);
        int masked = value & mask;

        String bits = Integer.toBinaryString(masked);
        int pad = length - bits.length();
        if (pad > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pad; i++) sb.append('0');
            sb.append(bits);
            return sb.toString();
        }
        return bits;
    }

    // ============ 带回调的字符串加解密（用于 GUI 展示过程） ============

    /**
     * 字符串加解密过程回调接口。
     * groupLabel 为组别标题（如"第 1 组"）；整体说明类的日志 groupLabel 传 ""。
     */
    public interface ProcessListener {
        /**
         * @param groupLabel 组别标签（如 "第 1 组"），无组别时为空串
         * @param fieldName  字段名（如 "原字符"、"8-bit 二进制"）
         * @param value      字段值
         */
        void onStep(String groupLabel, String fieldName, String value);
    }

    /**
     * 带过程回调的字符串加密。
     * 明文按 ASCII 逐字符分组（每字符 1 Byte），逐字符加密后拼装为密文字符串。
     */
    public static String encryptStringWithLog(String message, String key10,
                                              ProcessListener listener) {
        StringBuilder cipherText = new StringBuilder();

        if (listener != null) {
            listener.onStep("", "算法输入", "明文 \"" + message + "\"，密钥 " + key10);
        }

        for (int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);
            int asciiValue = (int) ch;
            String plainBits = toBinaryString(asciiValue, 8);
            String cipherBits = encrypt(plainBits, key10);
            int cipherAscii = Integer.parseInt(cipherBits, 2);
            char cipherChar = (char) cipherAscii;
            cipherText.append(cipherChar);

            if (listener != null) {
                String group = "第 " + (i + 1) + " 组";
                listener.onStep(group, "原字符",       "'" + ch + "'");
                listener.onStep(group, "ASCII 十进制", String.valueOf(asciiValue));
                listener.onStep(group, "8-bit 二进制", plainBits);
                listener.onStep(group, "S-DES 密文",
                        cipherBits + "  (十进制 " + cipherAscii + ")");
                listener.onStep(group, "密文字符",
                        isPrintable(cipherChar)
                                ? "'" + cipherChar + "'"
                                : "不可打印 ASCII:" + cipherAscii );
            }
        }

        if (listener != null) {
            listener.onStep("", "拼装完成",
                    "密文字符串长度 = " + cipherText.length() + " 字符");
        }
        return cipherText.toString();
    }

    /**
     * 带过程回调的字符串解密。
     */
    public static String decryptStringWithLog(String cipherMessage, String key10,
                                              ProcessListener listener) {
        StringBuilder plainText = new StringBuilder();

        if (listener != null) {
            listener.onStep("", "算法输入", "密文长度 " + cipherMessage.length()
                    + " 字符，密钥 = " + key10);
        }

        for (int i = 0; i < cipherMessage.length(); i++) {
            char cipherChar = cipherMessage.charAt(i);
            int cipherAscii = (int) cipherChar;
            String cipherBits = toBinaryString(cipherAscii, 8);
            String plainBits = decrypt(cipherBits, key10);
            int plainAscii = Integer.parseInt(plainBits, 2);
            char plainChar = (char) plainAscii;
            plainText.append(plainChar);

            if (listener != null) {
                String group = "第 " + (i + 1) + " 组";
                listener.onStep(group, "密文字符",
                        isPrintable(cipherChar)
                                ? "'" + cipherChar + "'"
                                : "不可打印 ASCII:" + cipherAscii );
                listener.onStep(group, "密文 8-bit 二进制",
                        cipherBits + "  (十进制 " + cipherAscii + ")");
                listener.onStep(group, "S-DES 明文",
                        plainBits + "  (十进制 " + plainAscii + ")");
                listener.onStep(group, "还原字符", "'" + plainChar + "'");
            }
        }

        if (listener != null) {
            listener.onStep("", "拼装完成", "解密结果 = \"" + plainText + "\"");
        }
        return plainText.toString();
    }

    /** 判断字符是否为可打印 ASCII（用于日志友好显示） */
    public static boolean isPrintable(char c) {
        return c >= 32 && c < 127;
    }

    // ============ 带过程回调的 8-bit 加解密（用于 GUI 展示运算过程） ============

    /**
     * 带过程回调的单比特（8 bit）加密。
     * 依次回调：输入 → 子密钥 → IP → 第1轮 f_k1 → SW → 第2轮 f_k2 → IP^-1 → 输出。
     *
     * @param plainText8 8 bit 明文
     * @param key10      10 bit 密钥
     * @param listener   过程监听器，可为 null
     * @return           8 bit 密文
     */
    public static String encryptWithLog(String plainText8, String key10,
                                        ProcessListener listener) {
        validateBinary(plainText8, 8, "明文");

        if (listener != null) {
            listener.onStep("", "输入", "明文 P = " + plainText8 + "，密钥 K = " + key10);
        }

        // 1. 子密钥扩展
        String[] subKeys = generateSubKeys(key10);
        if (listener != null) {
            listener.onStep("", "密钥扩展", "k1 = " + subKeys[0] + "，k2 = " + subKeys[1]);
        }

        // 2. 初始置换
        String ipResult = permute(plainText8, IP);
        if (listener != null) {
            listener.onStep("", "初始置换 IP", ipResult
                    + "  (L0=" + ipResult.substring(0, 4)
                    + ", R0=" + ipResult.substring(4) + ")");
        }

        // 3. 第 1 轮 f_k1
        String round1 = fk(ipResult, subKeys[0]);
        if (listener != null) {
            String l0 = ipResult.substring(0, 4);
            String r0 = ipResult.substring(4);
            String f1 = roundFunction(r0, subKeys[0]);
            listener.onStep("", "第 1 轮 F(R0,k1)", f1);
            listener.onStep("", "第 1 轮 L0⊕F", xor(l0, f1) + "，R 保持 " + r0);
            listener.onStep("", "第 1 轮 f_k1 输出", round1);
        }

        // 4. SW 交换
        String swapped = swapHalves(round1);
        if (listener != null) {
            listener.onStep("", "交换 SW", swapped
                    + "  (L2=" + swapped.substring(0, 4)
                    + ", R2=" + swapped.substring(4) + ")");
        }

        // 5. 第 2 轮 f_k2
        String round2 = fk(swapped, subKeys[1]);
        if (listener != null) {
            String l2 = swapped.substring(0, 4);
            String r2 = swapped.substring(4);
            String f2 = roundFunction(r2, subKeys[1]);
            listener.onStep("", "第 2 轮 F(R2,k2)", f2);
            listener.onStep("", "第 2 轮 L2⊕F", xor(l2, f2) + "，R 保持 " + r2);
            listener.onStep("", "第 2 轮 f_k2 输出", round2);
        }

        // 6. 逆初始置换
        String cipher = permute(round2, IP_INV);
        if (listener != null) {
            listener.onStep("", "逆初始置换 IP⁻¹", cipher);
            listener.onStep("", "输出", "密文 C = " + cipher);
        }
        return cipher;
    }

    /**
     * 带过程回调的单比特（8 bit）解密。
     */
    public static String decryptWithLog(String cipherText8, String key10,
                                        ProcessListener listener) {
        validateBinary(cipherText8, 8, "密文");

        if (listener != null) {
            listener.onStep("", "输入", "密文 C = " + cipherText8 + "，密钥 K = " + key10);
        }

        String[] subKeys = generateSubKeys(key10);
        if (listener != null) {
            listener.onStep("", "密钥扩展", "k1 = " + subKeys[0] + "，k2 = " + subKeys[1]);
        }

        String ipResult = permute(cipherText8, IP);
        if (listener != null) {
            listener.onStep("", "初始置换 IP", ipResult
                    + "  (L0=" + ipResult.substring(0, 4)
                    + ", R0=" + ipResult.substring(4) + ")");
        }

        // 解密第 1 轮用 k2
        String round1 = fk(ipResult, subKeys[1]);
        if (listener != null) {
            String l0 = ipResult.substring(0, 4);
            String r0 = ipResult.substring(4);
            String f1 = roundFunction(r0, subKeys[1]);
            listener.onStep("", "第 1 轮 F(R0,k2)", f1);
            listener.onStep("", "第 1 轮 L0⊕F", xor(l0, f1) + "，R 保持 " + r0);
            listener.onStep("", "第 1 轮 f_k2 输出", round1);
        }

        String swapped = swapHalves(round1);
        if (listener != null) {
            listener.onStep("", "交换 SW", swapped);
        }

        // 解密第 2 轮用 k1
        String round2 = fk(swapped, subKeys[0]);
        if (listener != null) {
            String l2 = swapped.substring(0, 4);
            String r2 = swapped.substring(4);
            String f2 = roundFunction(r2, subKeys[0]);
            listener.onStep("", "第 2 轮 F(R2,k1)", f2);
            listener.onStep("", "第 2 轮 L2⊕F", xor(l2, f2) + "，R 保持 " + r2);
            listener.onStep("", "第 2 轮 f_k1 输出", round2);
        }

        String plain = permute(round2, IP_INV);
        if (listener != null) {
            listener.onStep("", "逆初始置换 IP⁻¹", plain);
            listener.onStep("", "输出", "明文 P = " + plain);
        }
        return plain;
    }

    // ============ 暴力破解 ============

    /** 暴力破解结果封装 */
    public static class BruteForceResult {
        public final String cipherText;        // 被破解的密文
        public final String knownPlainText;    // 已知的明文
        public final java.util.List<String> candidates;  // 所有候选密钥
        public final long elapsedNanos;        // 总耗时（纳秒）
        public final int totalTried;           // 穷举的密钥总数

        public BruteForceResult(String cipherText, String knownPlainText,
                                java.util.List<String> candidates,
                                long elapsedNanos, int totalTried) {
            this.cipherText = cipherText;
            this.knownPlainText = knownPlainText;
            this.candidates = candidates;
            this.elapsedNanos = elapsedNanos;
            this.totalTried = totalTried;
        }

        /** 总耗时（毫秒，保留 3 位小数） */
        public double elapsedMillis() {
            return elapsedNanos / 1_000_000.0;
        }

        /** 每个密钥的平均耗时（纳秒） */
        public double averageNanosPerKey() {
            return totalTried == 0 ? 0 : (double) elapsedNanos / totalTried;
        }
    }

    /**
     * 已知明文攻击：给定 (明文P, 密文C)，穷举 0~1023 全部密钥，找出能
     * 使 encrypt(P,K) == C 的所有候选密钥，并统计破解耗时。
     *
     * @param knownPlainText8 已知的 8 bit 明文
     * @param cipherText8     对应的 8 bit 密文
     * @param listener        可选，每找到一个候选就回调一次
     * @return                破解结果
     */
    public static BruteForceResult bruteForce(String knownPlainText8,
                                              String cipherText8,
                                              ProcessListener listener) {
        validateBinary(knownPlainText8, 8, "已知明文");
        validateBinary(cipherText8, 8, "目标密文");

        java.util.List<String> candidates = new java.util.ArrayList<>();
        int totalKeys = 1 << 10;   // 1024
        int tried = 0;

        long startNanos = System.nanoTime();
        for (int k = 0; k < totalKeys; k++) {
            String candidateKey = toBinaryString(k, 10);
            String produced = encrypt(knownPlainText8, candidateKey);
            tried++;
            if (produced.equals(cipherText8)) {
                candidates.add(candidateKey);
                if (listener != null) {
                    listener.onStep("", "命中候选",
                            "K = " + candidateKey + "   (第 " + tried + " 次尝试)");
                }
            }
        }
        long elapsedNanos = System.nanoTime() - startNanos;

        return new BruteForceResult(cipherText8, knownPlainText8,
                candidates, elapsedNanos, tried);
    }
}

