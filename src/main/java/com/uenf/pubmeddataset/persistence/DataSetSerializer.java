/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uenf.pubmeddataset.persistence;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.uenf.pubmeddataset.util.DataSet;

/**
 *
 * @author Kirill
 */
public class DataSetSerializer {

    private static final Logger logger = Logger.getLogger(DataSetSerializer.class.getName());

    /**
     *
     * @param dataSet DataSet to be serialized
     * @param fileName The name of the file to be created, the extension .ser is appended automatically
     */
    public static String serializeDataSet(DataSet dataSet, String fileName) throws IOException {
        int articlesNum = dataSet.getArticlesSize();
        String createdFileName = fileName + "_" + articlesNum + "_articles.ser";
        if (dataSet.getArticlesSize() == 0) {
            logger.log(Level.INFO, "There are no articles to serialize");
            return null;
        }
        logger.log(Level.INFO, "Starting serialization ({0})...", createdFileName.replace(".ser", ""));
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream oos = null;

        try {
            fos = new FileOutputStream(createdFileName);
            bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);
            oos.writeObject(dataSet);
            oos.flush();
            bos.flush();
            fos.flush();
            logger.log(Level.INFO, "Serialization concluded");
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, "{0}.ser not found.", fileName);
        } finally {
            oos.close();
        }
        return createdFileName.replace(".ser", "");
    }

    /**
     *
     * @param fileName The name of the file to be unserialized, the .ser extension is appended automatically
     * @return DataSet Unserialized DataSet
     */
    public static DataSet unserializeDataSet(String fileName) throws IOException {
        logger.log(Level.INFO, "Starting unserialization ({0})...", fileName);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(fileName + ".ser");
            bis = new BufferedInputStream(fis);
            ois = new ObjectInputStream(bis);
            DataSet set = (DataSet) ois.readObject();
            logger.log(Level.INFO, "Unserialization concluded");
            return set;
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, "{0}.ser not found", fileName);
        } catch (ClassNotFoundException ex3) {
            logger.log(Level.SEVERE, "Could not found: {0}\n{1}", new Object[]{ex3.getCause().getClass(), ex3.getMessage()});
        } finally {
            ois.close();
        }
        logger.log(Level.INFO, "It was not possible to unserialize");
        return null;
    }
}
