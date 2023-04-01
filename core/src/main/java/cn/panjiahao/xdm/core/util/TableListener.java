package cn.panjiahao.xdm.core.util;

import cn.panjiahao.xdm.base.enums.Code;
import cn.panjiahao.xdm.base.exception.BizException;
import cn.panjiahao.xdm.core.entity.Cell;
import cn.panjiahao.xdm.core.entity.Table;
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

/**
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/28 15:06
 */
@Slf4j
public class TableListener extends AnalysisEventListener<Map<Integer, String>> {
    private int headerRowNumber;
    /**
     * 表头是否已经检查，false没有检查，true表示已经检查通过
     */
    private boolean hasCheckHead = false;
    private List<List<String>> tableHead = new ArrayList<>();
    private List<List<String>> tableBody = new ArrayList<>();
    private int maxCol;

    public TableListener() {
        this.headerRowNumber = 0;
    }

    public TableListener(int headerRowNumber) {
        this.headerRowNumber = headerRowNumber;
        this.tableBody = new ArrayList<>(headerRowNumber);
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
        if (headerRowNumber > 0) {
            int maxKey = data.keySet().stream().max(Comparator.comparingInt(k -> k)).get() + 1;
            if (maxKey > maxCol) {
                throw new BizException(Code.ROW_SPAN_OVER_TABLE_SPAN);
            }
            for (int i = 0; i < maxCol; i++) {
                if (data.get(i) == null) {
                    row.add("");
                } else {
                    row.add(data.get(i));
                }
            }
        } else {
            int maxKey = data.keySet().stream().max(Comparator.comparingInt(k -> k)).get() + 1;
            maxCol = Math.max(maxKey, maxCol);
            for (int i = 0; i < maxKey; i++) {
                if (data.get(i) == null) {
                    row.add("");
                } else {
                    row.add(data.get(i));
                }
            }
        }
        tableBody.add(row);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info(String.format("所有数据解析完成！该表有%d行,%d列", tableBody.size(), maxCol));
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headerMap, AnalysisContext context) {
        int maxKey = headerMap.keySet().stream().max(Comparator.comparingInt(k -> k)).get() + 1;
        maxCol = Math.max(maxKey, maxCol);
        List<String> row = new ArrayList<>();
        for (int i = 0; i < maxKey; i++) {
            if (headerMap.get(i) == null) {
                row.add("");
            } else {
                row.add(headerMap.get(i).getStringValue());
            }
        }
        tableHead.add(row);
        log.info("解析到一条头数据:{}", JSON.toJSONString(headerMap));
    }

    /**
     * 检查表头是否有空的单元格
     *
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
        if (headerRowNumber == 0) {
            for (List<String> row : tableBody) {
                while (row.size() < maxCol) {
                    row.add("");
                }
            }
        }
        Cell[][] bodyCells = CommonUtil.strMatrixToCells(CommonUtil.list2Array(tableBody));
        Cell[][] headerCells = CommonUtil.strMatrixToCells(CommonUtil.list2Array(tableHead));
        Table.TableBuilder tableBuilder = Table.builder();
        return tableBuilder.bodyRowNum(tableBody.size()).headerRowNum(tableHead.size()).colNum(maxCol).bodyCells(bodyCells).headerCells(headerCells).build();
    }
}
