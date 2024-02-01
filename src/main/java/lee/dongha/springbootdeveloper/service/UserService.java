package lee.dongha.springbootdeveloper.service;

import lee.dongha.springbootdeveloper.domain.User;
import lee.dongha.springbootdeveloper.dto.AddUserRequest;
import lee.dongha.springbootdeveloper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public Long save(AddUserRequest request){
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return userRepository.save(User.builder().email(request.getEmail()).password(bCryptPasswordEncoder.encode(request.getPassword())).build()).getId();
    }

    public User findById(Long userId){
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }
}
