package ch.fhnw.oceandive.securityConfig;

import ch.fhnw.oceandive.model.UserEntity;
import ch.fhnw.oceandive.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CustomUserDetailsImpl implements UserDetailsService {


    private final UserRepository userRepository;
    public CustomUserDetailsImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return User.builder()
            .username(username)
            .password(userEntity.getPassword())
            .authorities(userEntity.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(
                    role.getRole().startsWith("ROLE_") ? role.getRole() : "ROLE_" + role.getRole()
                ))
                .collect(Collectors.toList()))
            .build();
    }
}