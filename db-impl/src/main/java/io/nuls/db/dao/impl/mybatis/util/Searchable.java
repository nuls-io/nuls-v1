package io.nuls.db.dao.impl.mybatis.util;

import java.util.ArrayList;
import java.util.List;

/**
 * dao查询接口，封装查询语句工具类
 * @author zoro
 *
 */
public class Searchable {

	/**
     * 查询连接符，如：= != like
     */
    private List<Condition> operators;

    public Searchable() {
    	operators = new ArrayList<Condition>();
    }

    public Searchable(List<Condition> operators) {
    	this.operators = operators;
    }

    /**
     * 添加查询条件
     * @param c
     */
    public void addCondition(Condition c) {
    	this.operators.add(c);
    }


    public void addCondition(String key , SearchOperator operator, Object value) {
    	this.addCondition(new Condition(key,operator,value));
	}

    /**
     * 根据key删除某个条件
     * @param key
     */
    public void removeCondition(String key) {
    	Condition c = null;
    	for(int i=0; i<operators.size(); i++) {
    		c = operators.get(i);
    		if(c.getKey().equals(key)) {
    			operators.remove(i);
    			break;
    		}
    	}
    }

    /**
     * 根据key获取一个Condition
     * @param key
     * @return
     */
    public Condition getCondition(String key) {
    	Condition c = null;
    	for(int i=0; i<operators.size(); i++) {
    		c = operators.get(i);
    		if(c.getKey().equals(key)) {
    			return c;
    		}
    	}
    	return null;
    }
    
    
    public void removeAll() {
    	operators = new ArrayList<Condition>();
    }
    
}
