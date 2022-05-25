package com.example.todolist.config;

import com.example.todolist.entity.Users;
import com.example.todolist.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private UsersRepo usersRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Users> user = usersRepo.findByEmail(username);
        if (user == null || !user.isPresent()) {
            throw new UsernameNotFoundException(username);
        }
//        return new User(user.get().getEmail(), user.get().getPassword(), new ArrayList<>());
        return new User(username, user.get().getPassword(), new ArrayList<>());
    }
}
