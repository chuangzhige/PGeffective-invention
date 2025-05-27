package com.lark.oapi.sample.apiall.bitablev1;
import com.google.gson.JsonParser;
import com.lark.oapi.Client;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.bitable.v1.model.*;
import java.util.HashMap;
import com.lark.oapi.core.request.RequestOptions;

// SDK 使用文档：https://open.feishu.cn/document/uAjLw4CM/ukTMukTMukTM/server-side-sdk/java-sdk-guide/preparations
// 复制该 Demo 后, 需要将 "YOUR_APP_ID", "YOUR_APP_SECRET" 替换为自己应用的 APP_ID, APP_SECRET.
// 以下示例代码默认根据文档示例值填充，如果存在代码问题，请在 API 调试台填上相关必要参数后再复制代码使用
public class GetAppSample {

    public static void main(String arg[]) throws Exception {
        // 构建client
        Client client = Client.newBuilder("cli_a8b80b73f6bc5013", "osgCKn8CUYNU3H14fY3X53RQfi5UkxKU").build();

        // 创建请求对象
        GetAppReq req = GetAppReq.newBuilder()
                .appToken("TQQ1bZKcDa36LSsuSCUcnQjQnpc")
                .build();

        // 发起请求
        GetAppResp resp = client.bitable().v1().app().get(req);

        // 处理服务端错误
        if(!resp.success()) {
            System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
                    resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
            return;
        }

        // 业务数据处理
        System.out.println(Jsons.DEFAULT.toJson(resp.getData()));
    }
}
