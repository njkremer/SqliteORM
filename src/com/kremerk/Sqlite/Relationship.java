package com.kremerk.Sqlite;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.kremerk.Sqlite.Annotations.OneToMany;
import com.kremerk.Sqlite.utils.SqliteUtils;

public class Relationship {

    public enum RelationshipType {
        ONE_TO_MANY
    }
    
    public Relationship(Field field) {
        if(field.isAnnotationPresent(OneToMany.class)) {
            type = RelationshipType.ONE_TO_MANY;
            fk = ((OneToMany) field.getAnnotation(OneToMany.class)).value();
            fieldName = field.getName();
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            Type[] types = genericType.getActualTypeArguments();
            relatedClassType = (Class<?>) types[0];
        }
    }
    
    public String getterName() {
        return "get" + SqliteUtils.capitalize(fieldName);
    }
    
    public String setterName() {
        return "set" + SqliteUtils.capitalize(fieldName);
    }
    
    public String getFk() {
        return fk;
    }

    public void setFk(String fk) {
        this.fk = fk;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public Class<?> getRelatedClassType() {
        return relatedClassType;
    }

    public void setRelatedClassType(Class<?> relationshipClassType) {
        this.relatedClassType = relationshipClassType;
    }
    
    public RelationshipType getType() {
        return type;
    }

    public void setType(RelationshipType type) {
        this.type = type;
    }



    private String fk;
    private String fieldName;
    private Class<?> relatedClassType;
    private RelationshipType type;
    
}
