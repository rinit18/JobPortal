package com.jobportal.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import com.jobportal.dto.ApplicantDTO;
import com.jobportal.dto.Application;
import com.jobportal.dto.ApplicationStatus;
import com.jobportal.dto.JobDTO;
import com.jobportal.exception.JobPortalException;



public interface JobService {

	public JobDTO postJob(JobDTO jobDTO) throws JobPortalException;

	public List<JobDTO> getAllJobs() throws JobPortalException;

	public Map<String, Object> getJobsPage(int page, int size) throws JobPortalException;

	public JobDTO getJob(Long id) throws JobPortalException;

	public void applyJob(Long id, ApplicantDTO applicantDTO) throws JobPortalException;

	public List<JobDTO> getHistory(Long id, ApplicationStatus applicationStatus);

	public List<JobDTO> getJobsPostedBy(Long id) throws JobPortalException;

	public void changeAppStatus(Application application) throws JobPortalException;
	
	

}
