在这个技术飞速发展的世代，各类用户对软件的要求越来越高，软件本身也变得越来越复杂。因此。软件设计人员往往会采用各种方式对软件划分模块，以得到更清晰的设计及更高的重用性。

之前，我们的背景案例账户注册服务就被划分成了`account-email`、`account-persist` 等五个模块。  Maven 的聚合特性能够把项目的各个模块聚合在一起构建，而Maven 的继承特性则能帮助抽取各模块相同的依赖和插件等配置，在简化 POM 的同时，还能促进各个模块配置的一致性。

# 1.  account-persist
在讨论多模块 Maven 项目的聚合与继承之前，本文引入账户注册服务的`account-persist`模块。 该模块负责账户数据的持久化，以 XML 文件的形式保存账户数据，并支持账户的创建、读取、更新、删除等操作。

## 1.1 account-persist 的 POM
首先，看一下`account-persist`模块的POM文件：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
         <modelVersion>4.0.0</modelVersion>
         <groupId>com.juvenxu.mvnbook.account</groupId>
         <artifactId>account-persist</artifactId>
         <name>Account Persist</name>
         <version>1.0.0-SNAPSHOT</version>

         <dependencies>
            <dependency>
                <groupId>dom4j</groupId>
                <artifactId>dom4j</artifactId>
                <version>1.6.1</version>
            </dependency>
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
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
                <version>4.7</version>
                <scope>test</scope>
            </dependency>
        </dependencies>

        <build>
            <testResources>
                <testResource>
                    <directory>src/test/resources</directory>
                    <filtering>true</filtering>
                </testResource>
            </testResources>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <configuration>
                        <source>1.5</source>
                        <target>1.5</target>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-resources-plugin</artifactId>
                    <configuration>
                        <encoding>UTF-8</encoding>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </project>
```
该模块的坐标为`com.juvenxu.mvnbook.account:account-persist:1.0.0-SNAPSHOT`，回顾之前小节，能够发现，该模块`groupId`和`version`与`account-email`模块完全一致，而且`artifactId`也有相同的前缀。 一般来说，一个项目的子模块都应该使用同样的`groupId`，如果它们一起开发和发布，还应该使用同样的`version`，此外，它们的`artifactId`还应该使用一致的前缀，以便区分同类型的其他项目。

POM 中配置了一些依赖。 其中，`dom4j`是用来支持 XML 操作的； 接下来几个依赖与`account-email` 中一样，它们主要用来支持依赖注入； 最后是一个测试范围的 junit 依赖，用来支持单元测试。

接着是`<build>`元素，它先是包含了一个`<testResources>`子元素，这是为了开启资源过滤。 稍后讨论`account-persist`单元测试的时候，我们会详细介绍。

`<build>`元素下还包含了两个插件的配置。首先是配置`maven-compiler-plugin`支持 Java1.5，我们知道，虽然这里没有配置插件版本，但是由于`maven-compiler-plugin`是核心插件，它的版本已经在超级POM中设定了。 此外，如果这里不配置`groupId`，Maven 也会使用默认的 groupId`org.apache.maven.plugins`。 除了`maven-compiler-plugin`，这里还配置了`maven-resources-plugin`使用 UTF-8 编码处理资源文件。

## 1.2 account-persist 的主代码
account-persist 的Java主代码位于默认的`src/main/java`目录，包含`Account.java`、`AccountPersistService.java`、`AccountPersistSrviceImpl.java` 和 `AccountPersistException.java`四个文件，他们的包名都是`com.juvenxu.mvnbook.account.persist`，该包名与`account-persist`的groupId`com.juvenxu.mvnbook.account`及artifactId`account-persist`对应。

Account 类定义了账户的简单模型，它包括`id`、`name`等字段，并为每个字段提供了一组`getter`和`setter` 方法，如下：
```java
package com.juvenxu.mvnbook.account.persist;

public class Account {
    private String id;
    private String name;
    private String email;
    private String password;
    private boolean activated;

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }
}
```

`account-persist`对外提供的服务在接口`AccountPersistService`中定义，其方法对应了账户的增删改查：
```java
package com.juvenxu.mvnbook.account.persist;

public interface AccountPersistService{
    Account createAccount(Account account) throws AccountPersistException;

    Account readAccount(String id) throws AccountPersistException;

    Account updateAccount(Account account) throws AccountPersistException;

    void deleteAccount(String id) throws AccountPersistException;
}
```

其中 AccountPersistException 定义如下:
```java
package com.juvenxu.mvnbook.account.persist;

public class AccountPersistException extends Exception {

