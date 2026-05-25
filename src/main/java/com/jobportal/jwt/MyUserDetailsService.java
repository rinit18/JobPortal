package com.jobportal.jwt;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jobportal.dto.UserDTO;
import com.jobportal.exception.JobPortalException;
import com.jobportal.service.UserService;

@Service
public class MyUserDetailsService implements UserDetailsService{
	
	@Autowired
	private UserService userService;
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		try {
			UserDTO dto=userService.getUserByEmail(email);
			List<SimpleGrantedAuthority> authorities = new ArrayList<>();
			if (dto.getAccountType() != null) {
				authorities.add(new SimpleGrantedAuthority("ROLE_" + dto.getAccountType().name()));
			}
			return new CustomUserDetails(dto.getId(), email,dto.getName(), dto.getPassword(),dto.getProfileId(), dto.getAccountType(), authorities);
		} catch (JobPortalException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
