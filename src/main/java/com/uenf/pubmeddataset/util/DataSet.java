/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uenf.pubmeddataset.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import static com.uenf.pubmeddataset.internet.ParameterName.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kirill
 * Essa classe é o conjunto de dados utilizados para os testes.
 * Possui um conjunto de artigos, que serão analisados nos testes e
 * métodos.
 */
public abstract class DataSet implements Serializable {

    protected Set<DynaArticle> articles;
    /**
     * Term used on the query to the PubMed. Can be used to remove itself from the
     * DynaArticle.generatedKeyWords and from DynaArticle - MeshTerms if available
     */
    protected String searchTerm;

    public DataSet(Set<DynaArticle> articles, String searchTerm) {
        this.articles = articles;
        this.searchTerm = searchTerm;
    }

    public Set<DynaArticle> getArticles() {
        return articles;
    }

    public int getArticlesSize() {
        return articles.size();
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public Iterator<DynaArticle> getArticleIt() {
        return articles.iterator();
    }

    /**
     * This method must be overriden to specify how the generatedKeyWords of DynaArticle
     * will be generated. They can be in the format of any Collection.
     */
    public abstract void generateKeyWords();

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