    public AccountPersistException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

当增、删、改、查操作发生异常的时候，该服务则抛出`AccountPersistException` 异常。

`AccountPersistService` 对应的实现类为`AccountPersistServiceImpl`，它通过操作 XML 文件来实现账户数据的持久化。 

首先看一下该类的两个私有方法：`readDocument()`和`writeDocument()`：
```java
private String file;

private SAXReader reader = new SAXReader();

private Document readDocument() throws AccountPersistException {
    File dataFile = new File(file);

    if (!dataFile.exists()){
        dataFile.getParentFile().mkdirs();
        Document doc = DocumentFactory.getInstance().createDocument();
        Element rootEle = doc.addElement(ELEMENT_ROOT);
        rootEle.addElement(ELEMENT_ACCOUNTS);
        writeDocument(doc);
    }

    try {
        return reader.read(dataFile);
    } catch (DocumentException e) {
        throw new AccountPersistException("Unable to read persist data xml file!", e);
    }
}

private void writeDocument(Document doc) throws AccountPersistException{
    Writer out = null;

    try {
        out = new OutputStreamWriter(new FileOutputStream(file),"utf-8");
        XMLWriter writer = new XMLWriter(out, OutputFormat.createPrettyPrint());
        writer.write(doc);
    } catch (IOException e) {
        throw new AccountPersistException("Unable to write persist data xml file!", e);
    } finally {
        try {
            if (out != null){
                out.close();
            }
        } catch (IOException e) {
            throw new AccountPersistException("Unable to close persist data xml file writer!", e);
        }
    }
}
```
先看`writeDocument()`方法。该方法首先使用变量 `file` 构建一个文本输出流，`file`是`AccountPersistServiceImpl`的一个私有变量，它的值通过 SpringFramework 注入。 得到输出流后，该方法再使用 DOM4J 创建的 `XMLWriter`，这里的`OutputFormat.createPrettyPrint()`用来创建一个带缩进及换行的友好格式。 得到`XMLWriter`后，就调用其`write()`方法，将 Document 写入到文件中。 该方法的其他代码用做处理流的关闭及异常处理。

`readDocument()`方法与`writeDocument()`对应，它负责从文件中读取 XML 数据，也就是 Document对象。 不过，在这之前，该方法首先会检查文件是否存在，如果不存在，则需要初始化一个 XML 文档，于是借助`DocumentFactory`创建一个`Document`对象，接着添加 XML 元素，再把这个不包含任何账户数据的 XML 文档写入到文件中。 如果文件已经被初始化了，则该方法使用 `SAXReader` 读取文件至 Document对象。

用来存储账户数据的 XML 文件结构十分简单，如下是一个包含一个*账户数据的文件*：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<account-persist>
    <accounts>
        <account>
            <id>juven</id>
            <name>Juven xu</name>
            <email>juven@changeme.com</email>
            <password>this_should_be_encrypted</password>
            <activated>false</activated>
        </account>
    </accounts>
</account-persist>
```

这个 XML 文件的根元素是`<account-persist>`，其下是`<accounts>`元素，`<accounts>`可以包含零个或者多个`<account>`元素，每个`<account>`元素代表一个账户，其子元素表示该账户的`<id>`、姓名、电子邮件、密码以及是否被激活等信息。

现在看一下`readAccount()`方法是如何从 XML 文档读取并构建 Account对象的：
```java
public Account readAccount(String id) throws AccountPersistException {
    Document doc = readDocument();

    Element accountsEle = doc.getRootElement().element(ELEMENT_ACCOUNTS);

    for (Element accountEle : (List<Element>) accountsEle.elements()) {
        if (accountEle.elementText(ELEMENT_ACCOUNT_ID).equas(id)) {
            return buildAccount(accountEle);
        }
    }
    return null;
}

private Account buildAccount(Element element) {
    Account account = new Account();

    account.setId(element.elementText(ELEMENT_ACCOUNT_ID));
    account.setName(element.elementText(ELEMENT_ACCOUNT_NAME));
    account.setEmail(element.elementText(ELEMENT_ACCOUNT_EMAIL));
    account.setPassword(element.elementText(ELEMENT_ACCOUNT_PASSWORD));
    account.setActivated("true".equals(element.elementText(ELEMENT_ACCOUNT_ACTIVATED)));

    return account;
}
```
`readAccount()` 方法首先获取 XML 文档的 Document 对象，接着获取根元素的`accounts`子元素，这里的`ELEMENT_ACCOUNTS`是一个静态常量，其值就是`accounts`。 接着遍历`account`的子元素，如果当前子元素的`id`与要读取的账户的`id`一致，并且基于该子元素构建`Account`对象，这也就是`buildAccount()`方法。

在`buildAccount()`方法中，先创建一个`Account`对象，然后当前 XML 元素的子元素的值设置该对象。 Element 的 `elementText()` 方法能够根据子元素名称返回子元素的值，与`ELEMENT_ACCOUNTS`类似，这里使用了一些静态常量表示`id`、`name`、`email` 等 XML 中的元素名称。`Account` 对象设置完后就直接返回，如果 XML 文档中没有匹配的`id`，则返回 `null`。

为了不使本节内容过于冗长，这里就不再介绍`createAccount()`、`updateAccount()` 和 `deleteAccount` 几个方法的实现。这部分见 [Github 的示例代码](https://github.com/dellnoantechnp/mvnbook/)。

除了 Java 代码，`account-persist`模块还需要一个`SpringFramework` 的配置文件，它位于`src/main/resources` 目录。

account-persist 的 Spring 配置文件：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="propertyConfigurer" class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
        <property name="location" value="classpath:account-serivce.properties"/>
    </bean>
    
    <bean id="accountPersistService" class="com.juvenxu.mvnbook.account.persist.AccountPersistServiceImpl">
        <property name="file" value="${persis.file}"/>
    </bean>
</beans>
```

该配置文件首先配置了一个`id` 为`propertyConfigurer` 的 bean，其实现为`PropertyPlaceholderConfigurer`，作用是从项目`classpath`载入名为`account-service.properties`的配置文件。 

随后的 bean 是`accountPersistService`，实现为`AccountPersistServiceImpl`，同时这里使用属性`persist.file` 配置其 `file` 字段的值。 也就是说，XML 数据文档的位置是由项目 `classpath` 下 `account-service.properties` 文件中的`persist.file` 属性的值配置的。

## 1.3 account-persist 的测试代码
定义并实现了账户的增、删、改、查操作，当然也不能少了响应的测试。 测试代码位于`src/test/java/` 目录下，测试资源文件位于`src/test/resources/` 目录下。

上一节 SpringFramework 的定义要求项目 classpath 下有一个名为`account-service.properties` 的文件，并且该文件中需要包含一个`persist.file`属性，以定义文件存储的位置。 为了能够测试账户数据的持久化，在测试资源目录下创建属性文件`account-service.properties`。 其内容如下：
```properties
persist.file=${project.build.testOutputDirectory}/persist-data.xml
```

该文件只包含一个`persist.file` 属性，表示存储账户数据的文件路径，但是它的值并不是简单的文件路径，其值而是包含了`${project.build.testOutputDirectory}` 。 **这是一个 Maven 属性，这里读者暂时只要了解该属性表示了 Maven 的测试输出目录，其默认的地址为项目根目录下的 `target/test-classes` 文件夹。**  也就是说，在测试中使用测试输出目录下的`persist-data.xml` 文件存储账户数据。

现在编写测试用例测试`AccountPersistService` 。这里重点关注`test_readAccount()` 方法。
```java
package com.juvenxu.mvnbook.account.persist;

import static org.junit.Assert.*;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
//import org.springframework.util.Assert;


public class AccountPersistServiceTest {

    private AccountPersistService service;

    @Before
    public void prepare() throws Exception{
        File persistDataFile = new File("target/test-classes/persist-data.xml");
        if (persistDataFile.exists()){
            persistDataFile.delete();
        }

        ApplicationContext ctx = new ClassPathXmlApplicationContext("account-persist.xml");

        service = (AccountPersistService)ctx.getBean("accountPersistService");

        Account account = new Account();
        account.setId("juven");
        account.setName("Juven Xu");
        account.setEmail("juven@changeme.com");
        account.setPassword("this_should_be_encrypted");
        account.setActivated(true);

        service.createAccount(account);
    }

    @Test
    public void test_readAccount() throws Exception{
        Account account = service.readAccount("juven");
        
        assertNotNull(account);
        assertEquals("juven", account.getId());
        assertEquals("Juven Xu", account.getName());
        assertEquals("juvenxu@changeme.com", account.getEmail());
        assertEquals("this_should_be_encrypted", account.getPassword());
        assertTrue(account.isActivated());
//        Assert.notNull(account);
    }
}
```

该测试用例使用与`AccountPersistService`一致的包名，它有两个方法: `prepare()` 与 `test_readAccount()` 。前者使用了 `@Before` 注解，表示在执行测试用例之前执行该方法。 它首先检查数据文件是否存在，如果存在则删除以得到个干净的测试环境，接着使用`account-persist.xml`配置文件初始化 SpringFramework 的 IoC 容器，再从容器中获取要测试的`AccountPersistService` 对象。 最后`prepare()` 方法创建一个 `Account` 对象，设置对象字段的值之后，使用 `AccountPersistService` 的 `createAccount()` 方法将其持久化。

使用`@Test`注解的 `testReadAccount()` 方法就是要测试的方法。 该方法非常简单，它根据 id 使用`AccountPersistService` 读取`Account` 对象，然后检查该对象不为空，并且每个字段的值必须与刚才插入的对象值完全一致，即可通过测试。

该测试用例遵守了测试接口而不测试实现这一原则。 也就是说，测试代码不能引用实现类，由于测试是从接口用户的角度编写的，这样就能保证接口的用户无须知晓接口的实现细节，既保证了代码的解耦，也促进了代码的设计。

# 2. 聚合
到目前为止，本文实现了注册服务的两个模块，它们分别是之前的 `account-email` 和 本文实现的`account-persist` 。 这时，一个简单的需求就自然而然的显现出来：**我们会想要一次构建两个项目，而不是到两个模块的目录下分别执行 mvn 命令。** Maven **聚合**（或者称为多模块）这一特性就是为该需求服务的。

为了能够使用一条命令就能构建`account-email` 和`account-persist` 两个模块，我们需要创建一个额外的包名为`account-aggregator` 的模块，然后通过该模块构建整个项目的所有模块。 `account-aggregator` 本身作为一个 Maven 项目，它必须要有自己的 POM，不过，同时作为一个聚合项目，其 POM 又有特殊的地方。 

如下为 `account-aggregator` 的 `pom.xml` 内容：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.juvenxu.mvnbook.account</groupId>
    <artifactId>account-aggregator</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>Account Aggregator</name>

