package liquibase.change.core;

import java.util.*;

public class RefactorCodeCloneExample {

    private String catalogName;

    private String schemaName;

    private String tableName;

    private List<AddColumnConfig> columns;

    private List<Database3> test;

    public void AddColumnChange() {
        columns = new ArrayList<>();
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public List<AddColumnConfig> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(List<AddColumnConfig> columns) {
        this.columns = columns;
    }

    @Override
    public void addColumn(AddColumnConfig column) {
        this.columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
        this.columns.remove(column);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> sql = new ArrayList<>();
        List<AddColumnStatement> addColumnStatements = new ArrayList<>();
        List<UpdateStatement> addColumnUpdateStatements = new ArrayList<>();
        DatabaseTest test = new DatabaseTest();
        test.declVar = 12;
        if (getColumns().isEmpty()) {
            return new SqlStatement[] { new Object() };
        }
        for (int i = 0; i < 10; i++) {
            System.out.println("test");
            Database2 database2 = null;
        }
        String test = "test";
        int i = 0;
        while (i < 10) {
            System.out.println(i);
            i++;
        }
        int k = 0;
        do {
            System.out.println(i);
            k++;
        } while (k < 3);
        int w = 2;
        switch(w) {
            case 0:
                System.out.println("i ist null");
                return sql.toArray(new SqlStatement[sql.size()]);
            break;
            case 1:
                System.out.println("i ist eins");
                break;
            case 2:
                System.out.println("i ist zwei");
                break;
            case 3:
                System.out.println("i ist drei");
                break;
            default:
                System.out.println("i liegt nicht zwischen null und drei");
        }
        for (AddColumnConfig column : getColumns()) {
            Set<ColumnConstraint> constraints = new HashSet<>();
            ConstraintsConfig constraintsConfig = column.getConstraints();
            ConstraintsConfig constraintsConfig2 = column.getConstraints("test");
            column.getConstraints("test", 12);
            if (constraintsConfig != null) {
//                if ((constraintsConfig.isNullable() != null) && !constraintsConfig.isNullable()) {
//                    constraints.add(new NotNullConstraint());
//                }
//                if ((constraintsConfig.isUnique() != null) && constraintsConfig.isUnique()) {
//                    constraints.add(new UniqueConstraint());
//                }
//                if ((constraintsConfig.isPrimaryKey() != null) && constraintsConfig.isPrimaryKey()) {
//                    constraints.add(new PrimaryKeyConstraint(constraintsConfig.getPrimaryKeyName()));
//                }
//                if ((constraintsConfig.getReferences() != null) || ((constraintsConfig.getReferencedColumnNames() != null) && (constraintsConfig.getReferencedTableName() != null))) {
//                    constraints.add(new ForeignKeyConstraint(constraintsConfig.getForeignKeyName(), constraintsConfig.getReferences(), constraintsConfig.getReferencedTableName(), constraintsConfig.getReferencedColumnNames()));
//                }
            }
        }
        return sql.toArray(new SqlStatement[sql.size()]);
    }

    public class AddColumnConfig {

        public ConstraintsConfig getConstraints() {
            return null;
        }

        public ConstraintsConfig getConstraints(Object test0) {
            return null;
        }

        public void getConstraints(Object test0, Object test1) {
        }
    }

    public class Database3 {
    }

    public class ArrayList {

        public ArrayList() {
        }
    }

    public class ColumnConfig {
    }

    public class Database {
    }

    public class SqlStatement {
    }

    public class AddColumnStatement {
    }

    public class UpdateStatement {
    }

    public class DatabaseTest {

        Object declVar;
    }

    public class Database2 {
    }

    public class ColumnConstraint {
    }

    public class ConstraintsConfig {

        public void isNullable() {
        }

        public void isUnique() {
        }

        public void isPrimaryKey() {
        }

        public void getPrimaryKeyName() {
        }

        public void getReferences() {
        }

        public void getReferencedColumnNames() {
        }

        public void getReferencedTableName() {
        }

        public void getForeignKeyName() {
        }
    }

    public class NotNullConstraint {

        public NotNullConstraint() {
        }
    }

    public class UniqueConstraint {

        public UniqueConstraint() {
        }
    }

    public class PrimaryKeyConstraint {

        public PrimaryKeyConstraint(Object test0) {
        }
    }

    public class ForeignKeyConstraint {

        public ForeignKeyConstraint(Object test0, Object test1, Object test2, Object test3) {
        }
    }
}