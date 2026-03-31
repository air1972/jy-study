package com.jy.study.web.controller.study;

import com.jy.study.common.annotation.Log;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.core.page.TableDataInfo;
import com.jy.study.common.enums.BusinessType;
import com.jy.study.lesson.domain.StudyComment;
import com.jy.study.lesson.service.IStudyCommentService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/study/comment")
public class CommentController extends BaseController {
    private final String prefix = "study/comment";

    @Autowired
    private IStudyCommentService commentService;

    @RequiresPermissions("study:comment:view")
    @GetMapping()
    public String comment() {
        return prefix + "/comment";
    }

    @RequiresPermissions("study:comment:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(StudyComment comment) {
        startPage();
        List<StudyComment> list = commentService.selectCommentList(comment);
        return getDataTable(list);
    }

    @RequiresPermissions("study:comment:edit")
    @Log(title = "评论管理", businessType = BusinessType.UPDATE)
    @PostMapping("/changeStatus")
    @ResponseBody
    public AjaxResult changeStatus(@RequestParam("ids") String ids, @RequestParam("status") String status) {
        return toAjax(commentService.updateCommentStatusByIds(ids, status, getLoginName()));
    }

    @RequiresPermissions("study:comment:remove")
    @Log(title = "评论管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(@RequestParam("ids") String ids) {
        return toAjax(commentService.deleteCommentByIds(ids));
    }
}
