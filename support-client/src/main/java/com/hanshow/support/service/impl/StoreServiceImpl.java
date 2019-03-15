package com.hanshow.support.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.hanshow.support.dao.StoreRepository;
import com.hanshow.support.model.Store;
import com.hanshow.support.service.StoreService;

@Service
public class StoreServiceImpl extends BaseServiceImpl<Store, Integer>implements StoreService {

	@Autowired
	private StoreRepository storeRepository;

	@Override
	public Store query(Integer storeId) {
		// TODO Auto-generated method stub
		return storeRepository.getOne(storeId);
	}

	@Override
	public Store query(String name) {
		// TODO Auto-generated method stub
		Store store = new Store();
		store.setName(name);
		return storeRepository.findOne(Example.of(store)).get();
	}

}
