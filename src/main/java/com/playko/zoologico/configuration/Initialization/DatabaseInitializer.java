package com.playko.zoologico.configuration.Initialization;

import com.playko.zoologico.entity.Role;
import com.playko.zoologico.entity.Usuario;
import com.playko.zoologico.repository.IRoleRepository;
import com.playko.zoologico.repository.IUsuarioRepository;
import com.playko.zoologico.service.IAuthPasswordEncoderPort;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {
    private final IUsuarioRepository userRepository;
    private final IAuthPasswordEncoderPort passwordEncoder;
    private final IRoleRepository roleRepository;

    public DatabaseInitializer(IUsuarioRepository userRepository, IAuthPasswordEncoderPort passwordEncoder, IRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void initialize() {
        initializeRoles();
        initializeAdminUser();
    }

    private void initializeRoles() {
        createRoleIfNotExists("ROLE_ADMIN", "ROLE_ADMIN");
        createRoleIfNotExists("ROLE_EMPLEADO", "ROLE_EMPLEADO");
    }

    private void createRoleIfNotExists(String name, String description) {
        Role role = roleRepository.findByNombre(name);
        if (role == null) {
            role = new Role();
            role.setNombre(name);
            role.setDescripcion(description);
            roleRepository.save(role);
        }
    }

    private void initializeAdminUser() {
        if (userRepository.findByEmail("admin@mail.com") == null) {
            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setEmail("admin@mail.com");
            admin.setPassword(passwordEncoder.encodePassword("admin"));

            Role adminRole = roleRepository.findByNombre("ROLE_ADMIN");
            admin.setRole(adminRole);

            userRepository.save(admin);
        }
    }
}