    <modules>
        <module>account-email</module>
        <module>account-persist</module>
    </modules>
</project>
```

> 附 IDEA 将普通项目转为 Maven 项目方法：
> ![1.png](https://raw.githubusercontent.com/dellnoantechnp/mvnbook/main/Chapter8/.pic/1.png)
> ![2.png](https://raw.githubusercontent.com/dellnoantechnp/mvnbook/main/Chapter8/.pic/2.png)

上述 POM 依旧使用了账户注册服务共同的 groupId `com.juvenxu.mvnbook.account`，artifactId 为独立的`account-aggregator`，版本也与其他两个模块一致。  这里的第一个特殊的地方为`<packaging>`元素，其值为 `pom`。 回顾`account-email`和`account-persist`，它们都没有声明`packaging`。 即使用了默认值`jar`。  **对于聚合模块来说，其打包方式 `<packaging>` 的值必须为 `pom`，否则就无法构建。**

`<name>` 元素时为了给项目提供一个更容易阅读的名字。 **后面是本文之前都没有提到过的元素`<modules>`，这是实现聚合的最核心的配置。 用户可以通过在一个打包方式为 pom 的 Maven 项目中声明任意数量的 `<module>` 元素来实现模块的聚合。 这里每个`<module>`的值都是一个当前 POM 的相对目录。**  这两个目录各自包含了`pom.xml`、`src/main/java/` 、`src/test/java/` 等内容，离开`account-aggregator` 也能独立构建。

`account-aagregator` 的 pom 中配置的`module` 元素值是子模块的目录名称，并不是子模块的 artifactId，所以应当与目录名称一致，如果聚合模块与子模块在目录层级平级的话，那么`<module>`元素的值应当配置为例如`<module>../account-email</module>`的方式。

为了方便用户构建项目，通常将聚合模块放在项目的最顶层，其他模块则作为聚合模块的子目录存在，这样用户得到源码的时候，第一眼发现的就是聚合模块的 POM，不用从多个模块中去寻找聚合模块来构建整个项目。

**关于目录结构还需要注意的是，聚合模块与其他模块的目录结构并非一定是父子关系。**

```log
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO]
[INFO] Account Email                                                      [jar]
[INFO] Account Persist                                                    [jar]
[INFO] Account Aggregator                                                 [pom]
[INFO]
[INFO] -------------< com.juvenxu.mvnbook.account:account-email >--------------
[INFO] Building Account Email 1.0.0-SNAPSHOT                              [1/3]
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ account-email ---
[INFO] Deleting D:\ProjectWorkspace\****\mvnbook\account-email\target
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ account-email ---
[INFO] Copying 2 resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ account-email ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 3 source files to D:\ProjectWorkspace\****\mvnbook\account-email\target\classes
[INFO]
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ account-email ---
[INFO] Copying 1 resource
[INFO]
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ account-email ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 1 source file to D:\ProjectWorkspace\****\mvnbook\account-email\target\test-classes
[INFO]
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ account-email ---
[INFO] Surefire report directory: D:\ProjectWorkspace\****\mvnbook\account-email\target\surefire-reports


[INFO]
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ account-email ---
[INFO] Building jar: D:\ProjectWorkspace\****\mvnbook\account-email\target\account-email-1.0.0-SNAPSHOT.jar
[INFO]
[INFO] --- maven-source-plugin:2.1.1:jar-no-fork (attach-sources) @ account-email ---
[INFO] Building jar: D:\ProjectWorkspace\****\mvnbook\account-email\target\account-email-1.0.0-SNAPSHOT-sources.jar
[INFO]
[INFO] --- maven-install-plugin:2.4:install (default-install) @ account-email ---
[INFO] Installing D:\ProjectWorkspace\****\mvnbook\account-email\target\account-email-1.0.0-SNAPSHOT.jar to C:\Users\*****\repository\com\juvenxu\mvnbook\account\account-email\1.0.0-SNAPSHOT\account-email-1.0.0-SNAPSHOT.jar
[INFO] Installing D:\ProjectWorkspace\****\mvnbook\account-email\pom.xml to C:\Users\****\repository\com\juvenxu\mvnbook\account\account-email\1.0.0-SNAPSHOT\account-email-1.0.0-SNAPSHOT.pom
[INFO] Installing D:\ProjectWorkspace\****\mvnbook\account-email\target\account-email-1.0.0-SNAPSHOT-sources.jar to C:\Users\****\repository\com\juvenxu\mvnbook\account\account-email\1.0.0-SNAPSHOT\account-email-1.0.0-SN
APSHOT-sources.jar
[INFO]
[INFO] ------------< com.juvenxu.mvnbook.account:account-persist >-------------
[INFO] Building Account Persist 1.0.0-SNAPSHOT                            [2/3]
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ account-persist ---
[INFO] Deleting D:\ProjectWorkspace\****\mvnbook\account-persist\target
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ account-persist ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 2 resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ account-persist ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 4 source files to D:\ProjectWorkspace\****\mvnbook\account-persist\target\classes
[INFO]
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ account-persist ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO]
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ account-persist ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 1 source file to D:\ProjectWorkspace\****\mvnbook\account-persist\target\test-classes
[INFO]
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ account-persist ---
[INFO] Surefire report directory: D:\ProjectWorkspace\****\mvnbook\account-persist\target\surefire-reports



