/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testing;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import pmdataset.util.DataSet;
import java.util.Set;
import org.junit.Test;
import pmdataset.internet.ArticleDownloader;
import pmdataset.internet.DownloadConfiguration;
import pmdataset.util.DynaArticle;
import static pmdataset.internet.ParameterName.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Kirill
 */
public class DataSetTest {

    @Test
    public void shouldGenerateKeyWords() throws Exception {
        DownloadConfiguration config = new DownloadConfiguration(ABSTRACT, TITLE, PMID);
        ArticleDownloader downloader = new ArticleDownloader(config);
        Set<DynaArticle> articles = downloader.getDynaArticles("mycobacterium tuberculosis", 10);
        ConceptDataSet cds = new ConceptDataSet(articles, "mycobacterium tuberculosis");
        cds.generateKeyWords();
        for (DynaArticle article : articles) {
            List<String> genKws = (List<String>) article.getGeneratedKws();
            for (String kw : genKws) {
                assertNotNull(kw);
                assertTrue(kw.length() > 0);
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
        DownloadConfiguration config = new DownloadConfiguration(ABSTRACT, MESH_TERMS);
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
        DownloadConfiguration config = new DownloadConfiguration(ABSTRACT, MESH_TERMS);
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
        DownloadConfiguration config = new DownloadConfiguration(ABSTRACT, MESH_TERMS);
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
        assertThat(generatedKws, hasItems("Kirill", "Lassounski","Fodastico","Horrores"));
        assertThat(generatedKws.size(), is(equalTo(4)));
    }

    @Test
    public void shouldRemoveArticleIfRunsOutOfWordsAfterSearchTermRemoval() throws Exception {
        DownloadConfiguration config = new DownloadConfiguration(ABSTRACT, MESH_TERMS);
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

//REFATORAR METODO PUT EM DYNAARTICLE PARA ACEITAR O ENUM
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
            } catch (NoSuchFieldException e) {
                System.out.println("Abstract nao existe");
            }
            String[] tokens = abstractText.split(" ");
            article.setGeneratedKeyWords(new ArrayList(Arrays.asList(tokens)));
        }
    }

}