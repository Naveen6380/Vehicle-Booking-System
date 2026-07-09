package com.heavyequip.rental.service;

import com.heavyequip.rental.dto.request.LoginRequestDTO;
import com.heavyequip.rental.dto.request.RegisterRequestDTO;
import com.heavyequip.rental.dto.response.AuthResponseDTO;
import com.heavyequip.rental.entity.User;
import com.heavyequip.rental.exception.DuplicateEmailException;
import com.heavyequip.rental.exception.ResourceNotFoundException;
import com.heavyequip.rental.repository.UserRepository;
import com.heavyequip.rental.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Register a new Customer or Owner.
     * Steps:
     * 1. Check email duplicate
     * 2. Hash password (BCrypt)
     * 3. Save user
     * 4. Return JWT token
     */
    public AuthResponseDTO register(RegisterRequestDTO dto) {

        // Step 1: Duplicate email check
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException(
                    "Email already registered: " + dto.getEmail());
        }

        // Step 2 & 3: Build User entity with hashed password
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) // BCrypt hash
                .phone(dto.getPhone())
                .role(dto.getRole())
                .build();

        User savedUser = userRepository.save(user);

        // Step 4: Generate JWT and return
        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        return new AuthResponseDTO(
                token,
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    /**
     * Login with email + password.
     * Steps:
     * 1. Find user by email
     * 2. Verify password (BCrypt.matches)
     * 3. Return JWT token
     */
    public AuthResponseDTO login(LoginRequestDTO dto) {

        // Step 1: Find user
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account found with email: " + dto.getEmail()));

        // Step 2: Check password
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ResourceNotFoundException("Invalid password");
        }

        // Step 3: Generate token
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new AuthResponseDTO(
                token,
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}