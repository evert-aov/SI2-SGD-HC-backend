package com.sgd_hc.sgd_hc.module_users.dto;

import java.util.Set;

public record UserUpdateDto(
    String ci,
    String email,
    String firstName,
    String lastName,
    String password,
    Boolean isActive,
    Set<Long> rolesIds
) {

}
