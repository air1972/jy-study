package com.jy.study.web.controller.study;

import com.jy.study.common.annotation.Log;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.core.page.TableDataInfo;
import com.jy.study.common.enums.BusinessType;
import com.jy.study.lesson.domain.StudyExercise;
import com.jy.study.lesson.domain.StudyLessonExercise;
import com.jy.study.lesson.domain.StudyWrongAnswer;
import com.jy.study.lesson.service.IStudyExerciseService;
import com.jy.study.lesson.service.IStudyLessonExerciseService;
import com.jy.study.lesson.service.IStudyWrongAnswerService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 错题管理（后台）
 */
@Controller
@RequestMapping("/study/wrong")
public class WrongAnswerController extends BaseController {
    private String prefix = "study/wrong";

    @Autowired
    private IStudyWrongAnswerService wrongAnswerService;

    @Autowired
    private IStudyExerciseService exerciseService;
    @Autowired
    private IStudyLessonExerciseService lessonExerciseService;

    @RequiresPermissions("study:wrong:view")
    @GetMapping()
    public String wrong()
    {
        return prefix + "/wrong";
    }

    /**
     * 分页查询错题列表（带题目内容）
     */
    @RequiresPermissions("study:wrong:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(StudyWrongAnswer query)
    {
        wrongAnswerService.purgeInvalidWrongAnswers();
        startPage();
        List<StudyWrongAnswer> list = wrongAnswerService.selectStudyWrongAnswerList(query);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (StudyWrongAnswer wa : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", wa.getId());
            item.put("exerciseId", wa.getExerciseId());
            item.put("wrongCount", wa.getWrongCount());
            item.put("lastWrongTime", wa.getLastWrongTime());
            Long exerciseId = wa.getExerciseId();
            if (exerciseId != null && exerciseId < 0) {
                Long lessonExerciseId = -exerciseId;
                StudyLessonExercise ex = lessonExerciseService.selectStudyLessonExerciseById(lessonExerciseId);
                if (ex != null) {
                    item.put("content", ex.getTitle());
                    item.put("type", ex.getExerciseType());
                    item.put("answer", ex.getAnswer());
                    item.put("explanation", ex.getAnalysis());
                }
            } else {
                StudyExercise ex = exerciseService.selectStudyExerciseById(exerciseId);
                if (ex != null) {
                    item.put("content", ex.getContent());
                    item.put("type", ex.getType());
                    item.put("answer", ex.getAnswer());
                    item.put("explanation", ex.getExplanation());
                }
            }
            rows.add(item);
        }
        return getDataTable(rows);
    }

    /**
     * 导出占位（可扩展）
     */
    @RequiresPermissions("study:wrong:export")
    @Log(title = "错题管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(StudyWrongAnswer query) {
        return AjaxResult.success("暂不支持导出");
    }
}
