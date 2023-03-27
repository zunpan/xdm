package cn.panjiahao.dnm.app;

import cn.panjiahao.dnm.core.TableDiff;
import cn.panjiahao.dnm.core.entity.DiffJob;
import cn.panjiahao.dnm.core.entity.Table;
import cn.panjiahao.dnm.core.util.NoModelDataListener;
import com.alibaba.excel.EasyExcel;
import org.junit.jupiter.api.Test;
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

    /**
     * tips：将测试文件放在resources模块下的xlsx文件夹
     */
    @Test
    void diffTest(){
        String resourceDirPath = LocalTest.class.getClassLoader().getResource("").getPath();
        String xlsxDirPath = resourceDirPath+ File.separator+"xlsx";
        String fileName1 = xlsxDirPath + File.separator+ "demo2.xlsx";
        String fileName2 = xlsxDirPath + File.separator+ "demo3.xlsx";
        NoModelDataListener listener1 = new NoModelDataListener();
        NoModelDataListener listener2 = new NoModelDataListener();
        EasyExcel.read(fileName1, listener1).sheet().doRead();
        EasyExcel.read(fileName2, listener2).sheet().doRead();
        Table leftTable = listener1.getTable();
        Table rightTable = listener2.getTable();

        DiffJob diffJob = new DiffJob(leftTable, rightTable);
        TableDiff.diff(diffJob);
    }
}
