package com.zepic.micrometer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * BK - <bk@zepic.com>
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping
    @TrackTime
    public List<String> getUsers() {
        return List.of("BK", "Naveen", "Sunil");
    }

}
