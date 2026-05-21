package com.sgd_hc.sgd_hc;

import com.sgd_hc.sgd_hc.module_expedientes.entity.Expediente;
import com.sgd_hc.sgd_hc.module_expedientes.repository.ExpedienteRepository;
import com.sgd_hc.sgd_hc.module_patients.entity.Patient;
import com.sgd_hc.sgd_hc.module_patients.repository.PatientRepository;

import com.sgd_hc.sgd_hc.module_users.entity.Permission;
import com.sgd_hc.sgd_hc.module_users.entity.Role;
import com.sgd_hc.sgd_hc.module_users.entity.User;
import com.sgd_hc.sgd_hc.module_users.repository.PermissionRepository;
import com.sgd_hc.sgd_hc.module_users.repository.RoleRepository;
import com.sgd_hc.sgd_hc.module_users.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    private final PatientRepository patientRepository;
    private final ExpedienteRepository expedienteRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info(">>> Iniciando DataInitializer...");

        // 1. Crear Permisos por defecto
        Set<Permission> allPermissions = createDefaultPermissions();
        Set<Permission> limitedPermissions = new HashSet<>(allPermissions);
        
        limitedPermissions.removeIf(p -> p.getName().endsWith("_DELETE"));

        // 2. Manejo de Roles 
        Map<String, String> rolesToCreate = Map.of(
                "ROLE_SUPERUSER", "Superusuario con acceso total",
                "ROLE_ADMIN", "Administrador con restricciones de borrado",
                "ROLE_MEDICO", "Personal médico del sistema",
                "ROLE_ARCHIVO", "Encargado de archivo histórico",
                "ROLE_DIRECTOR", "Director del hospital",
                "ROLE_PATIENT", "Paciente con acceso a sus propios expedientes",
                "ROLE_CLIENTE", "Cliente con acceso limitado"
        );

        for (Map.Entry<String, String> entry : rolesToCreate.entrySet()) {
            String roleName = entry.getKey();
            String roleDesc = entry.getValue();

            Role role = roleRepository.findByName(roleName)
                    .or(() -> roleRepository.findAll().stream()
                            .filter(r -> r.getName().equalsIgnoreCase(roleName))
                            .findFirst())
                    .orElse(null);

            if (role == null) {
                log.info(">>> Rol {} no encontrado, creando...", roleName);
                role = Role.builder()
                        .name(roleName)
                        .description(roleDesc)
                        .build();
                try {
                    role = roleRepository.saveAndFlush(role);
                } catch (Exception e) {
                    role = roleRepository.findAll().stream()
                            .filter(r -> r.getName().equalsIgnoreCase(roleName))
                            .findFirst().orElseThrow();
                }
            }

            // Asignación de permisos específicos
            if (roleName.equals("ROLE_SUPERUSER")) {
                role.setPermissions(allPermissions);
                roleRepository.saveAndFlush(role);
            } else if (roleName.equals("ROLE_ADMIN")) {
                role.setPermissions(limitedPermissions);
                roleRepository.saveAndFlush(role);
            } else if (roleName.equals("ROLE_PATIENT") || roleName.equals("ROLE_CLIENTE")) {
                Set<Permission> patientPermissions = new HashSet<>();
                permissionRepository.findByName("PATIENT_READ").ifPresent(patientPermissions::add);
                role.setPermissions(patientPermissions);
                roleRepository.saveAndFlush(role);
            }
        }

        // 3. Crear o actualizar Usuarios (Obteniendo roles necesarios)
        Role superuserRole = roleRepository.findByName("ROLE_SUPERUSER").orElseThrow();
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        Role medicoRole = roleRepository.findByName("ROLE_MEDICO").orElseThrow(() -> new RuntimeException("ROLE_MEDICO not found"));
        Role patientRole = roleRepository.findByName("ROLE_PATIENT").orElseThrow();

        setupUser("superuser", "superuser@sgd.com", "Super", "User", "superuser123", "CI", "0000000", superuserRole);
        setupUser("admin", "admin@sgd.com", "Admin", "Limited", "admin123", "CI", "1111111", adminRole);
        setupUser("dr.garcia", "dr.garcia@clinica.com", "Carlos", "García", "medico123", "CI", "2222222", medicoRole);
        setupUser("paciente.juan", "juan.perez@example.com", "Juan", "Pérez", "paciente123", "CI", "12345678", patientRole);
        setupUser("paciente.maria", "maria.lopez@example.com", "María", "López", "paciente123", "CI", "87654321", patientRole);

        createPatientsIfNotExist();
        createExpedientesIfNotExist();
        
        log.info(">>> DataInitializer finalizado correctamente.");
    }

    private void setupUser(String username, String email, String first, String last, String pass, String docType, String docNum, Role role) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            log.info(">>> Actualizando roles y credenciales para el usuario: {}", username);
            user.setRoles(new HashSet<>(Set.of(role)));
            user.setPassword(passwordEncoder.encode(pass));
            // Sincronizamos campos extra si faltan
            if (user.getDocumentNumber() == null) {
                user.setDocumentType(docType);
                user.setDocumentNumber(docNum);
            }
            userRepository.saveAndFlush(user);
        } else {
            log.info(">>> Creando nuevo usuario: {}", username);
            userRepository.saveAndFlush(User.builder()
                    .username(username)
                    .email(email)
                    .firstName(first)
                    .lastName(last)
                    .password(passwordEncoder.encode(pass))
                    .documentType(docType)
                    .documentNumber(docNum)
                    .gender("M")
                    .isActive(true)
                    .roles(new HashSet<>(Set.of(role)))
                    .build());
        }
    }

    private Set<Permission> createDefaultPermissions() {
    log.info(">>> Creando permisos por defecto...");
    Set<Permission> permissions = new HashSet<>();
    
    // Lista única de permisos (eliminé los duplicados PATIENT_READ, etc.)
    List<String> permissionNames = List.of(
        "USER_READ", "USER_CREATE", "USER_UPDATE", "USER_DELETE",
        "ROLE_READ", "ROLE_CREATE", "ROLE_UPDATE", "ROLE_DELETE",
        "PERMISSION_READ", "PERMISSION_CREATE", "PERMISSION_UPDATE", "PERMISSION_DELETE",
        "PATIENT_READ", "PATIENT_CREATE", "PATIENT_UPDATE", "PATIENT_DELETE",
        "EXPEDIENTE_READ", "EXPEDIENTE_CREATE", "EXPEDIENTE_UPDATE", "EXPEDIENTE_DELETE"
    );

    for (String name : permissionNames) {
        // 1. Intentar encontrar el permiso existente
        Permission permission = permissionRepository.findByName(name).orElse(null);
        
        // 2. Si no existe, crearlo
        if (permission == null) {
            try {
                log.info(">>> Creando permiso: {}", name);
                permission = Permission.builder()
                        .name(name)
                        .module(name.split("_")[0])
                        .action(name)
                        .description("Permiso para " + name)
                        .build();
                permission = permissionRepository.save(permission);
            } catch (Exception e) {
                log.warn(">>> No se pudo crear permiso {}: {}", name, e.getMessage());
                // Intentar recuperar por si acaso ya existe (carrera crítica)
                permission = permissionRepository.findByName(name).orElse(null);
            }
        }
        
        if (permission != null) {
            permissions.add(permission);
        }
    }
    
    log.info(">>> Total de permisos creados/encontrados: {}", permissions.size());
    return permissions;
}
    
    
    private void createPatientsIfNotExist() {
    log.info(">>> Saltando creación de pacientes (los usuarios ya existen como pacientes)");
    // No hacer nada - los usuarios ya tienen los datos necesarios
}



    private void createExpedientesIfNotExist() {
        log.info(">>> Creando expedientes de prueba...");
        
        // Obtener referencias necesarias
        Patient patientJuan = patientRepository.findByDocumentNumber("12345678").orElse(null);
        Patient patientMaria = patientRepository.findByDocumentNumber("87654321").orElse(null);
        User medico = userRepository.findByUsername("dr.garcia").orElse(null);
        
        // Expediente para Juan Pérez
        if (patientJuan != null && expedienteRepository.findByNumeroExpediente("EXP-001").isEmpty()) {
            Expediente expediente = Expediente.builder()
                    .numeroExpediente("EXP-001")
                    .patient(patientJuan)
                    .estado("ACTIVO")
                    .fechaApertura(LocalDate.of(2024, 1, 15))
                    .diagnostico("Hipertensión arterial esencial")
                    .tratamiento("Enalapril 10mg cada 24 horas, dieta hiposódica")
                    .observaciones("Paciente con buen cumplimiento del tratamiento. Próximo control en 3 meses.")
                    .antecedentesMedicos("Familiar con hipertensión. No alergias conocidas. No fumador.")
                    .medico(medico)
                    .build();
            expedienteRepository.save(expediente);
            log.info(">>> Expediente EXP-001 creado para paciente Juan Pérez");
        }
        
        // Expediente para María López
        if (patientMaria != null && expedienteRepository.findByNumeroExpediente("EXP-002").isEmpty()) {
            Expediente expediente = Expediente.builder()
                    .numeroExpediente("EXP-002")
                    .patient(patientMaria)
                    .estado("ACTIVO")
                    .fechaApertura(LocalDate.of(2024, 2, 20))
                    .diagnostico("Diabetes tipo 2")
                    .tratamiento("Metformina 850mg cada 12 horas. Control de glucosa semanal.")
                    .observaciones("Paciente requiere seguimiento cada 3 meses. Derivada a nutricionista.")
                    .antecedentesMedicos("Antecedentes de obesidad. Madre con diabetes tipo 2.")
                    .medico(medico)
                    .build();
            expedienteRepository.save(expediente);
            log.info(">>> Expediente EXP-002 creado para paciente María López");
        }
        
        // Expediente archivado para Juan Pérez (histórico)
        if (patientJuan != null && expedienteRepository.findByNumeroExpediente("EXP-ARCH-001").isEmpty()) {
            Expediente expedienteArch = Expediente.builder()
                    .numeroExpediente("EXP-ARCH-001")
                    .patient(patientJuan)
                    .estado("ARCHIVADO")
                    .fechaApertura(LocalDate.of(2023, 6, 10))
                    .diagnostico("Infección respiratoria aguda")
                    .tratamiento("Amoxicilina 500mg cada 8 horas por 7 días")
                    .observaciones("Paciente recuperado sin complicaciones. Seguimiento completado.")
                    .antecedentesMedicos("Alergia a la penicilina? No confirmado en ese momento.")
                    .medico(medico)
                    .build();
            expedienteRepository.save(expedienteArch);
            log.info(">>> Expediente EXP-ARCH-001 (archivado) creado para paciente Juan Pérez");
        }
    }
}
