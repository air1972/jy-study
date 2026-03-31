package com.jy.study.web.controller.study;

import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.core.page.TableDataInfo;
import com.jy.study.lesson.domain.StudyAiChat;
import com.jy.study.lesson.service.IStudyAiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/aichat")
public class AIChatController extends BaseController {
    
    private String prefix = "aichat";
    
    @Autowired
    private IStudyAiChatService studyAiChatService;

    @GetMapping("/list")
    public String list() {
        return prefix + "/ai-chat-list";
    }

    @PostMapping("/listData")
    @ResponseBody
    public TableDataInfo listData(StudyAiChat aiChat) {
        startPage();
        List<StudyAiChat> list = studyAiChatService.selectStudyAiChatList(aiChat);
        return getDataTable(list);
    }

    @GetMapping("/detail/{conversationId}")
    public String detail(@PathVariable("conversationId") String conversationId, ModelMap mmap) {
        StudyAiChat query = new StudyAiChat();
        query.setConversationId(conversationId);
        List<StudyAiChat> chatList = studyAiChatService.selectStudyAiChatList(query);
        mmap.put("chatList", chatList);
        if (!chatList.isEmpty()) {
            mmap.put("userId", chatList.get(0).getUserId());
            mmap.put("createTime", chatList.get(0).getCreateTime());
        }
        return prefix + "/detail";
    }

}
