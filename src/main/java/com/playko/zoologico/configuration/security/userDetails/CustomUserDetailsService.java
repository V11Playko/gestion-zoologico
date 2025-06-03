package com.playko.zoologico.configuration.security.userDetails;


import com.playko.zoologico.entity.Role;
import com.playko.zoologico.entity.Usuario;
import com.playko.zoologico.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final IUsuarioRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario user = this.userRepository.findByEmail(email);

        List<Usuario> userEntity = userRepository.findAllById(user.getId());

        if (userEntity.isEmpty()) {
            throw new UsernameNotFoundException("Invalid email or password");
        }

        List<Role> roles = new ArrayList<>();

        for (Usuario usuario : userEntity) {
            roles.add(user.getRole());
        }
        return CustomUserDetails.build(user, roles);
    }

}
