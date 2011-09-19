/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pmdataset.util;

import java.io.Serializable;
import java.util.Collection;
import pmdataset.internet.DownloadConfiguration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import pmdataset.internet.ParameterName;

/**
 *
 * @author Kirill
 */
public class DynaArticle implements Serializable{

    private Map<String,ArticleAttribute> attributes;
    private Collection<Object> generatedKeyWords;

    public DynaArticle(DownloadConfiguration config) {
        attributes = new HashMap<String, ArticleAttribute>(config.getParametersSize());
        for(Entry<String,Class> entry:config.getParametersEntrySet()){
            String name = entry.getKey();
            Class type = entry.getValue();
            attributes.put(name, new ArticleAttribute(name,type));
        }
    }

    public Map<String,ArticleAttribute> getAttributes() throws EmptyObjectException {
        if(attributes.values().isEmpty())
            throw new EmptyObjectException("There are no Attributes");
        return attributes;
    }

    public void put(String parameterName, Object paramValue) throws EmptyObjectException {
        ArticleAttribute input = attributes.get(parameterName);
        input.setValue(paramValue);
        attributes.put(parameterName, input);
    }

    public void put(ParameterName parameterName, Object paramValue) throws EmptyObjectException {
        ArticleAttribute input = attributes.get(parameterName.getAttributeName());
        input.setValue(paramValue);
        attributes.put(parameterName.getAttributeName(), input);
    }

    public ArticleAttribute getAttribute(ParameterName parameterName) throws NoSuchFieldException {
        ArticleAttribute out = attributes.get(parameterName.getAttributeName());
        if(out == null)
            throw new NoSuchFieldException("There is no "+parameterName.getAttributeName()+" parameter");
        return out;
    }

    public Collection getGeneratedKws() throws EmptyObjectException{
        if(generatedKeyWords == null)
            throw new EmptyObjectException("There are no generated key words");
        return generatedKeyWords;
    }

    public void setGeneratedKeyWords(Collection generatedKeyWords) {
        this.generatedKeyWords = generatedKeyWords;
    }

}
