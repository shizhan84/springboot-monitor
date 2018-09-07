package cn.okcoming.monitor.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * 支持列表注释的修订版本
 * 采用@Data来管理getset及toString
 */
public class GenEntityMysql {
    private static final GenEntityMysql INSTANCE = new GenEntityMysql();

    private String[] colNames; // 列名数组
    private Integer[] colTypes; // 列名类型数组
    private String[] colRemarks; // 列名注释

    private boolean needUtil = false; // 是否需要导入包java.util.*
    private boolean needSql = false; // 是否需要导入包java.sql.*
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    // TODO 需要修改的地方
    private static final String URL = "jdbc:mysql://10.10.5.175:3306/report_data_screen?useUnicode=true&characterEncoding=utf-8";
    private static final String NAME = "root";
    private static final String PASS = "123456";
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private String packageOutPath = "com.kunsheng.dw.asset.domain";// 指定实体生成所在包的路径
    private String authorName = "bluces";// 作者名字

    private String tableName ; //"z_relation_person_company";// 表名 指定需要生成的表的表名 全部生成设置为null

    /**
     * 类的构造方法
     */
    private GenEntityMysql() {
    }

    /**
     * @return
     * @description 生成class的所有内容
     * @author paul
     * @date 2017年8月18日 下午5:30:07
     * @update 2017年8月18日 下午5:30:07
     * @version V1.0
     */
    private String parse(String[] table) {
        StringBuffer sb = new StringBuffer();
        sb.append("package " + packageOutPath + ";\r\n");
        sb.append("\r\n");
        // 判断是否导入工具包
        if (needUtil) {
            sb.append("import java.util.Date;\r\n");
        }
        if (needSql) {
            sb.append("import java.sql.*;\r\n");
        }
        sb.append("import lombok.Data;\r\n\r\n");
        // 注释部分
        sb.append("/**\r\n");
        sb.append(" *  " + table[1] + "\r\n");
        sb.append(" *  \r\n");
        sb.append(" * author name: " + authorName + "\r\n");
        sb.append(" * create time: " + SDF.format(new Date()) + "\r\n");
        sb.append(" */ \r\n");
        sb.append("@Data\r\n");
        // 实体部分
        sb.append("public class " + getTransStr(table[0], true) + "DO {\r\n\r\n");
        processAllAttrs(sb);// 属性
        sb.append("\r\n");
        sb.append("}\r\n");
        return sb.toString();
    }

    /**
     * @param sb
     * @description 生成所有成员变量
     * @author paul
     * @date 2017年8月18日 下午5:15:04
     * @update 2017年8月18日 下午5:15:04
     * @version V1.0
     */
    private void processAllAttrs(StringBuffer sb) {
        for (int i = 0; i < colNames.length; i++) {
            sb.append("\t/** "+ colRemarks[i] + " */\r\n");
            sb.append("\tprivate " + sqlType2JavaType(colTypes[i]) + " " + getTransStr(colNames[i], false) + ";\r\n");
        }
    }


    /**
     * @param str 传入字符串
     * @return
     * @description 将传入字符串的首字母转成大写
     * @author paul
     * @date 2017年8月18日 下午5:12:12
     * @update 2017年8月18日 下午5:12:12
     * @version V1.0
     */
    private String initCap(String str) {
        char[] ch = str.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z')
            ch[0] = (char) (ch[0] - 32);
        return new String(ch);
    }

    /**
     * @return
     * @description 将mysql中表名和字段名转换成驼峰形式
     * @author paul
     * @date 2017年8月18日 下午4:55:07
     * @update 2017年8月18日 下午4:55:07
     * @version V1.0
     */
    private String getTransStr(String before, boolean firstChar2Upper) {
        //不带"_"的字符串,则直接首字母大写后返回
        if (!before.contains("_"))
            return firstChar2Upper ? initCap(before) : before;
        String[] strs = before.split("_");
        StringBuffer after = null;
        if (firstChar2Upper) {
            after = new StringBuffer(initCap(strs[0]));
        } else {
            after = new StringBuffer(strs[0]);
        }
        if (strs.length > 1) {
            for (int i=1; i<strs.length; i++)
                after.append(initCap(strs[i]));
        }
        return after.toString();
    }

