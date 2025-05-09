package com.fifo.ticketing.domain.user.service;

import com.fifo.ticketing.domain.user.dto.oauth.MemberOAuthDetails;
import com.fifo.ticketing.domain.user.entity.User;
import com.fifo.ticketing.domain.user.repository.UserRepository;
import com.fifo.ticketing.global.exception.ErrorCode;
import com.fifo.ticketing.global.exception.ErrorException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberOAuthService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    OAuth2User oAuth2User = super.loadUser(userRequest);
    String provider = userRequest.getClientRegistration().getRegistrationId();
    MemberOAuthDetails memberOAuthDetails = MemberDetailsFactory.memberFormDetails(provider,
        oAuth2User);
    Optional<User> userOptional = userRepository.findByEmail(memberOAuthDetails.getEmail());
    User user = userOptional.orElseGet(
        () -> {
          User saved = User.builder()
              .email(memberOAuthDetails.getEmail())
              .username(memberOAuthDetails.getName())
              .provider(provider)
              .build();
          return userRepository.save(saved);
        }
    );
    if (user.getProvider().equals(provider)) {
      return memberOAuthDetails.setRole(user.getRole());
    } else {
      throw new ErrorException(ErrorCode.WRONG_PROVIDER);
    }
  }
}