[INFO]
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ account-persist ---
[INFO] Building jar: D:\ProjectWorkspace\****\mvnbook\account-persist\target\account-persist-1.0.0-SNAPSHOT.jar
[INFO]
[INFO] --- maven-install-plugin:2.4:install (default-install) @ account-persist ---
[INFO] Installing D:\ProjectWorkspace\****\mvnbook\account-persist\target\account-persist-1.0.0-SNAPSHOT.jar to C:\Users\****\repository\com\juvenxu\mvnbook\account\account-persist\1.0.0-SNAPSHOT\account-persist-1.0.0-SN
APSHOT.jar
[INFO] Installing D:\ProjectWorkspace\****\mvnbook\account-persist\pom.xml to C:\Users\****\repository\com\juvenxu\mvnbook\account\account-persist\1.0.0-SNAPSHOT\account-persist-1.0.0-SNAPSHOT.pom
[INFO]
[INFO] -----------< com.juvenxu.mvnbook.account:account-aggregator >-----------
[INFO] Building Account Aggregator 1.0-SNAPSHOT                           [3/3]
[INFO] --------------------------------[ pom ]---------------------------------
[INFO]
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ account-aggregator ---
[INFO]
[INFO] --- maven-install-plugin:2.4:install (default-install) @ account-aggregator ---
[INFO] Installing D:\ProjectWorkspace\****\mvnbook\pom.xml to C:\Users\*****\repository\com\juvenxu\mvnbook\account\account-aggregator\1.0-SNAPSHOT\account-aggregator-1.0-SNAPSHOT.pom
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO]
[INFO] Account Email 1.0.0-SNAPSHOT ....................... SUCCESS [  3.119 s]
[INFO] Account Persist 1.0.0-SNAPSHOT ..................... SUCCESS [  0.952 s]
[INFO] Account Aggregator 1.0-SNAPSHOT .................... SUCCESS [  0.040 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  4.301 s
[INFO] Finished at: 2021-09-01T17:51:19+08:00
[INFO] ------------------------------------------------------------------------
```

Maven 会首先解析聚合模块的 POM、分析要构建的模块、并计算出一个反应堆构建顺序，然后根据这个顺序依次构建各个模块。 反应堆是所有模块组成的一个构建结构。 后面小节会详细讲述 Maven 的反应堆。

# 3. 继承
到目前为止，我们已经能够使用 Maven 的聚合特性通过一条命令同时构建两个模块，不过这仅仅解决了多模块 Maven 项目的一个问题。那么多模块的项目还有什么问题呢？ 

我们发现`account-email` 和 `email-persist` 两个模块的 POM 有着很多相同的配置，例如 `groupId` 和 `version`，也有相同的`spring-core`、`spring-beans`、`spring-context`  和 `junit`依赖，还有相同的 `maven-compiler-plugin` 与 `maven-resources-plugin`配置。

在面向对象世界中，程序员可以使用类继承在一定程度上消除重复，在 Maven 世界里，也有类似的机制让我们抽取出重复的配置也就是 POM 的继承。

## 3.1 account-parent
面向对象设计中，程序员可以建立一种类的父子结构，然后在父类中声明一些字段和方法供子类继承，这样就可以做到“一处声明，多处使用”。 类似的，我们需要创建 POM 的父子结构，然后在父 POM 中声明一些配置供子 POM 继承，以实现目的。

我们继续以账户注册服务为基础，在`account-aggregator`下创建一个名为`account-parent`的子目录，然后在该子目录下建立一个所有除`account-aagregator`之外模块的父模块。

为此，在该目录创建一个`pom.xml`文件，如下：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
         <modelVersion>4.0.0</modelVersion>
         <groupId>com.juvenxu.mvnbook.account</groupId>
         <artifactId>account-parent</artifactId>
         <version>1.0.0-SNAPSHOT</version>
         <packaging>pom</packaging>
         <name>Account Parent</name>
     </project>
```

该 POM 十分简单，它使用了与其他模块一致的`groupId`和`version`，使用的`artifactId`为`account-parent`表示这是一个父模块。 需要特别注意的是，它的`packaging`为`pom`，这一点与聚合模块一样，作为父模块的 POM，其打包类型也必须为`pom`。

由于父模块只是为了帮助消除配置的重复，因此它本身不包含除 POM 之外的项目文件，也就不需要`src/main/java/`之类的目录结构了。

有了父模块，就需要让其他模块来继承它。 首先将`account-email`的 POM 修改如下：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
	    <groupId>com.juvenxu.mvnbook.account</groupId>
	    <artifactId>account-parent</artifactId>
	    <version>1.0.0-SNAPSHOT</version>
	    <relativePath>../account-parent/pom.xml</relativePath>
	</parent>

	<artifactId>account-email</artifactId>
	<name>Account Email</name>

	<dependencies>
		....
	</dependencies>

	<build>
		<plugins>
			...
		</plugins>
	</build>
</project>
```

上述 POM 中使用`<parent>`元素声明父模块，其下的子元素`<groupId>`、`<artifactId>`和`<version>`指定了父模块的坐标，这三个元素是必须的。 元素`<relativePath>`表示父模块 POM 的相对路径，该例中的`../account-parent/pom.xml`表示父 POM 的位置在与`account-email/`目录平行的`account-parent/`目录下。 当项目构建时，Maven 会首先根据`relativePath`检查父 POM，如果找不到，再从本地仓库查找。`relativePath`的默认值是`../pom.xml`，也就是说，Maven 默认父 POM 在上一层目录下。

正确设置`relativePath`非常重要。 这个更新过的 POM 没有为`account-email`声明`groupId`和`version`，不过这并不代表没有`groupId`和`version`。 实际上，这个子模块隐式地从父模块继承了这两个元素，这也就消除了一些不必要的配置。 

对于`artifactId`来说，子模块应该显式声明，一方面，如果完全继承，会造成坐标冲突； 另一方面，即使使用不同的`groupId`和`version`，同样的`artifactId`容易造成混淆。

为了节省篇幅，上述 POM 中省略了依赖配置和插件配置，稍后本文会介绍如何将共同的依赖配置提取到父模块中。

同理，`account-persist`更新后的 POM 如下：
```xml
?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
	    <groupId>com.juvenxu.mvnbook.account</groupId>
	    <artifactId>account-parent</artifactId>
	    <version>1.0.0-SNAPSHOT</version>
	    <relativePath>../account-parent/pom.xml</relativePath>
	</parent>

    <artifactId>account-persist</artifactId>
    <name>Account Persist</name>

    <dependencies>
		....
	</dependencies>

	<build>
		<testResources>
			<testResource>
				<directory>src/test/resources</directory>
				<filtering>true</filtering>
			</testResource>
		</testResources>

		<plugins>
			...
		</plugins>
	</build>
</project>
```

最后，同样还需要把`account-parent`加入到聚合模块`account-aggregator`中：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.juvenxu.mvnbook.account</groupId>
    <artifactId>account-aggregator</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>Account Aggregator</name>

    <modules>
        <module>account-email</module>
        <module>account-persist</module>
        <module>account-parent</module>
    </modules>
</project>
```

---
## 3.2 可继承的 POM 元素
上一小节看到，`groupId`和`version`是可以被继承的，那么还有哪些元素可以被继承呢？ 一下是一个完整的列表，并附带了简单的说明：

- [x] groupId: 项目组ID，项目坐标的核心元素。
- [x] version: 项目版本，项目坐标的核心元素。
- [x] description: 项目的描述信息。
- [x] organization: 项目的组织信息。
- [x] inceptionYear: 项目的创始年份。
- [x] url: 项目的 URL 地址。
- [x] developers: 项目的开发者信息。
- [x] contributors: 项目的贡献者信息。
- [x] destributionManagement: 项目的部署配置。
- [x] issueManagement: 项目的缺陷跟踪系统信息。
- [x] ciManagement: 项目的持续集成系统信息。
- [x] scm: 项目的版本控制系统信息。
- [x] mailingLists: 项目的邮件列表信息。
- [x] properties: 自定义的 Maven 属性。
- [x] dependencies: 项目的依赖配置。
- [x] dependencyManagement: 项目的依赖管理配置。
- [x] repositories: 项目的仓库配置。
- [x] build: 包括项目的源码目录配置、输出目录配置、插件配置、插件管理配置等。
- [x] reporting: 包括项目的报告输出目录配置、报告插件配置等。
 
## 3.3 依赖管理
上述信息我们知道，也可以在父模块中配置`<dependencies>`依赖信息，但是并不推荐这样做。Maven 有提供更好的依赖继承管理。

Maven 提供的`<dependencyManagement>`元素既能让子模块继承到父模块的依赖配置，又能保证子模块依赖使用的灵活性。 在`<dependencyManagement>`元素下的依赖声明不会引入实际的依赖，不过它能够约束`<dependencies>`下的依赖使用。 例如，可以在`account-parent`中加入这样的`<dependencyManagement>`配置：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
         <modelVersion>4.0.0</modelVersion>
         <groupId>com.juvenxu.mvnbook.account</groupId>
         <artifactId>account-parent</artifactId>
         <version>1.0.0-SNAPSHOT</version>
         <packaging>pom</packaging>
         <name>Account Parent</name>
         <properties>
         	<springframework.version>2.5.6</springframework.version>
         	<junit.version>4.7</junit.version>
         </properties>

         <dependencyManagement>
         	<dependencies>
	         	<dependency>
	         		<groupId>org.springframework</groupId>
	         		<artifactId>spring-core</artifactId>
	         		<version>${springframework.version}</version>
	         	</dependency>
	         	<dependency>
	         		<groupId>org.springframework</groupId>
	         		<artifactId>spring-beans</artifactId>
	         		<version>${springframework.version}</version>
	         	</dependency>
	         	<dependency>
	         		<groupId>org.springframework</groupId>
	         		<artifactId>spring-context</artifactId>
	         		<version>${springframework.version}</version>
	         	</dependency>
	         	<dependency>
	         		<groupId>org.springframework</groupId>
	         		<artifactId>spring-context-support</artifactId>
	         		<version>${springframework.version}</version>
	         	</dependency>
	         	<dependency>
	         		<groupId>junit</groupId>
	         		<artifactId>junit</artifactId>
	         		<version>${junit.version}</version>
	         		<scope>test</scope>
	         	</dependency>
	         </dependencies>
         </dependencyManagement>
     </project>
```

这里使用`<dependencyManagement>`声明的依赖既不会给`account-parent`引入依赖，也不会给它的子模块引入依赖，不过这段配置是会被继承的。

下面我们来修改`account-email`的 POM 配置如下：
```xml
<properties>
	<javax.mail.version>1.4.1</javax.mail.version>
	<greenmail.version>1.5.0-b01</greenmail.version>
</properties>

<dependencies>
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-core</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-beans</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-context</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-context-support</artifactId>
	</dependency>
	<dependency>
		<groupId>junit</groupId>
		<artifactId>junit</artifactId>
	</dependency>
	<dependency>
		<groupId>javax.mail</groupId>
		<artifactId>mail</artifactId>
		<version>${javax.mail.version}</version>
	</dependency>
	<dependency>
		<groupId>com.icegreen</groupId>
		<artifactId>greenmail</artifactId>
		<version>${greenmail.version}</version>
		<scope>test</scope>
	</dependency>
</dependencies>
```

上述 POM 中的依赖配置较原来简单了一些，所有的`springframework`依赖只配置了`groupId`和`artifactId`，省去了`version`，而`junit`依赖不仅省去了`version`，还省去了依赖范围`<scope>`。 这些信息可以省略是因为`account-email`继承了`account-parent`中的`<dependencyManagement>`配置，完整的依赖声明已经包含在父POM中，子模块只需要配置简单的`groupId`和`artifactId`就能够获得对应的依赖信息，从而引入正确的依赖。

使用这种依赖管理机制似乎不能减少太多的POM配置，不过还是强烈推荐采用这种方法。其主要原因在于父POM中使用`<dependencyManagement>`声明依赖能够统一项目范围中依赖的版本，当依赖版本在父POM中声明之后，子模块在使用依赖的时候就无需声明版本，也就不会发生多个子模块使用依赖版本不一致的情况。这可以帮助降低依赖冲突的几率。

如果子模块不声明依赖的使用，即使该依赖已经在父POM的`<dependencyManagement>`中声明了，也不会产生任何实际效果，如`account-persist`的POM：
```xml
<properties>
	<dom4j.version>1.6.1</dom4j.version>
</properties>

<dependencies>
	<dependency>
		<groupId>dom4j</groupId>
		<artifactId>dom4j</artifactId>
		<version>${dom4j.version}</version>
	</dependency>
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-core</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-beans</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-context</artifactId>
	</dependency>
	<dependency>
		<groupId>junit</groupId>
		<artifactId>junit</artifactId>
	</dependency>
</dependencies>
```

这里没有声明`spring-context-support`，那么该依赖就不会引入。这正是`<dependencyManagement>`的灵活性所在。

在第五章介绍依赖范围的时候提到了名为`import`的依赖范围，推迟到现在介绍是因为该范围的依赖只在`<dependencyManagement>`元素下才有效果，使用该范围的依赖通常指向一个POM，作用是将目标POM中的`<dependencyManagement>`配置导入并合并到当前POM的`<dependencyManagement>`元素中。

例如想要在另一个模块中使用上面POM中完全一样的`<dependencyManagement>`配置，除了复制配置或者继承这两种方式之外，还可以使用`import`范围依赖将这一配置导入：
```xml
<dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>com.juvenxu.mvnbook.account</groupId>
			<artifactId>account-parent</artifactId>
			<version>1.0-SNAPSHOT</version>
			<type>pom</type>
			<scope>import</scope>
		</dependency>
	</dependencies>
</dependencyManagement>
```
注意，上述代码中依赖的`type`值为`pom`，`import`范围依赖由于其特殊性，一般都是指向打包类型为`pom`的模块。  如果有多个项目，它们使用的依赖版本都是一致的，则就可以定义一个使用`<dependencyManagement>`专门管理依赖的POM，然后在各个项目中导入这些依赖管理配置。

---
## 3.4 插件管理
Maven 提供了`<dependencyManagement>`元素帮助管理依赖，类似地，Maven 也提供了`<pluginManagement>`元素帮助管理插件。  在钙元素中配置的依赖不会造成实际的插件调用行为，当POM中配置了真正的`plugin`元素，并且其`groupId`和`artifactId`与`<pluginManagement>`中配置的插件匹配时，`<pluginManagement>`的配置才会影响实际的插件行为。

如果一个项目中有很多子模块，并且需要得到所有这些模块的源码包，那么很显然，为所有模块重复类似的插件配置不是最好的办法。 这时更好的方法是在父POM中使用`<pluginManagement>`配置插件：
```xml
<build>
	<pluginManagement>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-source-plugin</artifactId>
				<version>2.1.1</version>
				<executions>
					<execution>
						<id>attach-sources</id>
						<phase>verify</phase>
						<goals>
							<goal>jar-no-fork</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</pluginManagement>
</build>
```

当子模块需要生成源码包的时候，只需要如下简单的配置：
```xml
<build>
	<pluginManagement>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-source-plugin</artifactId>
			</plugin>
		</plugins>
	</pluginManagement>
</build>
```

子模块声明使用了`maven-source-plugin`插件，同时又继承了父模块的`<pluginManagement>`配置，两者基于`groupId`和`artifactId`匹配合并之后就相当于第七章中的插件配置。

如果子模块不需要使用父模块中`<pluginManagement>`配置的插件，可以尽管将其忽略。 如果子模块需要不同的插件配置，则可以自行配置以覆盖父模块的`<pluginManagement>`配置。

有了`<pluginManagement>`元素，`account-email`和`account-persist`的POM也能得以简化了，它们都配置了`maven-compiler-plugin`和`maven-resources-plugin`。可以将这两个插件的配置移到`account-parent`的`<pluginManagement>`元素中：
```xml
<build>
	<pluginManagement>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<source>1.5</source>
					<target>1.5</target>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-resources-plugin</artifactId>
				<configuration>
					<encoding>UTF-8</encoding>
				</configuration>
			</plugin>
		</plugins>
	</pluginManagement>
</build>
```

`account-email`和`account-persist`两个模块可以完全地移除关于`maven-compiler-plugin`和`maven-resources-plugin`的配置，但它们仍能享受这两个插件的服务。  

这背后涉及了很多 Maven 机制，首先，内置的插件绑定关系将两个插件绑定到了`account-email`和`account-persist`的生命周期上； 其次，超级POM为这两个插件声明了版本；  最后，`account-parent`中的`<pluginManagement>`对这两个插件的行为进行了配置。

当项目中的多个模块有同样的插件配置时，应当将配置移到父POM的`<pluginManagement>`元素中。 即使各个模块对于同一插件的具体配置不尽相同，也应当使用父POM的`<pluginManagement>`元素统一声明插件的版本。 甚至可以要求将所有用到的插件的版本在父POM该元素中声明，子模块使用插件时不配置版本信息，这么做可以统一项目的插件版本，避免潜在的插件不一致或者不稳定问题，也更易于维护。

---
# 4. 聚合与继承的关系
基于前面三小节的内容，了解到多模块 Maven 项目中的聚合与继承其实是两个概念，其目的完全是不同的。 前者主要是为了方便快速构建项目，后者主要是为了消除重复配置。

对于模块聚合来说，它知道有哪些被聚合的模块，但那些被聚合的模块不知道这个聚合模块的存在。

对于继承关系的父POM来说，它不知道有哪些模块继承于它，但那些子模块都必须知道自己的父POM是什么。

如果非要说这两个特性的共同点，那么可以看到，聚合POM与继承关系中的父POM的`<packageing>`都必须是`pom`，同时，聚合模块与继承关系中的父模块除了 POM 之外都没有实际的内容。

![3.png](https://raw.githubusercontent.com/dellnoantechnp/mvnbook/main/Chapter8/.pic/3.png)

在现有的实际项目中，大家往往会发现一个POM即是聚合POM，又是父POM，这么做主要是为了方便。一般来说，融合使用`聚合`与`继承`也没有什么问题，例如可以将`account-aggregator`和`account-parent`合并成一个新的`account-parent`，其 POM 代码如下：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.juvenxu.mvnbook.account</groupId>
    <artifactId>account-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>Account Parent</name>
    <modules>
    	<module>account-email</module>
    	<module>account-persist</module>
    </modules>
    <properties>
    	<springframework.version>2.5.6</springframework.version>
    	<junit.version>4.7</junit.version>
    </properties>
    <dependencyManagement>
    	<dependencies>
	        <dependency>
	            <groupId>org.springframework</groupId>
	            <artifactId>spring-core</artifactId>
	            <!-- 引用属性定义的值 -->
	            <version>${springframework.version}</version>
	        </dependency>
	        <dependency>
	            <groupId>org.springframework</groupId>
	            <artifactId>spring-beans</artifactId>
	            <version>${springframework.version}</version>
	        </dependency>
	        <dependency>
	            <groupId>org.springframework</groupId>
	            <artifactId>spring-context</artifactId>
	            <version>${springframework.version}</version>
	        </dependency>
	        <dependency>
	            <groupId>org.springframework</groupId>
	            <artifactId>spring-context-support</artifactId>
	            <version>2.5.6</version>
	        </dependency>
	        <dependency>
	            <groupId>junit</groupId>
	            <artifactId>junit</artifactId>
	            <version>${junit.version}</version>
	            <scope>test</scope>
	        </dependency>
    	</dependencies>
	</dependencyManagement>
<build>
	<pluginManagement>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<source>1.5</source>
					<target>1.5</target>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-resources-plugin</artifactId>
				<configuration>
					<encoding>UTF-8</encoding>
				</configuration>
			</plugin>
		</plugins>
	</pluginManagement>
</build>
```

在该代码中可以看到，该POM的打包方式为`pom`，它包含了一个`<modules>`元素，表示用来聚合`account-persist`和`account-email`两个模块，它还包含了`properties`、`dependencyManagement`和`pluginManagement`元素供子模块继承。

相应的，`account-email`和`account-persist`的POM配置也要做微小的修改。 本来`account-parent`和它们位于同级目录，因此需要使用值为`../account-parent/pom.xml`的`<relativePaht>`元素。 现在新的 `account-parent`在上一层目录，这是 Maven 默认能识别的父模块位置，因此不再需要配置`<relativePath>`：
```xml
<parent>
	<groupId>com.juvenxu.mvnbook.account</groupId>
	<artifactId>account-parent</artifactId>
	<version>1.0.0-SNAPSHOT</version>
</parent>

<artifactId>account-email</artifactId>
<name>Account Email</name>
.....
```

---
# 5. 约定优于配置
Java 成功的重要原因之一就是它能屏蔽大部分操作系统的差异，XML流行的原因之一是所有语言都接受它。 Maven 当然还不能和这些既成功又成熟的技术想必，但Maven的用户都应该清楚，Maven提成“约定优于配置”（Convention Over Configuration），这是 Maven 最核心的设计理念之一。

那么为什么要使用约定而不是自己更灵活的配置呢 ？ 原因之一是，使用约定可以大量减少配置。 

使用 Ant 来完成清除构建目录、创建目录、编译代码、复制代码至目标目录，最后打包，需要至少几十行XML配置定义。但是做同样的事，Maven需要什么配置呢？  Maven 只需要一个最简单的POM：
```xml
<project>
	<moduelVersion>4.0.0</moduelVersion>
	<groupId>com.juvenxu.mvnbook</groupId>
	<artifactId>my-project</artifactId>
	<version>1.0</version>
</project>
```

这段配置简单的令人惊奇，但为了获得这样简洁的配置，用户是需要付出一定的代价的，那就是遵循Maven的约定。 Maven会假设用户的项目是这样的：

- [x] 源码目录为`src/main/java/`
- [x] 编译输出目录为`target/classes/`
- [x] 打包方式和为 jar
- [x] 包输出目录为`target/`

遵循约定虽然损失了一定的灵活性，用户不能随意安排目录结构，但是却能减少配置。 更重要的是，遵循约定能够帮助用户遵循构建标准。

有了 Maven 的约定，大家都知道什么目录放什么内容。此外，与 Ant 的自定义目标名称不同，Maven 在命令行暴露的用户接口是统一的，像`mvn clean install`这样的命令可以用来构件几乎任何的 Maven 项目。

也许有的读者会问，如果不想遵循约定该怎么办？ 这时，请首先问自己三遍，“你真的需要这么做嘛？” 如果仅仅是因为喜好，就不要耍个性，个性往往意味着牺牲通用性，意味着增加无畏的复杂度。

本文曾多次提到**超级POM**，任何一个Maven项目都隐式地继承自该POM，这点类似于任何一个Java类都隐式地继承于`Object`类一样。 因此，大量超级POM的配置都会被所有Maven项目继承，这些配置也就成为了Maven所提倡的约定。

对于Maven3，超级POM在文件`$MAVEN_HOME/lib/maven-model-builder-x.x.x.jar`中的`org/apache/maven/model/pom-4.0.0.xml`路径下。  对于Maven2，超级POM在文件`$MAVEN_HOME/lib/maven-x.x.x-uber.jar`中的`org/apache/maven/project/pom-4.0.0.xml`目录下。 *这里的`x.x.x`表示Maven的具体版本号。*

以下是取自 Maven3.3.9 版本的超级POM内容：
```xml
<?xml version="1.0" encoding="UTF-8"?>

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<!-- START SNIPPET: superpom -->
<project>
  <modelVersion>4.0.0</modelVersion>

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

  <pluginRepositories>
    <pluginRepository>
      <id>central</id>
      <name>Central Repository</name>
      <url>https://repo.maven.apache.org/maven2</url>
      <layout>default</layout>
      <snapshots>
        <enabled>false</enabled>
      </snapshots>
      <releases>
        <updatePolicy>never</updatePolicy>
      </releases>
    </pluginRepository>
  </pluginRepositories>

  <build>
    <directory>${project.basedir}/target</directory>
    <outputDirectory>${project.build.directory}/classes</outputDirectory>
    <finalName>${project.artifactId}-${project.version}</finalName>
    <testOutputDirectory>${project.build.directory}/test-classes</testOutputDirectory>
    <sourceDirectory>${project.basedir}/src/main/java</sourceDirectory>
    <scriptSourceDirectory>${project.basedir}/src/main/scripts</scriptSourceDirectory>
    <testSourceDirectory>${project.basedir}/src/test/java</testSourceDirectory>
    <resources>
      <resource>
        <directory>${project.basedir}/src/main/resources</directory>
      </resource>
    </resources>
    <testResources>
      <testResource>
        <directory>${project.basedir}/src/test/resources</directory>
      </testResource>
    </testResources>
    <pluginManagement>
      <!-- NOTE: These plugins will be removed from future versions of the super POM -->
      <!-- They are kept for the moment as they are very unlikely to conflict with lifecycle mappings (MNG-4453) -->
      <plugins>
        <plugin>
          <artifactId>maven-antrun-plugin</artifactId>
          <version>1.3</version>
        </plugin>
        <plugin>
          <artifactId>maven-assembly-plugin</artifactId>
          <version>2.2-beta-5</version>
        </plugin>
        <plugin>
          <artifactId>maven-dependency-plugin</artifactId>
          <version>2.8</version>
        </plugin>
        <plugin>
          <artifactId>maven-release-plugin</artifactId>
          <version>2.3.2</version>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>

  <reporting>
    <outputDirectory>${project.build.directory}/site</outputDirectory>
  </reporting>

  <profiles>
    <!-- NOTE: The release profile will be removed from future versions of the super POM -->
    <profile>
      <id>release-profile</id>

      <activation>
        <property>
          <name>performRelease</name>
          <value>true</value>
        </property>
      </activation>

      <build>
        <plugins>
          <plugin>
            <inherited>true</inherited>
            <artifactId>maven-source-plugin</artifactId>
            <executions>
              <execution>
                <id>attach-sources</id>
                <goals>
                  <goal>jar</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
          <plugin>
            <inherited>true</inherited>
            <artifactId>maven-javadoc-plugin</artifactId>
            <executions>
              <execution>
                <id>attach-javadocs</id>
                <goals>
                  <goal>jar</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
          <plugin>
            <inherited>true</inherited>
            <artifactId>maven-deploy-plugin</artifactId>
            <configuration>
              <updateReleaseInfo>true</updateReleaseInfo>
            </configuration>
          </plugin>
        </plugins>
      </build>
    </profile>
  </profiles>

</project>
<!-- END SNIPPET: superpom -->
```

首先，**超级POM**定义了仓库地址及插件仓库地址，并都关闭了 SNAPSHOT 的支持。这也就结识了为什么 Maven 默认就可以按需要从中央仓库下载构件。

**其次，我们来看`<build>`元素段，这里依次定义了项目的主输出目录、主代码输出目录、最终构件的名称格式、测试代码输出目录、主源码目录、脚本源码目录、测试源码目录、主资源目录和测试资源目录。这就是Maven项目结构的约定。**

紧接着在`<pluginManagement>`段，超级POM为核心插件设定版本：
```xml
<pluginManagement>
      <plugins>
        <plugin>
          <artifactId>maven-antrun-plugin</artifactId>
          <version>1.3</version>
        </plugin>
        <plugin>
          <artifactId>maven-assembly-plugin</artifactId>
          <version>2.2-beta-5</version>
        </plugin>
        <plugin>
          <artifactId>maven-dependency-plugin</artifactId>
          <version>2.8</version>
        </plugin>
        <plugin>
          <artifactId>maven-release-plugin</artifactId>
          <version>2.3.2</version>
        </plugin>
      </plugins>
    </pluginManagement>
```

由于篇幅原因，这里不完整罗列，大家感兴趣的话可以找到超级POM了解插件的具体版本。 

Maven 设定核心插件版本的原因是放置由于插件版本的变化而造成构建不稳定。

超级POM的最后是关于项目报告输出目录的配置和一个关于项目发布的`profile`，这里暂不深入解释。

我们可以看到，超级POM实际上很简单，但从这个POM我们就能够知晓 Maven 约定的由来，不仅理解了什么是约定，为什么要遵循约定，还明白约定是如何实现的。

---
# 6. 反应堆
在一个多模块的Maven项目中，反应堆（Reactor）是指所有模块组成的一个**构建结构**。对于单模块的项目，反应堆就是该模块本身，但对于多模块项目来说，反应堆就包含了各模块之间继承与依赖的关系，从而能够自动计算出合理的模块构建顺序。

## 6.1 反应堆的构件顺序
本节仍然以账户注册服务为例来解释反应堆。 首先，为了能够更清楚地解释反应堆的构建顺序，将`account-aggregator`的聚合配置修改如下：
```xml
<modules>
	<module>account-email</module>
	<module>account-persist</module>
	<module>account-parent</module>
</modules>
```
修改完毕之后构建`account-aggregator`会看到如下的输出：
```log
[INFO]----------------------------
[INFO]Reactor Build Order:
[INFO]
[INFO]Account Aggregator
[INFO]Account Parent
[INFO]Account Email
[INFO]Account Persist
[INFO]
[INFO]----------------------------
```

上述输出告诉了我们反应堆的构建顺序。我们知道，如果按顺序读取POM文件，首先应该读到的是`account-aggregator`的POM，实际情况与预料一致，可是接下来几个模块的构建顺序显然与配置中的声明顺序不一致，`account-parent`跑到了`account-email`前面，这是为什么呢？  为了解释这一现象，来看下图：

![4.png](https://raw.githubusercontent.com/dellnoantechnp/mvnbook/main/Chapter8/.pic/4.png)

上图中，从上至下的箭头表示POM的读取次序，但这不足以决定反应堆的构建顺序，Maven还需要考虑模块之间的**继承**和**依赖**关系，图中的有向虚线连接表示模块之间的继承或者依赖，例子中`account-email`和`account-persist`依赖于`account-parent`，那么`parent`就必须先于另外两个模块构建。 也就是说，这里还有一个从右向左的箭头。

实际的构件顺序是这样形成的：Maven按序读取POM，如果该POM没有依赖模块，那么就构建该模块，否则就先构件其依赖模块，如果该模块依赖于其他模块，则进一步先构建依赖的依赖。

## 6.2 裁剪反应堆
一般来说，用户会选择构建整个项目或者选择构建单个项目，但有些时候，用户会想要仅仅构建完整反应堆中的某些个模块。 换句话说，用户需要实时的裁剪反应堆。

Maven 提供很多的命令行选项支持裁剪反应堆，输入`mvn -h`可以看到这些选项：

- [x] **`-am,--also-make`**        If project list is specified, also build projects required by he list. 同时构建所列模块的依赖模块
- [x] **`-amd,--also-make-dependents`**     If project list is specified, also build projects that depend on projects on the list. 同时构建依赖于所列模块的模块
- [x] **`-pl,--projects <arg>`**      Comma-delimited list of specified reactor projects to build instead of all projects. A project can be specified by [groupId]:artifactId or by its relative path. 构建指定的模块，模块间用逗号分隔
- [x] **`-rf,--resume-from <arg>`**     Resume reactor from specified project. 从指定的模块回复反应堆

举例来说：
默认情况从`account-aggregator`执行`mvn clean install`会得到如下完整的反应堆：
```log
[INFO]----------------------------
[INFO]Reactor Build Order:
[INFO]
[INFO]Account Aggregator
[INFO]Account Parent
[INFO]Account Email
[INFO]Account Persist
[INFO]
[INFO]----------------------------
```

可以使用`-pl`选项指定构建某几个模块：
```shell
$ mvn clean install -pl account-email,account-persist
```

得到的反应堆为：
```log
[INFO]----------------------------
[INFO]Reactor Build Order:
[INFO]
[INFO]Account Email
[INFO]Account Persist
[INFO]
[INFO]----------------------------
```


使用`-am`选项可以同时构建所列模块的依赖模块：
```shell
$ mvn clean install -pl account-email -am
```
由于`account-email`依赖于`account-parent`，因此会得到如下反应堆：
```log
[INFO]----------------------------
[INFO]Reactor Build Order:
[INFO]
[INFO]Account Parent
[INFO]Account Email
[INFO]
[INFO]----------------------------
```


使用`-amd`选项可以同时构建依赖于所列模块的模块：
```shell
$ mvn clean install -pl account-parent -amd
```
由于`account-email`和`account-persist`都依赖于`account-parent`，因此会得到如下反应堆：
```log
[INFO]----------------------------
[INFO]Reactor Build Order:
[INFO]
[INFO]Account Parent
[INFO]Account Email
[INFO]Account Persist
[INFO]
[INFO]----------------------------
```

使用`-rf`选项可以在完整的反应堆构建顺序基础上指定从哪个模块开始构建：
```shell
$ mvn clean install -rf accont-email
```
完整的反应堆构建顺序中，`account-email`位于第三，它之后只有`account-persist`，因此，会得到如下的裁剪反应堆：
```log
[INFO]----------------------------
[INFO]Reactor Build Order:
[INFO]
[INFO]Account Email
[INFO]Account Persist
[INFO]
[INFO]----------------------------
```

最后，在`-pl -am`或者`-pl -amd`的基础上，还能应用`-rf`参数，以对裁剪后的反应堆再次裁剪。例如：
```shell
$ mvn clean install -pl account-parent -amd -rf account-email
```

该命令中的`-pl`和`-amd`参数会裁剪出一个`account-parent`、`account-email`和`account-persist`的反应堆，在此基础上，`-rf`参数指定从`account-email`参数构建。 因此会得到如下的反应堆：
```log
[INFO]----------------------------
[INFO]Reactor Build Order:
[INFO]
[INFO]Account Email
[INFO]Account Persist
[INFO]
[INFO]----------------------------
```

在开发过程中，灵活应用上述4个参数，可以帮助我们跳过无须构建的模块，从而加速构建。 在项目庞大、构架特别多的时候，这种效果就会异常明显。

---
# 7. 小结
本章介绍并实现了账户注册服务的第二个模块`account-persist`。 基于这一模块和第五章实现的`account-email`，Maven 的聚合特性得到了介绍和使用，从而产生了`account-aggregator`模块。 

除了聚合之外，继承也是多模块项目不可不用的特性。 `account-parent`模块伴随着继承的概念被一并引入，有了继承，项目的依赖和插件配置也得以大幅优化。

本问最后介绍了多模块构建的反应堆，包括其构建的顺序，以及可以通过怎样的方式裁剪反应堆。