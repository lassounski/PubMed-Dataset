/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uenf.pubmeddataset.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Collection;

/**
 *
 * @author Kirill
 * Essa classe é o conjunto de dados utilizados para os testes.
 * Possui um conjunto de artigos, que serão analisados nos testes e
 * métodos.
 */
public abstract class DataSet implements Serializable {

    protected Collection<DynaArticle> articles;
    /**
     * Term used on the query to the PubMed. Can be used to remove itself from the
     * DynaArticle.generatedKeyWords and from DynaArticle - MeshTerms if available
     */
    protected String searchTerm;

    public DataSet(Collection<DynaArticle> articles, String searchTerm) {
        this.articles = articles;
        this.searchTerm = searchTerm;
    }

    public Collection<DynaArticle> getArticles() {
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
}
