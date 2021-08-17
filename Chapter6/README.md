坐标和依赖是任何一个构件在 Maven 世界中的逻辑表示方式； 而构件的物理表示方式是文件，Maven 通过仓库来同一管理这些文件。本问将详细介绍 Maven 仓库，在了解了 Maven 如何使用仓库之后，将能够更高效地使用 Maven 。

# 1. 何为 Maven 仓库
在 Maven 世界中，任何一个依赖、插件或者项目构件的输出，都可以称为**构件**。

得益于坐标机制，任何 Maven 项目使用任何一个构件的方式都是完全相同的。 再次基础上，Maven 可以在某个位置统一存储所有 Maven 项目共享的构件，这个统一的位置就是**仓库**。 所以只需要声明这些依赖的坐标，在需要的时候，Maven 会自动根据坐标找到仓库中的构件，并使用它们。

为了实现重用，项目构件完毕后生成的构件也可以安装或者部署到仓库中，供其他项目使用。

# 2. 仓库的布局
任何一个构件都有其唯一的坐标，根据这个坐标可以定义其在仓库中唯一存储路径，这便是 Maven 的仓库布局方式。

> 例如， log4j:log4j:1.2.15 这一依赖，其对应仓库路径为 `log4j/log4j/1.2.15/log4j-1.2.15.jar`，可以观察到，该路径与坐标的大致对应关系为 `groupId/artifactId/version/artifactId-version.packaging`。

下面看一段 Maven 的源码，并结合具体的示例来理解 Maven 仓库的布局方式：
```java
private static final char PATH_SEPARATOR='/';
private static final char GROUP_SEPARATOR='.';
private static final char ARTIFACT_SEPARATOR='-';

public String pathOf(Artifact artifact) {
  ArtifactHandler artifactHandler = artifact.getArtifactHandler();
  StringBuilder path = new StringBuilder(128);
  path.append(formatAdDirector(artifact.getGroupId())).append(PATH_SEPARATOR);
  path.append(artifact.getArtifactId()).append(PATH_SEPARATOR);
  path.append(artifact.getBaseVersion()).append(PATH_SEPARATOR);
  path.append(artifact.getArtifactId()).append(ARTIFACT_SEPARATOR).append(artifact.getVersion());
  
  if (artifact.hasClassifier()) {
    path.append(ARTIFACT_SEPARATOR).append(artifact.getClassifier());
  }
  
  if (artifactHandler.getExtension() != null && artifactHandler.getExtension().length() > 0) {
    path.append(GROUP_SEPARATOR).append(artifactHandler.getExtension());
  }
  
  return path.toString();
}

private String formatAsDirector(String directory) {
  return directory.replace(GROUP_SEPARATOR, PATH_SEPARATOR);
}
```
该 `pathOf()` 方法的目的是根据构件信息生成其在仓库中的路径。 这里根据一个实际的例子来分析路径的生成，考虑遮掩给一个构件：`groupId=org.testng`、`artifactId=testng`、`version=5.8`、`classifier=jdk15`、`packaging=jar`，其对应的路径按如下步骤生成：

