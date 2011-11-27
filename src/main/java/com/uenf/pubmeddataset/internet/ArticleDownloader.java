/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uenf.pubmeddataset.internet;

import com.uenf.pubmeddataset.util.DynaArticle;
import com.uenf.pubmeddataset.util.EmptyObjectException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.Element;

/**
 *
 * @author Kirill
 */
public class ArticleDownloader {

    private DownloadConfiguration downloadConfig;
    /**
     * The article download will be splitted by this factor to avoid TimeOutException
     */
    private final int splitFactor = 100;
    private Document doc;
    private SAXBuilder builder;
    private Element root;
    private final String searchPubMedUrl = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?retmode=xml&db=pubmed";
    private final String fetchPubMedUrl = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?retmode=xml&db=pubmed&id=";

    public ArticleDownloader(DownloadConfiguration config) {
        this.downloadConfig = config;
    }

    /**
     * Converts whitespaces to +
     * @param String searchTerm
     * @return normalized searchTerm
     */
    private String normalizeSearchTerm(String searchTerm) {
        return searchTerm.trim().replaceAll("\\s+", "+");
    }

    /**
     * Builds an url to the DOM
     * @param url to build
     */
    private void build(String url) {
        try {
            builder = new SAXBuilder();
            doc = builder.build(url);
        } catch (JDOMException ex) {
            System.out.println("Parsing error");
            Logger.getLogger(ArticleDownloader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("I/O error");
            Logger.getLogger(ArticleDownloader.class.getName()).log(Level.SEVERE, null, ex);
        }
        root = doc.getRootElement();
    }

    /**
     * Formats a list of PMID so that can be used by the EUtils
     * @param ids  PMIDs to be formatted
     * @return formatted String
     */
    private String formatIds(List<String> ids) {
        String idsString = ids.toString();
        idsString = idsString.replaceAll("\\[", "");
        idsString = idsString.replaceAll("\\]", "");
        idsString = idsString.replace(" ", "");
        return idsString;
    }

    /**
     * 
     * @param term - to be queried
     * @return int - the total number of articles found on PubMed related to this SearchTerm
     */
    public int getResultsCount(String term) {
        String url = searchPubMedUrl;
        url += "&term=" + normalizeSearchTerm(term);
        build(url);
        return Integer.parseInt(root.getChild("Count").getValue());
    }
    
    /**
     * Get the PMID List for the query
     * @param term - to be queried
     * @param maxHits - number of maximum results from PubMed
     * @return Set<String> - Lista de identificadores.
     */
    public Set<String> getIds(String term, int maxHits) {
        String url = searchPubMedUrl;
        url += "&term=" + term + "&retmax=" + maxHits;
        build(url);

        List ids = root.getChild("IdList").getChildren();
        Iterator idsIt = ids.iterator();

        Set<String> idsOut = new HashSet<String>(ids.size());
        while (idsIt.hasNext()) {
            Element e = (Element) idsIt.next();
            idsOut.add(e.getValue());
        }
        System.out.println("Ids retieved: " + idsOut.size());
        return idsOut;
    }
    
    /**
     * Get the PMID List for the query
     * @param term - to be queried
     * @param first - the starting id to be downloaded
     * @parm pageSize - the offset
     * @return List<String> - Id list.
     */
    public List getIds(String term, int first, int pageSize) {
        System.out.println("Downloader: retrieving ids from " + first + " to " + (first + pageSize));

        String url = searchPubMedUrl;
        url += "&term=" + normalizeSearchTerm(term) + "&retstart=" + first + "&retmax=" + pageSize;
        build(url);

        List ids = root.getChild("IdList").getChildren();
        Iterator idsIt = ids.iterator();

        List<String> idsOut = new ArrayList<String>(ids.size());
        while (idsIt.hasNext()) {
            Element e = (Element) idsIt.next();
            idsOut.add(e.getValue());
        }
        System.out.println("Downloader: retrieved ids = " + idsOut.size());
        return idsOut;
    }

    /**
     * Retrieves articles from PubMed
     * @param searchTerm term to be queried
     * @param maxHits number of maximum results from PubMed
     * @return a Set containing DynaArticle`s
     */
    public Set<DynaArticle> getDynaArticles(String searchTerm, int maxHits) {
        Set<DynaArticle> articles = new HashSet<DynaArticle>(maxHits / 2);
        searchTerm = normalizeSearchTerm(searchTerm);
        Set<String> ids = getIds(searchTerm, maxHits);

        //List with the ID`s that will be downloaded on current round
        List<String> idList = new ArrayList<String>(splitFactor);
        //Slices already processed
        int splitCounter = 0;
        //Number of slices
        int splitNumber = (int) Math.floor((double) ids.size() / (double) splitFactor);
        //Fraction of Id`s that will be used in the current round
        int fraction = splitFactor;
        //Runs over all Id`s
        for (Iterator<String> idsIt = ids.iterator(); idsIt.hasNext();) {
            if (splitCounter == splitNumber) {
                fraction = ids.size() - splitCounter * splitFactor;
            }
            idList = new ArrayList<String>(fraction);
            for (int i = 0; i < fraction; i++) {
                idList.add(idsIt.next());
            }
            articles.addAll(downloadArticles(idList));
            System.out.println("Downloaded " + articles.size() + " abstracts" + " - from("+splitCounter+"/"+splitNumber+")");
            splitCounter += 1;
        }
        return articles;
    }

    /**
     * Downloads a List of DynaArticles
     * @param ids - to be downloaded
     * @return List - of DynaArticless
     */
    public List<DynaArticle> downloadArticlesList(List<String> ids) {
        System.out.println("Downloader: downloading " + ids.size() + " articles...");
        List<DynaArticle> articles = new ArrayList<DynaArticle>(ids.size());
        String idsString = formatIds(ids);
        String url = fetchPubMedUrl + idsString;
        build(url);
        List pubMedArticleElements = root.getChildren();

        for (Iterator pubMedArticlesIt = pubMedArticleElements.iterator(); pubMedArticlesIt.hasNext();) {
            DynaArticle dynaArticle = extractAttributes(pubMedArticlesIt.next());
            if (dynaArticle != null) {
                articles.add(dynaArticle);
            }
        }
        System.out.println("Downloader: downloaded " + articles.size() + " articles");
        return articles;
    }
    
    
    protected Set<DynaArticle> downloadArticles(List<String> ids) {
        Set<DynaArticle> articles = new HashSet<DynaArticle>(ids.size() / 2);
        String idsString = formatIds(ids);
        String url = fetchPubMedUrl + idsString;
        build(url);
        List pubMedArticleElements = root.getChildren();

        for (Iterator pubMedArticlesIt = pubMedArticleElements.iterator(); pubMedArticlesIt.hasNext();) {
            DynaArticle dynaArticle = extractAttributes(pubMedArticlesIt.next());
            if (dynaArticle != null) {
                articles.add(dynaArticle);
            }
        }
        return articles;
    }

    /**
     * Extracts the attributes requested in DownloadConfiguration
     * and puts the into a DynaArticle
     * @return the DynaArticle or null if an attribute fails to load
     */
    private DynaArticle extractAttributes(Object pubMedArticleElement) {

        Class downloaderClass = ArticleDownloader.class;
        DynaArticle downloadItem = new DynaArticle(downloadConfig);

        for (Iterator<String> it = downloadConfig.getDownloadParameters().keySet().iterator(); it.hasNext();) {
            String parameterName = it.next();
            //nome do metodo a ser chamado para pegar o resultado
            String getter = "get" + parameterName;

            Object paramValue = null;
            //tenta obter o metodo da classe para o atributo requisitado
            try {
                Method privateMet = downloaderClass.getDeclaredMethod(getter, Class.forName("org.jdom.Element"));
                privateMet.setAccessible(true);
                paramValue = privateMet.invoke(this, pubMedArticleElement);
                if (paramValue == null) {
                    return null;
                }
            } catch (Exception e) {
                System.out.println("The method " + getter + " does not exist");
                return null;
            }
            //setar o valor em DynaArticle
            try {
                downloadItem.put(parameterName, paramValue);
            } catch (EmptyObjectException e) {
                return null;
            }
        }
        return downloadItem;
    }

    private String getPMID(Element e) {
        String PMID = null;
        try {
            PMID = e.getChild("MedlineCitation").getChild("PMID").getValue();
        } catch (NullPointerException a) {
            System.out.println("Could not retrieve PMID");
        }
        return PMID;
    }

    private String getAbstractText(Element e) {
        String abstractText = null;
        try {
            abstractText = e.getChild("MedlineCitation").getChild("Article").getChild("Abstract").getChild("AbstractText").getValue();
        } catch (NullPointerException a) {
            System.out.println("Could not retrieve abstract");
        }
        return abstractText;
    }

    private String getTitle(Element e) {
        String title = null;
        try {
            title = e.getChild("MedlineCitation").getChild("Article").getChild("ArticleTitle").getValue();
        } catch (NullPointerException a) {
            System.out.println("Could not retrieve title");
        }
        return title;
    }

    private Set<String> getAuthorKeyWords(Element e){
        Set<String> keyWords = new HashSet<String>();
        List keyWordsList;
        try {
            keyWordsList = e.getChild("MedlineCitation").getChild("KeywordList").getChildren();
        } catch (NullPointerException a) {
            System.out.println("Could not retrive AuthorKeyWords");
            return null;
        }
        Iterator i = keyWordsList.iterator();
        while(i.hasNext()){
            Element keyWordElement = (Element) i.next();
            String keyWord = keyWordElement.getValue().toLowerCase();
            keyWords.add(keyWord);
        }
        return keyWords;
    }
    
    private Set<String> getMeshTerms(Element e) {

        Set<String> keyWords = new HashSet<String>();
        List keyWordsList;
        try {
            keyWordsList = e.getChild("MedlineCitation").getChild("MeshHeadingList").getChildren();
        } catch (NullPointerException a) {
            System.out.println("Could not retrive MeshTerms");
            return null;
        }
        Iterator i = keyWordsList.iterator();
        while (i.hasNext()) {
            Element keyWordElement = (Element) i.next();
            String descriptorName = keyWordElement.getChild("DescriptorName").getValue().toLowerCase();
            List<Element> qualifierNameList;
            //Tries to get the qualiferNames
            try {
                qualifierNameList = keyWordElement.getChildren("QualifierName");
            } catch (NullPointerException ex) {
                qualifierNameList = null;
            }
            //Strores the descriptor names splitting it by commas
            storeKwdsFromDescriptor(descriptorName, keyWords);
            //If there is qualifier names, store a combinateion between descriptor names and qulifier names
            if (qualifierNameList != null & !qualifierNameList.isEmpty()) {
                storeKwdsFromDescriptorAndQualifier(descriptorName, qualifierNameList, keyWords);
            }
        }
        return keyWords;
    }

    private void storeKwdsFromDescriptorAndQualifier(String descriptor, List<Element> qualifiers, Set<String> kwds) {
        String[] descriptorParts = descriptor.split(",");
        for (String descriptorPart : descriptorParts) {
            for (Element qualifier : qualifiers) {
                String qualifierValue = qualifier.getValue();
                kwds.add(descriptorPart.trim() + " " + qualifierValue.trim().toLowerCase());
            }
        }
    }

    private void storeKwdsFromDescriptor(String descriptor, Set<String> keyWords) {
        String[] descriptorParts = descriptor.split(",");
        for (String descriptorPart : descriptorParts) {
            keyWords.add(descriptorPart.trim());
        }
    }
}
