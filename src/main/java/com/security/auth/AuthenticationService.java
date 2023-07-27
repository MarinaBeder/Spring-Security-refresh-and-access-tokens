package com.security.auth;

import java.io.IOException;



import java.net.http.HttpHeaders;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;



import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.config.JwtService;
import com.security.exceptions.UnauthorizedException;
import com.security.token.Token;
import com.security.token.TokenRepository;
import com.security.token.TokenType;
import com.security.user.Role;
import com.security.user.User;
import com.security.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
public class AuthenticationService {
	@Autowired
private UserRepository repository;
	
	@Autowired
private PasswordEncoder passwordEncoder;
	@Autowired
	private JwtService jwtService;
	@Autowired
    private AuthenticationManager authenticationManager;
	@Autowired
	private TokenRepository tokenRepo; 
	
	@Autowired
	UserRepository userRepo;


	public AuthenticationResponse register(RegisterRequest request ) {

		User user = new User();
		user.setFirstname(request.getFirstname());
		user.setEmail(request.getEmail());
		user.setLastname(request.getLastname());
	user.setPasswordChangeTime(new Date());
		user.setRole(Role.USER);

		user.setPassword(passwordEncoder.encode(request.getPassword()));
		var savedUser = repository.save(user);
String jwtToken = jwtService.generateToken(savedUser);
var refreshToken=jwtService.generateRefreshToken(savedUser);
 saveUserToken(savedUser, refreshToken);

 AuthenticationResponse authenticationResponse = new AuthenticationResponse();
 authenticationResponse.setAccessToken(jwtToken);
 authenticationResponse.setRefreshToken(refreshToken);

		return authenticationResponse;
				
	}
	private void saveUserToken(User user, String jwtToken) {
		Token token=new Token();
		 token.setToken(jwtToken);
		token.setExpired(false);
		token.setRevoked(false);
		token.setUser(user);
		token.setTokenType(TokenType.BEARER);
		tokenRepo.save(token);
	}
	
	private void revokeAllUserTokens(User user) {
		var validUserTokens=tokenRepo.findAllValidTokensByUser(user.getId());
		if(validUserTokens.isEmpty())
			return;
		validUserTokens.forEach(t->{
			t.setExpired(true);
			t.setRevoked(true);
		});
		tokenRepo.saveAll(validUserTokens);
	}
	
	public AuthenticationResponse authenticate(AuthenticationRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getEmail(),
						request.getPassword())
				);
		var user =repository.findByEmail(request.getEmail())
				.orElseThrow();
var jwtToken= jwtService.generateToken(user);
var refreshToken=jwtService.generateRefreshToken(user);
revokeAllUserTokens( user);
saveUserToken( user, refreshToken);

AuthenticationResponse authenticationResponse = new AuthenticationResponse();
authenticationResponse.setAccessToken(jwtToken);
authenticationResponse.setRefreshToken(refreshToken);
		return authenticationResponse;
	}

	
	
	public void refreshToken(
			HttpServletRequest request, 
			HttpServletResponse response) throws StreamWriteException, DatabindException, IOException 
	{
		final String authHeader = request.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION);	
		final String refreshToken;
		final String userEmail;
		if(authHeader==null||!authHeader.startsWith("Bearer ")) {
			return;
			
		}
		refreshToken=authHeader.substring(7);
		userEmail = jwtService.extractUsername(refreshToken);
		if(userEmail!=null) {
			var   user=
					this.repository.findByEmail(userEmail).orElseThrow();
			
			var isTokenValid=isRefreshTokenNotRevoked(refreshToken);
			
		 if(jwtService.isTokenValid(refreshToken, user)&&isTokenValid) {
		 var accessToken=jwtService.generateToken(user);
		AuthenticationResponse authResponse = new AuthenticationResponse();

		authResponse.setAccessToken(accessToken);
		authResponse.setRefreshToken(refreshToken);		
		
			new ObjectMapper().writeValue(response.getOutputStream(),authResponse);
			}
			else {
				throw new UnauthorizedException("invalid Refresh Token");
				
			}
	}
	}
	
	
	public Boolean isRefreshTokenNotRevoked(String refreshToken) {
		var isTokenValid=tokenRepo.findByToken(refreshToken)
				.map(t->!t.isExpired()&&!t.isRevoked())
				.orElse(false);
		return isTokenValid;
		
	}
}