/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uenf.pubmeddataset.util;

import java.io.Serializable;

/**
 *
 * @author Kirill
 */
public class ArticleAttribute implements Serializable{


    private String name;
    private Class type;
    private Object value;

    public ArticleAttribute(String name, Class type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Class getType() {
        return type;
    }

    public Object getValue() throws NullValueException {
        if(value == null)
            throw new NullValueException("The value is null");
        return value;
    }

    void setValue(Object paramValue) throws EmptyObjectException {
        try {
            //verifica se o objeto recebido é do tipo deste parametro
            if (Class.forName(type.getName()).isInstance(paramValue)) {
                value = paramValue;
            }
        } catch (Exception e) {
            System.out.println("The parameter submitted is not compatible with: " + type.getSimpleName());
        }
    }
}
