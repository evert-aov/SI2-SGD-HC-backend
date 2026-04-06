package com.sgd_hc.sgd_hc.module_users.dto;

import lombok.Builder;

@Builder
public record PermissionResponseDto(
    Long id,
    String name,
    String module,
    String action,
    String description,
    Boolean active
) {
}
