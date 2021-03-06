package com.warrenbuffett.server.service;

import com.warrenbuffett.server.common.RedisUtil;
import com.warrenbuffett.server.common.SecurityUtil;
import com.warrenbuffett.server.controller.dto.*;
import com.warrenbuffett.server.jwt.JwtTokenProvider;
import com.warrenbuffett.server.domain.User;
import com.warrenbuffett.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RedisUtil redisUtil;

    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll(){
        return userRepository.findAll().stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
    }
    public User searchUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    public User searchUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public boolean deleteUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user!=null) {
            userRepository.delete(user);
            return true;
        }
        return false;
    }
    @Transactional
    public User resetUserPassword(PasswordResetRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.getEmail()).orElse(null);
        user.setPassword(passwordEncoder.encode(requestDto.getNewpassword()));
        return userRepository.save(user);
    }

    @Transactional
    public TokenDto loginUser(LoginRequestDto loginRequestDto) {
        // Login ID/PW ??? ???????????? AuthenticationToken ??????
        UsernamePasswordAuthenticationToken authenticationToken = loginRequestDto.toAuthentication();

        // ??????(????????? ???????????? ??????)
        // authenticate ???????????? ????????? ??? CustomUserDetailService ?????? ???????????? loadUserByUsername ????????? ??????
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        // JWT ?????? ??????
        TokenDto tokenDto = jwtTokenProvider.generateToken(authentication);
        redisUtil.setDataExpire(authentication.getName(),tokenDto.getRefreshToken(), 60 * 30L);
        return tokenDto;
    }

    @Transactional
    public TokenDto reissue(TokenRequestDto tokenRequestDto) {
        if (!jwtTokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("Refresh Token ??? ???????????? ????????????.");
        }
        // get user ID from Access Token
        Authentication authentication = jwtTokenProvider.getAuthentication(tokenRequestDto.getAccessToken());

        // get Refresh Token
        String refreshToken = redisUtil.getData(authentication.getName());
        if (refreshToken==null) throw new RuntimeException("???????????? ??? ??????????????????.");
        if (!refreshToken.equals(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("????????? ?????? ????????? ???????????? ????????????.");
        }

        TokenDto tokenDto = jwtTokenProvider.generateToken(authentication);
        redisUtil.setDataExpire(authentication.getName(),tokenDto.getRefreshToken(), 60 * 30L);
        return tokenDto;
    }

    @Transactional(readOnly = true)
    public User getUserInfo(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // ?????? SecurityContext ??? ?????? ?????? ?????? ????????????
    @Transactional(readOnly = true)
    public User getMyInfo() {
        return userRepository.findById(SecurityUtil.getCurrentMemberId()).orElse(null);
    }
}
