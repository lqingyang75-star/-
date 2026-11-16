package com.example.stockpicker.system.controller;

import com.example.stockpicker.common.R;
import com.example.stockpicker.security.RequirePerm;
import com.example.stockpicker.security.UserContext;
import com.example.stockpicker.system.dto.MenuForm;
import com.example.stockpicker.system.dto.MenuNode;
import com.example.stockpicker.system.dto.MenuVO;
import com.example.stockpicker.system.service.MenuService;
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
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /** 当前登录人可见菜单树，前端据此动态注册路由与渲染侧边栏 */
    @GetMapping("/routes")
    public R<List<MenuNode>> routes() {
        return R.ok(menuService.treeForUser(UserContext.require().id()));
    }

    /** 导航菜单树（不含按钮），菜单管理页展示用 */
    @GetMapping("/tree")
    @RequirePerm("system:menu:list")
    public R<List<MenuNode>> tree() {
        return R.ok(menuService.treeAll());
    }

    /** 含按钮与停用项的全量菜单树，菜单管理与角色授权使用 */
    @GetMapping("/admin-tree")
    @RequirePerm("system:menu:list")
    public R<List<MenuVO>> adminTree() {
        return R.ok(menuService.adminTree());
    }

    @PostMapping
    @RequirePerm("system:menu:add")
    public R<Void> create(@RequestBody @Valid MenuForm form) {
        menuService.createMenu(form);
        return R.ok();
    }

    @PutMapping("/{id}")
    @RequirePerm("system:menu:edit")
    public R<Void> update(@PathVariable Long id, @RequestBody @Valid MenuForm form) {
        menuService.updateMenu(id, form);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePerm("system:menu:delete")
    public R<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return R.ok();
    }
}
