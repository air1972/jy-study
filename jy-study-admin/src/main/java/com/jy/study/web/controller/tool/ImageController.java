package com.jy.study.web.controller.tool;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.ossfile.OssClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/image")
public class ImageController {
    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    @PostMapping("/upload")
    public AjaxResult uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return AjaxResult.error("请选择要上传的图片");
        }

        OSS ossClient = null;
        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + suffix;
            String objectKey = "images/" + fileName;

            ossClient = OssClientUtil.getOSSClient();
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    OssClientUtil.getBucketName(), 
                    objectKey, 
                    file.getInputStream()
            );
            
            ossClient.putObject(putObjectRequest);

            String imageUrl = "https://" + OssClientUtil.getBucketName() + "." + OssClientUtil.getEndpoint() + "/" + objectKey;
            log.info("图片上传成功，URL: {}", imageUrl);
            
            return AjaxResult.success("上传成功", imageUrl);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return AjaxResult.error("上传失败：" + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
