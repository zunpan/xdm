# dnm

## 介绍

该系统支持Excel文件的比对

## 功能

1. 无表头表的比对，支持行和列的增、删、修改、移动操作
2. 表头行数相同的表的比对，支持行的增、删、修改、移动操作，支持表头的增、删、修改操作

## 使用

对app模块打包,然后执行`java -jar app-0.1.1.jar [ExcelPath1] [ExcalPath2] [headerRowNumber]`
headerRowNumber 不输入时默认是0，使用无表头比对算法

![图 2](https://cdn.jsdelivr.net/gh/zunpan/note-imgur@main/IMG_20230401-170926175.png)  

![图 1](https://cdn.jsdelivr.net/gh/zunpan/note-imgur@main/IMG_20230401-170545707.png)  

## TODO

- [x] 一维数据比对（diff结果支持增加、删除、修改）
- [x] 二维数据比对（diff结果支持增加、删除、修改）
- [x] 二维数据比对优化（优化diff流程和性能）
- [x] diff结果支持行移动
- [x] 接入EasyExcel
- [x] 支持表头diff
- [ ] diff结果可视化
- [ ] 公式处理
- [ ] 二维数据合并
