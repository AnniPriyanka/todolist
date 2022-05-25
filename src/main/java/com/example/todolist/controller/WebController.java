package com.example.todolist.controller;

import com.example.todolist.config.JwtTokenUtil;
import com.example.todolist.entity.UserTask;
import com.example.todolist.modal.AssignTaskPojo;
import com.example.todolist.modal.UserTasksPojo;
import com.example.todolist.modal.UserTasksStatusPojo;
import com.example.todolist.repository.TasksRepo;
import com.example.todolist.repository.UserTaskRepo;
import com.example.todolist.repository.UsersRepo;
import com.example.todolist.entity.Users;
import com.example.todolist.entity.Tasks;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
public class WebController {
    @Autowired
    private UsersRepo usersRepo;
    @Autowired
    private TasksRepo tasksRepo;
    @Autowired
    private UserTaskRepo userTaskRepo;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //Admin Access
    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers(@RequestBody Tasks task,
                                       @RequestHeader(name = "Authorization") String token) {
        Boolean isAdmin = jwtTokenUtil.isAdminFromToken(token);
        if (!isAdmin) {
            return new ResponseEntity<>("You need to have Admin access.",
                    HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(usersRepo.findAll());

    }

    //access to both
    @GetMapping("/getAllTasks")
    public Iterable<Tasks> getAllTasks() {
        return tasksRepo.findAll();

    }


    //no restriction to access
    @PostMapping("/createUser")
    public String createUser(@RequestBody Users user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedOn(new Date());
        user.setUpdatedOn(new Date());
        user.setActive(true);
        user.setAdmin(false);
        usersRepo.save(user);
        return "User created successfully";
    }

    //no restriction to access
    @PostMapping("/createAdmin")
    public String createAdmin(@RequestBody Users user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedOn(new Date());
        user.setUpdatedOn(new Date());
//        user.setActive(true);
        user.setAdmin(true);
        usersRepo.save(user);
        return "Admin created successfully";
    }

    //admin access
    @PostMapping("/createTask")
    public ResponseEntity<?> createTask(@RequestBody Tasks task,
                            @RequestHeader(name = "Authorization") String token) {
        Boolean isAdmin = jwtTokenUtil.isAdminFromToken(token);
        if (!isAdmin) {
            return new ResponseEntity<>("You need to have Admin access.",
                    HttpStatus.BAD_REQUEST);
        }
        task.setCreatedOn(new Date());
        task.setUpdatedOn(new Date());
        task.setActive(true);
        return ResponseEntity.ok(tasksRepo.save(task));
    }

    //accessed by both
    @GetMapping("/getTaskById/{id}")
    public Optional<Tasks> getTaskById(@PathVariable(value = "id") Long id) {

        return tasksRepo.findById(id);
    }



    //Admin Access
    @PutMapping("/updateTask/{id}")
    public ResponseEntity<?> updateTask(@PathVariable("id") Long id, @RequestBody Tasks task,
                             @RequestHeader(name = "Authorization") String token) {
        Boolean isAdmin = jwtTokenUtil.isAdminFromToken(token);
        if (!isAdmin) {
            return new ResponseEntity<>("You need to have Admin access.",
                    HttpStatus.BAD_REQUEST);
        }
        Optional<Tasks> taskData = tasksRepo.findById(id);
        if (taskData.isPresent()) {
            Tasks x = taskData.get();
            x.setDescription(task.getDescription());
            x.setUpdatedOn(new Date());
            System.out.println(x);
            tasksRepo.save(x);

            return ResponseEntity.ok("Task Data updated successfully");
        }
        return ResponseEntity.ok("Task Data not present");
    }


    //Admin Access
    @PostMapping("/assignTask")
    public ResponseEntity<?> assignTask(@RequestBody AssignTaskPojo assignTaskPojo,
                             @RequestHeader(name = "Authorization") String token) {
        Boolean isAdmin = jwtTokenUtil.isAdminFromToken(token);
        if (!isAdmin) {
            return new ResponseEntity<>("You need to have Admin access.",
                    HttpStatus.BAD_REQUEST);
        }
        UserTask userTask = new UserTask();

        Optional<Tasks> t = tasksRepo.findById(assignTaskPojo.getTaskId());
        Optional<Users> u = usersRepo.findById(assignTaskPojo.getUserId());

        if (!t.isPresent() || !u.isPresent()) {
            return ResponseEntity.ok("User or task is not present.");
        }
        userTask.setTasks(t.get());
        userTask.setUsers(u.get());
        userTask.setStatus("Assigned");
        userTask.setCreatedOn(new Date());
        userTask.setCreatedBy("ADMIN");

        userTaskRepo.save(userTask);

        return ResponseEntity.ok("Successfully task assigned to " + u.get().getName());
    }

    //Admin Access
    @GetMapping("/getTasksByUser/{userId}")
    public ResponseEntity<?> getTasksByUser(@PathVariable(value = "userId") Long userId,
                                              @RequestHeader(name = "Authorization") String token) {
        Boolean isAdmin = jwtTokenUtil.isAdminFromToken(token);
        if (!isAdmin) {
            return new ResponseEntity<>("You need to have Admin access.",
                    HttpStatus.BAD_REQUEST);
        }
        List<UserTask> userTaskList = userTaskRepo.findByUsersId(userId);

        List<UserTasksPojo> res = new ArrayList<>();
        for (UserTask ut : userTaskList) {
            UserTasksPojo u = new UserTasksPojo();
            BeanUtils.copyProperties(ut.getTasks(), u);
            BeanUtils.copyProperties(ut, u);

            u.setUserId(ut.getUsers().getId());
            u.setTaskId(ut.getTasks().getId());

            res.add(u);
        }
        return ResponseEntity.ok(res);
    }

    //user access
    @GetMapping("/getTasksByStatus/{status}")
    public ResponseEntity<?> getTasksByStatus(@PathVariable(value = "status") String status,
                                                @RequestHeader(name = "Authorization") String token) {
        Boolean isAdmin = jwtTokenUtil.isAdminFromToken(token);
        if (isAdmin) {
            return new ResponseEntity<>("Only User access.",
                    HttpStatus.BAD_REQUEST);
        }
        List<UserTask> userTaskList=userTaskRepo.findByStatus(status);
        List<UserTasksPojo> res = new ArrayList<>();
        for (UserTask ut : userTaskList) {
            UserTasksPojo u = new UserTasksPojo();
            BeanUtils.copyProperties(ut.getTasks(), u);
            BeanUtils.copyProperties(ut, u);

            u.setUserId(ut.getUsers().getId());
            u.setTaskId(ut.getTasks().getId());

            res.add(u);
        }
        return ResponseEntity.ok(res);
    }

    //Admin Access
    @GetMapping("/getTaskByUserAndStatus/{userId}/{status}")
    public ResponseEntity<?> getTaskByUserAndStatus(@PathVariable(value = "userId") Long userId,
                                                    @PathVariable(value = "status") String status,
                                                    @RequestHeader(name = "Authorization") String token) {

        Boolean isAdmin = jwtTokenUtil.isAdminFromToken(token);
        if (!isAdmin) {
            return new ResponseEntity<>("You need to have Admin access.",
                    HttpStatus.BAD_REQUEST);
        }

        List<UserTask> userTaskList = userTaskRepo.findByUsersIdAndStatus(userId, status);

        List<UserTasksPojo> res = new ArrayList<>();
        for (UserTask ut : userTaskList) {
            UserTasksPojo u = new UserTasksPojo();
            BeanUtils.copyProperties(ut.getTasks(), u);
            BeanUtils.copyProperties(ut, u);

            u.setUserId(ut.getUsers().getId());
            u.setTaskId(ut.getTasks().getId());

            res.add(u);
        }
        return ResponseEntity.ok(res);
    }

    //User Access
    @PutMapping("/updateUserTaskStatus")
    public ResponseEntity<?> updateUserTask(@RequestBody UserTasksStatusPojo uts,
                                 @RequestHeader(name = "Authorization") String token) {
        Boolean isAdmin = jwtTokenUtil.isAdminFromToken(token);
        if (isAdmin) {
            return new ResponseEntity<>("Only User access.",
                    HttpStatus.BAD_REQUEST);
        }
        Optional<UserTask> userTaskData = userTaskRepo.findById(uts.getId());
        if (userTaskData.isPresent()) {
            UserTask ut = userTaskData.get();
            ut.setStatus(uts.getStatus());
            ut.setRemarks(uts.getRemarks());
            ut.setUpdatedOn(new Date());
            ut.setUpdatedBy("User");
            userTaskRepo.save(ut);

            return ResponseEntity.ok("Status updated successfully");
        }
        return ResponseEntity.ok("Data not present");
    }
}
