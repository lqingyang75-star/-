package com.example.stockpicker.system.controller;

import com.example.stockpicker.common.R;
import com.example.stockpicker.security.RequirePerm;
import com.example.stockpicker.system.dto.RoleForm;
import com.example.stockpicker.system.dto.RoleVO;
import com.example.stockpicker.system.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @RequirePerm("system:role:list")
    public R<List<RoleVO>> list() {
        return R.ok(roleService.list());
    }

    @PostMapping
    @RequirePerm("system:role:add")
    public R<Void> create(@RequestBody @Valid RoleForm form) {
        roleService.create(form);
        return R.ok();
    }

    @PutMapping("/{id}")
    @RequirePerm("system:role:edit")
    public R<Void> update(@PathVariable Long id, @RequestBody @Valid RoleForm form) {
        roleService.update(id, form);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePerm("system:role:delete")
    public R<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return R.ok();
    }

    /** 角色已勾选的菜单 id，权限分配弹窗回显 */
    @GetMapping("/{id}/menus")
    @RequirePerm("system:role:assign")
    public R<List<Long>> menuIds(@PathVariable Long id) {
        return R.ok(roleService.menuIds(id));
    }

    @PutMapping("/{id}/menus")
    @RequirePerm("system:role:assign")
    public R<Void> assignMenus(@PathVariable Long id, @RequestBody MenuIdsBody body) {
        roleService.assignMenus(id, body.menuIds());
        return R.ok();
    }

    public record MenuIdsBody(List<Long> menuIds) {
    }
}