1. 基于构件的 `groupId` 准备路径，`formatAsDirectory()` 将 `groupId` 中的句点分隔符转换成路径分隔符。该例中，`groupId:org.testng` 就会被转换成 `org/testng`，之后再加一个路径分隔符斜杠，那么，`org.testng` 就成为了`org/testng/`。
2. 基于构件的`artifactId`准备路径，也就是在前面的基础上加上`artifactId`以及一个路径分隔符。 该例中，`artifactId`为`testng`，那么，在这一步过后，路径就成为了`org/testng/testng/`。
3. 使用版本信息。在前面的基础上加上`version`和路径分隔符。 该例中版本是 5.8，那么路径就成为了`org/testng/testng/5.8`。
4. 依次加上`artifactId`，构件分隔符连字号，以及`version`，于是构件的路径就变成了`org/testng/testng/5.8/testng-5.8`。 大家可能会注意到，这里使用了`artifactId.getVersion()`，而上一步用的是`artifactId.getBaseVersion()`，`baseVersion` 主要是为`SNAPSHOT`版本服务的，例如 version 为 `1.0-SNAPSHOT` 的构件，其`baseVersion`就是 1.0。
5. 如果构件有`classifier`，就加上构件分隔符和`classifier`。  该例中构件的`classifier` 是 jdk15，那么路径就变为 `org/testng/testng/5.8/testng-5.8-jdk5`。
6. 检查构件的`extension`，若存在，则加上句点分隔符和`extension`。 从代码可以看到，`extension`是从`artifactHandler` 而非`artifact`获取，`artifactHandler`是由项目的packaging决定的。因此，可以说，`packaging` 决定了构件的扩展名，该例的`packaging` 是 jar，因此最终的路径为`org/testng/testng/5.8/testng-5.8-jdk5.jar`。

到这里，应该感谢 Maven 开源社区，正是由于 Maven 的所有源代码都是开放的，我们才能仔细深入到其内部工作的所有细节。

Maven 仓库是基于简单文件系统存储的，我们也理解了其存储方式，因此当遇到一些与仓库相关的问题时，可以很方便地查找相关文件，方便的定位问题。

# 3. 仓库的分类
对于 Maven 来说，仓库只分为两类：**本地仓库**和**远程仓库**。当 Maven 根据坐标寻找构件的时候，它首先会查看本地仓库，如果本地仓库存在此构件，则直接使用； 如果不存在，或者需要查看是否有更新的构件版本，Maven 就会去远程仓库查找，发现需要的构件之后，下载到本地仓库再使用。 如果本地和远程都没有需要的构件，Maven 就会报错。

在这个最基本分类打基础上，还有必要介绍一些特殊的远程仓库。 中央仓库是 Maven 核心自带的远程仓库，它包含了绝大部分开源的构件。 在默认配置下，当本地仓库没有 Maven 需要的构件的时候，它就会尝试从中央仓库下载。

私服是另一种特殊的远程仓库，为了节省带宽和时间，应该在局域网内假设一个私有的仓库服务器，用其代理所有外部的远程仓库。 内部的项目还能部署到私服上供其他项目使用。

