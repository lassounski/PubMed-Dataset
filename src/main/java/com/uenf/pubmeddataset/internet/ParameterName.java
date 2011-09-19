    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pmdataset.internet;

/**
 *
 * @author kirill
 */
public enum ParameterName {
    PMID("PMID","java.lang.String"),ABSTRACT("AbstractText","java.lang.String"),TITLE("Title","java.lang.String"),
        MESH_TERMS("MeshTerms","java.util.Set"), AUTHOR_KWS("AuthorKeyWords","java.util.Set");

        private String parameterName;
        private Class parameterClass;

        private ParameterName(String parameterName,String className) {
            this.parameterName = parameterName;
            try{
            this.parameterClass = Class.forName(className);
            }catch(ClassNotFoundException e){
                System.out.println("The class for "+parameterName+" was not found");
            }
        }

        public String getAttributeName() {
            return parameterName;
        }

        public Class getParameterClass() {
            return parameterClass;
        }
}