# Informa# S-DES 加解密工具

> 一个基于 Java Swing 的 S-DES（Simplified DES）加解密与暴力破解演示程序。
> 支持单字符 / 字符串加解密、TCP Socket 加密通信、已知明文暴力破解。

![Java](https://img.shields.io/badge/Java-8%2B-blue)
![Swing](https://img.shields.io/badge/GUI-Swing-green)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

---

## 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [快速开始](#快速开始)
- [使用说明](#使用说明)
- [算法说明](#算法说明)
- [接口文档](#接口文档)
- [测试结果](#测试结果)
- [项目结构](#项目结构)
- [作者](#作者)

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

```bash
# 1. 克隆仓库
git clone https://github.com/<your-username>/S-DES-Cipher.git
cd S-DES-Cipher

# 2. 编译
javac -d out src/sdes/*.java

# 3. 运行主界面（客户端 + 加解密 + 暴力破解）
java -cp out sdes.SDESGuiApp

# 4. 可选：运行服务端 GUI（接收密文 + 主动发送）
java -cp out sdes.SDESServerGuition-Security-assignment
