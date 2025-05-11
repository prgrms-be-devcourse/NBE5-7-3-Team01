package com.fifo.ticketing.domain.user.dto.form;

import com.fifo.ticketing.domain.user.entity.Role;
import com.fifo.ticketing.domain.user.entity.User;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserFormDetails implements UserDetails {


  private final String username;
  private final String password;
  private final Role role;

  @Getter
  private final Long id;

  @Getter
  private final String name;

  public UserFormDetails(User user) {
    this.id = user.getId();
    this.username = user.getEmail();
    this.password = user.getPassword();
    this.role = user.getRole();
    this.name = user.getUsername();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(role.name()));
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public String getUsername() {
    return this.username;
  }

}
