# S-DES 加解密工具

> 一个基于 Java Swing 的 S-DES（Simplified DES）加解密与暴力破解演示程序。
> 支持单字符 / 字符串加解密、TCP Socket 加密通信、已知明文暴力破解。

![Java](https://img.shields.io/badge/Java-8%2B-blue)
![Swing](https://img.shields.io/badge/GUI-Swing-green)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

---

## 目录

- [作者](#作者)
- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [快速开始](#快速开始)
- [使用说明](#使用说明)&emsp;{[Using Instrustions（完整版）](Using%20Instrustions.md)}
- [算法说明](#算法说明)
- [接口文档](#接口文档)&emsp;{[Development Manual（完整版）](Development%20Manual.md)}
- [测试结果](#测试结果)&emsp;{[Test Report（完整版）](Test%20Report.md)}
- [项目结构](#项目结构)

---
## 作者

| 学号 | 姓名 |
| ---------| ---- |
| 20240421 | 叶丁 |

---

## 项目简介

S-DES（Simplified DES）是一种教学用的简化版 DES 算法，分组长度 8 bit、
密钥长度 10 bit、2 轮 Feistel 结构。本项目完整实现了 S-DES 的加密、解密、
密钥扩展、S 盒查表、P 盒置换，并在此基础上扩展出：

- **图形化交互界面**
- **字符串加解密**（按 ASCII 每字符 1 Byte 分组）
- **TCP Socket 加密通信**（客户端 / 服务端双向）
- **暴力破解**（已知明文攻击，穷举 1024 个密钥并统计耗时）

---

## 功能特性

| 关卡 | 功能 | 状态 |
|---|---|---|
| 第 1 关 | 基本测试：8-bit 明文 + 10-bit 密钥 → 8-bit 密文，GUI 交互 | ✅ |
| 第 2 关 | 交叉测试：与其他程序结果一致 | ✅ |
| 第 3 关 | 扩展功能：ASCII 字符串加解密 + TCP Socket 通信 | ✅ |
| 第 4 关 | 暴力破解：穷举 1024 密钥，统计耗时与候选 | ✅ |
| 第 5 关 | 封闭测试：分析密钥-明文-密文的多对多关系 | ✅ |

---

## 快速开始

### 环境要求

- JDK 8 或更高版本
- 任意支持 Swing 的操作系统（Windows / macOS / Linux）
- 建议使用 IntelliJ IDEA 或 Eclipse

### 编译与运行

- 运行SDESServerGui.java开启服务端ui界面
- 运行SDESGuiApp.java开启客户端ui界面

```bash
# 1. 克隆仓库
git clone https://github.com/<your-username>/S-DES-Cipher.git
cd S-DES-Cipher

# 2. 编译
javac -d out src/sdes/*.java

# 3. 运行客户端 （加解密 + 暴力破解）
java -cp out sdes.SDESGuiApp

# 4. 可选：运行服务端 GUI（接收密文 + 主动发送）
java -cp out sdes.SDESServerGui
```

---

## 使用说明

### 主界面（4 个标签页）

| 标签页       | 功能                                        |
| ------------ | ------------------------------------------- |
| **加解密**   | 单字符 / 字符串加密解密，显示每一步运算过程 |
| **暴力破解** | 输入已知 (明文, 密文)，穷举密钥并统计耗时   |
| **网络通信** | 通过 TCP 把加密后的密文发送到指定服务器     |
| **运算日志** | 显示所有操作的过程日志，支持清空 / 复制     |

### 单字符加密示例

| 项目   | 值           |
| ------ | ------------ |
| 密钥 K | `1010000010` |
| 明文 P | `10000001`   |
| 密文 C | `11100111`   |

点击「单字符加密」，日志区会显示完整过程：

```text

══════════════ 单字符加密运算过程 ══════════════
  输入                 : 明文 P = 10000001，密钥 K = 1010000010
  密钥扩展              : k1 = 10100100，k2 = 01000011
  初始置换 IP           : 00010100  (L0=0001, R0=0100)
  第 1 轮 F(R0,k1)     : 0100
  第 1 轮 L0⊕F         : 0101，R 保持 0100
  第 1 轮 f_k1 输出     : 01010100
  交换 SW              : 01000101  (L2=0100, R2=0101)
  第 2 轮 F(R2,k2)     : 1011
  第 2 轮 L2⊕F         : 1111，R 保持 0101
  第 2 轮 f_k2 输出     : 11110101
  逆初始置换 IP⁻¹       : 11100111
  输出                 : 密文 C = 11100111

```



输入任意字符串（如 `Hello`），按 ASCII 每字符 1 Byte 分组，  
逐字符 S-DES 加密后拼装为密文字符串。日志区显示每组的：

```text
原字符 → ASCII 十进制 → 8-bit 二进制 → S-DES 密文 → 密文字符
```



### TCP 加密通信

1. 先启动服务端：`java -cp out sdes.SDESServerGui`

2. 在客户端「网络通信」页填写服务端 IP（如 `127.0.0.1`）、端口（`8888`）、消息

3. 点击「加密并发送」，服务端日志显示来源与解密明文，并回传 `收到 + Base64(密文)`

4. 客户端日志显示回执，并验证回执携带的密文与发送的一致

### 暴力破解

已知一组 (明文 P, 密文 C)，点击「开始破解」：

- 穷举 1024 个密钥（`0000000000` ~ `1111111111`）

- 输出所有候选密钥、总耗时、平均每密钥耗时

- 若候选 > 1，提示需要用第二组 (P,C) 进一步筛选

---

## 算法说明

### 参数

| 参数     | 值     |
| -------- | ------ |
| 分组长度 | 8 bit  |
| 密钥长度 | 10 bit |
| 轮数     | 2      |

### 转换装置

| 装置  | 值                              |
| ----- | ------------------------------- |
| P10   | (3, 5, 2, 7, 4, 10, 1, 9, 8, 6) |
| P8    | (6, 3, 7, 4, 8, 5, 10, 9)       |
| IP    | (2, 6, 3, 1, 4, 8, 5, 7)        |
| IP⁻¹  | (4, 1, 3, 5, 7, 2, 8, 6)        |
| EPBox | (4, 1, 2, 3, 2, 3, 4, 1)        |
| SPBox | (2, 4, 3, 1)                    |

**S-Box 1**

|        | 00  | 01  | 10  | 11  |
| ------ | --- | --- | --- | --- |
| **00** | 1   | 0   | 3   | 2   |
| **01** | 3   | 2   | 1   | 0   |
| **10** | 0   | 2   | 1   | 3   |
| **11** | 3   | 1   | 0   | 2   |

**S-Box 2**

|        | 00  | 01  | 10  | 11  |
| ------ | --- | --- | --- | --- |
| **00** | 0   | 1   | 2   | 3   |
| **01** | 2   | 3   | 1   | 0   |
| **10** | 3   | 0   | 1   | 2   |
| **11** | 2   | 1   | 0   | 3   |

### 加解密公式

```text
加密：C = IP⁻¹( f_k2( SW( f_k1( IP(P) ) ) ) )
解密：P = IP⁻¹( f_k1( SW( f_k2( IP(C) ) ) ) )
密钥扩展：k_i = P8( Shift^i( P10(K) ) ),  i = 1, 2
轮函数：F(R, K) = SPBox( SBox( EPBox(R) ⊕ K ) )
```

---

## 接口文档

### `SDESCore` —— 算法核心

| 方法                                                                     | 说明                                  |
| ------------------------------------------------------------------------ | ------------------------------------- |
| `String encrypt(String p8, String k10)`                                  | 8-bit 明文 + 10-bit 密钥 → 8-bit 密文 |
| `String decrypt(String c8, String k10)`                                  | 8-bit 密文 + 10-bit 密钥 → 8-bit 明文 |
| `String encryptWithLog(String p8, String k10, ProcessListener l)`        | 带过程回调的单字符加密                |
| `String decryptWithLog(String c8, String k10, ProcessListener l)`        | 带过程回调的单字符解密                |
| `String encryptString(String msg, String k10)`                           | 字符串加密                            |
| `String decryptString(String msg, String k10)`                           | 字符串解密                            |
| `String encryptStringWithLog(String msg, String k10, ProcessListener l)` | 带过程回调的字符串加密                |
| `String decryptStringWithLog(String msg, String k10, ProcessListener l)` | 带过程回调的字符串解密                |
| `String[] generateSubKeys(String k10)`                                   | 生成 [k1, k2]                         |
| `BruteForceResult bruteForce(String p8, String c8, ProcessListener l)`   | 已知明文暴力破解                      |
| `String permute(String in, int[] table)`                                 | 通用位置换                            |
| `String xor(String a, String b)`                                         | 按位异或                              |
| `void validateBinary(String bits, int len, String name)`                 | 校验二进制串                          |

### 回调接口 `SDESCore.ProcessListener`

java

void onStep(String groupLabel, String fieldName, String value);

### `BruteForceResult` —— 破解结果

| 字段 / 方法                   | 说明                   |
| ----------------------------- | ---------------------- |
| `String cipherText`           | 被破解的密文           |
| `String knownPlainText`       | 已知的明文             |
| `List<String> candidates`     | 所有候选密钥           |
| `long elapsedNanos`           | 总耗时（纳秒）         |
| `int totalTried`              | 穷举的密钥总数         |
| `double elapsedMillis()`      | 总耗时（毫秒）         |
| `double averageNanosPerKey()` | 平均每密钥耗时（纳秒） |

---

## 测试结果

> 完整的测试报告见 [`Test Report.md`](Test%20Report.md)。

### 第 1 关：基本测试

| 项目    | 值           |
| ------- | ------------ |
| 密钥 K  | `1010000010` |
| 明文 P  | `10001100`   |
| 密文 C  | `10010111`   |
| 解密 P' | `10001100`   |
| P == P' | ✅            |

[单字符转换效果图](Screenshots/第一关/单字符转换.png)  

[日志过程记录](Screenshots/第一关/日志过程记录.png)

[正向加密过程](Screenshots/第一关/正向加密过程.png)  &emsp;[逆向解密过程](Screenshots/第一关/逆向解密过程.png)

### 第 2 关：交叉测试

> 使用其他组的程序进行验证实验

与同学程序使用**完全相同的置换表 / S 盒**，对同一组 (P, K) 加密结果一致。

| 密钥 K       | 明文 P     | 本程序密文 C | 同学程序密文 C' | 一致 |
| ------------ | ---------- | ------------ | --------------- | ---- |
| `1010000010` | `10000001` | `11100111`   | `11100111`      | ✅    |
| `1010110010` | `11000000` | `11010001`   | `11010001`      | ✅    |

[验证1密钥1010000010](Screenshots/第二关/验证1密钥1010000010.png)  

[验证2密钥1010110010](Screenshots/第二关/验证2密钥1010110010.png)  

### 第 3 关：扩展功能（字符串 + TCP）

字符串 `Hello` 加解密往返：

| 步骤     | 内容                     |
| -------- | ------------------------ |
| 原文     | `Hello`                  |
| 密文     | （乱码，逐字符加密拼装） |
| 解密     | `Hello`                  |
| 往返一致 | ✅                        |

TCP 通信：

- 客户端发送密文 → 服务端日志显示**信息来源**（IP:端口）与**解密明文**

- 服务端回传 `收到 + Base64(密文)` → 客户端验证回执携带的密文与原密文一致

[ACII字符串加密](Screenshots/第三关/ACII字符串加密.png)  

[字符串加密加载日志1](Screenshots/第三关/字符串加密加载日志1.png) &emsp; [字符串加密加载日志2](Screenshots/第三关/字符串加密加载日志2.png)  

[TCP服务端发送密文1](Screenshots/第三关/TCP服务端发送密文1.png) &emsp; [TCP服务端发送密文2](Screenshots/第三关/TCP服务端发送密文2.png) 

[TCP客户端发送密文](Screenshots/第三关/TCP客户端发送密文.png)

### 第 4 关：暴力破解

| 项目         | 值         |
| ------------ | ---------- |
| 已知明文 P   | `10001000` |
| 目标密文 C   | `00110010` |
| 穷举密钥总数 | 1024       |
| 总耗时       | 30.95ms    |
| 平均每密钥   | 30225ns    |
| 候选密钥数   | 6          |
| 命中密钥     | `后续筛选` |

[目标明密文组 ](Screenshots/第四关/目标明密文组.png)

[暴力破解 ](Screenshots/第四关/暴力破解.png)

[暴力破解日志 ](Screenshots/第四关/暴力破解日志.png)

### 第 5 关：封闭测试

**结论 1：一个明密文对可能对应多个密钥。**

由于 S-DES 密钥空间 1024，明文-密文空间 256×256，  
平均每个 (P,C) 对对应约 4 个密钥。实测随机选取一组 (P,C)，  
遍历发现候选密钥数 > 1 的比例约为 99%。

**结论 2：不同密钥可以加密同一明文得到相同密文。**

对固定明文 P，1024 个密钥产生 1024 个密文，但密文空间只有 256，  
因此**必然存在**多个密钥产生同一密文。

| 明文 P     | 得到的相同密文 C |
| ---------- | ---------------- |
| `10000001` | `11100111`       |

**意义**：仅凭一组 (P, C) 无法唯一确定密钥，需要多组明密文对才能收敛。

---

## 项目结构

S-DES-Cipher.
│  Development Manual.md
│  README.md
│  Test Report.md
│  Using Instrustions.md
│
├─S-DES
│  │  S-DES.iml
│  │
│  ├─out
│  │  └─production
│  │      └─S-DES
│  │          └─sdes
│  │                  FontProvider.class
│  │                  SDESCore$BruteForceResult.class
│  │                  SDESCore$ProcessListener.class
│  │                  SDESCore.class
│  │                  SDESGuiApp$1.class
│  │                  SDESGuiApp$2.class
│  │                  SDESGuiApp$3.class
│  │                  SDESGuiApp$RoundedBorder.class
│  │                  SDESGuiApp.class
│  │                  SDESServer.class
│  │                  SDESServerGui$1.class
│  │                  SDESServerGui.class
│  │
│  └─src
│      └─sdes
│              FontProvider.java
│              SDESCore.java
│              SDESGuiApp.java
│              SDESServer.java
│              SDESServerGui.java
│
└─Screenshots
    ├─第一关
    │      单字符转换.png
    │      日志过程记录.png
    │      正向加密过程 .png
    │      逆向解密过程 .png
    │
    ├─第三关
    │      ACII字符串加密.png
    │      TCP客户端发送密文.png
    │      TCP服务端发送密文1.png
    │      TCP服务端发送密文2.png
    │      字符串加密加载日志1.png
    │      字符串加载加密日志2.png
    │
    ├─第二关
    │      验证1密钥1010000010.png
    │      验证2密钥1010110010.png
    │
    └─第四关
            暴力破解.png
            暴力破解日志.png
            目标明密文组 .png

--
