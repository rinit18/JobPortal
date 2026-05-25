package com.jobportal.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jobportal.dto.ApplicantDTO;
import com.jobportal.dto.Application;
import com.jobportal.dto.ApplicationStatus;
import com.jobportal.dto.JobDTO;
import com.jobportal.dto.JobStatus;
import com.jobportal.dto.NotificationDTO;
import com.jobportal.entity.Applicant;
import com.jobportal.entity.Job;
import com.jobportal.exception.JobPortalException;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.utility.Utilities;

@Service("jobService")
public class JobServiceImpl implements JobService {

	@Autowired
	private JobRepository jobRepository;
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private EmailService emailService;

	@Override
	public JobDTO postJob(JobDTO jobDTO) throws JobPortalException {
		if(jobDTO.getId()==0) {
			jobDTO.setId(Utilities.getNextSequenceId("jobs"));
			jobDTO.setPostTime(LocalDateTime.now());
			NotificationDTO notiDto=new NotificationDTO();
			notiDto.setAction("Job Posted");
			notiDto.setMessage("Job Posted Successfully for "+jobDTO.getJobTitle()+" at "+ jobDTO.getCompany());
			
			notiDto.setUserId(jobDTO.getPostedBy());
			notiDto.setRoute("/posted-jobs/"+jobDTO.getId());
				notificationService.sendNotification(notiDto);
		}
		else {
			Job job=jobRepository.findById(jobDTO.getId()).orElseThrow(() -> new JobPortalException("JOB_NOT_FOUND"));
			if(job.getJobStatus().equals(JobStatus.DRAFT) || jobDTO.getJobStatus().equals(JobStatus.CLOSED))jobDTO.setPostTime(LocalDateTime.now());
		}
		return jobRepository.save(jobDTO.toEntity()).toDTO();
	}

	
	@Override
	public List<JobDTO> getAllJobs() throws JobPortalException {
		return jobRepository.findByJobStatus(JobStatus.ACTIVE).stream().map((x) -> x.toDTO()).toList();
	}

	@Override
	public List<JobDTO> searchJobs(String query) throws JobPortalException {
		return jobRepository.findByJobStatus(JobStatus.ACTIVE).stream()
			.filter(j -> {
				if (Utilities.isFuzzyMatch(j.getJobTitle(), query)) return true;
				if (Utilities.isFuzzyMatch(j.getCompany(), query)) return true;
				if (Utilities.isFuzzyMatch(j.getLocation(), query)) return true;
				if (j.getSkillsRequired() != null && j.getSkillsRequired().stream().anyMatch(s -> Utilities.isFuzzyMatch(s, query))) return true;
				return false;
			})
			.map(Job::toDTO)
			.toList();
	}

	@Override
	public Map<String, Object> getJobsPage(int page, int size) throws JobPortalException {
		Pageable pageable = PageRequest.of(page, size);
		Page<Job> jobPage = jobRepository.findByJobStatus(JobStatus.ACTIVE, pageable);
		Map<String, Object> response = new HashMap<>();
		response.put("jobs", jobPage.getContent().stream().map(Job::toDTO).toList());
		response.put("totalElements", jobPage.getTotalElements());
		response.put("totalPages", jobPage.getTotalPages());
		response.put("currentPage", jobPage.getNumber());
		response.put("isLast", jobPage.isLast());
		return response;
	}

	@Override
	public JobDTO getJob(Long id) throws JobPortalException {
		return jobRepository.findById(id).orElseThrow(() -> new JobPortalException("JOB_NOT_FOUND")).toDTO();
	}

	@Override
	public void applyJob(Long id, ApplicantDTO applicantDTO) throws JobPortalException {
		Job job = jobRepository.findById(id).orElseThrow(() -> new JobPortalException("JOB_NOT_FOUND"));
		List<Applicant> applicants = job.getApplicants();
		if (applicants == null)applicants = new ArrayList<>();
		if (applicants.stream().anyMatch((x) -> x.getApplicantId().equals(applicantDTO.getApplicantId())))throw new JobPortalException("JOB_APPLIED_ALREADY");
		applicantDTO.setApplicationStatus(ApplicationStatus.APPLIED);
		applicants.add(applicantDTO.toEntity());
		job.setApplicants(applicants);
		jobRepository.save(job);
		
		NotificationDTO notiDto = new NotificationDTO();
		notiDto.setAction("New Application");
		notiDto.setMessage(applicantDTO.getName() + " applied for your job: " + job.getJobTitle());
		notiDto.setUserId(job.getPostedBy());
		notiDto.setRoute("/posted-jobs/" + id);
		notificationService.sendNotification(notiDto);

		// Send Gmail to employer
		userRepository.findById(job.getPostedBy()).ifPresent(employer -> {
			emailService.sendNewApplicationEmail(
				employer.getEmail(),
				employer.getName() != null ? employer.getName() : "Recruiter",
				applicantDTO.getName(),
				job.getJobTitle()
			);
		});
	}

	@Override
	public List<JobDTO> getHistory(Long id, ApplicationStatus applicationStatus) {
		return jobRepository.findByApplicantIdAndApplicationStatus(id, applicationStatus).stream().map((x) -> x.toDTO())
				.toList();
	}

	@Override
	public List<JobDTO> getJobsPostedBy(Long id) throws JobPortalException {
		return jobRepository.findByPostedBy(id).stream().map((x) -> x.toDTO()).toList();
	}


	@Override
	public void changeAppStatus(Application application) throws JobPortalException {
		Job job = jobRepository.findById(application.getId()).orElseThrow(() -> new JobPortalException("JOB_NOT_FOUND"));
		List<Applicant> apps = job.getApplicants().stream().map((x) -> {
			if (application.getApplicantId().equals(x.getApplicantId())) {
				x.setApplicationStatus(application.getApplicationStatus());
				if(application.getApplicationStatus().equals(ApplicationStatus.INTERVIEWING)) {
					x.setInterviewTime(application.getInterviewTime());
					NotificationDTO notiDto=new NotificationDTO();
					notiDto.setAction("Interview Scheduled");
					notiDto.setMessage("Interview scheduled for job: "+job.getJobTitle());
					notiDto.setUserId(application.getApplicantId());
					notiDto.setRoute("/job-history");
					try { notificationService.sendNotification(notiDto); } catch (JobPortalException e) { e.printStackTrace(); }
					// Gmail
					userRepository.findById(application.getApplicantId()).ifPresent(applicant ->
						emailService.sendStatusUpdateEmail(applicant.getEmail(), applicant.getName() != null ? applicant.getName() : "Applicant", job.getJobTitle(), "INTERVIEWING")
					);
				} else {
					NotificationDTO notiDto=new NotificationDTO();
					notiDto.setAction("Application Update");
					notiDto.setMessage("Your application status for "+job.getJobTitle()+" is now "+application.getApplicationStatus());
					notiDto.setUserId(application.getApplicantId());
					notiDto.setRoute("/job-history");
					try { notificationService.sendNotification(notiDto); } catch (JobPortalException e) { e.printStackTrace(); }
					// Gmail
					String statusStr = application.getApplicationStatus().name();
					userRepository.findById(application.getApplicantId()).ifPresent(applicant ->
						emailService.sendStatusUpdateEmail(applicant.getEmail(), applicant.getName() != null ? applicant.getName() : "Applicant", job.getJobTitle(), statusStr)
					);
				}
			}
			return x;
		}).toList();
		job.setApplicants(apps);
		jobRepository.save(job);
		
	}

}
