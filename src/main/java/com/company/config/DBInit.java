package com.company.config;

import com.company.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DBInit {

    private final AdminService adminService;
}
