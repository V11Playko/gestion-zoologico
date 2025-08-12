package com.playko.zoologico.configuration.security.userdetails;

import com.playko.zoologico.entity.Usuario;
import com.playko.zoologico.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final IUsuarioRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario user = userRepository.findByEmailWithRole(email);

        if (user == null) {
            throw new UsernameNotFoundException("Invalid email or password");
        }

        return CustomUserDetails.build(user, List.of(user.getRole()));
    }
}
