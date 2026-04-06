package com.sgd_hc.sgd_hc.module_users.dto;

import java.util.Set;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RoleCreateDto(
    @NotBlank(message = "Nombre de rol es requerido")
    String name,
    
    String description,
    
    Set<Long> permissionsIds
) {
}