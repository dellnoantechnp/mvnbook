package com.juvenxu.mvnbook.account.persist;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.List;


public class AccountPersistServiceImpl implements AccountPersistService {

    private static final String ELEMENT_ROOT = "account-persist";
    private static final String ELEMENT_ACCOUNTS = "accounts";
    private static final String ELEMENT_ACCOUNT_ID = "id";
    private static final String ELEMENT_ACCOUNT_NAME = "name";
    private static final String ELEMENT_ACCOUNT_EMAIL = "email";
    private static final String ELEMENT_ACCOUNT_PASSWORD = "password";
    private static final String ELEMENT_ACCOUNT_ACTIVATED = "activated";

    private String file;

    private SAXReader reader = new SAXReader();
    // readDocument() 会用到的对象

    public Account readAccount(String id) throws AccountPersistException {
        Document doc = readDocument();
        Element accountsEle = doc.getRootElement().element(ELEMENT_ACCOUNTS);   // 获取root根节点
        for(Element accountEle : (List<Element>)accountsEle.elements())         // 迭代accounts节点下的所有项目
            if (accountEle.elementText(ELEMENT_ACCOUNT_ID).equals(id)) {        // 根据传参对比，成功后返回该节点信息
                return buildAccount(accountEle);
            }
        return null;        // 否则返回空
    }

    public void createAccount(Account account) throws AccountPersistException {
        if(account != null){
            Document doc = readDocument();
            Element accountsEle = doc.getRootElement().element(ELEMENT_ACCOUNTS);
            Element accountEle = accountsEle.addElement("account");
            accountEle.addElement(ELEMENT_ACCOUNT_ID).setText(account.getId());
            accountEle.addElement(ELEMENT_ACCOUNT_NAME).setText(account.getName());
            accountEle.addElement(ELEMENT_ACCOUNT_PASSWORD).setText(account.getPassword());
            accountEle.addElement(ELEMENT_ACCOUNT_EMAIL).setText(account.getEmail());
            accountEle.addElement(ELEMENT_ACCOUNT_ACTIVATED).setText(account.isActivated()?"true":"false");
            writeDocument(doc);
        }
    }

    public Account updateAccount(Account account) throws AccountPersistException {
        return null;
    }

    public Account deleteAccount(String id) throws AccountPersistException {
        return null;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    private Account buildAccount(Element element) {     // 生成账户节点标签
        Account account = new Account();

        account.setId(element.elementText(ELEMENT_ACCOUNT_ID));
        account.setName(element.elementText(ELEMENT_ACCOUNT_NAME));
        account.setEmail(element.elementText(ELEMENT_ACCOUNT_EMAIL));
        account.setPassword(element.elementText(ELEMENT_ACCOUNT_PASSWORD));
        account.setActivated("true".equals(element.elementText(ELEMENT_ACCOUNT_ACTIVATED)));

        return account;
    }

    private Document readDocument() throws AccountPersistException {
        File dataFile = new File(file);
        // file 变量值通过 bean 配置注入

        if (!dataFile.exists()){    // 校验文件是否存在，如果不存在，进入如下逻辑
            dataFile.getParentFile().mkdirs();      // 创建上级路径
            Document doc = DocumentFactory.getInstance().createDocument();  // 创建Document对象
            Element rootEle = doc.addElement(ELEMENT_ROOT);     // xml 添加 root 节点，节点标签为 <account-persist>
            rootEle.addElement(ELEMENT_ACCOUNTS);
            writeDocument(doc);         // Element对象写入xml文件中
        }

        try {
            return reader.read(dataFile);       // 返回 xml 文件内容
        } catch (DocumentException e) {
            throw new AccountPersistException("Unable to read persist data xml file!", e);
        }
    }

    private void writeDocument(Document doc) throws AccountPersistException{
        Writer out = null;      // 初始化 out 局部变量

        try {
            out = new OutputStreamWriter(new FileOutputStream(file),"utf-8");   // OutputStreamWriter对象向上转型为Writer对象，私有变量file为SpringFramework注入值
            XMLWriter writer = new XMLWriter(out, OutputFormat.createPrettyPrint());    // 输出格式为OutputFormat.createPrettyPrint()的友好结果
            writer.write(doc);          // 写入xml文件
        } catch (IOException e) {
            throw new AccountPersistException("Unable to write persist data xml file!", e);
        } finally {
            try {
                if (out != null){
                    out.close();        // 关闭文件打开描述符
                }
            } catch (IOException e) {
                throw new AccountPersistException("Unable to close persist data xml file writer!", e);
            }
        }
    }
}