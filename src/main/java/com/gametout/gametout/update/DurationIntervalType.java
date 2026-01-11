package com.gametout.gametout.update;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import java.io.Serializable;
import java.sql.*;
import java.time.Duration;
import java.util.Objects;

public class DurationIntervalType implements UserType<Object> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class returnedClass() {
        return Duration.class;
    }

    @Override
    public boolean equals(Object x, Object y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Object x) {
        return Objects.hashCode(x);
    }

    @Override
    public Object nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String interval = rs.getString(position);
        if (rs.wasNull()) {
            return null;
        }
        return interval != null ? parseInterval(interval) : null;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            Duration duration = (Duration) value;
            st.setObject(index, formatInterval(duration), Types.OTHER);
        }
    }

    @Override
    public Object deepCopy(Object value) {
        if (value == null) {
            return null;
        }
        return Duration.ofSeconds(((Duration) value).getSeconds());
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) {
        return original;
    }

    private Duration parseInterval(String interval) {
        try {
            // Try ISO-8601 format first (e.g., "PT1H30M")
            return Duration.parse(interval);
        } catch (Exception e) {
            // Try PostgreSQL interval format (e.g., "01:30:00")
            try {
                String[] parts = interval.split(":");
                if (parts.length == 3) {
                    long hours = Long.parseLong(parts[0]);
                    long minutes = Long.parseLong(parts[1]);
                    long seconds = Long.parseLong(parts[2]);
                    return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
                } else if (parts.length == 2) {
                    long minutes = Long.parseLong(parts[0]);
                    long seconds = Long.parseLong(parts[1]);
                    return Duration.ofMinutes(minutes).plusSeconds(seconds);
                }
            } catch (Exception e2) {
                throw new IllegalArgumentException("Unsupported interval format: " + interval);
            }
        }
        throw new IllegalArgumentException("Unsupported interval format: " + interval);
    }

    private String formatInterval(Duration duration) {
        // Convert Duration to PostgreSQL interval format (e.g., "01:30:00")
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
