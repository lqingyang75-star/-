package com.example.stockpicker.system.service;

import com.example.stockpicker.common.BizException;
import com.example.stockpicker.system.dto.MenuForm;
import com.example.stockpicker.system.dto.MenuNode;
import com.example.stockpicker.system.dto.MenuVO;
import com.example.stockpicker.system.entity.Menu;
import com.example.stockpicker.system.entity.User;
import com.example.stockpicker.system.repository.MenuRepository;
import com.example.stockpicker.system.repository.RoleRepository;
import com.example.stockpicker.system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单服务：按当前用户权限组装菜单树，驱动前端动态路由与侧边栏；同时提供菜单管理功能。
 */
@Service
public class MenuService {

    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final RoleRepository roleRepository;

    public MenuService(UserRepository userRepository, MenuRepository menuRepository,
                       RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
        this.roleRepository = roleRepository;
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

    /**
     * 含按钮与停用项的全量菜单树，菜单管理与角色授权使用。
     */
    @Transactional(readOnly = true)
    public List<MenuVO> adminTree() {
        return toVOs(menuRepository.findAllByOrderBySortAscIdAsc(), 0L);
    }

    @Transactional
    public void createMenu(MenuForm form) {
        validate(form, null);
        Menu menu = new Menu();
        apply(menu, form);
        menuRepository.save(menu);
    }

    @Transactional
    public void updateMenu(Long id, MenuForm form) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new BizException(400, "菜单不存在或已被删除"));
        validate(form, id);
        apply(menu, form);
        menuRepository.save(menu);
    }

    @Transactional
    public void deleteMenu(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new BizException(400, "菜单不存在或已被删除"));
        if (menuRepository.existsByParentId(id)) {
            throw new BizException(400, "请先删除该菜单下的子节点");
        }
        roleRepository.removeMenuRefsByMenuId(id);
        menuRepository.delete(menu);
    }

    private void validate(MenuForm form, Long selfId) {
        if (form.type() == null || form.type() < 1 || form.type() > 3) {
            throw new BizException(400, "菜单类型只能是 1（目录）、2（菜单）、3（按钮）");
        }
        Long parentId = form.parentId() == null ? 0L : form.parentId();
        if (Objects.equals(parentId, selfId)) {
            throw new BizException(400, "上级菜单不能是自己");
        }
        if (parentId != 0L) {
            Menu parent = menuRepository.findById(parentId)
                    .orElseThrow(() -> new BizException(400, "上级菜单不存在"));
            if (Integer.valueOf(3).equals(parent.getType())) {
                throw new BizException(400, "按钮下不能再挂子节点");
            }
        }
        if (Integer.valueOf(2).equals(form.type()) && !StringUtils.hasText(form.path())) {
            throw new BizException(400, "菜单必须填写路由路径");
        }
        if (Integer.valueOf(3).equals(form.type()) && !StringUtils.hasText(form.perms())) {
            throw new BizException(400, "按钮必须填写权限标识");
        }
    }

    private void apply(Menu menu, MenuForm form) {
        menu.setParentId(form.parentId() == null ? 0L : form.parentId());
        menu.setName(form.name().trim());
        menu.setType(form.type());
        menu.setPath(form.path());
        menu.setComponent(form.component());
        menu.setPerms(form.perms());
        menu.setIcon(form.icon());
        menu.setSort(form.sort() == null ? 0 : form.sort());
        menu.setStatus(form.status() == null ? 1 : form.status());
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

    private List<MenuVO> toVOs(List<Menu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> Objects.equals(m.getParentId(), parentId))
                .sorted(Comparator.comparing(Menu::getSort).thenComparing(Menu::getId))
                .map(m -> new MenuVO(m.getId(), m.getParentId(), m.getName(), m.getType(),
                        m.getPath(), m.getComponent(), m.getPerms(), m.getIcon(),
                        m.getSort(), m.getStatus(), toVOs(menus, m.getId())))
                .toList();
    }
}
