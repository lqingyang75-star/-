package com.example.stockpicker.system.service;

import com.example.stockpicker.common.BizException;
import com.example.stockpicker.system.dto.MenuNode;
import com.example.stockpicker.system.entity.Menu;
import com.example.stockpicker.system.entity.User;
import com.example.stockpicker.system.repository.MenuRepository;
import com.example.stockpicker.system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单服务：按当前用户权限组装菜单树，驱动前端动态路由与侧边栏。
 */
@Service
public class MenuService {

    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    public MenuService(UserRepository userRepository, MenuRepository menuRepository) {
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
    }

    /**
     * 当前用户可见菜单树（只含目录与菜单，不含按钮）。
     */
    @Transactional(readOnly = true)
    public List<MenuNode> treeForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(401, "用户不存在或已被删除"));
        Set<Menu> visible = user.getRoles().stream()
                .flatMap(role -> role.getMenus().stream())
                .filter(this::isNavigable)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return toNodes(visible, 0L);
    }

    /**
     * 全量菜单树，供菜单管理页使用。
     */
    @Transactional(readOnly = true)
    public List<MenuNode> treeAll() {
        Set<Menu> all = menuRepository.findAllByOrderBySortAscIdAsc().stream()
                .filter(this::isNavigable)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return toNodes(all, 0L);
    }

    private boolean isNavigable(Menu menu) {
        return menu.getType() != null && menu.getType() <= 2
                && Integer.valueOf(1).equals(menu.getStatus());
    }

    private List<MenuNode> toNodes(Collection<Menu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> Objects.equals(m.getParentId(), parentId))
                .sorted(Comparator.comparing(Menu::getSort).thenComparing(Menu::getId))
                .map(m -> new MenuNode(m.getId(), m.getParentId(), m.getName(), m.getType(),
                        m.getPath(), m.getComponent(), m.getIcon(), m.getSort(),
                        toNodes(menus, m.getId())))
                .toList();
    }
}
