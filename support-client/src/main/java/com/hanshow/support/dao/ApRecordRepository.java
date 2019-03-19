package com.hanshow.support.dao;

import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hanshow.support.model.ApRecord;

@Repository
public interface ApRecordRepository extends BaseRepository<ApRecord, Integer> {

	
	@Query(value="DELETE FROM `ap_record` WHERE `log_time` < :logTime",nativeQuery = true)
	@Modifying
	@Transactional
	void deleteByDateBefore(@Param(value="logTime")Date logTime);
}
