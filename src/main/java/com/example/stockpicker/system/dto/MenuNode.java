package com.example.stockpicker.system.dto;

import java.util.List;

/**
 * 菜单树节点。type：1 目录 2 菜单（按钮不进树，按钮只作为权限码返回）。
 */
public record MenuNode(
        Long id,
        Long parentId,
        String name,
        Integer type,
        String path,
        String component,
        String icon,
        Integer sort,
        List<MenuNode> children) {
}
