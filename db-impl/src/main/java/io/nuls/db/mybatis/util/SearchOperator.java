package io.nuls.db.mybatis.util;

/**
 * 查询操作符
 * 
 * @author zhouwei
 * 
 */
public enum SearchOperator {
	eq("等于", "="), ne("不等于", "!="),
	gt("大于", ">"), gte("大于等于", ">="),
    lt("小于","<"), lte("小于等于", "<="),
    like("模糊匹配", "like"), notLike("不匹配","not like"),
	prefixLike("前缀模糊匹配", "like"), prefixNotLike("前缀模糊不匹配", "not like"),
	suffixLike("后缀模糊匹配", "like"), suffixNotLike("后缀模糊不匹配", "not like"),
	isNull("空", "is null"), isNotNull("非空", "is not null"),
	in("包含", "in"), notIn("不包含", "not in"),custom("自定义默认的", null);

	private final String info;
	private final String symbol;

	SearchOperator(final String info, String symbol) {
		this.info = info;
		this.symbol = symbol;
	}

	public String getInfo() {
		return info;
	}

	public String getSymbol() {
		return symbol;
	}
}
