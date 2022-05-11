package de.dis;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        try {
            ScheduleRunner runner = new ScheduleRunner();

            //S1 = r1(x) w2(x) c2 w1(x) r1(x) c1
//            runner.run(Arrays.asList(
//                    new RunnableOperation(1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
//                    new RunnableOperation(2, 'w', "UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;"),
//                    new RunnableOperation(2, 'c', "COMMIT;"),
//                    new RunnableOperation(1, 'w', "UPDATE dissheet3 SET name = name || ' + Max' WHERE id = 1;"),
//                    new RunnableOperation(1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
//                    new RunnableOperation(1, 'c', "COMMIT;"))
//            );

            //S2 = r1(x) w2(x) c2 r1(x) c1
//            runner.run(Arrays.asList(
//                    new RunnableOperation(1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
//                    new RunnableOperation(2, 'w', "UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;"),
//                    new RunnableOperation(2, 'c', "COMMIT;"),
//                    new RunnableOperation(1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
//                    new RunnableOperation(1, 'c', "COMMIT;"))
//            );

            //S3 = r2(x) w1(x) w1(y) c1 r2(y) w2(x) w2(y) c2
//            runner.run(Arrays.asList(
//                    new RunnableOperation(2, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
//                    new RunnableOperation(1, 'w', "UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;"),
//                    new RunnableOperation(1, 'w', "UPDATE dissheet3 SET name = 'Kiki' WHERE id = 2;"),
//                    new RunnableOperation(1, 'c', "COMMIT;"),
//                    new RunnableOperation(2, 'r', "SELECT name FROM dissheet3 WHERE id = 2;"),
//                    new RunnableOperation(2, 'w', "UPDATE dissheet3 SET name = 'Spickey' WHERE id = 1;"),
//                    new RunnableOperation(2, 'w', "UPDATE dissheet3 SET name = 'Viki' WHERE id = 2;"),
//                    new RunnableOperation(2, 'c', "COMMIT;"))
//            );

            //S1 = r1(x) w2(x) c2 w1(x) r1(x) c1
//            runner.run(Arrays.asList(
//                    // r-lock 1
//                    new RunnableOperation(1, 'r', "SELECT name FROM dissheet3 WHERE id = 1 FOR SHARE;"),
//                    // x-lock 2
//                    new RunnableOperation(2, 'r', "SELECT name FROM dissheet3 WHERE id = 1 FOR UPDATE;"),
//                    new RunnableOperation(2, 'w', "UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;"),
//                    new RunnableOperation(2, 'c', "COMMIT;"),
//                    // x-lock 1
//                    new RunnableOperation(1, 'r', "SELECT name FROM dissheet3 WHERE id = 1 FOR UPDATE;"),
//                    new RunnableOperation(1, 'w', "UPDATE dissheet3 SET name = name || ' + Max' WHERE id = 1;"),
//                    new RunnableOperation(1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
//                    new RunnableOperation(1, 'c', "COMMIT;"))
//            );

            //S2 = r1(x) w2(x) c2 r1(x) c1
//            runner.run(Arrays.asList(
//                    // r-lock 1
//                    new RunnableOperation(1, 'r', "SELECT name FROM dissheet3 WHERE id = 1 FOR SHARE;"),
//                    // x-lock 2
//                    new RunnableOperation(2, 'r', "SELECT name FROM dissheet3 WHERE id = 1 FOR UPDATE;"),
//                    new RunnableOperation(2, 'w', "UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;"),
//                    new RunnableOperation(2, 'c', "COMMIT;"),
//                    new RunnableOperation(1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
//                    new RunnableOperation(1, 'c', "COMMIT;"))
//            );

            //S3 = r2(x) w1(x) w1(y) c1 r2(y) w2(x) w2(y) c2
            runner.run(Arrays.asList(
                    // r-lock 1
                    new RunnableOperation(2, 'r', "SELECT name FROM dissheet3 WHERE id = 1 FOR SHARE;"),
                    // x-lock 1
                    new RunnableOperation(1, 'r', "SELECT name FROM dissheet3 WHERE id = 1 FOR UPDATE;"),
                    new RunnableOperation(1, 'w', "UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;"),
                    // x-lock 1
                    new RunnableOperation(1, 'r', "SELECT name FROM dissheet3 WHERE id = 2 FOR UPDATE;"),
                    new RunnableOperation(1, 'w', "UPDATE dissheet3 SET name = 'Kiki' WHERE id = 2;"),
                    new RunnableOperation(1, 'c', "COMMIT;"),
                    new RunnableOperation(2, 'r', "SELECT name FROM dissheet3 WHERE id = 2;"),
                    // x-lock 2
                    new RunnableOperation(2, 'r', "SELECT name FROM dissheet3 WHERE id = 1 FOR UPDATE;"),
                    new RunnableOperation(2, 'w', "UPDATE dissheet3 SET name = 'Spickey' WHERE id = 1;"),
                    // x-lock 2
                    new RunnableOperation(2, 'r', "SELECT name FROM dissheet3 WHERE id = 2 FOR UPDATE;"),
                    new RunnableOperation(2, 'w', "UPDATE dissheet3 SET name = 'Viki' WHERE id = 2;"),
                    new RunnableOperation(2, 'c', "COMMIT;"))
            );
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}

