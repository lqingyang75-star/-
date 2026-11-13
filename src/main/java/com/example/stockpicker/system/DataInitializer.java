package com.example.stockpicker.system;

import com.example.stockpicker.system.entity.Menu;
import com.example.stockpicker.system.entity.Role;
import com.example.stockpicker.system.entity.User;
import com.example.stockpicker.system.repository.MenuRepository;
import com.example.stockpicker.system.repository.RoleRepository;
import com.example.stockpicker.system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 初始数据：首次启动时写入管理员账号、角色与全量菜单/按钮权限。
 * 用 java 代码而不用 data.sql，是为了同时兼容 H2 与 MySQL 方言，且可重复启动不重复插入。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
                           MenuRepository menuRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        List<Menu> all = new ArrayList<>();

        // 仪表盘
        saveMenu(all, 0L, "仪表盘", 2, "/dashboard", "dashboard/index", "dashboard:view", 1);

        // 财务收支
        Menu finance = saveMenu(all, 0L, "财务收支", 1, "/finance", null, null, 2);
        Menu record = saveMenu(all, finance.getId(), "收支记录", 2, "/record/list", "record/list", "fin:record:list", 1);
        saveMenu(all, record.getId(), "新增记录", 3, null, null, "fin:record:add", 1);
        saveMenu(all, record.getId(), "编辑记录", 3, null, null, "fin:record:edit", 2);
        saveMenu(all, record.getId(), "删除记录", 3, null, null, "fin:record:delete", 3);
        saveMenu(all, record.getId(), "导出Excel", 3, null, null, "fin:record:export", 4);
        saveMenu(all, record.getId(), "批量操作", 3, null, null, "fin:record:batch", 5);
        Menu category = saveMenu(all, finance.getId(), "收支分类", 2, "/record/category", "record/category", "fin:category:list", 2);
        saveMenu(all, category.getId(), "新增分类", 3, null, null, "fin:category:add", 1);
        saveMenu(all, category.getId(), "编辑分类", 3, null, null, "fin:category:edit", 2);
        saveMenu(all, category.getId(), "删除分类", 3, null, null, "fin:category:delete", 3);

        // 系统管理
        Menu system = saveMenu(all, 0L, "系统管理", 1, "/system", null, null, 3);
        Menu user = saveMenu(all, system.getId(), "用户管理", 2, "/system/user", "system/user", "system:user:list", 1);
        saveMenu(all, user.getId(), "新增用户", 3, null, null, "system:user:add", 1);
        saveMenu(all, user.getId(), "编辑用户", 3, null, null, "system:user:edit", 2);
        saveMenu(all, user.getId(), "删除用户", 3, null, null, "system:user:delete", 3);
        saveMenu(all, user.getId(), "启用禁用", 3, null, null, "system:user:status", 4);
        saveMenu(all, user.getId(), "重置密码", 3, null, null, "system:user:reset", 5);
        Menu role = saveMenu(all, system.getId(), "角色管理", 2, "/system/role", "system/role", "system:role:list", 2);
        saveMenu(all, role.getId(), "新增角色", 3, null, null, "system:role:add", 1);
        saveMenu(all, role.getId(), "编辑角色", 3, null, null, "system:role:edit", 2);
        saveMenu(all, role.getId(), "删除角色", 3, null, null, "system:role:delete", 3);
        saveMenu(all, role.getId(), "分配权限", 3, null, null, "system:role:assign", 4);
        Menu menuMgr = saveMenu(all, system.getId(), "菜单管理", 2, "/system/menu", "system/menu", "system:menu:list", 3);
        saveMenu(all, menuMgr.getId(), "新增菜单", 3, null, null, "system:menu:add", 1);
        saveMenu(all, menuMgr.getId(), "编辑菜单", 3, null, null, "system:menu:edit", 2);
        saveMenu(all, menuMgr.getId(), "删除菜单", 3, null, null, "system:menu:delete", 3);
        saveMenu(all, system.getId(), "操作日志", 2, "/system/log", "system/log", "system:log:list", 4);

        // 选股管理（原有业务作为第三个业务模块接入）
        Menu stock = saveMenu(all, 0L, "选股管理", 1, "/stock", null, null, 4);
        saveMenu(all, stock.getId(), "行情查询", 2, "/stock/query", "stock/index", "stock:query", 1);

        Role admin = new Role();
        admin.setName("超级管理员");
        admin.setCode("ADMIN");
        admin.setRemark("拥有全部菜单与按钮权限");
        admin.setMenus(new HashSet<>(all));
        roleRepository.save(admin);

        User account = new User();
        account.setUsername("admin");
        account.setPassword(passwordEncoder.encode("admin123"));
        account.setRealName("管理员");
        account.setStatus(1);
        account.setRoles(new HashSet<>(Set.of(admin)));
        userRepository.save(account);

        log.info("初始数据已写入：账号 admin / admin123，菜单与按钮共 {} 条", all.size());
    }

    private Menu saveMenu(List<Menu> bucket, Long parentId, String name, int type,
                          String path, String component, String perms, int sort) {
        Menu menu = new Menu();
        menu.setParentId(parentId);
        menu.setName(name);
        menu.setType(type);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setPerms(perms);
        menu.setSort(sort);
        menu.setStatus(1);
        Menu saved = menuRepository.save(menu);
        bucket.add(saved);
        return saved;
    }
}
