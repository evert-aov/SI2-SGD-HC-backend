package com.sgd_hc.sgd_hc.module_users.dto;

import java.util.Set;

public record UserResponseDto(
    Long id,
    String ci,
    String email,
    String firstName,
    String lastName,
    Boolean isActive,
    Set<Long> rolesIds
) {

}
