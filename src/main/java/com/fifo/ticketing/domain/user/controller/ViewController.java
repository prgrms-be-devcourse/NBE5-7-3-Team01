package com.fifo.ticketing.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ViewController {

  @GetMapping("/signin")
  public String signin() {
    return "sign_in";
  }

  @GetMapping("/oauth/login")
  public String oauthLogin() {
    return "oauth_login";
  }

}
