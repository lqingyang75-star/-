package com.example.stockpicker.system.service;

import com.example.stockpicker.common.BizException;
import com.example.stockpicker.common.PageResult;
import com.example.stockpicker.system.dto.UserForm;
import com.example.stockpicker.system.dto.UserVO;
import com.example.stockpicker.system.entity.Role;
import com.example.stockpicker.system.entity.User;
import com.example.stockpicker.system.repository.RoleRepository;
import com.example.stockpicker.system.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户管理：增删改查、重置密码、启用禁用。
 * 用户名不可修改，避免与历史操作日志脱节。
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResult<UserVO> page(String keyword, Integer status, int page, int size) {
        int pageNo = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<User> result = userRepository.findAll(buildSpec(keyword, status), pageable);
        List<UserVO> items = result.getContent().stream().map(this::toVO).toList();
        return PageResult.of(result.getTotalElements(), pageNo, pageSize, items);
    }

    @Transactional(readOnly = true)
    public List<UserVO.RoleBrief> listAllRoles() {
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(r -> new UserVO.RoleBrief(r.getId(), r.getName(), r.getCode()))
                .toList();
    }

    @Transactional
    public void create(UserForm form) {
        String username = form.username().trim();
        if (!StringUtils.hasText(form.password())) {
            throw new BizException(400, "请设置初始密码");
        }
        if (form.password().length() < 6) {
            throw new BizException(400, "密码长度不能少于 6 位");
        }
        if (userRepository.existsByUsername(username)) {
            throw new BizException(400, "账号已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(form.password()));
        user.setRealName(form.realName());
        user.setStatus(form.status() == null ? 1 : form.status());
        user.setRoles(resolveRoles(form.roleIds()));
        userRepository.save(user);
    }

    @Transactional
    public void update(Long id, UserForm form) {
        User user = requireUser(id);
        user.setRealName(form.realName());
        if (form.status() != null) {
            user.setStatus(form.status());
        }
        user.setRoles(resolveRoles(form.roleIds()));
        userRepository.save(user);
    }

    @Transactional
    public void delete(Long id, Long operatorId) {
        if (id.equals(operatorId)) {
            throw new BizException(400, "不能删除当前登录账号");
        }
        User user = requireUser(id);
        userRepository.removeRoleRefs(id);
        user.setRoles(new HashSet<>());
        userRepository.delete(user);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 6) {
            throw new BizException(400, "新密码长度不能少于 6 位");
        }
        User user = requireUser(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void changeStatus(Long id, Integer status, Long operatorId) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException(400, "状态只能是 0（禁用）或 1（启用）");
        }
        if (id.equals(operatorId) && status == 0) {
            throw new BizException(400, "不能禁用当前登录账号");
        }
        User user = requireUser(id);
        user.setStatus(status);
        userRepository.save(user);
    }

    private Set<Role> resolveRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new BizException(400, "部分角色不存在或已被删除");
        }
        return new HashSet<>(roles);
    }

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BizException(400, "用户不存在或已被删除"));
    }

    private Specification<User> buildSpec(String keyword, Integer status) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("username"), like),
                        cb.like(root.get("realName"), like)));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private UserVO toVO(User user) {
        List<UserVO.RoleBrief> roles = user.getRoles().stream()
                .map(r -> new UserVO.RoleBrief(r.getId(), r.getName(), r.getCode()))
                .toList();
        return new UserVO(user.getId(), user.getUsername(), user.getRealName(),
                user.getStatus(), roles, user.getCreatedAt());
    }
}
