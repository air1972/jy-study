package com.jy.study.web.controller.common;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jy.study.common.ossfile.OssClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.jy.study.common.config.RuoYiConfig;
import com.jy.study.common.config.ServerConfig;
import com.jy.study.common.constant.Constants;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.utils.StringUtils;
import com.jy.study.common.utils.file.FileUploadUtils;
import com.jy.study.common.utils.file.FileUtils;

/**
 * 通用请求处理
 * 
 * @author jily
 */
@Controller
@RequestMapping("/common")
public class CommonController
{
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    private ServerConfig serverConfig;

    private static final String FILE_DELIMETER = ",";

    /**
     * 通用下载请求
     * 
     * @param fileName 文件名称
     * @param delete 是否删除
     */
    @GetMapping("/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request)
    {
        try
        {
            if (!FileUtils.checkAllowDownload(fileName))
            {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = RuoYiConfig.getDownloadPath() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete)
            {
                FileUtils.deleteFile(filePath);
            }
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求（单个）
     */
    @PostMapping("/upload")
    @ResponseBody
    public AjaxResult uploadFile(MultipartFile file) throws Exception
    {
        try
        {
            // 上传文件路径
            String filePath = RuoYiConfig.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", fileName);
            ajax.put("newFileName", FileUtils.getName(fileName));
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 通用上传请求（多个）
     */
    @PostMapping("/uploads")
    @ResponseBody
    public AjaxResult uploadFiles(List<MultipartFile> files) throws Exception
    {
        try
        {
            // 上传文件路径
            String filePath = RuoYiConfig.getUploadPath();
            List<String> urls = new ArrayList<String>();
            List<String> fileNames = new ArrayList<String>();
            List<String> newFileNames = new ArrayList<String>();
            List<String> originalFilenames = new ArrayList<String>();
            for (MultipartFile file : files)
            {
                // 上传并返回新文件名称
                String fileName = FileUploadUtils.upload(filePath, file);
                String url = serverConfig.getUrl() + fileName;
                urls.add(url);
                fileNames.add(fileName);
                newFileNames.add(FileUtils.getName(fileName));
                originalFilenames.add(file.getOriginalFilename());
            }
            AjaxResult ajax = AjaxResult.success();
            ajax.put("urls", StringUtils.join(urls, FILE_DELIMETER));
            ajax.put("fileNames", StringUtils.join(fileNames, FILE_DELIMETER));
            ajax.put("newFileNames", StringUtils.join(newFileNames, FILE_DELIMETER));
            ajax.put("originalFilenames", StringUtils.join(originalFilenames, FILE_DELIMETER));
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 本地资源通用下载
     */
    @GetMapping("/download/resource")
    public void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
        try
        {
            if (!FileUtils.checkAllowDownload(resource))
            {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", resource));
            }
            // 本地资源路径
            String localPath = RuoYiConfig.getProfile();
            // 数据库资源地址
            String downloadPath = localPath + StringUtils.substringAfter(resource, Constants.RESOURCE_PREFIX);
            // 下载名称
            String downloadName = StringUtils.substringAfterLast(downloadPath, "/");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            FileUtils.writeBytes(downloadPath, response.getOutputStream());
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }

    @PostMapping("/upload/image/{prefix}")
    @ResponseBody
    public AjaxResult uploadImage(@RequestParam("file") MultipartFile file, @PathVariable("prefix") String prefix) {
        try {
            // 先尝试 OSS 上传
            String imageUrl = OssClientUtil.uploadImage(file, prefix);
            if(StringUtils.isBlank(imageUrl)){
                // OSS 上传失败，使用本地上传
                return uploadImageLocal(file, prefix);
            }
            return AjaxResult.success("上传成功", imageUrl);
        } catch (Exception e) {
            log.warn("OSS 图片上传失败，切换到本地存储", e);
            // OSS 上传失败，使用本地上传
            return uploadImageLocal(file, prefix);
        }
    }

    /**
     * 本地图片上传
     */
    private AjaxResult uploadImageLocal(MultipartFile file, String prefix) {
        try {
            if (file.isEmpty()) {
                return AjaxResult.error("上传文件为空");
            }
            // 上传到本地路径
            String localPath = RuoYiConfig.getProfile() + "/" + prefix + "/";
            java.io.File dir = new java.io.File(localPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = java.util.UUID.randomUUID().toString().replaceAll("-", "") + 
                              java.io.File.separator + file.getOriginalFilename();
            java.io.File dest = new java.io.File(localPath + fileName);
            file.transferTo(dest);
            // 返回相对路径
            String url = "/profile/" + prefix + "/" + fileName;
            return AjaxResult.success("上传成功", url);
        } catch (Exception e) {
            log.error("本地图片上传失败", e);
            return AjaxResult.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传视频
     */
    @PostMapping("/upload/video/lesson")
    @ResponseBody
    public AjaxResult uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            String videoUrl = OssClientUtil.uploadVideo(file);
            if(StringUtils.isBlank(videoUrl)){
                return AjaxResult.error("上传失败");
            }
            return AjaxResult.success("上传成功", videoUrl);
        } catch (Exception e) {
            log.error("视频上传失败", e);
            return AjaxResult.error(e.getMessage());
        }
    }
}
