package com.jobportal.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobportal.dto.LoginDTO;
import com.jobportal.dto.ResetPasswordDTO;
import com.jobportal.dto.ResponseDTO;
import com.jobportal.dto.UserDTO;
import com.jobportal.dto.VerifyOtpDTO;
import com.jobportal.exception.JobPortalException;
import com.jobportal.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@RestController
@CrossOrigin
@RequestMapping("/users")
@Validated
public class UserAPI {
	@Autowired
	private UserService userService;
	
	@PostMapping("/register")
	public ResponseEntity<UserDTO>registerUser(@RequestBody @Valid UserDTO userDTO) throws JobPortalException{
		return new ResponseEntity<>(userService.registerUser(userDTO), HttpStatus.CREATED);
	} 
	@PostMapping("/login")
	public ResponseEntity<UserDTO>loginUser(@RequestBody @Valid LoginDTO loginDTO) throws JobPortalException{
		return new ResponseEntity<>(userService.loginUser(loginDTO), HttpStatus.OK);
	}
	@PostMapping("/changePass")
	public ResponseEntity<ResponseDTO>changePassword(@RequestBody @Valid ResetPasswordDTO resetPasswordDTO) throws JobPortalException{
		return new ResponseEntity<>(userService.changePassword(resetPasswordDTO), HttpStatus.OK);
	}
	@PostMapping("/sendOtp/{email}")
	public ResponseEntity<ResponseDTO>sendOtp(@PathVariable @Email(message="{user.email.invalid}")  String email) throws Exception{
		userService.sendOTP(email);
		ResponseDTO response=new ResponseDTO("OTP sent successfully.");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	@PostMapping("/sendOtp/Register/{email}")
	public ResponseEntity<ResponseDTO>sendRegistrationOtp(@PathVariable @Email(message="{user.email.invalid}")  String email) throws Exception{
		userService.sendRegistrationOTP(email);
		ResponseDTO response=new ResponseDTO("Registration OTP sent successfully.");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	@PostMapping("/verifyOtp")
	public ResponseEntity<ResponseDTO>verifyOtp(@RequestBody @Valid VerifyOtpDTO verifyOtpDTO) throws JobPortalException{
		return new ResponseEntity<>(userService.verifyOtp(verifyOtpDTO.getEmail(), verifyOtpDTO.getOtp()), HttpStatus.ACCEPTED);
	}
}
