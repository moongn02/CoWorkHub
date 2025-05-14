package cn.moongn.coworkhub.common.interceptor;

import cn.moongn.coworkhub.common.annotation.RequiresPermission;
import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.common.utils.JwtUtils;
import cn.moongn.coworkhub.mapper.UserMapper;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final PermissionService permissionService;
    private final ObjectMapper objectMapper;

    // 定义错误消息常量
    private static final String MSG_TOKEN_INVALID = "TOKEN_INVALID";
    private static final String MSG_USER_NOT_FOUND = "USER_NOT_FOUND";
    private static final String MSG_PERMISSION_DENIED = "PERMISSION_DENIED";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是方法请求，放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 获取方法上的权限注解
        RequiresPermission annotation = handlerMethod.getMethodAnnotation(RequiresPermission.class);
        if (annotation == null) {
            // 如果方法上没有注解，则检查类上是否有注解
            annotation = handlerMethod.getBeanType().getAnnotation(RequiresPermission.class);
            if (annotation == null) {
                // 没有权限要求，放行
                return true;
            }
        }

        // 获取权限代码
        String permissionValue = annotation.value();
        RequiresPermission.Logical logical = annotation.logical();

        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || !jwtUtils.validateToken(token)) {
            handleUnauthorized(response, MSG_TOKEN_INVALID);
            return false;
        }

        // 从token中获取用户名
        String username = jwtUtils.getUsernameFromToken(token);
        User user = userMapper.getByUsername(username);

        if (user == null) {
            handleUnauthorized(response, MSG_USER_NOT_FOUND);
            return false;
        }

        // 检查权限
        if (permissionValue.contains(",")) {
            // 多个权限判断
            String[] permissions = permissionValue.split(",");
            boolean hasPermission = false;

            if (logical == RequiresPermission.Logical.OR) {
                // 任一权限满足即可
                for (String permission : permissions) {
                    if (permissionService.hasPermission(user.getId(), permission.trim())) {
                        hasPermission = true;
                        break;
                    }
                }
            } else {
                // 需要满足所有权限
                hasPermission = Arrays.stream(permissions)
                        .allMatch(permission -> permissionService.hasPermission(user.getId(), permission.trim()));
            }

            if (!hasPermission) {
                handleUnauthorized(response, MSG_PERMISSION_DENIED);
                return false;
            }
        } else {
            // 单个权限判断
            if (!permissionService.hasPermission(user.getId(), permissionValue)) {
                handleUnauthorized(response, MSG_PERMISSION_DENIED);
                return false;
            }
        }

        return true;
    }

    /**
     * 处理未授权的请求
     */
    private void handleUnauthorized(HttpServletResponse response, String message) throws IOException {
        // 根据消息类型设置不同的状态码
        if (MSG_TOKEN_INVALID.equals(message)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401
        } else {
            response.setStatus(HttpStatus.FORBIDDEN.value()); // 403
        }

        response.setContentType("application/json;charset=UTF-8");

        Result<Object> result = Result.error(response.getStatus(), message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}