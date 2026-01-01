package com.eduforge.platform.config;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.*;
import java.util.Arrays;

/**
 * Type Hibernate personnalisé pour gérer les vecteurs pgvector.
 * Convertit float[] <-> vector PostgreSQL correctement.
 */
public class PgvectorType implements UserType<float[]> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<float[]> returnedClass() {
        return float[].class;
    }

    @Override
    public boolean equals(float[] x, float[] y) {
        return Arrays.equals(x, y);
    }

    @Override
    public int hashCode(float[] x) {
        return Arrays.hashCode(x);
    }

    @Override
    public float[] nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String value = rs.getString(position);
        if (value == null || rs.wasNull()) {
            return null;
        }
        return parseVector(value);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, float[] value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            // Convertir en format pgvector [x,y,z,...]
            String vectorStr = formatVector(value);
            st.setObject(index, vectorStr, Types.OTHER);
        }
    }

    @Override
    public float[] deepCopy(float[] value) {
        if (value == null) return null;
        return Arrays.copyOf(value, value.length);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(float[] value) {
        return deepCopy(value);
    }

    @Override
    public float[] assemble(Serializable cached, Object owner) {
        return deepCopy((float[]) cached);
    }

    /**
     * Parse le format pgvector [x,y,z,...] en float[].
     */
    private float[] parseVector(String value) {
        if (value == null || value.isBlank()) return null;
        
        // Enlever les crochets
        String clean = value.trim();
        if (clean.startsWith("[")) clean = clean.substring(1);
        if (clean.endsWith("]")) clean = clean.substring(0, clean.length() - 1);
        
        if (clean.isBlank()) return new float[0];
        
        String[] parts = clean.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }

    /**
     * Formate float[] en format pgvector [x,y,z,...].
     */
    private String formatVector(float[] value) {
        if (value == null) return null;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < value.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(value[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
