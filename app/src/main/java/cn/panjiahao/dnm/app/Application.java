package cn.panjiahao.dnm.app;

import cn.panjiahao.dnm.core.TableDiff;
import cn.panjiahao.dnm.core.entity.DiffJob;
import cn.panjiahao.dnm.core.entity.Table;
import cn.panjiahao.dnm.core.util.NoModelDataListener;
import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.io.File;

/**
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/27 20:04
 */
@SpringBootApplication
@ConfigurationPropertiesScan({"cn.panjiahao"})
@Slf4j
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        diff(args);
    }

    public static void diff(String[] filePaths){
        for (String filePath : filePaths) {
            log.info(filePath);
        }
        if (filePaths.length != 2) {
            log.error("请输入两个正确的Excel文件路径");
        }
        String fileName1 = filePaths[0];
        String fileName2 = filePaths[1];
        NoModelDataListener listener1;
        NoModelDataListener listener2;
        try {
            listener1 = new NoModelDataListener();
            listener2 = new NoModelDataListener();
            EasyExcel.read(fileName1, listener1).sheet().doRead();
            EasyExcel.read(fileName2, listener2).sheet().doRead();
            Table leftTable = listener1.getTable();
            Table rightTable = listener2.getTable();

            DiffJob diffJob = new DiffJob(leftTable, rightTable);
            TableDiff.diff(diffJob);
        } catch (Exception e) {
            log.error("请输入两个正确的Excel文件路径");
        }
    }
}
