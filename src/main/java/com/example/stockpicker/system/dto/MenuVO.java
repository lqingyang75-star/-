package com.example.stockpicker.system.dto;

import java.util.List;

/**
 * 菜单管理用节点，包含按钮与停用项，前端按此渲染树形表格与权限勾选树。
 */
public record MenuVO(
        Long id,
        Long parentId,
        String name,
        Integer type,
        String path,
        String component,
        String perms,
        String icon,
        Integer sort,
        Integer status,
        List<MenuVO> children) {
}
