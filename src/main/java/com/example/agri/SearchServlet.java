package com.example.agrisearch;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/searchPests")
public class SearchServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String keyword = req.getParameter("keyword");
        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = DBUtils.getConnection()) {
            String sql = "SELECT * FROM pests_diseases WHERE name LIKE ? OR symptoms LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            String searchStr = (keyword == null || keyword.isEmpty()) ? "%%" : "%" + keyword + "%";
            pstmt.setString(1, searchStr);
            pstmt.setString(2, searchStr);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("name", rs.getString("name"));
                map.put("cropId", rs.getInt("crop_id"));
                map.put("symptoms", rs.getString("symptoms"));
                map.put("prevention", rs.getString("prevention"));
                map.put("imageUrl", rs.getString("image_url"));
                list.add(map);
            }
            resp.getWriter().write(new Gson().toJson(list));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("[]");
        }
    }
}