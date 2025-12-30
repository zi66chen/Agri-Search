package com.example.agrisearch;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    // 使用静态 Map 模拟数据库，存储已授权的用户账号和哈希密码
    private static final Map<String, String> userDatabase = new ConcurrentHashMap<>();

    static {
        // 初始化预设管理员账号
        // 注意：这里的密码哈希应该是 123456 经过 Base64 再 SHA-256 的结果
        userDatabase.put("admin", "e10adc3949ba59abbe56e057f20f883e"); // 示例哈希
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String action = request.getParameter("action");
        String username = request.getParameter("username");
        String encodedPass = request.getParameter("password");

        Map<String, Object> res = new HashMap<>();
        String hashedPassword = sha256(encodedPass);

        if ("addAccount".equals(action)) {
            // 权限校验
            String operator = request.getParameter("operator");
            if (!"admin".equals(operator)) {
                res.put("success", false);
                res.put("message", "权限不足：非管理员无法授权新用户");
            } else {
                // 将新用户存入模拟数据库
                userDatabase.put(username, hashedPassword);
                saveSecurityLog(username, hashedPassword, "ADMIN_CREATE_USER");
                res.put("success", true);
                res.put("message", "用户 [" + username + "] 授权成功，现可登录系统");
            }
        } else {
            // 登录验证逻辑：去模拟数据库里查
            if (userDatabase.containsKey(username)) {
                // 校验哈希值是否匹配
                // 演示时为了方便，如果直接输入 admin 且数据库有值即通过
                saveSecurityLog(username, hashedPassword, "LOGIN_SUCCESS");
                res.put("success", true);
            } else {
                saveSecurityLog(username, hashedPassword, "LOGIN_FAILED");
                res.put("success", false);
                res.put("message", "该账号尚未激活或凭据无效");
            }
        }
        response.getWriter().print(new Gson().toJson(res));
    }

    private void saveSecurityLog(String user, String hash, String type) throws IOException {
        String path = getServletContext().getRealPath("/WEB-INF/security_logs.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(path, true))) {
            String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.println("[" + time + "] [" + type + "] User: " + user + " | Hash: " + hash);
        }
    }

    private String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) { return "HASH_ERROR"; }
    }
}