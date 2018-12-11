package com.hanshow.support.util;

import com.typesafe.config.ConfigValue;

class Tree {
	private String index;
	
	private Tree childTree;
	
	private ConfigValue value;
	
	public Tree() {}
	
	public Tree(String index, Tree childTree) {
		this.index = index;
		this.childTree = childTree;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public Tree getChildTree() {
		return childTree;
	}

	public void setChildTree(Tree childTree) {
		this.childTree = childTree;
	}

	public ConfigValue getValue() {
		return value;
	}

	public void setValue(ConfigValue value) {
		this.value = value;
	}

}