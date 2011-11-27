    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uenf.pubmeddataset;

import com.uenf.pubmeddataset.internet.ArticleDownloader;
import com.uenf.pubmeddataset.internet.DownloadConfiguration;
import com.uenf.pubmeddataset.util.DynaArticle;
import static com.uenf.pubmeddataset.internet.ParameterName.*;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author kirill
 */
public class DonwloaderTest {
    private static ArticleDownloader downloader;
    public DonwloaderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        DownloadConfiguration config = new DownloadConfiguration(PUB_YEAR,AUTHOR_NAMES);
        downloader = new ArticleDownloader(config);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    @Test
    public void shouldDownloadYear() throws NoSuchFieldException, Exception {
        List<DynaArticle> articles = getArticles(100);
        for (DynaArticle a : articles) {
            assertThat(a.getAttribute(PUB_YEAR).getValue(), is(not(equalTo(null))));
        }
    }

    @Test
    public void shouldDownloadAuthors() throws NoSuchFieldException, Exception {
        List<DynaArticle> articles = getArticles(10);
        for (DynaArticle a : articles) {
            List<String> authors = (List)a.getAttribute(AUTHOR_NAMES).getValue();
            for(String author:authors){
                assertFalse(author.isEmpty());                
            }
        }
    }
    
    @Test
    public void shouldDownloadJournal() {
        
    }

    private List<DynaArticle> getArticles(int numberOfArticles) {
        List ids = downloader.getIds("mycobacterium", 1, numberOfArticles);
        List articles = downloader.downloadArticlesList(ids);
        assertEquals(numberOfArticles, articles.size());
        return articles;
    }
}
