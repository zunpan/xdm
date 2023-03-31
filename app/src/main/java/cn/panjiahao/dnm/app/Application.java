package cn.panjiahao.dnm.app;

import cn.panjiahao.dnm.core.algorithm.TableDiff;
import cn.panjiahao.dnm.core.entity.DiffJob;
import cn.panjiahao.dnm.core.entity.Table;
import cn.panjiahao.dnm.core.util.TableListener;
import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

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
        try {
            String fileName1 = args[0];
            String fileName2 = args[1];
            int headerRowNumber = 1;
            if (args.length > 2) {
                headerRowNumber = Integer.parseInt(args[2]);
            }
            diff(fileName1, fileName2, headerRowNumber);
        } catch (Exception e) {
            log.error("请输入正确的命令：java -jar jarName [ExcelPath1] [ExcelPath2] [headerRowNumber]");
        }
    }

    public static void diff(String fileName1,String fileName2,int headerRowNumber){
        TableListener listener1;
        TableListener listener2;
        try {
            listener1 = new TableListener(headerRowNumber);
            listener2 = new TableListener(headerRowNumber);
            EasyExcel.read(fileName1, listener1).sheet().headRowNumber(headerRowNumber).doRead();
            EasyExcel.read(fileName2, listener2).sheet().headRowNumber(headerRowNumber).doRead();
            Table leftTable = listener1.getTable();
            Table rightTable = listener2.getTable();

            DiffJob diffJob = new DiffJob(leftTable, rightTable);
            TableDiff.diff(diffJob);
        } catch (Exception e) {
            log.error("请输入两个正确的Excel文件路径");
        }
    }
}
