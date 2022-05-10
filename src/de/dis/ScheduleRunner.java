package de.dis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScheduleRunner {
    static class Operation implements Runnable {
        public final char readwrite;
        public final String op;
        public final Connection c;

        public Operation(Connection connection, char readwrite, String operation) {
            this.readwrite = readwrite;
            this.op = operation;
            this.c = connection;
        }

        public void run() {
            System.out.println(Thread.currentThread().getName() + "sql = " + op);
            Statement st;
            try {
                st = c.createStatement();
                if (readwrite == 'r') {
                    ResultSet rs = st.executeQuery(op);
                    while (rs.next())
                        System.out.println(rs.getString("name"));
                } else if (readwrite == 'w') {
                    st.execute(op);
                } else if (readwrite == 'c') {
                    c.commit();
                }

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    private final Connection c1;
    private final Connection c2;

    private static Connection getConnection() {
        try {
            // Holen der Einstellungen aus der db.properties Datei
            Properties properties = new Properties();
            FileInputStream stream = new FileInputStream(new File("db.properties"));
            properties.load(stream);
            stream.close();

            String jdbcUser = properties.getProperty("jdbc_user");
            String jdbcPass = properties.getProperty("jdbc_pass");
            String jdbcUrl = properties.getProperty("jdbc_url");
            // Verbindung zur Datenbank herstellen
            return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ScheduleRunner() throws SQLException {
        Connection i1 = getConnection();
        Statement cs = i1.createStatement();
        cs.execute("DROP TABLE if exists dissheet3;" +
                "CREATE TABLE dissheet3 (" +
                "id integer primary key," +
                "name VARCHAR(50));" +
                "INSERT INTO dissheet3 (id, name) VALUES (1, 'Goofy'),(2, 'Donald'),(3, 'Tick')," +
                "                                  (4, 'Trick'),(5, 'Track');");
        i1.close();

        c1 = getConnection();
        c1.setAutoCommit(false);
        c1.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
//        c1.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        //c1.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        c2 = getConnection();
        c2.setAutoCommit(false);
        c2.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
//        c2.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        //c2.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
    }

    public void run(List<RunnableOperation> schedule) throws SQLException, InterruptedException {
        List<Operation> operations = schedule.stream()
                .map(op -> new Operation(op.connection() == 1 ? c1 : c2, op.readwrite(), op.instruction())).toList();

        ExecutorService executor_t1 = Executors.newFixedThreadPool(1);
        ExecutorService executor_t2 = Executors.newFixedThreadPool(1);

        for (Operation op : operations) {
            if (op.c == c1)
                executor_t1.execute(op);

            if (op.c == c2)
                executor_t2.execute(op);

            Thread.sleep(250);  // Sleep, so the threads in both pools get executed in the desired order
        }

        executor_t1.shutdown();
        executor_t2.shutdown();

        while (!executor_t1.isTerminated() && !executor_t2.isTerminated()) {
            Thread.sleep(1000);
            System.out.println("Waiting for threads");
        }

        System.out.println("Finished all threads");


        // GET Table at the end
        Connection i2 = getConnection();
        Statement cs2 = i2.createStatement();
        ResultSet rs = cs2.executeQuery("SELECT id, name FROM dissheet3 ORDER BY id");
        while (rs.next())
            System.out.println(Integer.toString(rs.getInt("id")) + "," + rs.getString("name"));
        cs2.close();
    }
}
