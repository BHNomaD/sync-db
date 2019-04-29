package com.nomad.app.model;

/**
 * @author Md Shariful Islam
 */
public class EnumerationList {

    public enum JdbcTemplates {
        originJdbcTemplate(1001),
        duplicateJdbcTemplate(1002);

        private int code;

        JdbcTemplates(int code) {
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

            throw new IllegalArgumentException("Unknown code for JdbcTemplates: " + code);
        }

        @Override
        public String toString() {
            String s = super.toString();
            return s.charAt(0) + s.substring(1);
        }
    }

    public enum ImportedKeys {
        PKTABLE_CAT(1101),
        PKTABLE_SCHEM(1102),
        PKTABLE_NAME(1103),
        PKCOLUMN_NAME(1104),
        FKTABLE_CAT(1105),
        FKTABLE_SCHEM(1106),
        FKTABLE_NAME(1107),
        FKCOLUMN_NAME(1108),
        KEY_SEQ(1109),
        UPDATE_RULE(1110),
        DELETE_RULE(1111),
        FK_NAME(1112),
        PK_NAME(1113),
        DEFERRABILITY(1114);

        private int code;

        ImportedKeys(int code) {
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

            throw new IllegalArgumentException("Unknown code for ImportedKeys: " + code);
        }

        @Override
        public String toString() {
            String s = super.toString();
            return s.charAt(0) + s.substring(1);
        }
    }

    public enum PrimaryKeys {
        TABLE_CAT(1201),
        TABLE_SCHEM(1202),
        TABLE_NAME(1203),
        COLUMN_NAME(1204),
        KEY_SEQ(1205),
        PK_NAME(1206);

        private int code;

        PrimaryKeys(int code) {
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

            throw new IllegalArgumentException("Unknown code for PrimaryKeys: " + code);
        }

        @Override
        public String toString() {
            String s = super.toString();
            return s.charAt(0) + s.substring(1);
        }
    }

    public enum Operator {
        INSERT(1301),
        DELETE(1302),
        UPDATE(1303);

        private int code;

        private Operator(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Operator valueOf(int code) {
            for (Operator s : Operator.values()) {
                if (code == s.getCode()) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Unknown code for Operator: " + code);
        }

        @Override
        public String toString() {
            String s = super.toString();
            return s.charAt(0) + s.substring(1);
        }
    }

    public enum Proeprties {
        DB_CONFIG_NAME(1401);

        private int code;

        private Proeprties(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Operator valueOf(int code) {
            for (Operator s : Operator.values()) {
                if (code == s.getCode()) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Unknown code for Properties: " + code);
        }

        @Override
        public String toString() {
            String s = super.toString();
            return s.charAt(0) + s.substring(1);
        }
    }

}
