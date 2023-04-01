package cn.panjiahao.dnm.core.excel;

import cn.panjiahao.dnm.base.enums.Code;
import cn.panjiahao.dnm.base.exception.BizException;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
public class NoModelDataListener extends AnalysisEventListener<Map<Integer, String>> {
    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5;
    private List<Map<Integer, String>> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    private int maxRow;
    private int maxCol;
    private List<List<String>> headerRows = new ArrayList<>();

    @SneakyThrows
    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        checkHeader();
        Integer maxKey = data.keySet().stream().max(Comparator.comparingInt(k -> k)).get() + 1;
        if (maxKey > maxCol) {
            throw new BizException(Code.ROW_SPAN_OVER_TABLE_SPAN);
        }
        log.info("解析到一条数据:{}", JSON.toJSONString(data));

        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    private void checkHeader() throws Exception {
        for (List<String> row : headerRows) {
            if (row.size() < maxCol) {
                throw new BizException(Code.COLUMN_NAME_IS_NULL);
            }
            for (String val : row) {
                if ("".equals(val)) {
                    throw new BizException(Code.COLUMN_NAME_IS_NULL);
                }
            }
        }
    }

    @SneakyThrows
    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headerMap, AnalysisContext context) {
        Integer maxKey = headerMap.keySet().stream().max(Comparator.comparingInt(k -> k)).get() + 1;
        maxCol = Math.max(maxKey, maxCol);
        List<String> row = new ArrayList<>();
        for (int i = 1; i <= maxKey; i++) {
            if (headerMap.get(i - 1) == null) {
                row.add("");
            } else {
                row.add(headerMap.get(i - 1).getStringValue());
            }
        }
        headerRows.add(row);
        log.info("解析到一条头数据:{}", JSON.toJSONString(headerMap));
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        log.info(String.format("该表有%d行,%d列", maxRow, maxCol));
        log.info("所有数据解析完成！");
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
        log.info("{}条数据，开始存储数据库！", cachedDataList.size());
        log.info("存储数据库成功！");
    }


}
