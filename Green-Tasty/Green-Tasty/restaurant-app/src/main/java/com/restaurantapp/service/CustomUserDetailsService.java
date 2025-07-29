package com.restaurantapp.service;

import com.restaurantapp.repo.AdminRepo;
import com.restaurantapp.repo.CustomerRepo;
import com.restaurantapp.repo.WaiterRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private WaiterRepo waiterRepo;
    private CustomerRepo customerRepo;
    private AdminRepo adminRepo;

    @Autowired
    public CustomUserDetailsService(WaiterRepo waiterRepo, CustomerRepo customerRepo, AdminRepo adminRepo) {
        this.waiterRepo = waiterRepo;
        this.customerRepo = customerRepo;
        this.adminRepo = adminRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        final String role;
        final Map<String, Object> userDetails;

        try {
            if (waiterRepo.isWaiter(userEmail)) {
                userDetails = waiterRepo.getWaiterDetails(userEmail);
                role = "WAITER";
            } else if(customerRepo.isCustomer(userEmail)) {
                userDetails = customerRepo.getCustomerDetails(userEmail);
                role = "CLIENT";
            } else {
                userDetails = adminRepo.getAdminDetails(userEmail);
                role = "ADMIN";
            }
        } catch (Exception e) {
            throw new UsernameNotFoundException("User doesn't exist.");
        }

        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.singleton(new SimpleGrantedAuthority(role));
            }

            @Override
            public String getPassword() {
                return (String) userDetails.get("password");
            }

            @Override
            public String getUsername() {
                return userEmail;
            }
        };
    }
}
