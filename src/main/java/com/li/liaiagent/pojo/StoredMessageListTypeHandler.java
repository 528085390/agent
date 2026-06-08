package com.li.liaiagent.pojo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 更健壮的 TypeHandler：兼顾 String / PGobject / byte[] 等情况，并在解析前打印原始值以便排查。
 */
public class StoredMessageListTypeHandler extends BaseTypeHandler<List<StoredMessage>> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<StoredMessage>> TYPE_REFERENCE = new TypeReference<>() {};

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<StoredMessage> parameter, JdbcType jdbcType)
            throws SQLException {
        try {
            ps.setString(i, OBJECT_MAPPER.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("Failed to serialize messages to JSON", e);
        }
    }

    @Override
    public List<StoredMessage> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object obj = rs.getObject(columnName);
        return parseObject(obj, columnName);
    }

    @Override
    public List<StoredMessage> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object obj = rs.getObject(columnIndex);
        return parseObject(obj, String.valueOf(columnIndex));
    }

    @Override
    public List<StoredMessage> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object obj = cs.getObject(columnIndex);
        return parseObject(obj, String.valueOf(columnIndex));
    }

    private List<StoredMessage> parseObject(Object obj, String columnIdentifier) throws SQLException {
        if (obj == null) {
            // 真实为空 -> 返回空列表（避免 NPE）
            return new ArrayList<>();
        }

        String json = null;

        try {
            if (obj instanceof String) {
                json = (String) obj;
            } else if (obj instanceof PGobject) {
                // PostgreSQL 的 json/jsonb 会以 PGobject 返回，其 getValue() 返回 JSON 文本
                json = ((PGobject) obj).getValue();
            } else if (obj instanceof byte[]) {
                // 有时候驱动或不当的 set 会写入 byte[]（以前日志里出现过 [B@... 的情况）
                json = new String((byte[]) obj);
            } else {
                // fallback to toString()
                json = obj.toString();
            }

            // debug 输出（运行时可观察）
            System.out.println("StoredMessageListTypeHandler.parse raw value for column " + columnIdentifier + " => " + (json == null ? "null" : json.substring(0, Math.min(200, json.length())) + (json.length() > 200 ? "..." : "")));

            if (json == null || json.isBlank()) {
                return new ArrayList<>();
            }
            return OBJECT_MAPPER.readValue(json, TYPE_REFERENCE);
        } catch (Exception e) {
            // 把错误信息包成 SQLException 抛出去，便于上层定位
            throw new SQLException("Failed to deserialize messages from JSON (raw=" + (json == null ? "null" : json) + ")", e);
        }
    }
}