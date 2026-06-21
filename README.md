# 易计件

一个使用 Kotlin、Jetpack Compose 和 Room 实现的本地计件 App。数据仅保存在本地，支持产品单价、计件记录、日补贴、日扣款、日历查看、列表查看、总览统计和 CSV 导出。

## 功能

- 产品管理：新增产品、修改产品名称和三位小数单价
- 计件记录：按产品录入数量，自动计算收入
- 金额调整：支持日补贴和日扣款
- 日历页：按日期查看当天计件详情
- 列表页：按日期分组查看整月明细
- 总览页：查看月总收入、计件收入、补贴扣款、产品统计
- 数据导出：导出当前月份 CSV 数据

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- Room
- StateFlow / ViewModel

## 数据设计

产品信息存储在 `products` 表中，计件记录通过 `productId` 关联产品。列表和统计查询使用 `LEFT JOIN products` 获取产品名称与单价，并在 SQL 中计算：

```sql
数量 * 单价 + 补贴 - 扣款
```

补贴和扣款使用独立记录保存，`productId = null`，这样它们可以参与日统计和月统计，但不会被错误地计入某个产品的数量。

日期使用 `LocalDate.toEpochDay()` 存成 `Long`，查询日统计时使用 `[day, day + 1)`，查询月统计时使用 `[monthStart, nextMonthStart)`。这一套范围查询可以同时覆盖日、月和自定义日期范围。

## 运行方式

用 Android Studio 打开当前目录，等待 Gradle 同步后运行 `app` 模块即可。

也可以在本地配置 Android SDK 和 JDK 17 后执行：

```bash
./gradlew assembleDebug
```