    /**
     * @return
     * @description 查找sql字段类型所对应的Java类型
     * @author paul
     * @date 2017年8月18日 下午4:55:41
     * @update 2017年8月18日 下午4:55:41
     * @version V1.0
     */
    private String sqlType2JavaType(Integer sqlType) {
        if (Types.BIT == sqlType) {
            return "Boolean";
        } else if (Types.TINYINT == sqlType) {
            return "Byte";
        } else if (Types.SMALLINT == sqlType) {
            return "Short";
        } else if (Types.INTEGER == sqlType) {
            return "Integer";
        } else if (Types.BIGINT== sqlType) {
            return "Long";
        } else if (Types.FLOAT== sqlType) {
            return "Float";
        } else if (Types.DECIMAL== sqlType || Types.NUMERIC== sqlType
                || Types.REAL== sqlType || Types.DOUBLE== sqlType) {
            return "Double";
        } else if (Types.VARCHAR== sqlType || Types.CHAR== sqlType
                || Types.NVARCHAR== sqlType || Types.NCHAR== sqlType
                || Types.LONGVARCHAR == sqlType) {
            return "String";
        } else if (Types.TIMESTAMP== sqlType) {
            return "Date";
        } else if (Types.BLOB== sqlType) {
            return "Blob";
        }
        return null;
    }

    /**
     *
     * @description 生成方法
     * @author paul
     * @date 2017年8月18日 下午2:04:20
     * @update 2017年8月18日 下午2:04:20
     * @version V1.0
     * @throws Exception
     */
    private void generate() throws Exception {
        //与数据库的连接
        Connection con;
        Class.forName(DRIVER);
        Properties props =new Properties();
        props.put("user", NAME);
        props.put("password",PASS);
        props.put("useInformationSchema","true"); //表注释
        con = DriverManager.getConnection(URL, props);
        System.out.println("connect database success...");
        //获取数据库的元数据
        DatabaseMetaData db = con.getMetaData();
        //是否有指定生成表，有指定则直接用指定表，没有则全表生成
        List<String[]> tableNames = new ArrayList<>();
        if (tableName == null) {
            //从元数据中获取到所有的表名
            ResultSet rs = db.getTables(null, null, null, new String[] { "TABLE" });
            while (rs.next())
                tableNames.add(new String[]{rs.getString("TABLE_NAME"),rs.getString("REMARKS")});
        } else {
            ResultSet rs = db.getTables(null, null, tableName, new String[] { "TABLE" });
            while (rs.next())
                tableNames.add(new String[]{rs.getString("TABLE_NAME"),rs.getString("REMARKS")});
        }

        PrintWriter pw = null;
        for (int j = 0; j < tableNames.size(); j++) {
            String[] table = tableNames.get(j);
            ResultSet rs = db.getColumns(null, "%", table[0], "%");
            List<String> a = new ArrayList<>();
            List<Integer> b = new ArrayList<>();
            List<String> c = new ArrayList<>();
            while(rs.next()){
                a.add(rs.getString("COLUMN_NAME"));
                b.add(rs.getInt("DATA_TYPE"));
                
                if(Objects.equals(rs.getInt("DATA_TYPE"), Types.TIMESTAMP)){
                    needUtil = true;
                }else if(Objects.equals(rs.getInt("DATA_TYPE"), Types.BLOB)){
                    needSql = true;
                }
                c.add(rs.getString("REMARKS"));
            }

            colNames = a.toArray(new String[0]);
            colTypes = b.toArray(new Integer[0]);
            colRemarks = c.toArray(new String[0]);

            //解析生成class的所有内容
            String content = parse(table);

            //输出生成文件
            File directory = new File("");
            String dirName = directory.getAbsolutePath() + "/src/main/java/" + packageOutPath.replace(".", "/");
            File dir = new File(dirName);
            if (!dir.exists() && dir.mkdirs()) System.out.println("generate dir 【" + dirName + "】");
            String javaPath = dirName + "/" + getTransStr(table[0], true) + "DO.java";
            FileWriter fw = new FileWriter(javaPath);
            pw = new PrintWriter(fw);
            pw.println(content);
            pw.flush();
            System.out.println("create class 【" + table[0] + "】");
            pw.close();
        }

    }

    /**
     * @param args
     * @description 执行方法
     * @author paul
     * @date 2017年8月18日 下午2:03:35
     * @update 2017年8月18日 下午2:03:35
     * @version V1.0
     */
    public static void main(String[] args) {
        try {
            INSTANCE.generate();
            System.out.println("generate classes success!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}