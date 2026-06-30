package com.jobportal.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jobportal.dto.ProfileDTO;
import com.jobportal.dto.UserDTO;
import com.jobportal.entity.Profile;
import com.jobportal.exception.JobPortalException;
import com.jobportal.repository.ProfileRepository;
import com.jobportal.utility.Utilities;

@Service("profileService")
public class ProfileServiceImpl implements ProfileService {

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private UserService userService;
	
	@Override
	public Long createProfile(UserDTO userDTO) throws JobPortalException {
		Profile profile=new Profile();
		profile.setId(Utilities.getNextSequenceId("profiles"));
		profile.setEmail(userDTO.getEmail());
		profile.setName(userDTO.getName());
		profile.setSkills(new ArrayList<>());
		profile.setExperiences(new ArrayList<>());
		profile.setCertifications(new ArrayList<>());
		profileRepository.save(profile);
		return profile.getId();
	}

	@Override
	public ProfileDTO getProfile(Long id) throws JobPortalException {
		return profileRepository.findById(id).orElseThrow(()->new JobPortalException("PROFILE_NOT_FOUND")).toDTO();
	}

	@Override
	public ProfileDTO updateProfile(ProfileDTO profileDTO) throws JobPortalException {
		com.jobportal.dto.UserDTO currentUser = userService.getCurrentUser();
		if (!currentUser.getProfileId().equals(profileDTO.getId())) {
			throw new JobPortalException("UNAUTHORIZED");
		}
		profileRepository.findById(profileDTO.getId()).orElseThrow(()->new JobPortalException("PROFILE_NOT_FOUND"));
		profileRepository.save(profileDTO.toEntity());
		return profileDTO;
	}

	@Override
	public List<ProfileDTO> getAllProfiles() throws JobPortalException {
		return profileRepository.findAll().stream().map((x)->x.toDTO()).toList();
	}
	@Override
	public List<ProfileDTO> searchProfiles(String query) throws JobPortalException {
		return profileRepository.findAll().stream()
			.filter(p -> {
				if (Utilities.isFuzzyMatch(p.getName(), query)) return true;
				if (Utilities.isFuzzyMatch(p.getJobTitle(), query)) return true;
				if (p.getSkills() != null && p.getSkills().stream().anyMatch(s -> Utilities.isFuzzyMatch(s, query))) return true;
				return false;
			})
			.map((x) -> x.toDTO())
			.toList();
	}
	
}
