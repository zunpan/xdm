package cn.panjiahao.dnm.app;

import cn.panjiahao.dnm.core.algorithm.TableDiff;
import cn.panjiahao.dnm.core.entity.DiffJob;
import cn.panjiahao.dnm.core.entity.Table;
import cn.panjiahao.dnm.core.util.TableListener;
import com.alibaba.excel.EasyExcel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import java.io.File;

/**
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/27 20:51
 */
@SpringBootTest(classes = LocalTest.class)
@ConfigurationPropertiesScan({"cn.panjiahao"})
@ComponentScan({"cn.panjiahao"})
public class LocalTest {

    @Value("${header-row-number}")
    int headerRowNumber;
    /**
     * tips：将测试文件放在resources模块下的xlsx文件夹
     */
    @Test
    void diffTest(){
        String resourceDirPath = LocalTest.class.getClassLoader().getResource("").getPath();
        String xlsxDirPath = resourceDirPath+ File.separator+"xlsx";
        String fileName1 = xlsxDirPath + File.separator+ "test14.xlsx";
        String fileName2 = xlsxDirPath + File.separator+ "test15.xlsx";
        TableListener listener1 = new TableListener(headerRowNumber);
        TableListener listener2 = new TableListener(headerRowNumber);
        EasyExcel.read(fileName1, listener1).sheet().headRowNumber(headerRowNumber).doRead();
        EasyExcel.read(fileName2, listener2).sheet().headRowNumber(headerRowNumber).doRead();
        Table leftTable = listener1.getTable();
        Table rightTable = listener2.getTable();

        DiffJob diffJob = new DiffJob(leftTable, rightTable);
        TableDiff.diff(diffJob);
    }
}
