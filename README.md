# dnm

## 介绍

该系统支持Excel文件的比对

## 使用

对app模块打包,然后执行`java -jar app-0.1.1.jar [ExcelPath1] [ExcalPath2] [headRowNumber]`
headRowNumber 不输入时默认是1

## 功能

支持行和列的增、删、修改、移动操作

## TODO

 - [x] 一维数据比对（diff结果支持增加、删除、修改）
 - [x] 二维数据比对（diff结果支持增加、删除、修改）
 - [ ] 二维数据比对优化（优化diff流程和性能） 
 - [x] diff结果支持行移动
 - [x] 接入EasyExcel
 - [ ] 支持表头diff
 - [ ] 公式处理
 - [ ] 二维数据合并
 
