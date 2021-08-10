建议按照本章的内容一步一步地编写代码并执行。

# 1. 编写 POM
就像 Make 的 `Makefile`，Ant 的 `build.xml` 一样，Maven 项目的核心是 `pom.xml`。

我们现在先为 Hello World 项目编写一个最简单的 pom.xml。
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.juvenxu.mvnbook</groupId>
  <artifactId>hello-world</artifactId>
  <version>1.0-SNAPSHOT</version>
  <name>Maven Hello World Project</name>
</project>
```
代码第一行是 xml 头，指定了该xml文档的版本和编码方式。
紧接着是`project`元素，`project`是pom.xml的根元素，它申明了一些 POM 相关的命名空间及 xsd 元素，虽然这些属性不是必须的，但是使用这些属性能够让第三方工具（如IDE中的XML编辑器）帮助我们快速编辑 POM 。

根元素下的第一个元素`modelVersion`指定了当前 POM 模型的版本，对于 Maven2 及 Maven3 来说，它只能是 **4.0.0**。
这段代码中**最重要**的是包含`groupId`、`artifactId` 和 `version` 的三行。这三个元素定义了一个项目**基本的坐标**，在 Maven 的世界，任何的 jar、pom 或者 war 都是以基于这些基本的坐标进行区分的。

`groupId` 定义了项目属于哪个组，这个组往往和项目所在的组织或公司存在关联。 譬如在 apache 上建立一个名为 myapp 的项目，那么`groupId`就应该是`com.apache.myapp`。

`artifactId` 定义了当前 Maven 项目在组中唯一的 ID，我们为这个 Hello World 项目定义为`hello-world`。

顾名思义，`version` 指定了 Hello World 项目当前的版本——`1.0-SNAPSHOT`。SNAPSHOT意为快照，说明该项目还处于开发中，是不稳定的版本。随着项目的发展，`version`会不断更新。

最后一个`name`元素声明了一个对于用户更为友好的项目名称，虽然这不是必须的，但还是推荐为每个 POM 声明 `name`，以方便信息交流。

没有任何实际的 Java 代码，我们就能够定义一个 Maven 项目的 POM，这体现了 Maven 的一大优点，它能让项目对象模型最大程度地与实际代码相独立，我们可以称之为解耦，或者正交性。 这在很大程度上避免了 Java 代码和 POM 代码的相互影响。

# 2. 编写主代码
项目**主代码**和**测试代码**不同，项目的主代码会被打包到最终的构件中（如jar），而测试代码只在运行测试时用到，不会被打包。
默认情况下，Maven 假设项目主代码位于 `src/main/java` 目录，我们遵循 Maven 的约定，创建该目录，然后在该目录下创建文件`com/juvenxu/mvnbook/helloworld/HelloWorld.java` ：
```shell
$ mkdir -pv src/main/java
$ mkdir -pv src/main/java/com/juvenxu/mvbook/helloworld/
```

Java 代码如下：
```java
package com.juvenxu.mvnbook.helloworld;

public class HelloWorld {
  public String sayHello() {
    return "Hello Maven!";
  }

