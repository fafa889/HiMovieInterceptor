# 华为视频接口拦截器

一个 LSPosed 模块，用于拦截华为视频应用的 `/poservice/getUserContracts` 接口数据。

## 功能特性

- 拦截华为视频的用户合约信息请求
- 修改响应数据，添加VIP特权
- 支持 OkHttp 拦截
- 详细的调试日志

## 使用方法

1. 安装 APK 到设备
2. 在 LSPosed 中启用本模块
3. 勾选目标应用 `com.huawei.himovie`
4. 重启华为视频应用

## 构建

本项目使用 GitHub Actions 自动构建 APK。每次推送到 main 分支时会自动生成 APK。

在 GitHub 仓库的 Actions 页面可以下载构建好的 APK。

## 日志查看

使用以下命令查看拦截日志：
```bash
adb logcat | grep -i "HiMovieInterceptor"
