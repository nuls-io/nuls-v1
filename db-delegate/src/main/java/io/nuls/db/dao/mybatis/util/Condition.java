package io.nuls.db.dao.mybatis.util;

/**
 * 封装sql查询条件赋值,
 * prefix和endfix主要针对有时候查询条件需要在整条条件语句前后加上括号等情况时赋值调用
 * 如  (a.name is null or a.password is null)
 * @author zhouwei
 *
 */
public class Condition {
	
	public static final String AND = " and ";
	
	public static final String OR = " or ";
	
	/**查询字段*/
	private String key;
	
	/**查询值*/
	private Object value;
	
	/**查询逻辑运算符号 =, != , > , < 等*/
	private SearchOperator operator;
	
	/**查询链接符号,默认是and*/
	private String sqlSeparator = Condition.AND;
	
	/**查询条件前置符号*/
	private String prefix = "";
	
	/**查询条件后置符号*/
	private String endfix = "";
	
	public Condition() {
		
    }
	
	public Condition(String key,SearchOperator operator,Object value) {
		if(value == null) {
			value = "";
		}
		this.key = key;
		this.operator = operator;
		this.value = valueFromOperator(operator, value);
    }
	
	
	public Condition(String sqlSeparator, String key,SearchOperator operator,Object value) {
		if(value == null) {
			value = "";
		}
		this.sqlSeparator = sqlSeparator;
		this.key = key;
		this.operator = operator;
		this.value = valueFromOperator(operator, value);
	}
	
	/**根据运算逻辑符，校验value值*/
	public Object valueFromOperator(SearchOperator operator,Object value) {
		if(value == null) {
			value = "";
		}
		if (value.equals("true")) {
            value = true;
        }
        if (value.equals("false")) {
            value = false;
        }
        
        if (operator == SearchOperator.like || operator == SearchOperator.notLike) {
            return "%" + value + "%";
        }
        if (operator == SearchOperator.prefixLike || operator == SearchOperator.prefixNotLike) {
            return value + "%";
        }
        if (operator == SearchOperator.suffixLike || operator == SearchOperator.suffixNotLike) {
            return "%" + value;
        }
        if (operator == SearchOperator.in || operator == SearchOperator.notIn) {
            if (value instanceof String) {
                return "(" + value + ")";
            }
        }
		return value;
	}
	

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		if(value == null) {
			value = "";
		}
		this.value = value;
	}

	public SearchOperator getOperator() {
		return operator;
	}

	public void setOperator(SearchOperator operator) {
		this.operator = operator;
	}
	
	public String getSqlSeparator() {
		return sqlSeparator;
	}

	public void setSqlSeparator(String sqlSeparator) {
		this.sqlSeparator = sqlSeparator;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getEndfix() {
		return endfix;
	}

	public void setEndfix(String endfix) {
		this.endfix = endfix;
	}
 
	//获取查询条件
	public String getString() {
		if(this.operator == null) {
			this.operator = SearchOperator.eq;
		}
		
		String keyStr = this.sqlSeparator + " " + this.prefix + this.key;
		if(this.operator.getSymbol().equals("in") || this.operator.getSymbol().equals("not in")) {
			return keyStr + " " + this.operator.getSymbol() + " (" + this.value + ")" + endfix; 
		}else {
			return keyStr + " " + this.operator.getSymbol() + " " + this.value + endfix;
		}
	}
	
}
