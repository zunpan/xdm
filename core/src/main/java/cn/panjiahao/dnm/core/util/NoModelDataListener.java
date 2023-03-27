package cn.panjiahao.dnm.core.util;

import cn.panjiahao.dnm.core.entity.Table;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static cn.panjiahao.dnm.core.util.CommonUtil.strMatrixToTable;

@Slf4j
public class NoModelDataListener extends AnalysisEventListener<Map<Integer, String>> {
    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    // private static final int BATCH_COUNT = 5;
    // private List<Map<Integer, String>> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    private List<List<String>> matrix = new ArrayList<>();
    private int maxCol;

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        int maxKey = data.keySet().stream().max(Comparator.comparingInt(k -> k)).get()+1;
        maxCol = Math.max(maxKey, maxCol);
        log.info("解析到一行数据:{}", JSON.toJSONString(data));
        List<String> row = new ArrayList<>();
        for (int i = 0; i < maxKey; i++) {
            if (data.get(i) == null) {
                row.add("");
            }else{
                row.add(data.get(i));
            }
        }
        matrix.add(row);
        // cachedDataList.add(data);
        // if (cachedDataList.size() >= BATCH_COUNT) {
        //     cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        // }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info(String.format("所有数据解析完成！该表有%d行,%d列",matrix.size(),maxCol));
    }

    public Table getTable() {
        String[][] strMatrix = new String[matrix.size()][maxCol];
        for(int i=0;i<strMatrix.length;i++){
            int j = 0;
            for (;j<matrix.get(i).size();j++){
                strMatrix[i][j] = matrix.get(i).get(j);
            }
            for (; j < strMatrix[i].length; j++) {
                strMatrix[i][j] = "";
            }
        }
        return strMatrixToTable(strMatrix);
    }
}