除了中央仓库和私服，还有很多其他公开的远程仓库，常见的有 Java.net Maven库（`http://download.java.net/maven/2/`） 和 JBoss Maven库（http://repository.jboss.com/maven2/）等。
![1.png](https://raw.githubusercontent.com/dellnoantechnp/mvnbook/main/Chapter6/.pic/1.png)

# 3.1 本地仓库
默认情况下，不管是在 Windows 还是在 Linux 上，每个用户在自己的用户目录下都有一个路径名为`.m2/repostory/` 的仓库目录。

用户想要自定义本地仓库目录地址。这时可以编辑`~/.m2/settings.xml`，设置 `localRepostory`元素的值为想要的仓库地址。
```xml
<settings>
  <localRepository>D:\java\repostory\</localRepostory>
</settings>
```
需要注意的是，默认情况下，`~/.m2/settings.xml` 文件是不存在的，用户需要从 Maven 安装目录复制`$M2_HOME/conf/settings.xml` 文件再进行编辑。 本文始终推荐大家不要直接修改全局目录的`settings.xml` 文件，具体原因后续小节会阐述。

一个构件只有在本地仓库中之后，才能由其他Maven项目使用，那么构件如何进入到本地仓库中呢？  最常见的是依赖Maven从远程仓库下载到本地的方式。 还有一种常见的情况是，将本地项目的构件安装到Maven仓库中。
```maven
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ account-email ---
[INFO] Building jar: /Users/****/IdeaProjects/mvnbook/account-email/target/account-email-1.0.0-SNAPSHOT.jar
[INFO] 
[INFO] --- maven-install-plugin:2.4:install (default-install) @ account-email ---
[INFO] Installing /Users/****/IdeaProjects/mvnbook/account-email/target/account-email-1.0.0-SNAPSHOT.jar to /Users/****/software/maven_repository/com/juvenxu/mvnbook/account/account-email/1.0.0-SNAPSHOT/account-email-1.0.0-SNAPSHOT.jar
[INFO] Installing /Users/****/IdeaProjects/mvnbook/account-email/pom.xml to /Users/****/software/maven_repository/com/juvenxu/mvnbook/account/account-email/1.0.0-SNAPSHOT/account-email-1.0.0-SNAPSHOT.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  4.100 s
[INFO] Finished at: 2021-08-15T13:49:48+08:00
[INFO] ------------------------------------------------------------------------
```
从上述输出中可以看到，Maven使用 `install` 插件将该文件复制到本地仓库中，具体的路径根据坐标计算获得。 路径计算逻辑见上一小节。

## 3.2 远程仓库
当 Maven 安装好后，如果不执行任何 Maven 命令，本地仓库目录是不存在的。 当用户输入第一条 Maven 命令之后，本地仓库目录才会被创建，然后根据配置和需要，从远程仓库下载构件至本地仓库。



## 3.3 中央仓库
由于最原始的本地仓库是空的，Maven 必须知道至少一个可用的远程仓库，才能在执行 Maven 命令的时候下载到需要的构件。 中央仓库就是这样一个默认的远程仓库，Maven 的安装文件自带了中央仓库的配置。  读者可以使用解压工具打开jar文件`$M2_HOME/lib/maven-model-builder-3.0.jar` （在Maven2中，jar文件路径类似于`$M2_HOME/lib/maven-2.2.1-uber.jar`），然后访问路径`org/apache/maven/model/pom-4.0.0.xml`，可以看到如下的配置：
```xml
  <repositories>
    <repository>
      <id>central</id>
      <name>Central Repository</name>
      <url>https://repo.maven.apache.org/maven2</url>
      <layout>default</layout>
      <snapshots>
        <enabled>false</enabled>
      </snapshots>
    </repository>
  </repositories>
```
包含这段配置的文件是所有 Maven 项目都会继承的超级 POM。  

这段配置使用`id: central` 对中央仓库进行唯一标识，其名称为Central Repository，它使用 default 仓库布局（对于Maven 1的仓库，需要配置值为 legacy 的 layout）。  最后需要注意的是 `snapshots` 元素，其子元素`enabled`的值为 `false`，表示不从该长裤下载快照版本的构件。

中央仓库包含了这个世界上绝大多数流行的开源 Java 构件，以及源码、作者信息、SCM、信息、许可证信息等，每个月这里都会接受全世界 Java 程序员大概数十亿次的访问，它对全世界 Java 开发者的贡献可见一斑。

## 3.4 私服
私服是一种特殊的远程仓库，它是架设在局域网内的仓库服务，私服代理广域网上的远程仓库，供局域网内的 Maven 用户使用。当 Maven 需要下载构件的时候，它从私服请求，如果私服上不存在该构件，则从外部的远程仓库下载，缓存在私服上之后，再为 Maven 的下载请求提供服务。此外，一些无法从外部仓库下载到的构件也能从本地上传到私服上供大家使用。

私服的好处：

* **节省自己的外网带宽**。对外的重复构件下载得以消除，即降低外网带宽的压力。
* **加速 Maven 构建**。不停的连接请求外部仓库是十分耗时的，但 Maven 的一些内部机制（比如快照更新）要求 Maven 在执行构建的时候不停的检查远程仓库数据。 因此，当配置了很多外部远程仓库的时候，构建的速度会被大大降低。 使用私服可以很好的解决这一问题，当 Maven 只需要检查局域网内私服的数据时，构建的速度便能得到很大程度的提高。
* **部署第三方构件**。Oracle 的 JDBC 驱动由于版权因素不能发布到公共仓库中。建立私服之后，便可以将这些构件部署到这个内部的仓库中，供内部的 Maven 项目使用。
* **提高稳定性，增强控制**。Maven 构件高度依赖于远程仓库，因此，当 Internet 不稳定的时候，Maven 构件也会变得不稳定，甚至无法构建。当使用私服之后，即使暂时没有 Internet 连接，由于私服中已经缓存了大量构件，Maven 也仍然可以正常运行。此外，一些私服软件（如Nexus）还提供了很多额外的功能，如权限管理、RELEASE/SNAPSHOT区分等，管理员可以对仓库进行一些更高级的控制。
* **降低中央仓库的负荷**。使用私服可以避免很多对中央仓库重复的下载。

后面文章会专门介绍如何使用流行的 Maven 私服软件 —— Nexus。

# 4. 远程仓库的配置
在很多情况下，默认的中央仓库无法满足项目的需求，可能项目需要的构件存在于另外一个远程仓库中，这里拿JBoss仓库举例。 这里，可以在 POM 中配置该仓库：
```xml
<project>
  ...
    <repositories>
    <repository>
      <id>jboss</id>
      <name>JBoss Repository</name>
      <url>https://repository.jboss.com/maven2/</url>
      <releases>
        <enabled>true</enabled>
      </release>
      <snapshots>
        <enabled>false</enabled>
      </snapshots>
      <layout>default</layout>
    </repository>
  </repositories>
  ....
</project>
```
在`repositories`元素下，可以使用`repository` 子元素声明一个或者多个远程仓库。  任何一个仓库声明的`id`必须是唯一的，尤其需要注意的是，Maven自带的中央仓库使用的`id`为`central`，如果其他的仓库声明也使用该`id`，就会覆盖中央仓库的配置。 该配置中的url值指向了仓库的地址，一般来说，该地址都基于`http`协议，Maven用户都可以在浏览器中打开仓库地址浏览构件。

该例配置中的`releases`和`snapshots`元素比较重要，它们用来控制 Maven 对于发布版构件和快照版构件的下载。（关于快照版本，在下一节会详细解释。） 这里需要注意的是`enabled`子元素，该例中`releases`的`enabled`值为`true`，表示开启 JBoss 仓库的发布版本下载支持，而`snapshots`的`enabled`值为`false`，表示关闭该仓库的快照版本的下载支持。  

因此，根据该配置，Maven 只会从 JBoss 仓库下载发布版的构件，而不会下载快照版的构件。

> `layout` 元素值`default` 表示仓库的布局是 Maven2 及 Maven3 的默认布局，而不是 Maven1 的布局。

对于`releases`和`snapshots`来说，除了`enabled`，它们还包含另外两个子元素`updatePolicy` 和 `checksumPolicy`：
```xml
<snapshots>
  <enabled>true</enabled>
  <updatePolicy>daily</updatePolicy>
  <checksumPolicy>ignore</checksumPolicy>
</snapshots>
```

下面来解释一下 `updatePolicy` 子元素选项说明：

| updatePolicy元素可选项 | 解释说明 |
| ----   | -------- |
| daily（默认值） | 表示每天检查一次 | 
| never | 从不检查更新 | 
| always | 每次构建都检查更新 |
| interval: X | 每隔 X 分钟检查一次更新（X为任意整数） |

元素`checksumPolicy`用来配置 Maven 检查检验和文件的策略。 当构件被部署到 Maven 仓库时，会同时部署对应的校验和文件。  

下面来解释一下`checksumPolicy`元素的可选值：

| checksumPolicy元素可选项 | 解释说明 | 
| ------------------------ | -------- |
| warn （默认值）         | 在执行构建时输出警告信息 |
| fail                   | 在执行构建遇到校验错误就让构建失败 |
| ignore                 | 忽略校验错误 |


## 4.1 远程仓库的认证
大部分远程仓库无需认证就可以访问，但有时候出于安全方面考虑，我们需要提供认证信息才能访问一些远程仓库。 

**配置认证信息和配置仓库信息不同，仓库信息可以直接配置在 POM 文件中，但是认证信息必须配置在 `settings.xml` 文件中**。这是因为 POM 往往是被提交到代码仓库中供所有成员访问的，而 `settings.xml` 一般只放在本机。因此，在`settings.xml`中配置认证信息更为安全。

假设需要为一个`id`为 `my-proj` 的仓库配置认证信息，编辑`settings.xml`代码如下：
```xml
<settings>
  ...
  <servers>
    <server>
      <id>my-proj</id>
      <username>repo-user</username>
      <password>repo-pwd</password>
    </server>
  </servers>
  ...
</settings>
```
这里关键的是`id`元素，`settings.xml`中的server元素的`id`必须与 POM 中需要认证的 `repository` 元素的`id`完全一致。  换句话说，正是这个id将认证信息与仓库配置联系在了一起。

## 4.2 部署至远程仓库
Maven 除了能对项目进行编译、测试、打包之外，还能将项目生成的构架部署到仓库中。 首先需要编辑项目的`pom.xml`文件。 配置`distributionManagement`元素见下列代码：
```xml
<project>
  ...
  <distributionManagement>
    <repository>
      <id>proj-release</id>
      <name>Proj Release Repository</name>
      <url>http://192.168.1.100/content/repositories/proj-release</url>
    </repository>
    <snapshotRepository>
      <id>proj-snapshots</id>
      <name>Proj Snapshot Repository</name>
      <url>http://192.168.1.100/content/repositories/proj-snapshots</url>
    </snapshotRepository>
  </distributionManagement>
  ...
</project>
```
`distributionManagement`包含`repository`和`snapshotRepository`子元素，前者为发布版本仓库，后者为快照版本仓库。（发布版本和快照版本，在下一小节会详细解释）。这两个元素下都需要配置`id`、`name` 和 `url`，`id`为该远程仓库的唯一标识，`name`是为了方便人阅读，关键的`url`表示该仓库的地址。

往远程仓库部署构件的时候，往往需要认证。配置方式上一节已经详细阐述。不论从远程仓库下载构件，还是部署构件至远程仓库，当需要认证的时候，配置的方式是一样的。

配置正确后，在命令行运行`mvn clean deploy`，Maven 就会将项目构件输出的构件部署到配置对应的远程仓库，如果项目当前的版本时快照版本，则部署到快照版本仓库地址，否则就部署到发布版本仓库地址。

# 5. 快照版本
在 Maven 的世界中，任何一个项目或者构件都必须有自己的版本。版本的值可能是`1.0.0`、`1.3-alpha-4`、`2.0`、`2.1-SNAPSHOT` 或者`2.1-20210814.211474-13`。Maven 在发布到私服的过程中，会自动为构件打上时间戳。 有了该时间戳，Maven 就能随时找到仓库中对应构件指定版本最新的文件。
当项目需要该构件的时候，Maven 会自动从仓库中检查最新构件，当发现有更新时便进行下载。 **默认情况下，Maven 每天检查一次更新（由仓库配置的 `updatePolicy` 控制），用户也可以使用命令行`-U`参数强制让Maven检查更新，如`mvn clean install -U`。**

快照版本只应该在组织内部的项目或模块间依赖使用，因为这时，组织对于这些快照版本的依赖具有完全的理解及控制权。 项目不应该依赖于任何组织外部的快照版本依赖，由于快照版本的不稳定性，这样的依赖会造成潜在的危险。 也就是说，即使项目构建今天是成功的，由于外部的快照版本依赖实际对应的构件随时可能变化，项目的构件就可能由于这些外部的不受控的因素而失败。

# 6. 从仓库解析依赖的机制
当本地仓库没有找到依赖构件的时候，Maven 会自动从远程仓库下载；当依赖版本为快照版本的时候，Maven 会自动找到最新的快照。 背后的逻辑可以概括如下：

1. 当依赖的范围是 `system` 的时候，Maven 直接从本地文件系统解析构件。
2. 根据依赖坐标计算仓库路径后，尝试直接从本地仓库寻找构件，如果发现相应构件，则解析成功。
3. 在本地仓库不存在相应构件的情况下，如果依赖的版本时显式的发布版本构件，如`1.2`、`2.1-beta-1`等，则便利所有的远程仓库，发现后，下载并解析使用。
4. 如果依赖的版本时`RELEASE`或`LATEST`，则基于更新策略读取所有远程仓库的元数据`groupId/artifactId/maven-metadata.xml`，将其与本地仓库的对应元数据合并后，计算出`RELEASE`或者`LATEST`真实的值，然后基于这个真实的值检查本地和远程仓库，如步骤2 和步骤3。
5. 如果依赖的版本时`SNAPSHOT`，则基于更新策略读取所有远程仓库的元数据`groupId/artifactId/version/maven-metadata.xml`，将其与本地仓库的对应原数据合并后，得到最新快照版本的值，然后基于该值检查本地仓库，或者从远程仓库下载。
6. 如果最后解析得到的构件版本时时间戳格式的快照，如`1.4.1-20091104.121450-121`，则复制其时间戳格式的文件至非时间戳格式，如`SNAPSHOT`，并使用该非时间戳格式的构件。

当依赖的版本不清晰的时候，如`RELEASE`、`LATEST`和`SNAPSHOT`， Maven 就需要基于更新远程仓库的更新策略来检查更新。 在第4节提到的仓库配置中，有一些配置与此有关：首先是`<release><enabled>` 和 `<snapshots><enabled>`，只有仓库开启了对于发布版本的支持时，才能访问该仓库的发布版本构件信息，对于快照版本也是同理； 其次要注意的是`<releases>` 和 `<snapshots>` 的子元素`<updatePolicy>`， 该子元素配置了检查更新的频率，每日检查更新、永远检查更新、从不检查更新、自定义时间间隔检查更新等。 最后，用户还可以从命令行加入`-U` 参数，强制检查更新，使用参数后，Maven 就会忽略`<updatePolicy>` 的配置。

当 Maven 检查完更新策略，并决定检查依赖更新的时候，就需要检查仓库元数据 `maven-metadata.xml`。

回顾一下前面提到的`RELEASE`和`LATEST`版本，它们分别对应了仓库中存在的该构件的最新发布版本和最新版本（包含快照），而这两个“最新”是基于`groupId/artifactId/maven-metadata.xml` 计算出来的，见下列maven-metadata.xml代码清单：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.sonatype.nexus</groupId>
  <artifactId>nexus</artifactId>
  <versioning>
    <latest>1.4.2-SNAPSHOT</latest>
    <release>1.4.0</release>
    <versions>
      <version>1.3.5</version>
      <version>1.3.6</version>
      <version>1.4.0-SNAPSHOT</version>
      <version>1.4.0</version>
      <version>1.4.0.1-SNAPSHOT</version>
      <version>1.4.1-SNAPSHOT</version>
      <version>1.4.2-SNAPSHOT</version>
    </versions>
    <lastUpdated>20210721221446</lastUpdated>
  </versioning>
</metadata>
```
该 xml 文件列出了仓库中存在的该构件所有可用的版本，同时`latest`元素指向了这些版本中最新的那个版本，该例中是`1.4.2-SNAPSHOT`。而`release`元素指向了这些版本中最新的发布版本，该例中是`1.4.0`。 Maven 通过合并多个远程仓库及本地仓库的元数据，就能计算出基于所有仓库的`latest`和`release`分别是什么，然后再解析具体的构件。

需要注意的是，在依赖声明中使用`LATEST`和`RELEASE`是不推荐的做法，因为 Maven 随时都可能解析到不同的构件，可能今天 LATEST 是1.3.6，明天就称为1.4.0-SNAPSHOT了，而且 Maven 不会明确告诉用户这样的变化。 当这种变化造成构建失败的时候，发现问题会变得比较困难。 `RELEASE` 因为对应的是最新发布版构件，还相对可靠，`LATEST` 就非常不可靠了，为此，Maven3 不再支持在插件中使用`LATEST`和`RELEASE`。  如果不设置插件版本，其效果就和`RELEASE`一样，Maven 只会解析最新的发布版本构件。 不过即使这样，也还存在潜在的问题。  例如，某个依赖的1.1版本与1.2版本可能发生一些接口的变化，从而导致当前 Maven 构建的失败。

当依赖的版本设为快照版本的时候，Maven 也需要检查更新，这时，Maven 会检查仓库元数据`groupId/artifactId/version/maven-metadata.xml`，见下面maven-metadata.xml代码清单：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.sonatype.nexus</groupId>
  <artifactId>nexus</artifactId>
  <version>1.4.2-SNAPSHOT</version>
  <versioning>
    <snapshot>
      <timestamp>20191214.221434</timestamp>
      <buildNumber>13</buildNumber>
    </snapshot>
    <lastUpdated>20191214221533</lastUpdated>
  </versioning>
</metadata>
```
该 XML 文件的 `snapshot` 元素包含了`timestamp`和`buildNumber`两个子元素，分别代表了这一快照的时间戳和构件号，基于这两个元素得到该仓库中此快照的最新构件版本实际为`1.4.2-SNAPSHOT-20191214.221434-13`。通过合并所有远程仓库和本地仓库的元数据，Maven 就能知道所有仓库中该构件的最新快照。

最后，从仓库元数据并不是永远正确的，有时候当用户发现无法解析某些构件，或者解析得到错误构件的时候，就有可能是出现了仓库元数据错误，这时就需要手工的，或者使用工具（如Nexus）对其进行修复。

# 7. 镜像
如果仓库 X 可以提供仓库 Y 存储的所有内容，那么就可以认为 X 是 Y 的一个镜像。 换句话说，任何一个可以从仓库 Y 获得的构件，都能够从它的镜像中获取。 因此，可以配置 Maven 使用该镜像来替代中央仓库。 编辑`settings.xml` 如下修改：
```xml
<settings>
  ...
  <mirrors>
    <mirror>
      <id>maven.net.cn</id>
      <name>one of the central mirros in China</name>
      <url>http://maven.net.cn/content/groups/public/</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
  ...
</settings>
```
该例中，`<mirrorOf>`的值为`central`，表示该配置为中央仓库的镜像，任何对于中央仓库的请求都会转至该镜像，用户也可以使用同样的方法配置其他仓库的镜像（比如阿里云maven镜像仓库）。  另外三个元素`id`、`name`、`url` 与一般仓库配置无异，表示该仓库的唯一标识符、名称以及地址。 类似的，如果镜像需要认证，也可以基于该`id`配置仓库认证。

关于镜像的一个更为常见的用法是结合私服。 由于私服可以代理任何外部的公共仓库（包括中央仓库），因此，对于组织内部的 Maven 用户来说，使用一个私服地址就等于使用了所有需要的外部仓库，这可以将配置集中到私服，从而简化 Maven 本身的配置。在这种情况下，任何需要的构件都可以从私服获得，私服就是所有仓库的镜像。 这时，可以配置这样的一个镜像：
```xml
<settings>
  ...
  <mirrors>
    <mirror>
      <id>internal-repository</id>
      <name>Internal Repository Manager</name>
      <url>http://192.168.1.100/maven2/</url>
      <mirrorOf>*</mirrorOf>
    </mirror>
  </mirrors>
  ...
</settings>
```
该例中`<mirrorOf>`元素的值为星号，表示该配置是所有 Maven 仓库的镜像，任何对于远程仓库的请求都会被转至`http://192.168.1.100/maven2/`。  如果该镜像仓库需要认证，则配置一个 `id` 为`internal-repository` 的 `<server>` 即可。

为了满足一些复杂的绣球，Maven 还支持更高级的镜像配置：

* `<mirrorOf>*</mirrorOf>`: 匹配所有远程仓库；
* `<mirrorOf>external:*</mirrorOf>`: 匹配所有远程仓库，使用 localhost 的除外，使用`file://`协议的除外。 也就是说，匹配所有不在本机上的远程仓库。
* `<mirrorOf>repo1,repo2</mirrorOf>`: 匹配仓库 repo1 和 repo2，使用逗号分隔多个远程仓库。
* `<mirrorOf>*,!repo1</mirrorOf>`: 匹配所有远程仓库，repo1 除外，使用感叹号将仓库从匹配中排除。

**需要注意的是，由于镜像仓库完全屏蔽了被镜像仓库，当镜像仓库不稳定或者停止服务的时候，Maven 仍将无法访问被镜像仓库，因而将无法下载构件。**

# 8. 仓库搜索服务
使用 Maven 进行日常开发的时候，一个常见的问题就是如何寻找需要的依赖，我们可能只知道需要使用类库的项目名称，但添加 Maven 依赖要求提供确切的 Maven 坐标。 这时，就可以使用仓库搜索服务来根据关键字得到 Maven 坐标。

本小节将介绍几个常用的、功能强大的公共 Maven 仓库搜索服务。

## 8.1 Sonatype Nexus
地址： [https://repository.sonatype.org/](https://repository.sonatype.org/)

Nexus 是当前最流行的开源 Maven 仓库管理软件，本文后面章节会讲述如何使用 Nexus 假设私服。  这里要介绍的是 Sonatype 假设的一个公共 Nexus 仓库实例。

`Nexus` 提供了关键字搜索、类名搜索、坐标搜索、校验和搜索等功能。 搜索后，页面清晰的列出了结果构件的坐标及所述仓库。 用户可以直接下载相应构件，还可以直接复制已经根据坐标生成的 XML 依赖声明。
![2.png](https://raw.githubusercontent.com/dellnoantechnp/mvnbook/main/Chapter6/.pic/2.png)

## 8.2 MVNrepository
地址：[https://mvnrepository.com](https://mvnrepository.com)
MVNrepository 的界面比较清新，它提供了基于关键字的搜索、依赖声明代码片段、构件下载、依赖于被依赖关系信息、构件所含包信息等功能。  MVNrepository 还能提供一个简单的图表，显示某个构件各版本间的大小变化。
![3.png](https://raw.githubusercontent.com/dellnoantechnp/mvnbook/main/Chapter6/.pic/3.png)

## 8.3 国内的镜像仓库 阿里云Maven镜像仓库
地址：[https://maven.aliyun.com/](https://maven.aliyun.com/)
国内的镜像仓库属 aliyun 比较给力了，只提供了较为基本的构件搜索功能。
[https://maven.aliyun.com/mvn/search](https://maven.aliyun.com/mvn/search) 

settings.xml 配置指南：
```xml
<mirror>
  <id>aliyunmaven</id>
  <mirrorOf>central</mirrorOf>
  <name>阿里云公共仓库</name>
  <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

## 8.4 国内的镜像加速服务 腾讯云镜像源加速maven
腾讯云的镜像加速只提供仓库配置功能，并不提供搜索服务。
[https://mirrors.cloud.tencent.com/help/maven.html](https://mirrors.cloud.tencent.com/help/maven.html)
```xml
<mirror>
    <id>nexus-tencentyun</id>
    <mirrorOf>central</mirrorOf>
    <name>Nexus tencentyun</name>
    <url>http://mirrors.cloud.tencent.com/nexus/repository/maven-public/</url>
</mirror> 
```

# 9. 小结
本文深入阐述了仓库这一 Maven 的核心概念。

本文还解释了镜像的概念及用法。