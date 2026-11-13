package com.example.stockpicker.system.controller;

import com.example.stockpicker.common.R;
import com.example.stockpicker.security.RequirePerm;
import com.example.stockpicker.security.UserContext;
import com.example.stockpicker.system.dto.MenuNode;
import com.example.stockpicker.system.service.MenuService;
import org.springframework.web.bind.annotation.GetMapping;
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

    /** 全量菜单树，菜单管理页使用 */
    @GetMapping("/tree")
    @RequirePerm("system:menu:list")
    public R<List<MenuNode>> tree() {
        return R.ok(menuService.treeAll());
    }
}
