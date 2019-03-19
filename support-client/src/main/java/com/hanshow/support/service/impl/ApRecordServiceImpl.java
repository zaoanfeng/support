package com.hanshow.support.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hanshow.support.dao.ApRecordRepository;
import com.hanshow.support.model.ApRecord;
import com.hanshow.support.service.ApRecordService;

@Service
public class ApRecordServiceImpl extends BaseServiceImpl<ApRecord, Integer> implements ApRecordService {

	@Autowired
	private ApRecordRepository apRecordRepository;
	
	@Override
	public boolean deleteByDateBefore(Date date) {
		// TODO Auto-generated method stub
		apRecordRepository.deleteByDateBefore(date);
		return true;
	}

}
