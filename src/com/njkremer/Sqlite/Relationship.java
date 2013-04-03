package com.njkremer.Sqlite;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.njkremer.Sqlite.Annotations.OneToMany;
import com.njkremer.Sqlite.utils.SqliteUtils;

public class Relationship {

    public enum RelationshipType {
        ONE_TO_MANY
    }
    
    public Relationship(Field field) throws DataConnectionException {
        if(field.isAnnotationPresent(OneToMany.class)) {
            type = RelationshipType.ONE_TO_MANY;
            fk = ((OneToMany) field.getAnnotation(OneToMany.class)).value();
            fieldName = field.getName();
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            Type[] types = genericType.getActualTypeArguments();
            relatedClassType = (Class<?>) types[0];
            
            try {
                fkClassType = relatedClassType.getDeclaredField(fk).getType();
            }
            catch (Exception e) {
                throw new DataConnectionException(String.format("Error occured when trying to get foreign key's type. The foreign key of %s does not exist on %s", fk, relatedClassType));
            }
        }
    }
    
    public String getterName() {
        return "get" + SqliteUtils.capitalize(fieldName);
    }
    
    public String setterName() {
        return "set" + SqliteUtils.capitalize(fieldName);
    }
    
    public String foreignGetterName() {
        return "get" + SqliteUtils.capitalize(fk);
    }
    
    public String foreignSetterName() {
        return "set" + SqliteUtils.capitalize(fk);
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

    public Class<?> getFkClassType() {
        return fkClassType;
    }

    public void setFkClassType(Class<?> fkClassType) {
        this.fkClassType = fkClassType;
    }



    private String fk;
    private String fieldName;
    private Class<?> relatedClassType;
    private Class<?> fkClassType;
    private RelationshipType type;
    
}
