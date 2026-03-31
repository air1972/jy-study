package com.jy.study.web.controller.study;

import com.jy.study.common.annotation.Anonymous;
import com.jy.study.common.annotation.Log;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.core.page.TableDataInfo;
import com.jy.study.common.enums.BusinessType;
import com.jy.study.common.utils.poi.ExcelUtil;
import com.jy.study.lesson.domain.*;
import com.jy.study.lesson.service.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理控制器
 */
@Controller
@RequestMapping("/study/classification")
public class StudyClassificationController extends BaseController {

    @Autowired
    private IStudyVersionService versionService;

    @Autowired
    private IStudySubjectService subjectService;

    @Autowired
    private IStudyGradeService gradeService;

    @Autowired
    private IStudyVolumeService volumeService;

    // ==================== 版本管理 ====================

    @RequiresPermissions("study:classification:version:view")
    @GetMapping("/version")
    public String version() {
        return "study/classification/version";
    }

    @RequiresPermissions("study:classification:version:list")
    @PostMapping("/version/list")
    @ResponseBody
    public TableDataInfo versionList(StudyVersion studyVersion) {
        startPage();
        List<StudyVersion> list = versionService.selectStudyVersionList(studyVersion);
        return getDataTable(list);
    }

    @RequiresPermissions("study:classification:version:export")
    @Log(title = "版本管理", businessType = BusinessType.EXPORT)
    @PostMapping("/version/export")
    @ResponseBody
    public AjaxResult versionExport(StudyVersion studyVersion) {
        List<StudyVersion> list = versionService.selectStudyVersionList(studyVersion);
        ExcelUtil<StudyVersion> util = new ExcelUtil<>(StudyVersion.class);
        return util.exportExcel(list, "版本数据");
    }

    @GetMapping("/version/add")
    public String versionAdd() {
        return "study/classification/versionAdd";
    }

    @RequiresPermissions("study:classification:version:add")
    @Log(title = "版本管理", businessType = BusinessType.INSERT)
    @PostMapping("/version/add")
    @ResponseBody
    public AjaxResult versionAddSave(StudyVersion studyVersion) {
        return toAjax(versionService.insertStudyVersion(studyVersion));
    }

    @GetMapping("/version/edit/{versionId}")
    public String versionEdit(@PathVariable("versionId") Long versionId, ModelMap mmap) {
        StudyVersion studyVersion = versionService.selectStudyVersionById(versionId);
        mmap.put("studyVersion", studyVersion);
        return "study/classification/versionEdit";
    }

    @RequiresPermissions("study:classification:version:edit")
    @Log(title = "版本管理", businessType = BusinessType.UPDATE)
    @PostMapping("/version/edit")
    @ResponseBody
    public AjaxResult versionEditSave(StudyVersion studyVersion) {
        return toAjax(versionService.updateStudyVersion(studyVersion));
    }

    @RequiresPermissions("study:classification:version:remove")
    @Log(title = "版本管理", businessType = BusinessType.DELETE)
    @PostMapping("/version/remove")
    @ResponseBody
    public AjaxResult versionRemove(String ids) {
        return toAjax(versionService.deleteStudyVersionByIds(ids));
    }

    // ==================== 科目管理 ====================

    @RequiresPermissions("study:classification:subject:view")
    @GetMapping("/subject")
    public String subject() {
        return "study/classification/subject";
    }

    @RequiresPermissions("study:classification:subject:list")
    @PostMapping("/subject/list")
    @ResponseBody
    public TableDataInfo subjectList(StudySubject studySubject) {
        startPage();
        List<StudySubject> list = subjectService.selectStudySubjectList(studySubject);
        return getDataTable(list);
    }

    @RequiresPermissions("study:classification:subject:export")
    @Log(title = "科目管理", businessType = BusinessType.EXPORT)
    @PostMapping("/subject/export")
    @ResponseBody
    public AjaxResult subjectExport(StudySubject studySubject) {
        List<StudySubject> list = subjectService.selectStudySubjectList(studySubject);
        ExcelUtil<StudySubject> util = new ExcelUtil<>(StudySubject.class);
        return util.exportExcel(list, "科目数据");
    }

    @GetMapping("/subject/add")
    public String subjectAdd() {
        return "study/classification/subjectAdd";
    }

    @RequiresPermissions("study:classification:subject:add")
    @Log(title = "科目管理", businessType = BusinessType.INSERT)
    @PostMapping("/subject/add")
    @ResponseBody
    public AjaxResult subjectAddSave(StudySubject studySubject) {
        return toAjax(subjectService.insertStudySubject(studySubject));
    }

    @GetMapping("/subject/edit/{subjectId}")
    public String subjectEdit(@PathVariable("subjectId") Long subjectId, ModelMap mmap) {
        StudySubject studySubject = subjectService.selectStudySubjectById(subjectId);
        mmap.put("studySubject", studySubject);
        return "study/classification/subjectEdit";
    }

    @RequiresPermissions("study:classification:subject:edit")
    @Log(title = "科目管理", businessType = BusinessType.UPDATE)
    @PostMapping("/subject/edit")
    @ResponseBody
    public AjaxResult subjectEditSave(StudySubject studySubject) {
        return toAjax(subjectService.updateStudySubject(studySubject));
    }

    @RequiresPermissions("study:classification:subject:remove")
    @Log(title = "科目管理", businessType = BusinessType.DELETE)
    @PostMapping("/subject/remove")
    @ResponseBody
    public AjaxResult subjectRemove(String ids) {
        return toAjax(subjectService.deleteStudySubjectByIds(ids));
    }

