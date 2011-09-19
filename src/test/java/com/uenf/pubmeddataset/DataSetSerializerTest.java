/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testing;

import java.io.File;
import java.util.HashSet;
import pmdataset.persistence.DataSetSerializer;
import java.util.Set;
import org.junit.Test;
import pmdataset.internet.DownloadConfiguration;
import pmdataset.util.DynaArticle;
import static pmdataset.internet.ParameterName.*;
import static org.junit.Assert.*;

/**
 *
 * @author Kirill
 */
public class DataSetSerializerTest {

    @Test
    public void shouldSerializeDataset() throws Exception {
        DownloadConfiguration config = new DownloadConfiguration(ABSTRACT, TITLE, PMID);
        Set<DynaArticle> articles = new HashSet<DynaArticle>();
        articles.add(new DynaArticle(config));
        articles.add(new DynaArticle(config));
        articles.add(new DynaArticle(config));
        ConceptDataSet cds = new ConceptDataSet(articles, "mycobacterium tuberculosis");

        DataSetSerializer.serializeDataSet(cds, "mycobacterium tuberculosis");
        File file = new File("mycobacterium tuberculosis_3_articles.ser");
        assertTrue(file.isFile());
        file.delete();
    }

    @Test
    public void shouldUnserializeDataset() throws Exception {
        DownloadConfiguration config = new DownloadConfiguration(ABSTRACT, TITLE, PMID);
        Set<DynaArticle> articles = new HashSet<DynaArticle>();
        articles.add(new DynaArticle(config));
        articles.add(new DynaArticle(config));
        articles.add(new DynaArticle(config));
        ConceptDataSet cds = new ConceptDataSet(articles, "mycobacterium tuberculosis");

        DataSetSerializer.serializeDataSet(cds, "mycobacterium tuberculosis");
        File file = new File("mycobacterium tuberculosis_3_articles.ser");

        ConceptDataSet cdsUnser = (ConceptDataSet) DataSetSerializer.unserializeDataSet("mycobacterium tuberculosis_3_articles");
        assertTrue(cdsUnser.getArticlesSize() == 3);
        file.delete();
    }
}
