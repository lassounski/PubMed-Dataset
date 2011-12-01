/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uenf.pubmeddataset;

import java.util.Collection;
import com.uenf.pubmeddataset.util.EmptyObjectException;
import com.uenf.pubmeddataset.util.NullValueException;
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
        List<DynaArticle> articles = downloader.getDynaArticles("whey protein", 10);
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
        List<DynaArticle> articles = new ArrayList<DynaArticle>();
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
        List<DynaArticle> articles = new ArrayList<DynaArticle>();
        articles.add(article);
        ConceptDataSet cds = new ConceptDataSet(articles, "Kirill");
        cds.generateKeyWords();
        cds.removeSearchTermFromData();

        assertTrue(cds.getArticles().isEmpty());
    }
}

class ConceptDataSet extends DataSet {

    ConceptDataSet(Collection<DynaArticle> articles, String searchTerm) {
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
    
    /**
     * Makes the intersection between MeshTerms and AbstractText if availiable.
     * If an article runs out of MeshTerms it is removed from the dataset.
     */
    public void intersectMeshTermsWithAbstract() throws Exception {
        List<DynaArticle> removeArticleList = new ArrayList<DynaArticle>();
        
        nullAttribute:
        for (DynaArticle article : articles) {
            String abstractText = null;
            Set<String> meshTerms = null;
            try {
                abstractText = (String) article.getAttribute(ABSTRACT).getValue();
                meshTerms = (Set<String>) article.getAttribute(MESH_TERMS).getValue();
            } catch (Exception ex) {
                Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Attribute for " + (String) article.getAttribute(PMID).getValue() + " is null");
                continue nullAttribute;
            }
            List<String> removeTermList = new ArrayList<String>();
            for (String term : meshTerms) {
                if (!abstractText.contains(term)) {
                    removeTermList.add(term);
                }
            }
            meshTerms.removeAll(removeTermList);
            if (meshTerms.isEmpty()) {
                removeArticleList.add(article);
            }
        }
        articles.removeAll(removeArticleList);
    }

    /**
     * Removes the searchTerm from the MeshTerms if avaliable
     * Remove o termo que foi utilizado na busca do conjunto de palavras geradas e
     * do conjunto de palavras definidas.
     */
    public void removeSearchTermFromData() {
        List articlesToRemove = new ArrayList();
        
        nullAttribute:
        for (DynaArticle article : articles) {
            Set<String> meshTerms = null;
            List generatedkws = null;
            try {
                try {
                    meshTerms = (Set<String>) article.getAttribute(MESH_TERMS).getValue();
                } catch (NullValueException ex) {
                    Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, ex);
                    continue nullAttribute;
                }
                meshTerms.remove(searchTerm);
                if (meshTerms.isEmpty()) {
                    articlesToRemove.add(article);
                }
            } catch (NoSuchFieldException e) {
                System.out.println("There are no Mesh Terms");
                continue nullAttribute;
            }
            try {
                generatedkws = (List) article.getGeneratedKws();
                generatedkws.remove(searchTerm);
            } catch (EmptyObjectException e) {
                System.out.println("There are no Generated Key Words");
                continue nullAttribute;
            }
        }
        articles.removeAll(articlesToRemove);
    }
}