Maven 的一大功能就是管理项目依赖。为了能自动化的解析任何一个 Java 构件，Maven 就必须将它们**唯一标识**，这就依赖管理的底层基础 —— **坐标**。 

本篇将详细分析 Maven 坐标的作用，解释其每一个元素；再次基础上，再介绍如何配置 Maven，以及先关的经验和技巧。

# 1. 何为 Maven 坐标
Maven 的世界中拥有数量非常巨大的构件，也就是平时用的一些 jar、war 等文件。 Maven 定义了这样一组规则：世界上任何一个构件都可以使用 Maven 坐标唯一标识，Maven 坐标的元素包括`groupId`、`artifactId`、`version`、`packaging`、`classifier`。 现在，只要我们提供正确的坐标元素，Maven 就能找到对应的构件。

比如说，当需要使用 Java5 平台上的 TestNG 的 5.8 版本时，就告诉 Maven：`groupId=org.testng; artifactId=testng; version=5.8; classifier=jdk5`，Maven 就会从长裤中寻找相应的构件供我们使用。
Maven 内置了一个中央仓库的地址（[https://repo.maven.apache.org/maven2](https://repo.maven.apache.org/maven2)），该中央仓库包含了世界上大部分流行的开源项目构件。

# 2. 坐标详解
Maven 坐标为各种构件引入了秩序，任何一个构件都必须明确定义自己的坐标，而一组 Maven 坐标是通过一些元素定义的，它们是`groupId`、`artifactId`、`version`、`packaging`、`classifier`。
先看一组坐标定义，举例说明如下：
```xml
<groupId>org.sonatype.nexus</groupId>
<artifactId>nexus-indexer</artifactId>
<version>2.0.0</version>
<packaging>jar</packaging>
```
这是 `nexus-indexer` 的坐标定义，`nexus-indexer` 是一个对 Maven 仓库编撰索引并提供搜索功能的类库，它是 Nexus 项目的一个子模块。
上述代码中，其坐标分别为`groupId: rog.sonatype.nexus`、`artifactId: nexus-indexer`、`version: 2.0.0`、`packaging: jar`，没有`classifier`。 
下面详细解释一下各坐标元素：

* **`groupId`** ：定义当前 Maven 项目隶属的实际项目。 首先，Maven 项目和实际项目不一定是一对一的关系。 比如`SpringFramework`这一实际项目，其对应的Maven项目会有很多，如`spring-core`、`spring-context` 等。 这是由于 Maven 中模块的概念，一次你，一个实际项目往往会被划分成多个模块。 其次，`groupId` 不应该对应项目隶属的组织或公司。最后，`groupId`的表示方式与 Java 包名的表示方式类似，通常与域名反向一一对应。上例中，`groupId`为`org.sonatype.nexus`，`org.sonatype`表示 Sonatype 公司建立的一个非营利性组织，`nexus`表示Nexus这一实际项目，该`groupId`与域名`nexus.sonatype.org`对应。
* **`artifactId`** ：该元素定义实际项目中的一个Maven项目（模块），推荐的做法是使用实际项目名称作为`artifactId`的前缀。 比如上例中的`artifactId`是`nexus-indexer`，使用了实际项目名 nexus 作为前缀，这样做的好处是方便寻找实际构件。 **在默认情况下，Maven 生成的构件，其文件名会以`artifactId`作为开头**，比如`nexus-indexer-2.0.0.jar`，使用实际项目名称作为前缀之后，就能方便从一个 lib 文件夹中找到某个项目的一组构件。考虑有 5 个项目，每个项目都有一个`core`模块，如果没有前缀，我们会看到很多`core-1.2.jar`这样的文件，机上实际项目名前缀之后，便能很容易区分了。
* **`version`** ：该元素定义 Maven 项目当前所处的版本。需要注意的是，Maven 定义了一套完整的版本规范，以及快照（SNAPSHOT）的概念。
* **`packaging`** ：该元素定义 Maven 项目的打包方式。首先，打包方式通常与所生成构件的文件扩展名对应，而使用 **war** 打包的方式，最终生成的构件还有一个`.war`文件，不过这不是绝对的。 **最后，当不定义 packaging 的时候，Maven会使用默认值 jar。**
* **`classifier`** ：该元素用来帮助定义构件输出的一些附属构件。附属构件与主构件对应，如上例中的主构件是`nexus-indexer-2.0.0.jar`，该项目可能还会通过使用一些插件生成如`nexus-indexer-2.0.0-javadoc.jar`、`nexus-indexer-2.0.0-sources.jar` 这样一些附属构件，其包含了 Java 文档和源代码。 这时候，javadoc和sources就是这两个附属构件的`classifier`。 这样，附属构件也就拥有了自己唯一的坐标。 *注意，不能直接定义项目的`classifier`，因为附属构件不是项目直接默认生成的，而是由附加的插件帮助生成的。*

上述 5 个元素中，`groupId`、`artifactId`、`version` 是必须定义的，`packaging` 是可选的（默认为jar），而`classifier`是不能直接定义的。

同时，项目构件的文件名是与坐标相对应的，一般的规则为`artifactId-version[-classfier].packaging`。 这里还要强调一点是，`packaging`并非一定与构件扩展名对应，比如`packaging`为 `maven-plugin`的构件扩展名为 jar。

# 3. account-email
在详细讨论 Maven 依赖之前，先稍微回顾一下上一章提到的背景案例。 案例中有一个 email 模块负责发送账户激活的电子邮件，本节就详细阐述该模块的实现，包括 POM 配置、主代码和测试代码。 由于该背景案例的实现是基于`SpringFramework`，因此还会设计相关的 `Spring` 配置。

## 3.1 account-email 的 POM
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.juvenxu.mvnbook.account</groupId>
    <artifactId>account-email</artifactId>
    <name>Account Email</name>
    <version>1.0.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>2.5.6</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-beans</artifactId>
            <version>2.5.6</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>2.5.6</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context-support</artifactId>
            <version>2.5.6</version>
        </dependency>
        <dependency>
            <groupId>javax.mail</groupId>
            <artifactId>mail</artifactId>
            <version>1.5.0-b01</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.7</version>
        </dependency>
        <dependency>
            <groupId>com.icegreen</groupId>
            <artifactId>greenmail</artifactId>
            <version>1.3.1b</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <!--<build>-->
        <!--<plugins>-->
            <!--<plugin>-->
                <!--<groupId>org.apache.maven.plugins</groupId>-->
                <!--<artifactId>maven-compiler-plugin</artifactId>-->
                <!--<configuration>-->
                    <!--<source>1.8</source>-->
                    <!--<target>1.8</target>-->
                <!--</configuration>-->
            <!--</plugin>-->
        <!--</plugins>-->
    <!--</build>-->

</project>
```
先观察该项目模块的坐标，`groupId: com.juvenxu.mvnbook.account`; `artifactId: account-email`; `version: 1.0.0-SNAPSHOT`。

由于该模块属于账户注册服务项目的一部分，因此，其`groupId`对应了`account`项目。 紧接着，该模块的`artifactId`仍然以`account`作为前缀，以方便区分其他项目的构件。 最后，`1.0.0-SNAPSHOT` 表示该版本处于开发中，还不稳定。

再看 `dependencies` 元素，其包含了多个`dependency`子元素，这是 POM 中定义项目依赖的位置。 以第一个依赖为例，其`groupId: artifactId: version` 为`org.springframework: spring-core: 2.5.6`，这是依赖的坐标，任何一个 Maven 项目都需要定义自己的坐标，当这个 Maven项目成为其他 Maven 项目的依赖的时候，这组坐标就体现了其价值。 

本例中`spring-core`，以及后面的`spring-beans`、`spring-context`、`spring-context-support` 是 Spring Framework 实现依赖注入等必要的构件，本文的关注点在于 Maven，只会涉及简单的 Spring Framework 的使用。

在`spring-context-support`之后，有一个依赖为`javax.mail:mail:1.5.0-b01`，这是实现发送邮件必须的类库。

紧接着的依赖为`junit:junit:4.7`，Junit 是 Java 社区事实上的单元测试标准，详细信息请参阅 [http://www.junit.org](http://www.junit.org)，这个依赖的特殊地方在于一个值为 test 的`scope`子元素，`scope` 用来定义依赖范围。这里读者暂时只需要了解当依赖范围是 test 的时候，该依赖只会被加入到测试代码的 `classpath` 中。也就是说，对于项目主代码，该依赖是没有任何作用的。 JUnit 是单元测试框架，只有在测试的时候才需要，因此使用该依赖的范围。

随后的依赖是`com.icegreen:greenmail:1.3.1b`，其依赖范围同样为`test`。GreenMail 是开源的邮件服务测试套件，`account-email` 模块使用该套件来测试邮件的发送。关于 GreenMail的详细信息可以访问[http://www.icegreen.com/greenmail/](http://www.icegreen.com/greenmail/) 。

最后，POM 中有一段关于 `maven-compiler-plugin` 的配置，其目的是开启 Java5 的支持。

## 3.2 account-email 的主代码
`account-email` 项目的 Java 主代码位于 `src/main/java`，资源文件（非Java）位于`src/main/resources` 目录下。

`account-email`只有一个很简单的接口。

接口代码 **AccountEmailService.java**：
```java
package com.juvenxu.mvnbook.account.email;

public interface AccountEmailService {
  void sendMail(String to, String subject, String htmlText) throws AccountEmailException;
}
```
`sendMail()` 方法用来发送 html 格式的邮件，如果发送邮件出错，则抛出`AccountEmailException` 异常。


对应该接口的**实现**见如下 **`AccountEmailServiceImpl.java`** 代码：
```java
package com.juvenxu.mvnbook.account.email;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

public class AccountEmailServiceImpl implements AccountEmailService {
    private JavaMailSender javaMailSender;
    private String systemEmail;
    public void sendMail(String to, String subject, String htmlText) throws AccountEmailException {
        try {
            MimeMessage msg = javaMailSender.createMimeMessage();
            MimeMessageHelper msgHelper = new MimeMessageHelper(msg);

            msgHelper.setFrom(systemEmail);
            msgHelper.setTo(to);
            msgHelper.setSubject(subject);
            msgHelper.setText(htmlText, true);

            javaMailSender.send(msg);
        } catch (MessagingException e) {
            throw new AccountEmailException("Faild to send mail.", e);
        }
    }

    public JavaMailSender getJavaMailSender() {
        return javaMailSender;
    }

    public void setJavaMailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public String getSystemEmail() {
        return systemEmail;
    }

    public void setSystemEmail(String systemEmail) {
        this.systemEmail = systemEmail;
    }
}
```
首先，该`AccountEmailServiceImpl`类有一个私有字段`javaMailSender`，该字段的类型`org.springframework.mail.javamail.JavaMailSender` 是来自于`Spring Framework`的帮助简化邮件发送的工具类库，对于该字段有一组`getter()`和`setter()`方法，它们用来帮助实现依赖注入。 本文随后会讲述 Spring Framework 依赖注入相关的配置。

在`sendMail()`的方法实现中，首先使用`javaMailSender`创建一个`MimeMessage`，该 msg 对应了将要发送的邮件。接着使用`MimeMessageHelper`帮助设置该邮件的发送地址、收件人地址、标题以及内容，`msgHelper.setText(htmlText, true)` 中的 true 表示邮件的内容为`html`格式。 最后，使用`javaMailSender`发送该邮件，如果发送出错，则捕捉 `MessageException` 异常，包装后再抛出该模块自己定义的`AccountEmailException` 异常。

**`AccountEmailException.java`** 异常类定义代码如下：
```java
package com.juvenxu.mvnbook.account.email;

public class AccountEmailException extends Exception {
  public AccountEmailException(String message, Throwable cause) {
    super(message, cause);
  }
}
```

这段 Java 代码中没有邮件服务器配置信息，这得益于 Spring Framework  的依赖诸如，这些配置都通过外部的配置注入到了`javaMailSender`中，相关配置信息都在 `src/main/resources/account-email.xml` 这个配置文件之中：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="propertyConfigurer" class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
        <property name="location" value="classpath:service.properties"/>
    </bean>

    <bean id="javaMailSender" class="org.springframework.mail.javamail.JavaMailSenderImpl">
        <property name="protocol" value="${email.protocol}"/>
        <property name="host" value="${email.host}"/>
        <property name="port" value="${email.port}"/>
        <property name="username" value="${email.username}"/>
        <property name="password" value="${email.password}"/>
        <property name="javaMailProperties">
            <props>
                <prop key="mail.${email.protocol}.auth">${email.auth}</prop>
            </props>
        </property>
    </bean>

    <bean id="accountEmailService" class="com.juvenxu.mvnbook.account.email.AccountEmailServiceImpl">
        <property name="javaMailSender" ref="javaMailSender"/>
        <property name="systemEmail" value="${email.systemEmail}"/>
    </bean>
</beans>
```
`Spring Framework` 会使用该 XML 配置创建 ApplicationContext，以实现依赖注入。 该配置文件定义了一些 bean，基本对应了 Java 程序中的对象。 
**首先解释下 id 为 `propertyConfigurer` 的 bean，其实现为`org.springframework.beans.factory.config.PropertyPlaceholderConfigurer`，这是 Spring Framework 中用来帮助载入 `properties`文件的组件。 这里定义 `location` 的值为 `classpath:service.properties` ，表示从 classpath 的根路径下载入名为 `service.properties` 文件中的属性。**

**接着定义 id 为`javaMailSender` 的 bean，其实现为 `org.springframework.mail.javamail.JavaMailSenderImpl`，这里需要定义邮件服务器的一些配置，包括协议、端口、主机、用户名、密码，是否需要认证等属性。 这段配置还使用了 Spring Framework 的属性引用，比如 `host` 的值为 `${email.host}`，之前定义 `propertyConfigurer` 的作用就在于此。** 这么做可以将邮件服务器相关的配置分离到外部的`properties` 文件中，比如可以定义这样一个 `properties` 文件，配置 `javaMailSender` 使用 gmail：

**`src/main/java/resources/service.properties`** 定义：
```properties
email.protocol=smtps
email.host=smtp.gmail.com
email.port=465
email.username=your-id@gmail.com
email.password=your-password
email.auth=true
email.systemEmail=your-id@juvenxu.com
```
这样，JavaMailSender 实际使用的`protocol`就会成为`smtps`，`host`会成为`smtp.gmail.com`，同理还有 port、username 等其他属性。

**最后一个 bean 是 `accountEmailService`，对应了之前描述的`com.juvenxu.mvnbook.account.email.AccountEmailServiceImpl`，配置中将另外一个`bean` `javaMailSender` 注入，使其成为该类`javaMailSender` 字段的值。**

上述就是 Spring Framework 相关的配置，这里就不再进一步深入，读者如果有不是很理解的地方，请查询 Spring Framework 相关文档。

## 3.3 account-email 的测试代码
测试相关的 Java 代码位于 `src/test/java` 目录，相关的资源文件则位于 `src/test/resources` 目录。

该模块需要测试的只有一个 `AccountEmailService.sendMail()` 接口。为此，需要配置并启动一个测试使用的邮件服务器，然后提供对应的`properties` 配置文件供 Spring Framework 载入以配置程序。 准备就绪后，调用该接口发送邮件，然后检查邮件是否发送正确。最后，关闭测试邮件服务器。

AccountEmailServiceTest.java 代码如下：
```java
package com.juvenxu.mvnbook.account.email;

import static junit.framework.Assert.assertEquals;

import javax.mail.Message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

public class AccountEmailServiceTest {
    private GreenMail greenMail;

    @Before
    public void startMailServer() throws Exception {
        greenMail = new GreenMail(ServerSetup.SMTP);
        greenMail.setUser("test@juvenxu.com", "123456");
        greenMail.start();
    }

    @Test
    public void testSendMail() throws Exception {
        ApplicationContext ctx = new  ClassPathXmlApplicationContext("account-email.xml");
        AccountEmailService accountEmailService =
                (AccountEmailService) ctx.getBean( "accountEmailService" );

        String subject = "Test Subject";
        String htmlText = "<h3>I Love You !!!</h3>";

        accountEmailService.sendMail( "test@juvenxu.com", subject, htmlText );

        greenMail.waitForIncomingEmail( 2000, 1 );

        Message[] msgs = greenMail.getReceivedMessages();
        assertEquals(1, msgs.length );
        assertEquals(subject, msgs[0].getSubject() );
        assertEquals(htmlText, GreenMailUtil.getBody( msgs[0] ).trim());
    }


    @After
    public void stopMailServer() throws Exception {
        greenMail.stop();
    }
}
```
这里使用 GreenMail 作为测试邮件服务器，在 `startMailServer()` 中，基于 SMTP 协议初始化 GreenMail，然后创建一个邮件账户并启动邮件服务，该服务默认会监听 25 端口。*如果你的机器已经有程序使用该端口，请配置定义的`ServerSetup` 实例使用其他端口。*` startMailServer()` 方法使用了`@Before`  注解，表示该方法会先于测试方法`@Test` 之前执行。

对应于`startMailServer()`，该测试还有一个`stopMailServer()` 方法，标注`@After` 表示执行测试方法之后会调用该方法，停止`GreenMail` 的邮件服务。

代码的重点在于使用了`@Test` 标注的`testSendMail()` 方法，该方法首先会根据 classpath 路径汇总的 `account-email.xml` 配置创建一个 Spring Framework 的 `ApplicationContext`，然后从这个 ctx 中获取需要测试的 id 为 `accountEmailService` 的 bean，并转换成`AccountEmailService` 接口，针对接口测试是一个单元测试的最佳实践。 得到了`AccountEmailService` 之后，就能调用其 `sendMail()`  方法发送电子邮件。 当然，这个时候不能忘了邮件服务器的配置，其位于 `src/test/resources/service.properties`：
**`src/test/resources/service.properties`** 示例代码如下：
```properties
email.protocol=smtp
email.host=localhost
email.port=25
email.username=test@juvenxu.com
email.password=123456
email.auth=true
email.systemEmail=your-id@juvenxu.com
```

这段配置与之前 GreenMail 的配置对应，使用了 smtp 协议，使用本机的 25 端口，并有用户名、密码等认证配置。

回到测试方法中，邮件发送完毕后，再使用 GreenMail 进行检查。 
由于 GreenMail 服务完全基于内存，实际情况下基本不会超过 2s。随后的几行代码读取收到的邮件，检查邮件的数目以及第一封邮件的主题和内容。

接下来，可以运行 `mvn clean test` 执行测试，Maven 会编译主代码和测试代码，并执行测试，报告一个测试得以正确执行，构建成功。

## 3.4 构建 account-email
使用 `mvn clean install` 构建 `account-email`，Maven 会根据 POM 配置自动下载所需要的依赖构建，执行编译、测试、打包等工作，最后将项目生成的构建`account-email-1.0.0-SNAPSHOT.jar` 安装到本地仓库中。 这时，该模块就能提供其他 Maven 项目使用了。

# 4. 依赖的配置
上节已经罗列了一些简单的依赖配置，读者可以看到依赖会有基本的`groupId`、`artifactId` 和 `version` 等元素组成。 

其实一个依赖声明可以包含如下的一些元素：
```xml
<project>
...
  <dependencies>
    <dependency>
      <groupId>...</groupId>
      <artifactId>...</artifactId>
      <version>...</version>
      <type>...</type>
      <scope>...</scope>
      <optional>...</optional>
      <exclusions>
        <exclusion>
          ...
        </exclusion>
      ...
      </exclusions>
    </dependency>
  ...
  </dependencies>
...
</project>
```
根元素`project`下的 `dependencies` 可以包含一个或多个 `dependency` 元素，以声明一个或者多个项目依赖。 每个依赖可以包含的元素有：

* **`groupId`、`artifactId` 和 `version`** ：依赖的基本坐标，对于任何一个依赖来说，基本坐标是最重要的，Maven 根据坐标才能找到需要的依赖。
* **`type`** ：依赖的类型，对应于项目坐标定义的 `packaging` 。大部分情况下，该元素不必声明，其默认值为 jar。
* **`scope`** ：依赖的范围。
* **`optional`** ：标记依赖是否可选。
* **`exclusions`** ：用来排除传递性依赖。 

大部分依赖声明只包含基本坐标，然而在一些特殊情况下，其他元素至关重要。

## 5. 依赖范围
上一篇提到，JUnit 依赖的测试范围是`test`，测试范围用元素`scope`表示。

本节将详细解释说明什么是**测试范围**，以及各种测试范围的效果和用途。

首先需要知道，Maven 在编译项目主代码的时候需要使用一套`classpath`。在上例中，编译项目主代码的时候需要用到`spring-core`，该文件以依赖的方式被引入到`classpath` 中。

其次，Maven 在编译和执行测试的时候会使用另外一套 `classpath`。 上例中的 JUnit 就是一个很好的例子，该文件也以依赖的方式引入到测试使用的 `classpath` 中，不同的是这里的依赖范围是 `test`。 

最后，实际运行 Maven 项目的时候，又会使用一套 classpath，上例中的 `spring-core` 需要在该`classpath`中，而 JUnit 则不需要。

依赖范围就是用来控制依赖与这三种`classpath`（编译classpath、测试classpath、运行classpath）的关系，Maven 有以下几种依赖范围：

* **`compile`** ：编译依赖范围。如果没有指定，就会默认使用该依赖范围。使用此依赖范围的 Maven 依赖，对于编译、测试、运行三种 classpath 都有效。 典型的例子是`spring-core`，在**编译**、**测试**和**运行**的时候都需要使用该依赖。
* **`test`** ：测试依赖范围。使用此依赖范围的 Maven 依赖，**只对于测试** `classpath` 有效，在编译主代码或者运行项目的时候时将无法使用此类依赖。 典型的例子是 JUnit，它只有在编译测试代码及运行测试代码的时候才需要。
* **`provided`** ：**已提供**依赖范围。使用此依赖范围的 Maven 依赖，对于**编译**和**测试** classpath 有效，但在运行时无效。 典型的例子是`servlet-api`，编译和测试项目的时候需要该依赖，但在运行项目的时候，由于容器已经提供，就不需要 Maven 重复的引入一遍。
* **`runtime`** ：**运行时**依赖范围。 使用此依赖范围的 Maven 依赖，对于**测试**和**运行** `classpath` 有效，但在编译主代码时无效。典型的例子是 JDBC 驱动实现，项目主代码的编译只需要 JDK 提供的 JDBC 接口，只有在执行测试或者运行项目的时候才需要实现上述接口的具体 JDBC 驱动。
* **`system`** ：系统依赖范围。 该依赖与三种`classpath` 的关系，和`provided` 依赖范围完全一致。 但是，使用`system`范围的依赖时必须通过`systemPath` 元素显式地指定依赖文件的路径。 由于此类依赖不是通过`Maven` 仓库解析的，而且往往与本机系统绑定，可能造成构建的不可移植，因此应该谨慎使用。`systemPath` 元素可以引用环境变量。
```xml
<dependency>
  <groupId>javax.sql</groupId>
  <artifactId>jdbc-stdext</artifactId>
  <version>2.0</version>
  <scope>system</scope>
  <systemPath>${java.home}/lib/rt.jar</systemPath>
</dependency>
```

* **`import`（Maven 2.0.9及以上）** ：**导入**依赖范围。该依赖范围不会对三种`classpath`产生实际的影响，本文将在后续章节介绍 Maven 依赖和 `dependencyManagement` 的时候详细介绍此依赖范围。

上述除 `import` 以外的各种依赖范围与三种 `classpath`  的关系如下表所示：

| 依赖范围 (Scope) | 对于编译 classpath 有效 | 对于测试 classpath 有效 | 对于运行时 classpath 有效 | 例子 |
| -------         | -------                | ------                | ------                  | ---------- |
|compile          | Y                      | Y                     | Y                       |  `spring-core` |
|test             | —                      | Y                     | —                       | JUnit |
|provided         | Y                      | Y                     | —                       | `servlet-api`|
|runtime          | —                      | Y                     | Y                       | JDBC 驱动实现|
|system           | Y                      | Y                     | —                       | 本地的，Maven 仓库之外的类库文件|
                             
# 6. 传递性依赖
## 6.1 何为传递性依赖
考虑一个基于 Spring Framework 的项目，如果不使用 Maven，那么在项目中就需要手动下载相关依赖。 由于 Spring Framework 又会依赖于其他开源类库，因此实际中往往会下载一个很大的如 spring-framework-2.5.6-with-denpendencies.zip 的包，这里面包含了所有 Spring Framework 的 jar 包，以及所有它依赖的其他 jar 包。这么做往往就引入了很多不必要的依赖。另一种做法是只下载 spring-framework-2.5.6.zip 包，包里不包含其他相关依赖，到实际使用的时候，根据出错信息，查询相关文档，手动加入需要的其他依赖。 很显然，这是一键非常麻烦的事情。

Maven 的传递性依赖机制可以很好的解决这一问题。以`account-email` 项目为例，该项目有一个`org.springfamework:spring-core:2.5.6` 的依赖，而实际上`spring-core` 也有它自己的依赖，我们可以访问位于中央仓库的该构件的 POM：`http://repo1.maven.org/maven2/org/springframework/spring-core/2.5.6/spring-core-2.5.6.pom` 。该文件包含了一个`commons-logging` 依赖。

```xml
<dependency>
  <groupId>commons-logging</groupId>
  <artifactId>commons-logging</artifactId>
  <version>1.1.1</version>
</dependency>
```
该依赖没有声明依赖范围，那么其依赖范围就是默认的`compile`。 同时回顾一下`accont-email`，`spring-core` 的依赖范围也是`compile`。

`account-email` 有一个`compile` 范围的`spring-core`依赖，`spring-core` 有一个`compile`范围的`commons-logging` 依赖，那么`commons-logging` 就会称为`account-email` 的 `compile` 范围依赖，`commons-logging`是 `account-email` 的一个传递性依赖：
![1.png](https://github.com/dellnoantechnp/mvnbook/blob/main/Chapter5/.pic/1.png)

有了传递性依赖机制，在使用 Spring Framework 的时候就不用考虑它依赖了什么，也不用担心引入多余的依赖。 Maven 会解析各个直接依赖的 POM，将那些必要的间接依赖，以传递性依赖的形式引入到当前的项目中。

## 6.2 传递性依赖和依赖范围
依赖范围不仅可以控制依赖与三种`classpath` 的关系，还对传递性依赖产生影响。 

假设 A 依赖于 B，B 依赖于 C，我们说 A 对于 B 是第一直接依赖，B 对于 C 是第二直接依赖，A 对于 C 是依赖性依赖。

**第一直接依赖的范围和第二直接依赖的范围决定了传递性依赖的范围。** 如下表所示，最左边一行表示第一直接依赖范围，最上面一行表示第二直接依赖范围，中间的较差单元格则表示传递性依赖范围。

| -         | compile  | test   | provided | runtime  |
| :-------: | :------: | :----: | :------: | :-----:  |
| compile   | compile  | -      | -        | runtime  |
| test      | test     | -      | -        | test     |
| provided  | provided | -      | provided | provided |
| runtime   | runtime  | -      | -        | runtime  |

为了能够帮助读者更好的理解，这里再举个例子。  

`account-email` 项目有一个`com.icegreen:greenmail:1.3.1b` 的直接依赖，我们说这是第一个直接依赖，其依赖范围是`test`；而`greenmail`又有一个 `javax.mail:mail:1.5.0-b01` 的直接依赖，我们说这是第二直接依赖，其依赖范围是`compile`。 显然`javax.mail:mai;:1.5.0-b01` 是 `account-email` 的传递性依赖，对照上表，当第一直接依赖范围为 `test` ，第二直接依赖范围是 `compile` 的时候，传递性依赖的范围是 `test` ，因此 `javax.mail:mail:1.5.0-b01` 是 `account-email` 的第一个范围是 `test` 的传递性依赖。

在仔细观察下上表，可以发现这样的规律：**当第二个直接依赖的范围是`compile` 的时候，传递性依赖的范围与第一直接依赖范围一直； 当第二直接依赖的范围是 `test`的时候，依赖不会得以传递； 当第二直接依赖范围是 `provided` 的时候，只传递第一直接依赖范围也为 `provided` 的依赖，且传递性依赖的范围同样为 `provided` ；  当第二直接依赖范围是 `runtime` 的时候，传递性依赖的范围与第一直接依赖的范围一直，但 `compile` 例外，此时传递性依赖的范围为 `runtime`。**

# 7. 依赖调解
Maven 引入的传递性依赖机制，一方面大大简化和方便了依赖声明，另一方面，大部分情况下我们只需要关心项目的直接依赖是什么，不用考虑这些依赖会引入什么传递性依赖。   但有时候，当传递性依赖造成问题的时候，我们就需要清楚的知道该传递性依赖是从哪条依赖路径引入的。

例如，项目 A 有这样的依赖关系： `A --> B --> C --> X(1.0)`  与 `A --> D --> X(2.0)`，X 是 A 的传递性依赖，但是两条依赖路径上有两个版本的 X，那么哪个 X 会被 Maven 解析使用呢？ 

两个版本都被解析显然是不对的，因为那会造成依赖重复，因此必须选择一个。  Maven 依赖调解（`Dependency Mediation`）的第一原则是：**路径最近者优先**。  

该例中 X(1.0) 的路径长度为 3， 而 X(2.0) 的路径长度为 2 ，因此 X(2.0) 会被解析使用。

依赖调解第一原则不能解决所有问题，比如这样的依赖关系： `A --> B --Y(1.0)`  和 `A --> C --> Y(2.0)` ，其中 `Y(1.0)` 和 `Y(2.0)` 的依赖路径长度是一样的，都为 2。  那么到底谁会被解析使用呢？  在 Maven 2.0.8 及之前的版本中，这是不确定的，但是从 Maven 2.0.9 开始，为了尽可能避免构建的不确定性，**Maven 定义了依赖调解的第二原则：第一声明者优先。** 

**在依赖路径长度相等的前提下，在POM 中依赖声明的顺序决定了谁会被解析使用，顺序最靠前的那个依赖优胜。**

该例中，如果 B 的依赖声明在 C 之前，那么 `Y(1.0)` 就会被解析使用。

# 8. 可选依赖
假设有这样一个依赖关系，项目 A 依赖于项目 B，项目 B 依赖于项目 X 和 Y， B 对于 X 和 Y 的依赖都是可选依赖：`A --> B`、`B --> X(可选)`、`B --> Y(可选)` 。 

根据传递性依赖的定义，如果所有这三个依赖范围都是`compile`，那么 X、Y 就是 A 的 `compile`范围传递性依赖。 **然而，由于这里 X、Y 是可选依赖，依赖将不会得以传递。** 话句话说，X、Y 将不会对 A 有任何影响。
![2.png](https://github.com/dellnoantechnp/mvnbook/blob/main/Chapter5/.pic/2.png)

为什么要使用**可选依赖**这一特性呢？ 可能项目 B 实现了两个特性，其中的特性一 依赖于 X，特性二 依赖于 Y，而且这两个特性是互斥的，用户不可能同时使用两个特性。 比如 B 是一个持久层隔离工具包，它支持多种数据库，包括 MySQL、PostgreSQL 等，在构建这个工具包的时候，需要这两种数据库的驱动程序，但是在使用这个工具的时候，只会依赖一种数据库。

项目B 的依赖声明见下列代码：
```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.juvenxu.mvnbook</groupId>
    <artifactId>project-b</artifactId>
    <version>1.0.0</version>

    <dependencies>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.10</version>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>8.4-701jdbc3</version>
            <optional>true</optional>
        </dependency>
     <dependencies>
</project>
```
 上述XML代码中，使用`<optional>`元素表示 mysql 及 postgresql 两个依赖为可选依赖，他们只会对当前项目 B 产生影响，当其他项目依赖于 B 的时候，这两个依赖不会被传递。 因此，当项目 A 依赖于项目 B 的时候，如果其实际使用基于 MySQL 数据库，那么在项目 A 中就需要显式地声明 `mysql-connector-java` 这一依赖。 
 
 ```xml
<!--  可选依赖不被传递 -->
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.juvenxu.mvnbook</groupId>
    <artifactId>project-a</artifactId>
    <version>1.0.0</version>

    <dependencies>
        <dependency>
            <groupId>com.juvenxu.mvnbook</groupId>
            <artifactId>project-b</artifactId>
            <version>1.0.0</version>
            <!--  依赖项目 B  -->
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.10</version>
            <!--  显式地开启项目 B 的 mysql 依赖 -->
        </dependency>
     <dependencies>
</project>
```
最后，关于可选依赖需要说明的一点是，在理想的情况下，是不应该使用可选依赖的。

前面我们可以看到，使用可选依赖的原因是某一个项目实现了多个特性，在面向对象设计中，有个单一职责性原则，意指一个类应该只有一项职责，而不是糅合太多的功能。  这个原则在规划 Maven 项目的时候也同样适用。  在上面的例子中，更好的做法是为 MySQL 和 PostgreSQL 分别创建一个 Maven 项目，基于同样的 `groupId` 分配不同的 `artifactId` ，如 `com.juvenxu.mvnbook:project-b-mysql` 和 `com.juvenxu.mvnbook:project-b-postgresql` ，在各自的 POM 中声明对应的 JDBC 驱动依赖，而且不使用可选依赖，用户则根据需要选择适用 `project-b-mysql` 或者 `project-b-postgresql` 。由于传递性依赖的作用，就不用再声明 JDBC 驱动依赖。

# 9. 最佳实践
Maven 依赖涉及的知识点比较多，在理解了主要的功能和原理之后，最需要的当然就是前人的经验总结了，我们称之为最佳实践。

本小节归纳了一些使用 Maven 依赖常见的技巧，方便用来避免和处理很多常见的问题。

## 9.1 排除依赖
依赖性传递会给项目隐式地引入很多依赖，这极大地简化了项目依赖的管理，但是有些时候这种特性也会带来问题。 

例如，当前项目有一个第三方依赖，而这个第三方依赖由于某些原因依赖了另一个类库的 SNAPSHOT 版本，那么这个 SNAPSHOT 就会成为当前项目的传递性依赖，而 SNAPSHOT 的不稳定性会直接影响到当前的项目。 

这时就需要排除掉该 SNAPSHOT，并且在当前项目中声明该类库的某个正式发布的版本。 还有一些情况，你可能也想要替换某个传递性依赖，比如 Sun JTA API，Hibernate 依赖于这个 JAR，但是由于版权的因素，该类库不在中央仓库中，而 Apache Geronimo 项目有一个对应的实现。 这时你就可以排除 Sun JAT API，再声明 Geronimo 的 JTA API 实现，见下列代码：
```xml
<!--  可选依赖不被传递 -->
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.juvenxu.mvnbook</groupId>
    <artifactId>project-a</artifactId>
    <version>1.0.0</version>

    <dependencies>
        <dependency>
            <groupId>com.juvenxu.mvnbook</groupId>
            <artifactId>project-b</artifactId>
            <version>1.0.0</version>
            
            <!-- 排除依赖声明 -->
            <exclusions>
              <exclusion>
                <groupId>com.juvenxu.mvnbook</groupId>
                <artifactId>project-c</artifactId>
              </exclusion>
            </exclusions>
            <!-- 排除依赖声明 -->
            
        </dependency>
        <dependency>
            <groupId>com.juvenxu.mvnbook</groupId>
            <artifactId>project-c</artifactId>
            <version>1.1.0</version>
        </dependency>
     <dependencies>
</project>
```
上述代码中，项目 A 依赖于项目 B，但是由于一些原因，不想引入传递性依赖 C，而是自己显式地声明对于项目 C 1.1.0 版本的依赖。 

代码中使用 `<exclusions>` 元素声明排除依赖，`<exclusions>` 可以包含一个或者多个 `<exclusion>` 子元素，因此可以排除一个或者多个传递性依赖。 需要注意的是，声明该子元素的时候只需要 `groupId` 和 `artifactId` ，而不需要 `version` 元素，这是因为只需要`groupId`和 `artifactId` 就能唯一定位依赖图中的某个依赖。   换句话说，Maven 解析后的依赖中，不可能出现 `groupId` 和 `artifactId`相同，但是`version` 不同的两个依赖，这一点在 6小节中已做过解释。

![3.png](https://github.com/dellnoantechnp/mvnbook/blob/main/Chapter5/.pic/3.png)


## 9.2 归类依赖
在 3.1 节中，有很多关于 Spring Framework 的依赖，它们分别是`org.springframework:spring-core:1.5.6`、`org.springframework:spring-beans:2.5.6`、`org.springframework:spring-context:2.5.6` 和 `org.springframework:spring-context-support:2.5.6`，它们是来自同一项目的不同模块。因此，所有这些依赖的版本都是相同的，而且可以预见，如果将来需要升级 `Spring Framework`，这些依赖的版本会一起升级。 

对于 `account-email` 中这些 Spring Framework 来说，也应该在一个唯一的地方定义版本，并且在 `<dependency>` 声明中引用这一版本。 这样，在升级 Spring Framework 的时候就只需要修改一处，实现方式见下列代码：
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.juvenxu.mvnbook.account</groupId>
    <artifactId>account-email</artifactId>
    <name>Account Email</name>
    <version>1.0.0-SNAPSHOT</version>

    <!--  pom 文件中定义maven属性  -->
    <properties>
      <springframework.version>2.5.6</springframework.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <!-- 引用 maven 属性 -->
            <version>${springframework.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-beans</artifactId>
            <!-- 引用 maven 属性 -->
            <version>${springframework.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <!-- 引用 maven 属性 -->
            <version>${springframework.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context-support</artifactId>
            <!-- 引用 maven 属性 -->
            <version>${springframework.version}</version>
        </dependency>
      </dependencies>
<project>
```
这里简单用到了 Maven 属性，首先使用 `properties` 元素定义 Maven 属性。 有了这个属性定义之后，Maven 运行的时候会将 POM 中的所有的 `${springframework.version}` 替换成实际值 2.5.6 。 

## 9.3 优化依赖
程序员也应该能够对 Maven 项目的依赖了然于胸，并对其进行优化，如 去除多余的依赖，显式地声明某些必要的依赖。

通过阅读本文前面的内容，应该能够了解到：Maven 会自动解析所有项目的直接依赖和传递性依赖，并且根据规则正确拍段每个依赖的范围，对于一些依赖冲突，也能进行调解，以确保任何一个构件只有唯一的版本在依赖中存在。

在这些工作之后，最后得到的那些依赖被称为解析依赖（Resolved Dependency）。 在*命令行*中可以运行如下命令查看当前项目的已解析依赖：
```shell
mvn dependency:list
........
[INFO]
[INFO] The following files have been resolved:
[INFO]    org.springframework:spring-beans:jar:2.5.6:compile
[INFO]    javax.activation:activation:jar:1.1:compile
[INFO]    commons-logging:commons-logging:jar:1.1.1:compile
[INFO]    org.slf4j:slf4j-api:jar:1.3.1:test
[INFO]    org.springframework:spring-context-support:jar:2.5.6:compile
[INFO]    aopalliance:aopalliance:jar:1.0:compile
[INFO]    junit:junit:jar:4.7:compile
[INFO]    com.icegreen:greenmail:jar:1.3.1b:test
[INFO]    org.springframework:spring-core:jar:2.5.6:compile
[INFO]    org.springframework:spring-context:jar:2.5.6:compile
[INFO]    javax.mail:mail:jar:1.5.0-b01:compile
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  37.590 s
```
将直接在当前项目 POM 声明的依赖定义为顶层依赖，而这些顶层依赖的依赖则定义为第二层依赖，以此类推。

当这些依赖经 Maven 解析后，就会构成一个依赖树。可以运行如下命令查看当前项目的依赖树：
```shell
mvn dependency:tree
..........
[INFO] Scanning for projects...
[INFO]
[INFO] -------------< com.juvenxu.mvnbook.account:account-email >--------------
[INFO] Building Account Email 1.0.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-dependency-plugin:2.8:tree (default-cli) @ account-email ---
[INFO] com.juvenxu.mvnbook.account:account-email:jar:1.0.0-SNAPSHOT
[INFO] +- org.springframework:spring-core:jar:2.5.6:compile
[INFO] |  \- commons-logging:commons-logging:jar:1.1.1:compile
[INFO] +- org.springframework:spring-beans:jar:2.5.6:compile
[INFO] +- org.springframework:spring-context:jar:2.5.6:compile
[INFO] |  \- aopalliance:aopalliance:jar:1.0:compile
[INFO] +- org.springframework:spring-context-support:jar:2.5.6:compile
[INFO] +- javax.mail:mail:jar:1.5.0-b01:compile
[INFO] |  \- javax.activation:activation:jar:1.1:compile
[INFO] +- junit:junit:jar:4.7:compile
[INFO] \- com.icegreen:greenmail:jar:1.3.1b:test
[INFO]    \- org.slf4j:slf4j-api:jar:1.3.1:test
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.110 s
```

使用 `dependency:list` 和 `dependency:tree` 可以帮助我们详细了解项目所有依赖的具体信息，在此基础上，还有`dependency:analyze` 工具可以帮助分析当前项目的依赖。

```log
[INFO] <<< maven-dependency-plugin:2.8:analyze (default-cli) < test-compile @ account-email <<<
[INFO]
[INFO]
[INFO] --- maven-dependency-plugin:2.8:analyze (default-cli) @ account-email ---
WARNING] Used undeclar
[WARNING]    org.springframework:spring-context:jar:2.5.6:compile
[WARNING] Unused declared dependencies found:
[WARNING]    org.springframework:spring-core:jar:2.5.6:compile
[WARNING]    org.springframework:spring-beans:jar:2.5.6:compile
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

为了说明该工具的用途，先将 `spring-context` 依赖删除，然后构建项目，发现编译、测试和打包都不会有问题。  通过分析依赖树，可以看到 `spring-context` 是 `spring-context-support` 的依赖，因此会得以传递到项目的 `classpath` 中。

该结果中重要是两个部分。 首先是 `Used undeclar` 部分，意思是项目中使用到的，但是没有显式声明的依赖，这种依赖意味着潜在的风险，这种依赖是通过直接依赖传递进来的，当升级直接依赖的时候，相关传递性依赖的版本也可能发生变化，这种变化不易察觉，但是可能导致当前项目出错。  ** 因此，显式声明任何项目中直接用到的依赖。**

结果中还有一个重要的部分是 `Unused declared dependencies found:`，意思是项目中未使用的，但是显式声明的依赖，这里有`spring-core` 和 `spring-beans`，需要注意的是