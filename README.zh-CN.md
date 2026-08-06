# opencv-sdk

<div align="center">

**OpenCV + DeepLearning4j 人脸检测 / 识别工具包：OpenCV 人脸模板、ND4J 矩阵模板与 FaceNet 识别**

![Java](https://img.shields.io/badge/Java-17-orange) ![License](https://img.shields.io/badge/license-Apache%202.0-green)

[English](./README.md) | [简体中文](./README.zh-CN.md)

[1. 项目概述](#1-project-overview) · [2. 能力与状态](#2-features--status) · [3. 运行要求与兼容性](#3-requirements--compatibility) · [4. 架构与模块](#4-architecture--modules) · [5. 引入依赖](#5-installation) · [6. 快速开始](#6-quick-start) · [7. 配置](#7-configuration) · [8. 核心用法](#8-core-usage) · [9. 测试与构建](#9-testing--build) · [10. 版本线与分支](#10-versioning--branches) · [11. 贡献与许可证](#11-contributing--license)

</div>

---

> **当前分支**：`feature/2.0.x`<br>
> **版本**：`2.0.x.x.20260630-SNAPSHOT`<br>
> **JDK 基线**：8<br>
> **项目状态**：实验性（首个公开版本）。尚未发布 Maven Central；制品通过 Aliyun Maven 仓库与 GitHub Releases 分发。

<a id="1-project-overview"></a>
## 1. 项目概述

### 1.1 是什么

**opencv-sdk** 是整合 OpenCV（JavaCPP `opencv-platform`）与 DeepLearning4j（DL4J）的 Java 工具包：提供基于 OpenCV 的人脸检测与比对模板、ND4J 矩阵转换与存储，以及面向人脸识别流程的 FaceNet-small 模型。目标场景是 Spring Boot 应用中常见的图像处理与人脸识别功能（包命名遵循 `org.bytedeco.opencv.spring.boot` 约定；1.0.x 模块本身的 POM 不含 Spring 依赖）。

### 1.2 不是什么

- 本版本不是 Spring Boot 自动配置模块（此处无 starter 构件）。
- 不是完整的计算机视觉框架——它封装 OpenCV + DL4J 构建块。

### 1.3 典型使用场景

| 场景 | 推荐入口 | 结果 |
|---|---|---|
| 图像中的人脸检测 | `OpenCVFaceRecognitionTemplate.detect(File)` | `JSONObject` 检测结果 |
| 比对两张人脸图像 | `OpenCVFaceRecognitionTemplate.match(File, File)` | `JSONObject` 比对结果 |
| 人脸向量化 / 识别 | `Nd4jTemplate` + `FaceNetSmallV2Model` | 注册成员后 `search(group, memberId)` |
| 图像转 ND4J 矩阵 | `Nd4jTemplate.asMatrix(...)` | `INDArray` |
| 图像加载策略 | `ImageLoader`（CIFAR / DEFAULT / LFW / NATIVE） | 可插拔加载器 |
| 像素数据提取 | `ImageFactory.getRGBData / getGrayData` | 自定义处理用 `ImageInfo` |

<a id="2-features--status"></a>
## 2. 能力与状态

| 能力 | 状态 | 说明 |
|---|:---:|---|
| 人脸检测 | 可用 | `OpenCVFaceRecognitionTemplate.detect(String / byte[] / File)` |
| 人脸比对 | 可用 | `OpenCVFaceRecognitionTemplate.match(String / byte[] / File)` |
| 图像平滑 | 可用 | `OpenCVFaceRecognitionTemplate.smooth(String path)` |
| FaceNet-small 模型 | 可用 | `FaceNetSmallV2Model.conf()` / `init()` 返回 `ComputationGraph` |
| ND4J 图像模板 | 可用 | `Nd4jTemplate`：`asMatrix(...)`、`faceNew(group, memberId, ...)`、`search(...)`、`match(...)` |
| ND4J 存储提供者 | 可用 | `INDArrayStoreProvider` + `INDArrayLocalCacheStoreProvider` |
| 图像信息提取 | 可用 | `ImageFactory` RGB / 灰度数据 + `getBestRect` |
| 图像加载器 | 可用 | `ImageLoader` 枚举：`CIFAR`、`DEFAULT`、`LFW`、`NATIVE` |

<a id="3-requirements--compatibility"></a>
## 3. 运行要求与兼容性

| 组件 | 版本 | 说明 |
|---|---:|---|
| JDK | 17+ | 1.0.x 线基线 |
| opencv-platform（JavaCPP） | 4.1.0-1.5.1 | 按平台解析原生 OpenCV 二进制 |
| DeepLearning4j core / zoo / modelimport | 1.0.0-beta4 | 模型与导入 |
| ND4J api + native | 1.0.0-beta4 | 矩阵运算（`nd4j-native`） |
| datavec-data-image | 1.0.0-beta4 | 图像加载 |
| fastjson | 2.0.62 | JSON 结果 |
| Guava | 30.0-jre | 工具类 |
| commons-io | 2.22.0 | IO 工具 |
| SLF4J | 2.0.18 | 日志门面 |

版本线矩阵：

| 版本线 | 分支 | JDK | 版本模式 | 用途 |
|---|---|---:|---|---|
| 1.0.x | `feature/2.0.x`（当前分支） | 8 | `1.0.x.*` | 存量项目、Boot 2.x Starter 线 |
| 2.0.x | `feature/2.0.x` | 17 | `2.0.x.*` | JDK 17 线 |
| 3.0.x | `feature/3.0.x` | 21 | `3.0.x.*` | 新项目 |

<a id="4-architecture--modules"></a>
## 4. 架构与模块

```text
[ Java 应用 ]
        |
        | opencv-sdk
        v
+------------------------------------------+
| OpenCV 层（org.bytedeco.opencv）          |
|  OpenCVFaceRecognitionTemplate            |
|    detect / match / smooth                |
|  ImageFactory -> ImageInfo / Rect         |
+------------------------------------------+
| DL4J 层                                   |
|  FaceNetSmallV2Model -> ComputationGraph  |
|  Nd4jTemplate：asMatrix / faceNew /       |
|    search / match                         |
|  ImageLoader（CIFAR/DEFAULT/LFW/NATIVE）  |
|  INDArrayStoreProvider（+本地缓存）        |
+------------------------------------------+
        |
        v
[ OpenCV 原生（JavaCPP）+ ND4J 原生 ]
```

单模块库（打包类型 `jar`）。包结构：

| 包 | 职责 |
|---|---|
| `org.bytedeco.opencv.spring.boot` | `OpenCVFaceRecognitionTemplate`（detect / match / smooth） |
| `org.bytedeco.opencv.spring.boot.image` | `ImageFactory`、`ImageInfo`、`ImageFormat`、`Rect` |
| `org.bytedeco.opencv.spring.boot.nd4j` | `Nd4jTemplate`、`Nd4jUtils`、`ImageLoader` |
| `org.bytedeco.opencv.spring.boot.nd4j.store` | `INDArrayStoreProvider`、`INDArrayLocalCacheStoreProvider`、`INDArrayInfo` |
| `org.bytedeco.opencv.spring.boot.dl4j` | `FaceNetSmallV2Model` |

<a id="5-installation"></a>
## 5. 引入依赖

Maven：

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>opencv-sdk</artifactId>
    <version>2.0.x.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle：

```groovy
implementation 'io.github.easy4j:opencv-sdk:2.0.x.x.20260630-SNAPSHOT'
```

快照版本需要启用对应快照仓库（`pom.xml` 中 `distributionManagement` 指向 Aliyun Maven 仓库）。原生二进制由 JavaCPP（`opencv-platform`）按平台解析。

<a id="6-quick-start"></a>
## 6. 快速开始

```java
import org.bytedeco.opencv.global.objdetect.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.spring.boot.OpenCVFaceRecognitionTemplate;

// 1. 加载 Haar 级联分类器并构建模板
CascadeClassifier faceDetector = new CascadeClassifier("haarcascade_frontalface_default.xml");
OpenCVFaceRecognitionTemplate template =
        new OpenCVFaceRecognitionTemplate(faceDetector, "/tmp/opencv-work");

// 2. 检测图像中的人脸
JSONObject result = template.detect(new File("photo.jpg"));
System.out.println(result.toJSONString());
```

**预期结果**：`detect(...)` 返回 fastjson `JSONObject`，描述检测结果（如检测到的人脸数量与几何信息，视图像而定）。图像不可读 / 不存在时模板抛异常，而非返回空结果。

<a id="7-configuration"></a>
## 7. 配置

本库通过构造器与方法配置，无配置属性：

| 入口 | 配置面 |
|---|---|
| `OpenCVFaceRecognitionTemplate` | `CascadeClassifier`（检测器）+ 临时目录 |
| `Nd4jTemplate` | `ComputationGraph` + `BaseImageLoader`（`ImageLoader` 枚举）+ 模型输入高 / 宽 |
| `Nd4jTemplate.setStoreProvider(...)` | 替换 `INDArrayStoreProvider`（默认 `INDArrayLocalCacheStoreProvider`） |
| `ImageLoader` | `CIFAR`、`DEFAULT`、`LFW`、`NATIVE` 加载策略 |
| `FaceNetSmallV2Model` | `conf()` 返回图配置；`init()` 构建计算图 |

<a id="8-core-usage"></a>
## 8. 核心用法

### 8.1 基于 FaceNet + ND4J 的人脸识别

```java
FaceNetSmallV2Model model = new FaceNetSmallV2Model();
ComputationGraph graph = model.init();

// 高 / 宽须与模型输入匹配（FaceNet 通常为 160x160）
Nd4jTemplate nd4j = new Nd4jTemplate(graph, ImageLoader.DEFAULT, 160, 160);

nd4j.faceNew("group1", "member1", new File("face-a.jpg"));
String match = nd4j.search("group1", "member1");
System.out.println(match);
```

### 8.2 图像转 ND4J 矩阵

```java
INDArray matrix = nd4j.asMatrix(new File("photo.jpg"));   // 亦支持 InputStream / byte[] / 路径
```

<a id="9-testing--build"></a>
## 9. 测试与构建

```bash
mvn clean verify
```

- JaCoCo 在 `verify` 阶段执行 `prepare-agent`、`report` 与 `check`，行覆盖率规则为 **90%**（`haltOnFailure=false`）——当前分支暂无测试源码，该规则暂不生效，构建仍会生成覆盖率报告。
- 模块自带 Maven Wrapper（`mvnw`），保证构建可复现。
- 发布打包（`mvn -Prelease deploy`）附带 sources 与 javadoc 构件并执行 GPG 签名，对接 Sonatype Central Publishing；普通 `mvn deploy` 按版本后缀路由到 Aliyun Maven 仓库（见 `distributionManagement`）。

<a id="10-versioning--branches"></a>
## 10. 版本线与分支

| 分支 | 版本模式 | JDK | 维护策略 |
|---|---|---|---|
| `feature/1.0.x`（当前分支） | `1.0.x.*` | 8 | 仅接受兼容性修复与 JDK 8 安全的依赖升级 |
| `feature/2.0.x` | `2.0.x.*` | 17 | JDK 17 线 |
| `feature/3.0.x` | `3.0.x.*` | 21 | JDK 21 线 |

<a id="11-contributing--license"></a>
## 11. 贡献与许可证

欢迎贡献。提交 Pull Request 前请执行 `mvn clean verify`，并说明兼容性、测试与迁移影响。本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt) 许可证（声明于 `pom.xml`；当前 worktree 尚未附带 `LICENSE` 文件）。
