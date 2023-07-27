package com.security.user;


import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.security.token.Token;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name="_user")
public class User implements UserDetails{
private static final long serialVersionUID = 7321374061017039662L;
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
	private String firstname;
	private String lastname;

	private String email;
	private String password;
	@Enumerated(EnumType.STRING)//this tell spring this is enum with string value  of enum
	private Role role;
 
	@OneToMany(mappedBy="user")
	private List<Token>tokens;
	
    private Date passwordChangeTime;

	
	
	public Date getPasswordChangeTime() {
		return passwordChangeTime;
	}

	public void setPasswordChangeTime(Date passwordChangeTime) {
		this.passwordChangeTime = passwordChangeTime;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public void setTokens(List<Token> tokens) {
		this.tokens = tokens;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/*@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return List.of(new SimpleGrantedAuthority(role.name()));
	}*/

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return password;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return List.of(new SimpleGrantedAuthority(role.name()));
	}
	//Write method to check if password is expired or not
			public boolean isPasswordExpired() {
				
				if (this.passwordChangeTime==null)return false;
				
				long currentTime=System.currentTimeMillis();
				long lastPasswordChangedTime=this.passwordChangeTime.getTime();
				//System.out.println(currentTime +"currentTime");
				//System.out.println(lastPasswordChangedTime + PASSWORD_EXPIRATION_TIME +"lastPasswordChangedTime + PASSWORD_EXPIRATION_TIME");

				return currentTime >lastPasswordChangedTime + PASSWORD_EXPIRATION_TIME;
				
			}
		private static final long PASSWORD_EXPIRATION_TIME=30L*24L*60L*60L*1000L;
	
    
}
