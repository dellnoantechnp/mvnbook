除了坐标、依赖以及仓库之外，Maven 另外两个核心概念是**生命周期**和**插件**。 在有关 Maven 的日常使用中，命令行的输入往往就对应了生命周期，如`mvn package` 就表示执行默认生命周期阶段`package`。 Maven 的生命周期是抽象的，其实际行为都由插件来完成，如`package`阶段的任务可能就会由`maven-jar-plugin`完成。  

生命周期和插件两者协同工作，密不可分，本章对它们进行深入介绍。

# 1. 何为生命周期
Maven 的生命周期就是为了对所有的构建过程进行抽象和统一。  

Maven 从大量项目和构建工具中学习和反思，然后总结了一套高度完善的、易扩展的生命周期。 这个生命周期包含了项目的清理、初始化、编译、测试、打包、集成测试、验证、部署和站点生成等几乎所有构建步骤。也就是说，几乎所有项目的构件，都能映射到这样一个生命周期上。

Maven 的生命周期是抽象的，这意味着生命周期本身不做任何实际的工作，在 Maven 的实际中，实际的任务都交由插件来完成。

模拟生命周期的模板方法抽象类：
```java
package com.juvenxu.mvnbook.template.method;

public abstract class AbstractBuild {
  public void build() {
    initialize();
    compile();
    test();
    packagee();
    integrationTest();
    deploy();
  }
  
  protected abstract void initialize();
  
  protected abstract void compile();
  
  protected abstract void test();
  
  protected abstract void packagee();
  
  protected abstract void integrationTest();
  
  protected abstract void deploy();
}
```
这段代码非常简单，`build()`快放阀定义了整个构建的过程，依次**初始化**、**编译**、**测试**、**打包**、**集成测试**和**部署**，但是这个类中没有具体实现初始化、编译、测试等行为，它们都交由子类去实现。

生命周期抽象了构建的各个步骤，定义了它们的次序，为了测试又写一堆代码，那不就成了大家在重复发明轮子吗 ？  Maven 当然必须考虑这一点，因此它设计了插件机制。 每个构建步骤都可以绑定一个或者多个插件行为，而且 Maven 为大多数构建步骤编写并绑定了默认插件。  例如，针对编译的插件有`maven-compiler-plugin`，针对测试的插件有`maven-surefire-plugin`等。  当用户有特殊需要的时候，也可配置插件定制构建行为，甚至自己编写插件。

![1.png](https://raw.githubusercontent.com/dellnoantechnp/mvnbook/main/Chapter7/.pic/1.png)

Maven 定义的生命周期和插件机制一方面保证了所有 Maven 项目有一致的构件标准，另一方面又通过默认插件简化和稳定了实际项目的构建。  此外，该机制还提供了足够的扩展空间，用户可以通过配置现有插件或者自行编写插件来自定义构建行为。

# 2. 生命周期详解

## 2.1 三套生命周期
初学者往往会以为 Maven 的生命周期是一个整体，其实不然，Maven 拥有三套相互独立的生命周期，它们分别为 `clean`、`default` 和 `site`。 `clean` 生命周期的目的是清理项目，`default` 生命周期的目的是构建项目，而`site`生命周期的目的是建立项目站点。

每个生命周期包含一些阶段`(phase)`，这些阶段是有顺序的，而且后面的阶段依赖于前面的阶段，用户和 Maven 最直接的交互方式就是调用这些生命周期阶段。 以`clean`生命周期为例，它包含的阶段有`pre-clean`、`clean` 和 `post-clean`。 当用户调用`pre-clean`的时候，只有`pre-clean`阶段得以执行；调用`clean`的时候，`pre-clean`和`clean`阶段会得以顺序执行；当调用`post-clean`的时候，`pre-clean`、`clean` 和 `post-clean` 会得以顺序执行。

较之于生命周期阶段的前后依赖关系，三套生命周期本身是相互独立的，用户可以仅仅调用`clean`生命周期的某个阶段，或者仅仅调用`default`生命周期的某个阶段，而不会对其他生命周期产生任何影响。  例如，当用户调用`clean`生命周期的`clean`阶段的时候，不会触发`default`生命周期的任何阶段，反之亦然，当用户调用`default`生命周期的`compile`阶段的时候，也不会触发`clean`生命周期的任何阶段。

## 2.2 clean 生命周期
`clean`生命周期的目的是清理项目，它包含三个阶段：
1). **`pre-clean`** 执行一些清理前需要完成的工作。
2). **`clean`** 清理上一次构建生成的文件。
3). **`post-clean`** 执行一些清理后需要完成的工作。

## 2.3 default 生命周期
`default` 生命周期定义了真正构建时所需要执行的所有步骤，它是所有 生命周期中最核心的部分，其包含的阶段如下，这里只对重要的部分进行解释：

