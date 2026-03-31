package com.jy.study.common.ossfile;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.CreateBucketRequest;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.StorageClass;
import com.jy.study.common.config.OssProperties;
import com.jy.study.common.core.domain.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Component
public class OssClientUtil {
    private static final Logger log = LoggerFactory.getLogger(OssClientUtil.class);

    private static OSS ossClient;
    private static OssProperties ossProperties;

    @Autowired
    private OssProperties autowiredOssProperties;

    @PostConstruct
    public void init() {
        ossProperties = autowiredOssProperties;
        initOssClient();
    }

    private static void initOssClient() {
        try {
            log.info("正在初始化 OSS 客户端...");
            ossClient = new OSSClientBuilder().build(
                    ossProperties.getEndpoint(),
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret()
            );

            if (!ossClient.doesBucketExist(ossProperties.getBucketName())) {
                log.warn("Bucket {} does not exist", ossProperties.getBucketName());
            } else {
                log.info("OSS 客户端初始化成功");
            }
        } catch (Exception e) {
            log.error("连接 OSS 客户端错误！", e);
        }
    }

    public static OSS getOSSClient() {
        if (ossClient == null) {
            log.info("OSS 客户端未初始化，尝试重新初始化");
            initOssClient();
        }
        return ossClient;
    }

    public static void closeOSSClient() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSS 客户端已关闭");
            ossClient = null;
        }
    }

    public static String getBucketName() {
        return ossProperties.getBucketName();
    }

    public static String getEndpoint() {
        return ossProperties.getEndpoint();
    }

    public static String uploadImage(MultipartFile file, String prefix) {
        return uploadFile(file, prefix, "image");
    }

    public static String uploadVideo(MultipartFile file) {
        return uploadFile(file, "video", "video");
    }

    private static String uploadFile(MultipartFile file, String prefix, String type) {
        if (file.isEmpty()) {
            log.error("上传的文件为空");
            return "";
        }

        try {
            String originalFilename = file.getOriginalFilename();
            log.info("开始上传{}：{}", type, originalFilename);

            if (originalFilename == null) {
                log.error("文件名为空");
                return "";
            }

            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + suffix;
            String objectKey = prefix + "/" + fileName;

            log.info("生成的 OSS 对象键：{}", objectKey);

            ossClient = getOSSClient();
            if (ossClient == null) {
                log.error("获取 OSS 客户端失败");
                return "";
            }

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    getBucketName(),
                    objectKey,
                    file.getInputStream()
            );

            log.info("开始上传文件到 OSS");
            ossClient.putObject(putObjectRequest);

            // 构建 URL: 使用三级域名格式 bucketName.endpoint
            String endpointUrl = getEndpoint();
            if (!endpointUrl.startsWith("http://") && !endpointUrl.startsWith("https://")) {
                endpointUrl = "https://" + endpointUrl;
            }
            // 三级域名格式：https://bucketName.endpoint/objectKey
            String url = endpointUrl.replace("https://", "https://" + getBucketName() + ".") + "/" + objectKey;
            log.info("文件上传成功，URL: {}", url);

            return url;
        } catch (Exception e) {
            log.error("文件上传过程发生异常", e);
            return "";
        }
    }

    public static void main(String[] args) {
        try {
            // 获取 OSS 客户端实例
            OSS ossClient = OssClientUtil.getOSSClient();

            if (ossClient != null) {
                System.out.println("OSS 客户端连接成功！");
                // 测试 bucket 是否存在
                boolean exists = ossClient.doesBucketExist(OssClientUtil.getBucketName());
                System.out.println("Bucket 存在：" + exists);
            } else {
                System.out.println("OSS 客户端连接失败！");
            }
        } catch (Exception e) {
            System.out.println("发生错误：" + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭 OSS 客户端
            closeOSSClient();
        }
    }

}
