package com.hanshow.support.service;

import com.hanshow.support.model.Store;

public interface StoreService extends BaseService<Store, Integer> {

	Store query(Integer storeId);
	
	Store query(String name);
}