  public static void main(String[] args) {
    System.out.print(new HelloWorld().sayHello());
  }
}
```
关于该 Java diamante有两点需要注意。 首先，在绝大多数情况下，应该把项目主代码放到`src/main/java` 目录下，而无需额外的配置，Maven 会自动搜寻该目录找到项目主代码。 其次，该 Java 类的包名是 `com.juvenxu.mvnbook.helloworld`，这与之前 POM 中定义的 `groupId` 和 `artifactId` 相吻合。一般来说，项目中 Java 类的包都应该基于项目的`groupId`和`artifactId`，这样更加清晰，更加符合逻辑，这也方便搜索构件或者 Java 类。

代码编写完毕后，使用 Maven 进行编译，在项目根目录下运行命令 `mvn clean compile` 会得到如下输出：
```maven
$ mvn clean compile
[INFO] Scanning for projects...
[INFO] 
[INFO] ------------------< com.juvenxu.mvnbook:hello-world >-------------------
[INFO] Building Maven Hello World Project 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ hello-world ---
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ hello-world ---
[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory /Users/****/mvn_project/hello-world/src/main/resources
[INFO] 阿里云Maven中央仓库为阿里云云效提供的公共代理仓库，云效也提供了免费、可靠的Maven私有仓库Packages，欢迎您体验使用。https://www.aliyun.com/product/yunxiao/packages?channel=pd_maven_download
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ hello-world ---
[INFO] Changes detected - recompiling the module!
[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
[INFO] Compiling 1 source file to /Users/****/mvn_project/hello-world/target/classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.787 s
[INFO] Finished at: 2021-08-07T01:49:15+08:00
[INFO] ------------------------------------------------------------------------
```

`clean` 告诉 Maven 清理输出目录`target/`，`compile` 告诉 Maven 编译项目主代码，从输出中看到Maven首先执行了`clean`任务，删除`target/`目录。默认情况下，Maven构建的所有输出都在`target/`目录中； 接着执行`resources`任务； 最后执行`compile`任务，将项目主代码编译至`target/classes`目录。
```shell
$ tree target/
target/
├── classes
│   └── com
│       └── juvenxu
│           └── mvnbook
│               └── helloworld
│                   └── HelloWorld.class
└── maven-status
    └── maven-compiler-plugin
        └── compile
            └── default-compile
                ├── createdFiles.lst
                └── inputFiles.lst

9 directories, 3 files
```
上文提到的`clean`、`resources` 和 `compile` 对应了一些 Maven 插件及插件目标。后续会详细介绍 Maven 插件及编写方法。

至此，Maven 在没有任何额外的配置的情况下就执行了项目的清理和编译任务。 
接下来，编写一些单元测试代码并让 Maven 执行自动化测试。

# 3. 编写测试代码
为了使项目结构保持清晰，主代码与测试代码应该分别位于独立的目录中。 上面讲到过 Maven 项目中默认的测试代码目录是 `src/test/java`。 因此，我们先创建该目录。
```shell
$ mkdir -pv src/test/java
```
> 在 Java 世界里，由 *Kent Beck* 和 *Erich Gamma* 建立的 JUnit 是事实上的单元测试标准。

要使用 JUnit，首先需要为 Hello World 项目添加一个 JUnit 依赖，修改项目的 POM 如下：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.juvenxu.mvnbook</groupId>
  <artifactId>hello-world</artifactId>
  <version>1.0-SNAPSHOT</version>
  <name>Maven Hello World Project</name>
  
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.7</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
  
</project>
```
代码中添加了`dependencies`元素，该元素下可以包含多个`dependency`元素以声明项目的依赖。 

这里添加了一个依赖 —— `groupId`是`junit`，`artifactId`是`junit`，`version`是`4.7`。 前面提到`groupId`、`artifactId` 和 `verison`是任何一个 Maven 项目最基本的坐标，`JUnit` 也不例外，有了这段声明，Maven 就能够自动下载`junit-4.7.jar`。

上述 POM 代码中还有一个值为 test 的元素`scope`，`scope`为依赖范围，若依赖范围为`test`则表示该依赖只对测试有效。 换句话说，测试代码中的`import JUnit` 代码是没有问题的，但是如果在主代码中使用`import JUnit`代码，就会造成编译错误。 **如果不声明依赖范围，那么默认值就是`compile`，表示该依赖对主代码和测试代码都有效。**

配置了测试依赖，接着就可以编写测试类。 回顾一下前面的 HelloWorld 类，现在要测试该类的 `sayHello()` 方法，检查其返回值是否为`Hello Maven`。 在`src/test/java` 目录下创建文件，内容如下：
```java
package com.juvenxu.mvnbook.helloworld;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class HelloWorldTest {
  @Test
  public void testSayHello() {
    HelloWorld helloWorld = new HelloWorld();
    String result = helloWorld.sayHello();
    assertEquals("Hello Maven!", result);
  }
}
```
一个典型的单元测试包含三个步骤：
①准备测试类及数据；
②执行要测试的行为；
③检查结果。
上述样例首先初始化了一个要测试的 HelloWorld 实例，接着执行该实例的`sayHello()`方法并保存结果到`result`变量中，最后使用`JUnit`框架的`Assert`类检查结果是否为我们期望的`Hello Maven!`。 

在 JUnit3 中，约定所需要执行测试的的方法都以 `test` 开头，这里使用了`JUnit4`，但仍然遵循这一约定。 **在 JUnit4 中，需要执行的测试方法都应该以`@Test` 进行标注。**

测试用例编写完毕之后就可以调用`Maven` 执行测试。运行`mvn clean test`:
```maven
$ mvn clean test
[INFO] Scanning for projects...
[INFO] 
[INFO] ------------------< com.juvenxu.mvnbook:hello-world >-------------------
[INFO] Building Maven Hello World Project 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ hello-world ---
[INFO] Deleting /Users/****/mvn_project/hello-world/target
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ hello-world ---
[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory /Users/EricRen/mvn_project/hello-world/src/main/resources
[INFO] 阿里云Maven中央仓库为阿里云云效提供的公共代理仓库，云效也提供了免费、可靠的Maven私有仓库Packages，欢迎您体验使用。https://www.aliyun.com/product/yunxiao/packages?channel=pd_maven_download
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ hello-world ---
[INFO] Changes detected - recompiling the module!
[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
[INFO] Compiling 1 source file to /Users/EricRen/mvn_project/hello-world/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ hello-world ---
[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory /Users/****/mvn_project/hello-world/src/test/resources
[INFO] 阿里云Maven中央仓库为阿里云云效提供的公共代理仓库，云效也提供了免费、可靠的Maven私有仓库Packages，欢迎您体验使用。https://www.aliyun.com/product/yunxiao/packages?channel=pd_maven_download
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ hello-world ---
[INFO] Changes detected - recompiling the module!
[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
[INFO] Compiling 1 source file to /Users/****/mvn_project/hello-world/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ hello-world ---
[INFO] Surefire report directory: /Users/****/mvn_project/hello-world/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.juvenxu.mvnbook.helloworld.HelloWorldTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.075 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.740 s
[INFO] Finished at: 2021-08-07T21:32:35+08:00
[INFO] ------------------------------------------------------------------------
```
命令行输入的是 `mvn clean test`，而Maven实际执行的可不止这两个任务，还有`clean`、`resources`、`compile`、`testResources`、`testCompile`以及`test`。
**暂时需要了解的是，在 Maven 执行测试(test)之前，它会先自动执行项目主资源处理、主代码编译、测试资源处理、测试代码编译等工作，这是 Maven 生命周期的一个特性。**

首次运行 JUnit 测试指令，Maven 会从中央仓库下载 junit-4.7.pom 和 junit-4.7.jar 这两个文件到本地仓库，供所有 Maven 项目使用。

我们看到`testCompile`任务执行成功了，测试代码通过编译之后在 `target/test-classes` 下生成了二进制文件，紧接着`maven-surefire-plugin:2.12.4:test`任务运行测试，`maven-surefire-plugin`是Maven中负责执行测试的插件，这里它运行测试用例`Running com.juvenxu.mvnbook.helloworld.HelloWorldTest`，并且输出测试报告，显示一共运行了多少测试，失败了多少，出错了多少，跳过了多少。 显然，我们的测试通过了。

# 4. 打包和运行
将项目进行编译、测试之后，下一个重要步骤就是打包（package）。`Hello World`的POM中没有指定打包类型，使用默认打包类型`jar`。 简单地执行命令`mvn clean package` 进行打包，可以看到如下输出：
```maven
......
 T E S T S
-------------------------------------------------------
Running com.juvenxu.mvnbook.helloworld.HelloWorldTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.067 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ hello-world ---
[INFO] Building jar: /Users/****/mvn_project/hello-world/target/hello-world-1.0-SNAPSHOT.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
.......
```
类似的，Maven 会在打包之前进行编译、测试等操作。 这里看到 jar：jar 任务负责打包，实际上就是 maven-jar-plugin 插件的 jar 目标将项目主代码打包成一个名为`hello-world-1.0-SNAPSHOT.jar` 的文件。该文件也位于`target/` 输出目录中，它是根据`artifact-version.jar` 规则进行命名的。 如有需要，还可以使用`finalName` 来自定义该文件的名称，这里暂且不展开，后面会详细解释。

至此，我们得到了项目的输出，如果有需要的话，就可以复制这个jar文件到其他项目的`Classpath`中从而使用`HelloWorld`类。

但是，如何才能让其他的 Maven 项目直接引用这个 jar 呢？ 还需要一个安全的步骤，执行`mvn clean install`：
```maven
$ mvn clean install
[INFO] Scanning for projects...
[INFO] 
[INFO] ------------------< com.juvenxu.mvnbook:hello-world >-------------------
[INFO] Building Maven Hello World Project 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ hello-world ---
[INFO] Deleting /Users/****/mvn_project/hello-world/target
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ hello-world ---
[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory /Users/****/mvn_project/hello-world/src/main/resources
[INFO] 阿里云Maven中央仓库为阿里云云效提供的公共代理仓库，云效也提供了免费、可靠的Maven私有仓库Packages，欢迎您体验使用。https://www.aliyun.com/product/yunxiao/packages?channel=pd_maven_download
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ hello-world ---
[INFO] Changes detected - recompiling the module!
[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
[INFO] Compiling 1 source file to /Users/****/mvn_project/hello-world/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ hello-world ---
[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory /Users/****/mvn_project/hello-world/src/test/resources
[INFO] 阿里云Maven中央仓库为阿里云云效提供的公共代理仓库，云效也提供了免费、可靠的Maven私有仓库Packages，欢迎您体验使用。https://www.aliyun.com/product/yunxiao/packages?channel=pd_maven_download
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ hello-world ---
[INFO] Changes detected - recompiling the module!
[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
[INFO] Compiling 1 source file to /Users/EricRen/mvn_project/hello-world/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ hello-world ---
[INFO] Surefire report directory: /Users/****/mvn_project/hello-world/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.juvenxu.mvnbook.helloworld.HelloWorldTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.071 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ hello-world ---
[INFO] Building jar: /Users/****/mvn_project/hello-world/target/hello-world-1.0-SNAPSHOT.jar
[INFO] 
[INFO] --- maven-install-plugin:2.4:install (default-install) @ hello-world ---
[INFO] Installing /Users/****/mvn_project/hello-world/target/hello-world-1.0-SNAPSHOT.jar to /Users/****/software/maven_repository/com/juvenxu/mvnbook/hello-world/1.0-SNAPSHOT/hello-world-1.0-SNAPSHOT.jar
[INFO] Installing /Users/****/mvn_project/hello-world/pom.xml to /Users/EricRen/software/maven_repository/com/juvenxu/mvnbook/hello-world/1.0-SNAPSHOT/hello-world-1.0-SNAPSHOT.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
......
```
至此，我们在打包工作之后，又执行了安装的任务`install`。从输出可以看到该任务将项目输出的 jar 安装到了 Maven 本地仓库中，可以打开相应的文件夹看到`Hello World`项目的 pom 和 jar。 

之前讲述 **JUnit** 的 POM 及 jar 的下载的时候，我们说只有构件被下载到本地仓库后，才能由所有 Maven 项目使用，这里是同样的道理，只有将 `Hello World` 的构件安装到本地仓库之后，其他 Maven 项目才能使用它。

我们已经体验了 Maven 最主要的命令：`mvn clean compile`、`mvn clean test`、`mvn clean package`、`mvn clean install`。 
**执行 test 之前是会先执行`compile`的，执行`package`之前是会先执行`test`的，而类似的，`install`之前会执行`package`的。**

到目前为止，还没有运行 `Hello World`项目，不要忘了`HelloWorld`类时有一个`main`方法的。 默认打包生成的 jar 是不能够直接运行的，因为带有`main`方法的类信息不会添加到`manifest`中（打开jar文件中的`META-INF/MANIFEST.MF`文件，将无法看到`Main-Class`行）。 为了生成可执行的 jar 文件，需要借助`maven-shade-plugin`，配置该插件的项目 POM 元素描述如下：
```xml
........
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>1.2.1</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>shade</goal>
            </goals>
            <configuration>
              <transformers>
                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                  <mainClass>com.juvenxu.mvnbook.helloworld.HelloWorld</mainClass>
                </transformer>
              </transformers>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
..........
```

`plugin`元素在POM中相对位置应该在`<project>`下的`<build>`下的`<plugins>`下面。

我们配置了`mainClass`为`com.juvenxu.mvnbook.helloworld.HelloWorld`，项目在打包时会将该信息放到`MANIFEST`中。 

现在执行`mvn clean install`，待构建完成之后打开`target/`目录，可以看到`hello-world-1.0-SNAPSHOT.jar` 和`original-hello-world-1.0-SNAPSHOT.jar`，前者是带有`Main-Class`信息的**可运行`jar`** ，后者是原始的`jar`，打开`hello-world-1.0-SNAPSHOT.jar` 的`META-INF/MANIFEST.MF`，可以看到它包含这样一行信息：
```text
Main-Class: com.juvenxu.mvnbook.helloworld.HelloWorld
```
现在，在项目根目录中指定该`jar`文件：
```shell
$ java -jar target/hello-world-1.0-SNAPSHOT.jar 
Hello Maven!
```
这正是我们所期望的。

# 5. 使用 Archetype 生成项目骨架
Hello World 项目中有一些 Maven 的约定：在项目的根目录中放置 pom.xml，在`src/main/java`目录中存放项目的主代码，在`src/test/java`中放置项目的测试代码。

我们称这些基本的目录结构和 pom.xml 文件内容称为项目的骨架。
Maven 提供了 `Archetype` 以帮助我们快速勾勒出项目骨架。

```shell
### Maven2
$ mvn org.apache.maven.plugins:maven-archetype-plugin:2.0-alpha-5:generate

### Maven3
$ mvn archetype:generate
```
我们实际上是在运行插件`maven-archetype-plugin`，注意冒号的分隔，其格式为`groupId:artifactId:version:goal`，`org.apache.maven.plugins`是 maven官方插件的`groupId`，`maven-archetype-plugin`是Archetype插件的`artifactId`，`2.0-alpha-5`是目前该插件最新的稳定版，`generate`是要使用的插件目标。

紧接着会看到一段长长的输出，有很多可用的`Archetype`供选择，包括著名的`Appfuse`项目的`Archetype`、JPA项目的`Archetype`等。
```maven
$ mvn archetype:generate
[INFO] Scanning for projects...
[INFO] 
[INFO] ------------------< org.apache.maven:standalone-pom >-------------------
[INFO] Building Maven Stub Project (No POM) 1
[INFO] --------------------------------[ pom ]---------------------------------
[INFO] 
[INFO] >>> maven-archetype-plugin:3.2.0:generate (default-cli) > generate-sources @ standalone-pom >>>
[INFO] 
[INFO] <<< maven-archetype-plugin:3.2.0:generate (default-cli) < generate-sources @ standalone-pom <<<
[INFO] 
[INFO] 
[INFO] --- maven-archetype-plugin:3.2.0:generate (default-cli) @ standalone-pom ---
[INFO] Generating project in Interactive mode
[WARNING] No archetype found in remote catalog. Defaulting to internal catalog
..............
Define value for property 'groupId': com.juvenxu.mvnbook
Define value for property 'artifactId': hello-world
Define value for property 'version' 1.0-SNAPSHOT: : 
Define value for property 'package' com.juvenxu.mvnbook: : com.juvenxu.mvnbook.helloworld
Confirm properties configuration:
groupId: com.juvenxu.mvnbook
artifactId: hello-world
version: 1.0-SNAPSHOT
package: com.juvenxu.mvnbook.helloworld
 Y: : Y
```
Archetype 插件根据我们提供的信息创建项目骨架。 

在当前目录下，Archetype 插件会创建一个名为`hello-world`（我们定义的artifactId）的子目录，从中可以看到项目的基本结构：基本的 pom.xml 已经被创建，里面包含了必要的信息及一个`junit`依赖；主代码目录`src/main/java`已经被创建，在该目录下还有一个Java类`com.juvenxu.mvnbook.helloworld.App`，注意这里使用到了刚才定义的包名，而这个类也仅仅只有一个简单的输出`Hello World!`的`main`方法； 测试代码目录`src/test/java`也被创建好了，并且包含了一个测试用例`com.juvenxu.mvnbook.helloworld.AppTest`。

Archetype 可以帮助我们迅速地构建起项目的骨架，在前面的例子中，我们完全可以在 Archetype 生成的骨架的基础上开发 Hello World 项目以节省大量时间。

此外，这里仅仅是看到了一个最简单的 Archetype，如果有许多项目拥有类似的自定义项目结构以及配置文件，则完全可以一劳永逸的开发自己的 Archetype，然后在这些项目中使用自定义的 Archetype 来快速生成项目骨架。

