package com.hanshow.support.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hanshow.support.analysis.PayloadType;
import com.hanshow.support.dao.DiskAnalysisRepository;
import com.hanshow.support.model.DiskAnalysis;
import com.hanshow.support.service.DiskAnalysisService;

@Service
public class DiskAnalysisServiceImpl extends BaseServiceImpl<DiskAnalysis, Long, DiskAnalysisRepository>implements DiskAnalysisService {

	@Autowired
	private DiskAnalysisRepository analysisRepository;
	
	@Override
	public List<DiskAnalysis> queryAllByFrameSizeGroupBy(Integer minFrameSize, Integer maxFrameSize) {
		return analysisRepository.findAllByFrameSizeGroupBy(minFrameSize, maxFrameSize);		
	}

	@Override
	public DiskAnalysis queryAllByFrameSizeAndTypeIsUpdateGroupBy(Integer minFrameSize, Integer maxFrameSize) {
		List<DiskAnalysis> list = this.queryAllByFrameSizeGroupBy(minFrameSize, maxFrameSize);
		for (DiskAnalysis analysis : list) {
			if (analysis.getSessionType().equals(PayloadType.UPDATE.getValue())) {
				return analysis;
			}
		}
		return null;
	}

}
