package cn.panjiahao.dnm;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/21 21:09
 */
public class EasyExcelTest {
    Logger log = LogManager.getLogger(EasyExcelTest.class);
    String fileName = "C:\\code\\dnm\\src\\main\\resources\\demo1.xlsx";

    @Test
    void simpleRead() {
        // 这里默认每次会读取100条数据 然后返回过来 直接调用使用数据就行
        // 具体需要返回多少行可以在`PageReadListener`的构造函数设置
        EasyExcel.read(fileName, DemoData.class, new PageReadListener<DemoData>(dataList -> {
            for (DemoData demoData : dataList) {
                log.info("读取到一条数据{}", JSON.toJSONString(demoData));
            }
        })).sheet().doRead();
    }
    /**
     * 不创建对象的读
     */
    @Test
    public void noModelRead() {
        // 这里 只要，然后读取第一个sheet 同步读取会自动finish
        EasyExcel.read(fileName, new NoModelDataListener()).sheet().doRead();
    }
}
