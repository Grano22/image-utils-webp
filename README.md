<div align="center">

# Java Image Utils Webp "image-utils-webp"

</div>

## 💡 Motivation

Convert images to webp format fully in-memory, using Java.

## 🚀 Installation

Install via MVN:

```bash
mvn dependency:get -Dartifact=io.github.grano22:image-utils-webp:1.0.0
```

Or add manually to your project:

```xml
<dependency>
    <groupId>io.github.grano22</groupId>
    <artifactId>image-utils-webp</artifactId>
    <version>1.0.0</version>
</dependency>
```

## ✨ Features

### 🎯 Converting images to webp directly in memory—with bytes[]

```java
import io.github.grano22.WebPConverter;
import io.github.grano22.core.WebConverterConfig;

var converter = new WebPConverter(new WebConverterConfig(), new WslCommandCWebPBuilder(cWebPLinuxBinaryPath));
byte[] sourceImageBytes /* = ... */;
byte[] webPImage = converter.convert(targetImageBytes);
```

### 🎯 Converting images to webp using textual paths

```java
import io.github.grano22.WebPConverter;
import io.github.grano22.core.WebConverterConfig;

public void run() {
    var converter = new WebPConverter(new WebConverterConfig(), new WslCommandCWebPBuilder(cWebPLinuxBinaryPath));

    converter.convert("./img1.png", "./img1.webp");
}
```

---

<div align="center">
Made with ❤️ for the Java community
</div>