    // ==================== 年级管理 ====================

    @RequiresPermissions("study:classification:grade:view")
    @GetMapping("/grade")
    public String grade() {
        return "study/classification/grade";
    }

    @RequiresPermissions("study:classification:grade:list")
    @PostMapping("/grade/list")
    @ResponseBody
    public TableDataInfo gradeList(StudyGrade studyGrade) {
        startPage();
        List<StudyGrade> list = gradeService.selectStudyGradeList(studyGrade);
        return getDataTable(list);
    }

    @RequiresPermissions("study:classification:grade:export")
    @Log(title = "年级管理", businessType = BusinessType.EXPORT)
    @PostMapping("/grade/export")
    @ResponseBody
    public AjaxResult gradeExport(StudyGrade studyGrade) {
        List<StudyGrade> list = gradeService.selectStudyGradeList(studyGrade);
        ExcelUtil<StudyGrade> util = new ExcelUtil<>(StudyGrade.class);
        return util.exportExcel(list, "年级数据");
    }

    @GetMapping("/grade/add")
    public String gradeAdd() {
        return "study/classification/gradeAdd";
    }

    @RequiresPermissions("study:classification:grade:add")
    @Log(title = "年级管理", businessType = BusinessType.INSERT)
    @PostMapping("/grade/add")
    @ResponseBody
    public AjaxResult gradeAddSave(StudyGrade studyGrade) {
        return toAjax(gradeService.insertStudyGrade(studyGrade));
    }

    @GetMapping("/grade/edit/{gradeId}")
    public String gradeEdit(@PathVariable("gradeId") Long gradeId, ModelMap mmap) {
        StudyGrade studyGrade = gradeService.selectStudyGradeById(gradeId);
        mmap.put("studyGrade", studyGrade);
        return "study/classification/gradeEdit";
    }

    @RequiresPermissions("study:classification:grade:edit")
    @Log(title = "年级管理", businessType = BusinessType.UPDATE)
    @PostMapping("/grade/edit")
    @ResponseBody
    public AjaxResult gradeEditSave(StudyGrade studyGrade) {
        return toAjax(gradeService.updateStudyGrade(studyGrade));
    }

    @RequiresPermissions("study:classification:grade:remove")
    @Log(title = "年级管理", businessType = BusinessType.DELETE)
    @PostMapping("/grade/remove")
    @ResponseBody
    public AjaxResult gradeRemove(String ids) {
        return toAjax(gradeService.deleteStudyGradeByIds(ids));
    }

    // ==================== 册子管理 ====================

    @RequiresPermissions("study:classification:volume:view")
    @GetMapping("/volume")
    public String volume() {
        return "study/classification/volume";
    }

    @RequiresPermissions("study:classification:volume:list")
    @PostMapping("/volume/list")
    @ResponseBody
    public TableDataInfo volumeList(StudyVolume studyVolume) {
        startPage();
        List<StudyVolume> list = volumeService.selectStudyVolumeList(studyVolume);
        return getDataTable(list);
    }

    @RequiresPermissions("study:classification:volume:export")
    @Log(title = "册子管理", businessType = BusinessType.EXPORT)
    @PostMapping("/volume/export")
    @ResponseBody
    public AjaxResult volumeExport(StudyVolume studyVolume) {
        List<StudyVolume> list = volumeService.selectStudyVolumeList(studyVolume);
        ExcelUtil<StudyVolume> util = new ExcelUtil<>(StudyVolume.class);
        return util.exportExcel(list, "册子数据");
    }

    @GetMapping("/volume/add")
    public String volumeAdd() {
        return "study/classification/volumeAdd";
    }

    @RequiresPermissions("study:classification:volume:add")
    @Log(title = "册子管理", businessType = BusinessType.INSERT)
    @PostMapping("/volume/add")
    @ResponseBody
    public AjaxResult volumeAddSave(StudyVolume studyVolume) {
        return toAjax(volumeService.insertStudyVolume(studyVolume));
    }

    @GetMapping("/volume/edit/{volumeId}")
    public String volumeEdit(@PathVariable("volumeId") Long volumeId, ModelMap mmap) {
        StudyVolume studyVolume = volumeService.selectStudyVolumeById(volumeId);
        mmap.put("studyVolume", studyVolume);
        return "study/classification/volumeEdit";
    }

    @RequiresPermissions("study:classification:volume:edit")
    @Log(title = "册子管理", businessType = BusinessType.UPDATE)
    @PostMapping("/volume/edit")
    @ResponseBody
    public AjaxResult volumeEditSave(StudyVolume studyVolume) {
        return toAjax(volumeService.updateStudyVolume(studyVolume));
    }

    @RequiresPermissions("study:classification:volume:remove")
    @Log(title = "册子管理", businessType = BusinessType.DELETE)
    @PostMapping("/volume/remove")
    @ResponseBody
    public AjaxResult volumeRemove(String ids) {
        return toAjax(volumeService.deleteStudyVolumeByIds(ids));
    }

    // ==================== 公共接口 ====================

    @Anonymous
    @GetMapping("/all")
    @ResponseBody
    public AjaxResult getAllClassifications() {
        return AjaxResult.success()
                .put("versions", versionService.selectStudyVersionList(new StudyVersion()))
                .put("subjects", subjectService.selectStudySubjectList(new StudySubject()))
                .put("grades", gradeService.selectStudyGradeList(new StudyGrade()))
                .put("volumes", volumeService.selectStudyVolumeList(new StudyVolume()));
    }
}
