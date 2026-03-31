package com.jy.study.web.controller.common;

import com.jy.study.common.ai.TongYiPicture;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.ossfile.OssClientUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@Controller
@RequestMapping("/ttl")
public class TTLController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(TTLController.class);
    
    @Autowired
    private TongYiPicture tongYiPicture;
    
    @PostMapping("/generate/image/{prefix}")
    @ResponseBody
    public AjaxResult generateImage(String title, @PathVariable("prefix") String prefix) {
        try {
            // 1. 调用通义千问生成图片，获取临时URL
            String tempImageUrl = tongYiPicture.generaPic(title);
            log.info("获取到通义千问生成的临时图片URL: {}", tempImageUrl);
            
            // 2. 下载图片
            URL url = new URL(tempImageUrl);
            InputStream inputStream = url.openStream();
            
            // 3. 生成在OSS中的存储路径
            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".png";
            String objectKey = prefix + "/" + fileName;
            
            // 4. 上传到OSS
            OSS ossClient = OssClientUtil.getOSSClient();
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    OssClientUtil.getBucketName(),
                    objectKey,
                    inputStream
            );
            
            ossClient.putObject(putObjectRequest);
            
            // 5. 生成永久访问URL
            String permanentUrl = "https://" + OssClientUtil.getBucketName() + "." + OssClientUtil.getEndpoint() + "/" + objectKey;
            log.info("图片已保存到OSS，永久URL: {}", permanentUrl);
            
            return AjaxResult.success("生成成功", permanentUrl);
        } catch (Exception e) {
            log.error("生成图片过程发生异常", e);
            return AjaxResult.error("生成失败：" + e.getMessage());
        }
    }
}
