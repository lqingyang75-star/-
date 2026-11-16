package com.example.stockpicker.system.service;

import com.example.stockpicker.common.BizException;
import com.example.stockpicker.system.dto.RoleForm;
import com.example.stockpicker.system.dto.RoleVO;
import com.example.stockpicker.system.entity.Menu;
import com.example.stockpicker.system.entity.Role;
import com.example.stockpicker.system.repository.MenuRepository;
import com.example.stockpicker.system.repository.RoleRepository;
import com.example.stockpicker.system.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

/**
 * 角色管理与权限分配。角色与菜单多对多，勾选的菜单即授权范围。
 */
@Service
public class RoleService {

    private static final String ADMIN_CODE = "ADMIN";

    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    public RoleService(RoleRepository roleRepository, MenuRepository menuRepository,
                       UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleVO> list() {
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(r -> new RoleVO(r.getId(), r.getName(), r.getCode(), r.getRemark(), r.getMenus().size()))
                .toList();
    }

    @Transactional
    public void create(RoleForm form) {
        String code = form.code().trim().toUpperCase();
        if (roleRepository.existsByCode(code)) {
            throw new BizException(400, "角色标识已存在");
        }
        Role role = new Role();
        role.setName(form.name().trim());
        role.setCode(code);
        role.setRemark(form.remark());
        roleRepository.save(role);
    }

    @Transactional
    public void update(Long id, RoleForm form) {
        Role role = requireRole(id);
        String code = form.code().trim().toUpperCase();
        if (ADMIN_CODE.equals(role.getCode()) && !ADMIN_CODE.equals(code)) {
            throw new BizException(400, "内置管理员角色的标识不允许修改");
        }
        if (roleRepository.existsByCodeAndIdNot(code, id)) {
            throw new BizException(400, "角色标识已存在");
        }
        role.setName(form.name().trim());
        role.setCode(code);
        role.setRemark(form.remark());
        roleRepository.save(role);
    }

    @Transactional
    public void delete(Long id) {
        Role role = requireRole(id);
        if (ADMIN_CODE.equals(role.getCode())) {
            throw new BizException(400, "内置管理员角色不允许删除");
        }
        if (userRepository.countByRolesId(id) > 0) {
            throw new BizException(400, "该角色仍被用户使用，请先解除分配");
        }
        roleRepository.removeMenuRefs(id);
        role.setMenus(new HashSet<>());
        roleRepository.delete(role);
    }

    /** 角色已勾选的菜单 id，权限分配弹窗回显用 */
    @Transactional(readOnly = true)
    public List<Long> menuIds(Long roleId) {
        return requireRole(roleId).getMenus().stream().map(Menu::getId).toList();
    }

    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        Role role = requireRole(roleId);
        if (ADMIN_CODE.equals(role.getCode())) {
            throw new BizException(400, "内置管理员角色默认拥有全部权限，无需分配");
        }
        List<Menu> menus = menuIds == null || menuIds.isEmpty()
                ? List.of()
                : menuRepository.findAllById(menuIds);
        role.setMenus(new HashSet<>(menus));
        roleRepository.save(role);
    }

    private Role requireRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new BizException(400, "角色不存在或已被删除"));
    }
}
