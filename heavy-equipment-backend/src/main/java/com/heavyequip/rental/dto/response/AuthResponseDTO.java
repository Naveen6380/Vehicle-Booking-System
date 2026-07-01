package com.heavyequip.rental.dto.response;

import com.heavyequip.rental.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponseDTO {

    private String token;
    private String name;
    private String email;
    private Role role;
}