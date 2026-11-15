package com.marketsense.auth;


import com.marketsense.auth.dto.LoginUserDto;
import com.marketsense.auth.dto.RegisterUserDto;
import com.marketsense.common.dto.UserProfileDto;
import com.marketsense.common.enums.Role;
import com.marketsense.common.exceptions.EmailNotVerifiedException;
import com.marketsense.user.User;
import com.marketsense.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationService emailVerificationService;

    public AuthenticationService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                                 EmailVerificationService emailVerificationService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailVerificationService = emailVerificationService;
    }

    @Transactional
    public User register(@Valid @RequestBody RegisterUserDto request){
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getFirstName(), request.getLastName(), request.getEmail(), encodedPassword);
        user.setRole(Role.ROLE_USER);
        user.setEmailVerified(false);
        User savedUser = userRepository.save(user);
        emailVerificationService.createAndSend(savedUser);

        return savedUser;
    }

    public User login(LoginUserDto request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new RuntimeException("User not found."));

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email before signing in.");
        }

        return user;
    }


    public UserProfileDto getProfileDetails(User user){

        return new UserProfileDto(
                user
        );
    }

//    private String generateRandomAccountNumber(){
//        Random random = new Random();
//        String accountNumber;
//
//        do {
//            Long number = (long) (random.nextDouble() * 9_000_000_000L) + 1_000_000_000L;
//            accountNumber = String.valueOf(number);
//        } while(accountRepository.findByAccountNumber(accountNumber).isPresent()); // ensures no duplicate account number
//
//        return accountNumber;
//    }


}
