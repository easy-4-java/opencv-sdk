# opencv-sdk

<div align="center">

**OpenCV + DeepLearning4j face detection / recognition toolkit: OpenCV face templates, ND4J matrix templates and FaceNet-based recognition**

![Java](https://img.shields.io/badge/Java-17-orange) ![License](https://img.shields.io/badge/license-Apache%202.0-green)

[简体中文](./README.zh-CN.md)

[1. Project Overview](#1-project-overview) · [2. Features & Status](#2-features--status) · [3. Requirements & Compatibility](#3-requirements--compatibility) · [4. Architecture & Modules](#4-architecture--modules) · [5. Installation](#5-installation) · [6. Quick Start](#6-quick-start) · [7. Configuration](#7-configuration) · [8. Core Usage](#8-core-usage) · [9. Testing & Build](#9-testing--build) · [10. Versioning & Branches](#10-versioning--branches) · [11. Contributing & License](#11-contributing--license)

</div>

---

> **Current branch**: `feature/2.0.x`<br>
> **Version**: `2.0.x.x.20260630-SNAPSHOT`<br>
> **JDK baseline**: 8<br>
> **Project status**: experimental (initial public version). Not yet published to Maven Central; artifacts are distributed via the Aliyun Maven repository and GitHub Releases.

<a id="1-project-overview"></a>
## 1. Project Overview

### 1.1 What it is

**opencv-sdk** is a Java toolkit bundling OpenCV (via JavaCPP `opencv-platform`) with DeepLearning4j (DL4J): face detection and matching templates over OpenCV, ND4J matrix conversion and storage, plus a FaceNet-small model for face recognition workflows. It targets image-processing and face-recognition features that are commonly used inside Spring Boot applications (the package namespace follows the `org.bytedeco.opencv.spring.boot` convention; the 1.0.x module itself carries no Spring dependency in its POM).

### 1.2 What it is not

- Not a Spring Boot auto-configuration module in this version (no starter artifacts here).
- Not a full computer-vision framework — it wraps OpenCV + DL4J building blocks.

### 1.3 Typical scenarios

| Scenario | Recommended entry | Result |
|---|---|---|
| Face detection in an image | `OpenCVFaceRecognitionTemplate.detect(File)` | `JSONObject` detection result |
| Compare two face images | `OpenCVFaceRecognitionTemplate.match(File, File)` | `JSONObject` match result |
| Face embedding / recognition | `Nd4jTemplate` + `FaceNetSmallV2Model` | Register member, then `search(group, memberId)` |
| Image -> ND4J matrix | `Nd4jTemplate.asMatrix(...)` | `INDArray` |
| Image loading strategies | `ImageLoader` (CIFAR / DEFAULT / LFW / NATIVE) | Pluggable loaders |
| Pixel data extraction | `ImageFactory.getRGBData / getGrayData` | `ImageInfo` for custom processing |

<a id="2-features--status"></a>
## 2. Features & Status

| Capability | Status | Notes |
|---|:---:|---|
| Face detection | Available | `OpenCVFaceRecognitionTemplate.detect(String / byte[] / File)` |
| Face matching | Available | `OpenCVFaceRecognitionTemplate.match(String / byte[] / File)` |
| Image smoothing | Available | `OpenCVFaceRecognitionTemplate.smooth(String path)` |
| FaceNet-small model | Available | `FaceNetSmallV2Model.conf()` / `init()` returns a `ComputationGraph` |
| ND4J image templates | Available | `Nd4jTemplate`: `asMatrix(...)`, `faceNew(group, memberId, ...)`, `search(...)`, `match(...)` |
| ND4J store providers | Available | `INDArrayStoreProvider` + `INDArrayLocalCacheStoreProvider` |
| Image info extraction | Available | `ImageFactory` RGB / gray data + `getBestRect` |
| Image loaders | Available | `ImageLoader` enum: `CIFAR`, `DEFAULT`, `LFW`, `NATIVE` |

<a id="3-requirements--compatibility"></a>
## 3. Requirements & Compatibility

| Component | Version | Notes |
|---|---:|---|
| JDK | 17+ | 1.0.x line baseline |
| opencv-platform (JavaCPP) | 4.1.0-1.5.1 | Native OpenCV binaries per platform |
| DeepLearning4j core / zoo / modelimport | 1.0.0-beta4 | Models and import |
| ND4J api + native | 1.0.0-beta4 | Matrix ops (`nd4j-native`) |
| datavec-data-image | 1.0.0-beta4 | Image loading |
| fastjson | 2.0.62 | JSON results |
| Guava | 30.0-jre | Utilities |
| commons-io | 2.22.0 | IO helpers |
| SLF4J | 2.0.18 | Logging facade |

Version-line matrix:

| Version line | Branch | JDK | Version pattern | Purpose |
|---|---|---:|---|---|
| 1.0.x | `feature/2.0.x` (this branch) | 8 | `1.0.x.*` | Legacy projects, Boot 2.x starter line |
| 2.0.x | `feature/2.0.x` | 17 | `2.0.x.*` | JDK 17 line |
| 3.0.x | `feature/3.0.x` | 21 | `3.0.x.*` | New projects |

<a id="4-architecture--modules"></a>
## 4. Architecture & Modules

```text
[ Java Application ]
        |
        | opencv-sdk
        v
+------------------------------------------+
| OpenCV layer (org.bytedeco.opencv)        |
|  OpenCVFaceRecognitionTemplate            |
|    detect / match / smooth                |
|  ImageFactory -> ImageInfo / Rect         |
+------------------------------------------+
| DL4J layer                                |
|  FaceNetSmallV2Model -> ComputationGraph  |
|  Nd4jTemplate: asMatrix / faceNew /       |
|    search / match                         |
|  ImageLoader (CIFAR/DEFAULT/LFW/NATIVE)   |
|  INDArrayStoreProvider (+local cache)     |
+------------------------------------------+
        |
        v
[ OpenCV native (JavaCPP) + ND4J native ]
```

Single-module library (packaging `jar`). Package layout:

| Package | Responsibility |
|---|---|
| `org.bytedeco.opencv.spring.boot` | `OpenCVFaceRecognitionTemplate` (detect / match / smooth) |
| `org.bytedeco.opencv.spring.boot.image` | `ImageFactory`, `ImageInfo`, `ImageFormat`, `Rect` |
| `org.bytedeco.opencv.spring.boot.nd4j` | `Nd4jTemplate`, `Nd4jUtils`, `ImageLoader` |
| `org.bytedeco.opencv.spring.boot.nd4j.store` | `INDArrayStoreProvider`, `INDArrayLocalCacheStoreProvider`, `INDArrayInfo` |
| `org.bytedeco.opencv.spring.boot.dl4j` | `FaceNetSmallV2Model` |

<a id="5-installation"></a>
## 5. Installation

Maven:

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>opencv-sdk</artifactId>
    <version>2.0.x.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle:

```groovy
implementation 'io.github.easy4j:opencv-sdk:2.0.x.x.20260630-SNAPSHOT'
```

Snapshot builds require an enabled snapshot repository (Aliyun Maven snapshot repository per `distributionManagement` in `pom.xml`). Native binaries are resolved per-platform by JavaCPP (`opencv-platform`).

<a id="6-quick-start"></a>
## 6. Quick Start

```java
import org.bytedeco.opencv.global.objdetect.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.spring.boot.OpenCVFaceRecognitionTemplate;

// 1. Load a Haar cascade and build the template
CascadeClassifier faceDetector = new CascadeClassifier("haarcascade_frontalface_default.xml");
OpenCVFaceRecognitionTemplate template =
        new OpenCVFaceRecognitionTemplate(faceDetector, "/tmp/opencv-work");

// 2. Detect faces in an image
JSONObject result = template.detect(new File("photo.jpg"));
System.out.println(result.toJSONString());
```

**Expected result**: `detect(...)` returns a fastjson `JSONObject` describing the detection outcome (e.g. count and geometry of detected faces, depending on the image). With an unreadable/missing image the template throws rather than returning an empty result.

<a id="7-configuration"></a>
## 7. Configuration

This is a library configured through constructors and methods — no configuration properties:

| Entry | Configuration surface |
|---|---|
| `OpenCVFaceRecognitionTemplate` | `CascadeClassifier` (detector) + temp directory |
| `Nd4jTemplate` | `ComputationGraph` + `BaseImageLoader` (`ImageLoader` enum) + model input height / width |
| `Nd4jTemplate.setStoreProvider(...)` | Swap the `INDArrayStoreProvider` (default: `INDArrayLocalCacheStoreProvider`) |
| `ImageLoader` | `CIFAR`, `DEFAULT`, `LFW`, `NATIVE` loading strategies |
| `FaceNetSmallV2Model` | `conf()` returns the graph configuration; `init()` builds the graph |

<a id="8-core-usage"></a>
## 8. Core Usage

### 8.1 Face recognition with FaceNet + ND4J

```java
FaceNetSmallV2Model model = new FaceNetSmallV2Model();
ComputationGraph graph = model.init();

// height/width must match the model input (FaceNet is typically 160x160)
Nd4jTemplate nd4j = new Nd4jTemplate(graph, ImageLoader.DEFAULT, 160, 160);

nd4j.faceNew("group1", "member1", new File("face-a.jpg"));
String match = nd4j.search("group1", "member1");
System.out.println(match);
```

### 8.2 Image -> ND4J matrix

```java
INDArray matrix = nd4j.asMatrix(new File("photo.jpg"));   // also InputStream / byte[] / path
```

<a id="9-testing--build"></a>
## 9. Testing & Build

```bash
mvn clean verify
```

- JaCoCo runs `prepare-agent`, `report` and `check` on the `verify` phase with a **90% line-coverage** rule (`haltOnFailure=false`) — currently there are no test sources in this branch, so the rule is not yet binding; the build still produces the coverage report.
- The module ships the Maven wrapper (`mvnw`) for reproducible builds.
- Release packaging (`mvn -Prelease deploy`) attaches sources and javadoc jars, GPG-signs artifacts and is wired for Sonatype Central Publishing; plain `mvn deploy` routes SNAPSHOT/release artifacts to the Aliyun Maven repository per `distributionManagement`.

<a id="10-versioning--branches"></a>
## 10. Versioning & Branches

| Branch | Version pattern | JDK | Maintenance policy |
|---|---|---|---|
| `feature/1.0.x` (this branch) | `1.0.x.*` | 8 | Compatibility fixes and JDK-8-safe dependency upgrades only |
| `feature/2.0.x` | `2.0.x.*` | 17 | JDK 17 line |
| `feature/3.0.x` | `3.0.x.*` | 21 | JDK 21 line |

<a id="11-contributing--license"></a>
## 11. Contributing & License

Contributions are welcome. Run `mvn clean verify` before opening a pull request and describe compatibility, testing and migration impact. This project is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt) (declared in `pom.xml`; the worktree does not yet carry a `LICENSE` file).
