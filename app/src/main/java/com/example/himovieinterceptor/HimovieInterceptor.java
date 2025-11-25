package com.example.himovieinterceptor;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HimovieInterceptor implements IXposedHookLoadPackage {
    private static final String TARGET_PACKAGE = "com.huawei.himovie";
    private static final String TARGET_API = "/poservice/getUserContracts";
    private final Gson gson = new Gson();
    
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!TARGET_PACKAGE.equals(lpparam.packageName)) {
            return;
        }
        
        XposedBridge.log("[HiMovieInterceptor] 已加载到华为视频应用: " + lpparam.packageName);
        
        try {
            interceptOkHttp(lpparam);
            XposedBridge.log("[HiMovieInterceptor] 所有拦截器初始化完成");
        } catch (Throwable e) {
            XposedBridge.log("[HiMovieInterceptor] 初始化拦截失败: " + e.getMessage());
        }
    }
    
    private void interceptOkHttp(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> realCallClass = XposedHelpers.findClass("okhttp3.RealCall", lpparam.classLoader);
            
            XposedBridge.hookAllMethods(realCallClass, "execute", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        Object response = param.getResult();
                        if (response == null) return;
                        
                        Object request = XposedHelpers.callMethod(param.thisObject, "request");
                        Object httpUrl = XposedHelpers.callMethod(request, "url");
                        String urlString = httpUrl.toString();
                        
                        if (urlString.contains(TARGET_API)) {
                            XposedBridge.log("[HiMovieInterceptor] ✅ 拦截到目标接口: " + urlString);
                            
                            Object responseBody = XposedHelpers.callMethod(response, "body");
                            String originalResponse = (String) XposedHelpers.callMethod(responseBody, "string");
                            
                            XposedBridge.log("[HiMovieInterceptor] 📄 原始响应长度: " + originalResponse.length());
                            
                            String modifiedResponse = modifyUserContracts(originalResponse);
                            
                            Class<?> mediaTypeClass = XposedHelpers.findClass("okhttp3.MediaType", lpparam.classLoader);
                            Object jsonMediaType = XposedHelpers.callStaticMethod(mediaTypeClass, "parse", 
                                "application/json; charset=utf-8");
                            
                            Object newResponseBody = XposedHelpers.callStaticMethod(
                                XposedHelpers.findClass("okhttp3.ResponseBody", lpparam.classLoader),
                                "create",
                                new Class[]{mediaTypeClass, byte[].class},
                                jsonMediaType, modifiedResponse.getBytes("UTF-8")
                            );
                            
                            Object responseBuilder = XposedHelpers.callMethod(response, "newBuilder");
                            responseBuilder = XposedHelpers.callMethod(responseBuilder, "body", newResponseBody);
                            Object newResponse = XposedHelpers.callMethod(responseBuilder, "build");
                            
                            param.setResult(newResponse);
                            XposedBridge.log("[HiMovieInterceptor] ✅ 响应修改完成");
                        }
                    } catch (Throwable t) {
                        XposedBridge.log("[HiMovieInterceptor] ❌ OkHttp拦截错误: " + t.getMessage());
                    }
                }
            });
            
            XposedBridge.log("[HiMovieInterceptor] ✅ OkHttp拦截器初始化成功");
            
        } catch (Throwable t) {
            XposedBridge.log("[HiMovieInterceptor] ❌ OkHttp拦截初始化失败: " + t.getMessage());
        }
    }
    
    private String modifyUserContracts(String originalResponse) {
        try {
            JsonObject jsonObject = JsonParser.parseString(originalResponse).getAsJsonObject();
            
            jsonObject.addProperty("__intercepted", true);
            jsonObject.addProperty("__intercept_time", System.currentTimeMillis());
            jsonObject.addProperty("__interceptor_version", "1.0");
            
            if (jsonObject.has("data")) {
                JsonObject data = jsonObject.getAsJsonObject("data");
                
                if (data.has("contracts")) {
                    JsonObject contracts = data.getAsJsonObject("contracts");
                    contracts.addProperty("vip_level", 6);
                    contracts.addProperty("vip_expire", "2099-12-31");
                    contracts.addProperty("is_premium", true);
                }
                
                data.addProperty("modified_by", "HiMovieInterceptor");
            }
            
            String result = gson.toJson(jsonObject);
            XposedBridge.log("[HiMovieInterceptor] ✅ 响应修改完成，新长度: " + result.length());
            return result;
            
        } catch (Exception e) {
            XposedBridge.log("[HiMovieInterceptor] ❌ 修改响应失败: " + e.getMessage());
            return originalResponse;
        }
    }
}
