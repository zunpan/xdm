package cn.panjiahao.dnm.dimesion2;

import cn.panjiahao.dnm.dimesion2.entity.DiffJob;
import cn.panjiahao.dnm.dimesion2.entity.Table;
import cn.panjiahao.dnm.dimesion2.util.NoModelDataListener;
import com.alibaba.excel.EasyExcel;

import java.io.File;

/**
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/21 14:18
 */
public class Test {

    public static void main(String[] args) {
        String resourceDirPath = Test.class.getClassLoader().getResource("").getPath();
        String xlsxDirPath = resourceDirPath+File.separator+"xlsx";
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
