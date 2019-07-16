package com.tpp.persistence;

import org.hibernate.sql.JoinType;

import java.io.Serializable;

public class JoinCriteria implements Serializable {

	private static final long serialVersionUID = -7501057241800803697L;
	private JoinType joinType;
	private Meta criteria = new Meta();
	private String joinName;
	public static final String INNER_JOIN = "JOIN";
	public static final String LEFT_OUTER_JOIN = "LEFT_JOIN";
	public static final String RIGHT_OUTER_JOIN = "RIGHT_JOIN";

	public JoinType getJoinType() {
		return joinType;
	}

	public void setJoinType(String joinType) {
		switch (joinType) {
			case INNER_JOIN:
				this.joinType = JoinType.INNER_JOIN;
				break;
			case LEFT_OUTER_JOIN:
				this.joinType = JoinType.RIGHT_OUTER_JOIN;
				break;
			case RIGHT_OUTER_JOIN:
				this.joinType = JoinType.RIGHT_OUTER_JOIN;
				break;
			default:
				throw new IllegalArgumentException(ErrorCode.UNSUPPORTED_JOIN_PARAM.value(joinType));
		}
	}

	public Meta getCriteria() {
		return criteria;
	}

	public void setCriteria(Meta criteria) {
		this.criteria = criteria;
	}

	public String getJoinName() {
		return joinName;
	}

	public void setJoinName(String joinName) {
		this.joinName = joinName;
	}
}
