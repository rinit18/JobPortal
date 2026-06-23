package com.jobportal.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobportal.exception.JobPortalException;
import com.jobportal.jwt.AuthenticationRequest;
import com.jobportal.jwt.AuthenticationResponse;
import com.jobportal.jwt.JwtHelper;
import java.util.Map;
import com.jobportal.entity.BlacklistedToken;
import com.jobportal.repository.BlacklistedTokenRepository;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin
@RequestMapping("/auth")
public class AuthAPI {
	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private JwtHelper jwtHelper;

	@Autowired
	private BlacklistedTokenRepository blacklistedTokenRepository;
	
	@PostMapping("/login")
	public ResponseEntity<?>createAuthenticationToken(@RequestBody AuthenticationRequest request) throws JobPortalException{
		try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new JobPortalException("Password didn't match");
        } catch (org.springframework.security.authentication.InternalAuthenticationServiceException e) {
            throw new JobPortalException("Email ID didn't match");
        } catch (AuthenticationException e) {
            throw new JobPortalException("Authentication Failed");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        final String jwt = jwtHelper.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			try {
				java.util.Date expiry = jwtHelper.getExpirationDateFromToken(token);
				BlacklistedToken bt = new BlacklistedToken(token, expiry);
				blacklistedTokenRepository.save(bt);
				return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
			} catch (Exception e) {
				return ResponseEntity.badRequest().body(Map.of("message", "Invalid token"));
			}
		}
		return ResponseEntity.badRequest().body(Map.of("message", "No token provided"));
	}
}
