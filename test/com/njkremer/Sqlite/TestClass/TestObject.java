package com.njkremer.Sqlite.TestClass;

import java.util.Date;

public class TestObject {
    public Integer getIntType() {
        return intType;
    }
    public void setIntType(Integer intType) {
        this.intType = intType;
    }
    public String getStringType() {
        return stringType;
    }
    public void setStringType(String stringType) {
        this.stringType = stringType;
    }
    public Float getFloatType() {
        return floatType;
    }
    public void setFloatType(Float floatType) {
        this.floatType = floatType;
    }
    public Double getDoubleType() {
        return doubleType;
    }
    public void setDoubleType(Double doubleType) {
        this.doubleType = doubleType;
    }
    public Date getDateType() {
        return dateType;
    }
    public void setDateType(Date dateType) {
        this.dateType = dateType;
    }
    public Long getLongType() {
        return longType;
    }
    public void setLongType(Long longType) {
        this.longType = longType;
    }
    public Boolean isBooleanType() {
		return booleanType;
	}
	public void setBooleanType(Boolean booleanType) {
		this.booleanType = booleanType;
	}

	private Integer intType;
    private String stringType;
    private Float floatType;
    private Double doubleType;
    private Date dateType;
    private Long longType;
    private Boolean booleanType;
}
