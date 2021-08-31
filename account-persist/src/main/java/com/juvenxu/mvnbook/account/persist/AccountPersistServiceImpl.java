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

    private  String file;

    private SAXReader reader = new SAXReader();

    public Account readAccount(String id) throws AccountPersistException {
        Document doc = readDocument();
        Element accountsEle = doc.getRootElement().element(ELEMENT_ACCOUNTS);
        for(Element accountEle : (List<Element>)accountsEle.elements())
            if (accountEle.elementText(ELEMENT_ACCOUNT_ID).equals(id)) {
                return buildAccount(accountEle);
            }
        return null;
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

    private Account buildAccount(Element element) {
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
}