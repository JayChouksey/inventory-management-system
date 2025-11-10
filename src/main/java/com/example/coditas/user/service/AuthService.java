package com.example.coditas.user.service;

import com.example.coditas.user.dto.*;
import com.example.coditas.user.entity.RefreshToken;
import com.example.coditas.user.entity.User;
import com.example.coditas.user.repository.RefreshTokenRepository;
import com.example.coditas.user.repository.UserRepository;
import com.example.coditas.common.exception.CustomException;
import com.example.coditas.common.util.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public AuthService(UserRepository userRepository, AuthenticationManager authManager, JwtService jwtService, RefreshTokenService refreshTokenService, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public LoginResponseDto loginUser(LoginRequestDto request) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Fetch the user safely
            User savedUser = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

            if (!authentication.isAuthenticated()) {
                throw new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED);
            }


            // Generate JWT
            String jwtToken = jwtService.generateToken(request.getEmail());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());;

            // Build response DTO
            LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                    .userId(savedUser.getId())
                    .email(savedUser.getEmail())
                    .name(savedUser.getName())
                    .phone(savedUser.getPhone())
                    .roleName(savedUser.getRole().getName().getVal())
                    .refreshToken(refreshToken.getToken())
                    .accessToken(jwtToken)
                    .build();


            return loginResponseDto;

        } catch (BadCredentialsException ex) {
            throw new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        } catch (CustomException ex) {
            throw ex; // GlobalExceptionHandler will handle it
        } catch (Exception ex) {
            throw new CustomException("Something went wrong during login", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public RefreshTokenDto getRefreshToken(RefreshTokenRequestDto refreshTokenRequestDto){
        String requestToken = refreshTokenRequestDto.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new CustomException("Invalid Refresh Token", HttpStatus.BAD_REQUEST));

        if(refreshTokenService.isTokenExpired(refreshToken)){
            throw new CustomException("Refresh token expired. Please login again.", HttpStatus.BAD_REQUEST);
        }

        String newAccessToken = jwtService.generateToken(refreshToken.getUser().getEmail());

        RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
        refreshTokenDto.setAccessToken(newAccessToken);

        return refreshTokenDto;
    }

    public String logoutUser(RefreshTokenRequestDto refreshTokenRequestDto) {
        String requestToken = refreshTokenRequestDto.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow( () -> new CustomException("Invalid refresh token.", HttpStatus.BAD_REQUEST));

        refreshTokenRepository.delete(refreshToken);

        return "Logged out successfully.";
    }
}

