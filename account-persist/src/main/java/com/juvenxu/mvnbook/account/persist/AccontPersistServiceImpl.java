package com.juvenxu.mvnbook.account.persist;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.List;

public class AccontPersistServiceImpl implements AccountPersistService {
//    part 1 ----------------------------------------------------
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
            return reader.read(new File(file));
        } catch (DocumentException e){
            throw new AccountPersistException("Unable to read persist data xml", e);
        }
    }

    private void writeDocument(Document doc) throws AccountPersistException{
        Writer out = null;

        try {
            out = new OutputStreamWrite(new FileOutputStream(file), "utf-8");

            XMLWriter writer = newk XMLWriter(out, OutputFormat.createPrettyPrint());

            writer.write(doc);
        } catch (IOException e) {
            throw new AccountPersistException("Unable to write persist data xml", e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e){
                throw new AccountPersistException("Unable to close persist data xml writer", e);
            }
        }
    }


//    part 2 -----------------------------------------------------
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

    private Account buildAccount(Element element){
        Account account = new Account();

        account.setId(element.elementText(ELEMENT_ACCOUNT_ID));
        account.setName(element.elementText(ELEMENT_ACCOUNT_NAME));
        account.setEmail(element.elementText(ELEMENT_ACCOUNT_EMAIL));
        account.setPassword(element.elementText(ELEMENT_ACCOUNT_PASSWORD));
        account.setActivated(("true".equals(element.elementText(ELEMENT_ACCOUNT_ACTIVATED)) ? true : false));

        return account;
    }
}
