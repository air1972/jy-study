package com.jy.study.common.ai;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SiliconCloudAI {

    @Value("${siliconCouldAI.apiKey}")
    private String apiKey;
    
    public String qwenCoder(String systemSay,String userSay) {
        return request("deepseek/deepseek-chat:free",systemSay,userSay);
    }

    public String deepseekR1Free(String systemSay,String userSay) {
        return request("deepseek-ai/DeepSeek-R1-Distill-Qwen-7B",systemSay,userSay);
    }

    public String deepSeekV3(String systemSay,String userSay) {
        return request("deepseek-ai/DeepSeek-V3",systemSay,userSay);
    }

    public String deepseek32B(String systemSay,String userSay) {
        return request("deepseek/deepseek-chat:free",systemSay,userSay);
    }

    public List<String> requestSSE(String model, String systemSay, String userSay) {
        List<String> result = null;
        try {
            userSay = Jsoup.clean(userSay, Whitelist.none());
            Connection connection = Jsoup.connect("https://api.siliconflow.cn/v1/chat/completions");
            connection.header("accept", "application/json");
            connection.header("content-type", "application/json");
            connection.header("authorization", "Bearer "+apiKey);
            connection.header("Content-Type","application/json");
            String requestBody = "{\"model\":\""+model+"\",\"messages\":[{\"role\":\"user\",\"content\":\""+userSay+"\"},{\"role\":\"system\",\"content\":\""+systemSay+"\"}],\"stream\":true,\"max_tokens\":4096,\"temperature\":0.7,\"top_p\":0.7,\"top_k\":50,\"frequency_penalty\":0.5,\"n\":1}";
            connection.requestBody(requestBody);

            Response response = connection.ignoreContentType(true).timeout(60000).method(Method.POST).execute();
            if(response.statusCode()==200) {
                String resultStr = response.body();
                result =  getMsgs(resultStr);
            }else {
                return empty();
            }
        } catch (Exception e) {
            e.printStackTrace();

            return empty();
        }
        return result;
    }

    public String request(String model,String systemSay,String userSay) {
        String result = null;
        try {
            userSay = Jsoup.clean(userSay, Whitelist.none());
            Connection connection = Jsoup.connect("https://api.siliconflow.cn/v1/chat/completions");
            connection.header("accept", "application/json");
            connection.header("content-type", "application/json");
            connection.header("authorization", "Bearer "+apiKey);
            connection.header("Content-Type","application/json");
            String requestBody = "{\"model\":\""+model+"\",\"messages\":[{\"role\":\"user\",\"content\":\""+userSay+"\"},{\"role\":\"system\",\"content\":\""+systemSay+"\"}],\"stream\":false,\"max_tokens\":16384,\"temperature\":0.6,\"top_p\":1,\"top_k\":50,\"frequency_penalty\":0.5,\"n\":1}";
            connection.requestBody(requestBody);

            Response response = connection.ignoreContentType(true).timeout(60000).method(Method.POST).execute();
            if(response.statusCode()==200) {
                String resultStr = response.body();
                JSONObject resultJson = JSONObject.parseObject(resultStr);
                JSONArray choices = resultJson.getJSONArray("choices");
                JSONObject choicesOne = choices.getJSONObject(0);
                JSONObject message = choicesOne.getJSONObject("message");
                String  markdown = message.getString("content");
                Parser parser = Parser.builder().build();
                HtmlRenderer renderer = HtmlRenderer.builder().build();
                Node document = parser.parse(markdown);
                result =  renderer.render(document);
            }else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }
    private List<String> empty(){
        List<String> list = new ArrayList<String>();
        list.add("DONE");
        return list;
    }

    private List<String> getMsgs(String content) {
        String[] array = content.split("\n");
        List<String> list = new ArrayList<String>();
        for(String str:array) {
            if(str.startsWith("data")) {
                String rs = str.substring(5);
                if(rs.contains("DONE")){
                    list.add("DONE");
                }else {
                    JSONObject resultJson = JSONObject.parseObject(rs);
                    JSONArray choices = resultJson.getJSONArray("choices");
                    JSONObject choicesOne = choices.getJSONObject(0);
                    JSONObject message = choicesOne.getJSONObject("delta");
                    String c = message.getString("content");
                    if(c!=null) {
                        list.add(c);
                    }
                }
            }else {

            }
        }
        return list;
    }


    public static void main(String[] args) {
//        System.out.println(deepseekR1Free("写一篇关于春天的作文","主题i love you"));
    }
}
