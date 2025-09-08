package com.Satisfyre.app.controller;


import com.Satisfyre.app.config.dotenvConfig;
import com.Satisfyre.app.dto.LoginRequest;
import com.Satisfyre.app.dto.RegistrationRequest;
import com.Satisfyre.app.dto.Response;
import com.Satisfyre.app.entity.UserEntity;
import com.Satisfyre.app.repo.UserRepository;
import com.Satisfyre.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.net.URI;
import java.security.Principal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5500")
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;

    String BASEURL = dotenvConfig.get("BASEURL");
    String AUTHURL = dotenvConfig.get("AUTHURL");
    String FRONT_ENDPOINT = dotenvConfig.get("FRONTEND_BASEURL");

    @PostMapping("/registers")
    public ResponseEntity<Response> registerUser(@RequestBody @Valid RegistrationRequest request){
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<Response> loginUser(@RequestBody @Valid LoginRequest request){
        return ResponseEntity.ok(userService.loginUser(request));
    }

    @GetMapping("/me/referral-link")
    public ResponseEntity<?> getReferralLink(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
        }

        String email = principal.getName(); // Comes from Spring Security
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }

        String referralLink = AUTHURL + "/register?ref=" + user.getReferralCode();

        return ResponseEntity.ok(Map.of(
                "referralCode", user.getReferralCode(),
                "referralLink", referralLink
        ));
    }

    @GetMapping("/register")
    public ResponseEntity<Void> redirectToFrontend(@RequestParam(required = false) String ref) {
        String frontendUrl = FRONT_ENDPOINT + "/html/auth-register-basic.html";
        String redirectUrl = (ref != null) ? frontendUrl + "?ref=" + ref : frontendUrl;
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }




}
