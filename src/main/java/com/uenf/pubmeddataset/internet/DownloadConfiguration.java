/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pmdataset.internet;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Kirill
 */
public class DownloadConfiguration {

    private Map<String, Class> downloadParameters;

    public static ParameterName parameters;

    public DownloadConfiguration(ParameterName ... params){
        downloadParameters = new HashMap<String, Class>(params.length);
        for(ParameterName param:params){
            downloadParameters.put(param.getAttributeName(), param.getParameterClass());
        }
    }

    public int getParametersSize() {
        return downloadParameters.size();
    }

    //REFACTOR IS IT NECESSARY ?
    public Set<Entry<String, Class>> getParametersEntrySet() {
        return downloadParameters.entrySet();
    }

    public Map<String, Class> getDownloadParameters() {
        return downloadParameters;
    }

    //REFACTOR
    public String getParameterName(int parameterInt) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
