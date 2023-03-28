package cn.panjiahao.dnm.core.util;

import cn.panjiahao.dnm.base.enums.Code;
import cn.panjiahao.dnm.base.exception.BizException;
import cn.panjiahao.dnm.core.entity.Cell;
import cn.panjiahao.dnm.core.entity.Table;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static cn.panjiahao.dnm.core.util.CommonUtil.list2Array;
import static cn.panjiahao.dnm.core.util.CommonUtil.strMatrixToCells;

/**
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/28 15:06
 */
@Slf4j
public class TableListener extends AnalysisEventListener<Map<Integer, String>> {
    private int headRowNumber;
    /**
     * 表头是否已经检查，false没有检查，true表示已经检查通过
     */
    private boolean hasCheckHead = false;
    private List<List<String>> tableHead = new ArrayList<>();
    private List<List<String>> tableBody = new ArrayList<>();
    private int maxCol;

    public TableListener() {
        this.headRowNumber = 0;
    }
    public TableListener(int headRowNumber) {
        this.headRowNumber = headRowNumber;
        this.tableBody = new ArrayList<>(headRowNumber);
    }

    @SneakyThrows
    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        if (!hasCheckHead) {
            checkHeader();
        }
        log.info("解析到一行数据:{}", JSON.toJSONString(data));
        List<String> row = new ArrayList<>();
        // 如果有表头，表的宽度由表头决定，否则由最大的行宽决定
        if (headRowNumber > 0) {
            int maxKey = data.keySet().stream().max(Comparator.comparingInt(k -> k)).get() + 1;
            if (maxKey > maxCol) {
                throw new BizException(Code.ROW_SPAN_OVER_TABLE_SPAN);
            }
            for (int i = 0; i < maxCol; i++) {
                if (data.get(i) == null) {
                    row.add("");
                }else{
                    row.add(data.get(i));
                }
            }
        } else {
            int maxKey = data.keySet().stream().max(Comparator.comparingInt(k -> k)).get() + 1;
            maxCol = Math.max(maxKey, maxCol);
            for (int i = 0; i < maxKey; i++) {
                if (data.get(i) == null) {
                    row.add("");
                }else{
                    row.add(data.get(i));
                }
            }
        }
        tableBody.add(row);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info(String.format("所有数据解析完成！该表有%d行,%d列", tableBody.size(),maxCol));
    }
    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        int maxKey = headMap.keySet().stream().max(Comparator.comparingInt(k -> k)).get()+1;
        maxCol = Math.max(maxKey, maxCol);
        List<String> row = new ArrayList<>();
        for(int i=0;i<maxKey;i++){
            if (headMap.get(i) == null) {
                row.add("");
            } else {
                row.add(headMap.get(i).getStringValue());
            }
        }
        tableHead.add(row);
        log.info("解析到一条头数据:{}", JSON.toJSONString(headMap));
    }

    /**
     * 检查表头是否有空的单元格
     * @throws BizException
     */
    private void checkHeader() throws BizException {
        for (List<String> row : tableHead) {
            if (row.size() < maxCol) {
                throw new BizException(Code.COLUMN_NAME_IS_NULL);
            }
            for (String val : row) {
                if ("".equals(val)) {
                    throw new BizException(Code.COLUMN_NAME_IS_NULL);
                }
            }
        }
        this.hasCheckHead = true;
    }

    public Table getTable() {
        if (headRowNumber == 0) {
            for (List<String> row : tableBody) {
                while (row.size() < maxCol) {
                    row.add("");
                }
            }
        }
        Cell[][] bodyCells = strMatrixToCells(list2Array(tableBody));
        Cell[][] headCells = strMatrixToCells(list2Array(tableHead));
        Table.TableBuilder tableBuilder = Table.builder();
        return tableBuilder.bodyRowNum(tableBody.size()).headRowNum(tableHead.size()).colNum(maxCol).bodyCells(bodyCells).headCells(headCells).build();
    }
}
