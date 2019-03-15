package com.hanshow.support.service;

import java.util.List;

import com.hanshow.support.model.DiskAnalysis;

public interface DiskAnalysisService extends BaseService<DiskAnalysis, Long> {

	List<DiskAnalysis> queryAllByFrameSizeGroupBy(Integer minFrameSize, Integer maxFrameSize);
	
	DiskAnalysis queryAllByFrameSizeAndTypeIsUpdateGroupBy(Integer minFrameSize, Integer maxFrameSize);	
	
}