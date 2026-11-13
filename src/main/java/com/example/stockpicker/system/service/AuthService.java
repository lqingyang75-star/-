package com.example.stockpicker.system.service;

import com.example.stockpicker.common.BizException;
import com.example.stockpicker.security.JwtUtils;
import com.example.stockpicker.security.LoginUser;
import com.example.stockpicker.security.LoginUserLoader;
import com.example.stockpicker.system.dto.LoginRequest;
import com.example.stockpicker.system.dto.LoginResponse;
import com.example.stockpicker.system.dto.UserInfo;
import com.example.stockpicker.system.entity.Menu;
import com.example.stockpicker.system.entity.User;
import com.example.stockpicker.system.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证服务：登录签发 token，并按用户 id 装载登录人与权限码。
 */
@Service
public class AuthService implements LoginUserLoader {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BizException(401, "账号或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BizException(401, "账号或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BizException(403, "该账号已被禁用");
        }
        String token = jwtUtils.generate(user.getId(), user.getUsername());
        return new LoginResponse(token, toUserInfo(user));
    }

    @Override
    @Transactional(readOnly = true)
    public LoginUser load(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(401, "用户不存在或已被删除"));
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BizException(403, "该账号已被禁用");
        }
        UserInfo info = toUserInfo(user);
        return new LoginUser(info.id(), info.username(), info.realName(), info.perms());
    }

    /**
     * 权限码 = 该用户所有角色下所有菜单里配置的 perms（含按钮）。
     */
    private UserInfo toUserInfo(User user) {
        Set<String> perms = user.getRoles().stream()
                .flatMap(role -> role.getMenus().stream())
                .map(Menu::getPerms)
                .filter(Objects::nonNull)
                .filter(p -> !p.isBlank())
                .collect(Collectors.toSet());
        return new UserInfo(user.getId(), user.getUsername(), user.getRealName(), perms);
    }
}
