package com.hanshow.support.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hanshow.support.model.DiskAnalysis;

@Repository
public interface DiskAnalysisRepository extends BaseRepository<DiskAnalysis, Long> {

	/*@Query("SELECT date, store_id, CONVERT(TIMESTAMPDIFF(MINUTE, '?', log_time) /30, SIGNED) AS segment"
			+ "session_type, COUNT(*) AS sum FROM `analysis` GROUP BY date, store_id, segment, session_type;")*/
	@Query(value = "SELECT new DiskAnalysis(storeCode, sessionType, SUM(frames) AS frames, SUM(spentTime) AS spentTime, COUNT(*) AS eslCount) from DiskAnalysis where frames >= ?1 and frames < ?2 group by storeCode, sessionType")
	List<DiskAnalysis> findAllByFrameSizeGroupBy(long minFrameSize, long maxFrameSize);

}