* validate
* initialize
* generate-sources
* **`process-sources`** 处理项目主资源文件。一般来说，是对`src/main/resources` 目录的内容进行变量替换等工作，复制到项目输出的主`classpath`目录中。
* generate-resources
* process-resources
* **`compile`** 编译项目的主源码。一般来说，是编译`src/main/java` 目录下的 Java 文件至输出的主 `classpath` 目录中。
* process-classes
* generate-test-sources
* **`process-test-sources`**  处理项目测试资源文件。 一般来说，是对`src/test/resources` 目录的内容进行变量替换等工作后，复制到项目输出的测试`classpath`目录中。
* generate-test-resources
* process-test-resources
* **`test-compile`** 编译项目的测试代码。一般来说，是编译`src/test/java` 目录下的 Java 文件至项目输出的测试`classpath`目录中。
* process-test-classes
* **`test`** 使用单元测试框架运行测试，测试代码不会被打包或部署。
* prepare-package
* **`package`** 接受编译好的代码，打包成可发布的格式，如 JAR。
* pre-integration-test
* integration-test
* post-integration-test
* verify
* **`install`** 将包安装到Maven本地仓库，供本地其他Maven项目使用
* **`deploy`** 将最终的包复制到远程仓库，供其他开发人员和Maven项目使用。

对于上述未加解释的阶段，可以在官方文档了解进一步的详细解释，参考 [http://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html](http://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)。

## 2.4 site 生命周期
`site` 生命周期的目的是建立和发布项目站点，Maven 能够基于 POM 所包含的信息，自动生成一个友好的站点，方便团队交流和发布项目信息。 该生命周期包含如下阶段：

* **`pre-site`** 执行一些在生成项目站点之前需要完成的工作。
* **`site`** 生成项目站点文档。
* **`post-site`** 执行一些在生成项目占地案之后需要完成的工作。
* **`site-deploy`** 将生成的项目站点发布到服务器上。

## 2.5 命令行与生命周期
从命令行执行 Maven 任务的最主要方式就是调用 Maven 的生命周期阶段。需要注意的是，各个生命周期是相互独立的，而一个生命周期的阶段是右前后依赖关系的。 

下面以一些常见的 Maven 命令为例，解释其执行的生命周期阶段：

* `$mvn clean`：该命令调用`clean`生命周期的`clean`阶段。 实际执行的阶段为`clean`生命周期的`pre-clean`和`clean`阶段。
* `$mvn test`：该命令调用`default`生命周期的`test`阶段。 实际执行的阶段为`default`生命周期的`validate`、`initialize`等，直到`test`的所有阶段。这也解释了为什么在执行测试的时候，项目的代码能够自动得以编译。
* `$mvn clean install`：该命令调用`clean`生命周期的`clean`阶段和`default`生命周期的`install`阶段。实际执行的阶段为`clean`生命周期的`pre-clean`、`clean`阶段，以及`default`生命周期的从`validate`至`install`的所有阶段。 该命令结合了两个生命周期，在执行真正的项目构建之前清理项目是一个很好的实践。
* `$mvn clean deploy site-deploy`：该命令调用`clean`生命周期的`clean`阶段、`default`生命周期的`deploy`阶段，以及`site`生命周期的`site-deploy`阶段。 实际执行的阶段为`clean`生命周期的`pre-clean`、`clean`阶段，`default`生命周期的所有阶段，以及`site`生命周期的所有阶段。  该命令结合了 Maven 所有三个生命周期，且 `deploy` 为 `default` 生命周期的最后一个阶段，`site-deploy` 为 `site` 生命周期的最后一个阶段。

由于 Maven 中主要的生命周期阶段并不多，而常见的 Maven 命令实际都是基于这些阶段简单组合而成的，因此只要对 Maven 生命周期有一个基本的理解，读者就可以正确而熟练地使用 Maven 命令。

# 3. 插件目标
我们知道，Maven 的核心仅仅定义了抽象的生命周期，具体的任务是由插件完成的，插件以独立的构件形式存在，因此，Maven 核心的分发包只有不到 3MB 的大小，Maven 会在需要的时候下载并使用插件。

对于插件本身，为了能复用代码，它往往能够完成多个任务。 例如`maven-dependency-plugin`，它能够基于项目依赖做很多事情。它能够分析项目依赖，帮助找出潜在的无用依赖；它能够列出项目的依赖树，帮助分析依赖来源；它能够列出项目所有已解析的依赖，等等。   为每个这样的功能编写一个独立的插件显然是不可取的，因为这些任务背后有很多可以复用的代码，因此，这些功能聚集在一个插件里，每个功能就是一个插件目标。

`maven-dependency-plugin` 有十多个目标，每个目标对应了一个功能，上述提到的几个功能分别