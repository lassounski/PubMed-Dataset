/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uenf.pubmeddataset;

import com.uenf.pubmeddataset.internet.ArticleDownloader;
import com.uenf.pubmeddataset.internet.DownloadConfiguration;
import com.uenf.pubmeddataset.util.DataSet;
import com.uenf.pubmeddataset.util.DynaArticle;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static com.uenf.pubmeddataset.internet.ParameterName.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Kirill
 */
public class DataSetTest {

    DownloadConfiguration config = new DownloadConfiguration(ABSTRACT, TITLE, PMID, MESH_TERMS);

    @Test
    public void shouldGenerateKeyWords() throws Exception {

        ArticleDownloader downloader = new ArticleDownloader(config);
        Set<DynaArticle> articles = downloader.getDynaArticles("whey protein", 10);
        ConceptDataSet cds = new ConceptDataSet(articles, "whey protein");
        cds.generateKeyWords();
        for (DynaArticle article : articles) {
            List<String> genKws = (List<String>) article.getGeneratedKws();
            for (String kw : genKws) {
                assertNotNull(kw);
                if (kw.length() < 0) {
                    fail();
                }
            }
        }
    }

    /*
     * Dado que um artigo possui termos MeSH e um resumo,
     * este metodo deve fazer a intercessao dos termos MeSH como o resumo,
     * eliminando os termos MeSH que nao estiverem presentes no resumo.
     * Se todos os termos MeSH forem excluidos, o artigo é eliminado do DataSet.
     */
    @Test
    public void shouldIntersectMeshTermsWithTheAbstractText() throws Exception {
        DynaArticle article = new DynaArticle(config);
        article.put(ABSTRACT, "Kirill Lassounski Fodastico Horrores");
        Set<String> meshTerms = new HashSet<String>();
        meshTerms.add("Kirill");
        meshTerms.add("Kirillski");
        meshTerms.add("Kirillkoff");
        meshTerms.add("Lassounski");
        article.put(MESH_TERMS, meshTerms);
        Set<DynaArticle> articles = new HashSet<DynaArticle>();
        articles.add(article);

        ConceptDataSet cds = new ConceptDataSet(articles, "Kirill Lassounski");
        cds.intersectMeshTermsWithAbstract();
        DynaArticle alteredArticle = cds.getArticleIt().next();
        Set<String> alteredMeshTerms = (Set<String>) alteredArticle.getAttribute(MESH_TERMS).getValue();
        assertThat(alteredMeshTerms, hasItems("Kirill", "Lassounski"));
        assertThat(alteredMeshTerms.size(), is(equalTo(2)));
    }
    /*
     * Dado que um artigo possui termos MeSH e um resumo,
     * este metodo deve fazer a intercessao dos termos MeSH como o resumo,
     * eliminando os termos MeSH que nao estiverem presentes no resumo.
     * Se todos os termos MeSH forem excluidos, o artigo é eliminado do DataSet.
     */

    @Test
    public void shouldRemoveArticleFromDataSetIfIntersectionResultInEmptyMeshTermsSet() throws Exception {
        DynaArticle article = new DynaArticle(config);
        article.put(ABSTRACT, "Kirill Lassounski Fodastico Horrores");
        Set<String> meshTerms = new HashSet<String>();
        meshTerms.add("Kirillski");
        meshTerms.add("Kirillkoff");
        article.put(MESH_TERMS, meshTerms);
        Set<DynaArticle> articles = new HashSet<DynaArticle>();
        articles.add(article);

        ConceptDataSet cds = new ConceptDataSet(articles, "Fodastico Horrores");
        cds.intersectMeshTermsWithAbstract();
        assertTrue(cds.getArticles().isEmpty());
    }

    /*
     * Given that an article has MeshTerms and/or GeneratedKeyWords,
     * This method should remove the search term from this sets.
     * So that the the set does not contain the search term wich is irrelevant
     * in some testing cases. If some of the sets get empty the article is removed from the dataset.
     */
    @Test
    public void shouldRemoveSearchTermFromArticleAvailiableData() throws Exception {
        DynaArticle article = new DynaArticle(config);
        article.put(ABSTRACT, "Kirill Lassounski Fodastico Horrores");
        Set<String> meshTerms = new HashSet<String>();
        meshTerms.add("Kirillski");
        meshTerms.add("Kirillkoff");
        meshTerms.add("Kirill");
        meshTerms.add("Fodastico Horrores");
        article.put(MESH_TERMS, meshTerms);
        Set<DynaArticle> articles = new HashSet<DynaArticle>();
        articles.add(article);

        ConceptDataSet cds = new ConceptDataSet(articles, "Fodastico Horrores");
        cds.generateKeyWords();
        cds.removeSearchTermFromData();

        DynaArticle alteredArticle = cds.getArticleIt().next();

        Set<String> alteredMeshTerms = (Set<String>) alteredArticle.getAttribute(MESH_TERMS).getValue();
        assertThat(alteredMeshTerms, hasItems("Kirillski", "Kirillkoff", "Kirill"));
        assertThat(alteredMeshTerms.size(), is(equalTo(3)));

        List<String> generatedKws = (List) alteredArticle.getGeneratedKws();
        assertThat(generatedKws, hasItems("Kirill", "Lassounski", "Fodastico", "Horrores"));
        assertThat(generatedKws.size(), is(equalTo(4)));
    }

    @Test
    public void shouldRemoveArticleIfRunsOutOfWordsAfterSearchTermRemoval() throws Exception {
        DynaArticle article = new DynaArticle(config);
        article.put(ABSTRACT, "Kirill Lassounski");
        Set<String> meshTerms = new HashSet<String>();
        meshTerms.add("Kirill");
        article.put(MESH_TERMS, meshTerms);
        Set<DynaArticle> articles = new HashSet<DynaArticle>();
        articles.add(article);
        ConceptDataSet cds = new ConceptDataSet(articles, "Kirill");
        cds.generateKeyWords();
        cds.removeSearchTermFromData();

        assertTrue(cds.getArticles().isEmpty());
    }
}

class ConceptDataSet extends DataSet {

    ConceptDataSet(Set<DynaArticle> articles, String searchTerm) {
        super(articles, searchTerm);
    }

    @Override
    public void generateKeyWords() {
        for (Iterator<DynaArticle> articleIt = this.getArticleIt(); articleIt.hasNext();) {
            DynaArticle article = articleIt.next();
            String abstractText = null;

            try {
                abstractText = (String) article.getAttribute(ABSTRACT).getValue();
            } catch (Exception ex) {
                Logger.getLogger(ConceptDataSet.class.getName()).log(Level.SEVERE, null, ex);
            }

            String[] tokens = abstractText.split(" ");
            article.setGeneratedKeyWords(new ArrayList(Arrays.asList(tokens)));
        }
    }
}