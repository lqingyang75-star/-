package com.example.stockpicker.system.controller;

import com.example.stockpicker.common.PageResult;
import com.example.stockpicker.common.R;
import com.example.stockpicker.security.RequirePerm;
import com.example.stockpicker.security.UserContext;
import com.example.stockpicker.system.dto.UserForm;
import com.example.stockpicker.system.dto.UserVO;
import com.example.stockpicker.system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @RequirePerm("system:user:list")
    public R<PageResult<UserVO>> page(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Integer status,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return R.ok(userService.page(keyword, status, page, size));
    }

    /** 角色下拉选项，避免为了选角色额外申请角色管理权限 */
    @GetMapping("/role-options")
    @RequirePerm("system:user:list")
    public R<List<UserVO.RoleBrief>> roleOptions() {
        return R.ok(userService.listAllRoles());
    }

    @PostMapping
    @RequirePerm("system:user:add")
    public R<Void> create(@RequestBody @Valid UserForm form) {
        userService.create(form);
        return R.ok();
    }

    @PutMapping("/{id}")
    @RequirePerm("system:user:edit")
    public R<Void> update(@PathVariable Long id, @RequestBody @Valid UserForm form) {
        userService.update(id, form);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePerm("system:user:delete")
    public R<Void> delete(@PathVariable Long id) {
        userService.delete(id, UserContext.require().id());
        return R.ok();
    }

    @PutMapping("/{id}/status")
    @RequirePerm("system:user:status")
    public R<Void> changeStatus(@PathVariable Long id, @RequestBody StatusBody body) {
        userService.changeStatus(id, body.status(), UserContext.require().id());
        return R.ok();
    }

    @PutMapping("/{id}/password")
    @RequirePerm("system:user:reset")
    public R<Void> resetPassword(@PathVariable Long id, @RequestBody PasswordBody body) {
        userService.resetPassword(id, body.password());
        return R.ok();
    }

    public record StatusBody(Integer status) {
    }

    public record PasswordBody(String password) {
    }
}
