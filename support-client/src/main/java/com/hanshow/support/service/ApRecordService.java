package com.hanshow.support.service;

import java.util.Date;

import com.hanshow.support.model.ApRecord;

public interface ApRecordService extends BaseService<ApRecord, Integer> {

	boolean deleteByDateBefore(Date date);
}
