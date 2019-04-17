package com.nomad.app.model;

/**
 * @author Md Shariful Islam
 */
public class EnumerationList {

    public enum JdbcTemplates {
        originJdbcTemplate(10001),
        duplicateJdbcTemplate(10002);

        private int code;

        private JdbcTemplates(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static JdbcTemplates valueOf(int code) {
            for (JdbcTemplates s : JdbcTemplates.values()) {
                if (code == s.getCode()) {
                    return s;
                }
            }

            throw new IllegalArgumentException("Unknown code: " + code);
        }

        @Override
        public String toString() {
            String s = super.toString();
            return s.charAt(0) + s.substring(1);
        }
    }

    public enum ImportedKeys {
        PKTABLE_CAT(10101),
        PKTABLE_SCHEM(10102),
        PKTABLE_NAME(10103),
        PKCOLUMN_NAME(10104),
        FKTABLE_CAT(10105),
        FKTABLE_SCHEM(10106),
        FKTABLE_NAME(10107),
        FKCOLUMN_NAME(10108),
        KEY_SEQ(10109),
        UPDATE_RULE(10110),
        DELETE_RULE(10111),
        FK_NAME(10112),
        PK_NAME(10113),
        DEFERRABILITY(10114);

        private int code;

        private ImportedKeys(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static ImportedKeys valueOf(int code) {
            for (ImportedKeys s : ImportedKeys.values()) {
                if (code == s.getCode()) {
                    return s;
                }
            }

            throw new IllegalArgumentException("Unknown code: " + code);
        }

        @Override
        public String toString() {
            String s = super.toString();
            return s.charAt(0) + s.substring(1);
        }
    }

    public enum PrimaryKeys {
        TABLE_CAT(10201),
        TABLE_SCHEM(10202),
        TABLE_NAME(10203),
        COLUMN_NAME(10204),
        KEY_SEQ(10205),
        PK_NAME(10206);

        private int code;

        private PrimaryKeys(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static PrimaryKeys valueOf(int code) {
            for (PrimaryKeys s : PrimaryKeys.values()) {
                if (code == s.getCode()) {
                    return s;
                }
            }

            throw new IllegalArgumentException("Unknown code: " + code);
        }

        @Override
        public String toString() {
            String s = super.toString();
            return s.charAt(0) + s.substring(1);
        }
    }

}
