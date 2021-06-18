package com.jborned.vverh.web.controller;

import com.jborned.vverh.service.*;
import com.jborned.vverh.service.dto.*;
import com.jborned.vverh.web.exception.PageNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jborned.vverh.web.constant.Constants.Mapping.BaseController.*;

@Controller
@RequiredArgsConstructor
@RequestMapping(STUDENTS)
public class StudentIomController extends AbstractStudentController {

    private static final String STUDENT_DTO = "student";
    private static final String STUDENT_IOM_DTO = "studentIom";
    private static final String HEADER_NAME = "headerName";

    private final StudentService studentService;
    private final StudentIomService<StudentIomDTO> studentIomService;
    private final StudentIomService<StudentIomRouteDTO> studentIomRouteService;
    private final StudentIomService<StudentIomDiagnosticCardDTO> studentIomDCService;
    private final StudyGroupService studyGroupService;
    private final SubjectService subjectService;
    private final TeacherService teacherService;

    @RequestMapping(EDIT + STUDENT_IOM + "{studentId}")
    public String studentIomInfo(@PathVariable("studentId") Long id, Model model, HttpSession session) {
        StudentDTO studentDTO = getStudentDTO(id);
        checkSessionAndSetViewMode(model, session);

        Map<Long, String> subjects = subjectService.list().stream()
                .collect(Collectors.toMap(SubjectDto::getId, SubjectDto::getName));

        model.addAttribute(STUDENT_DTO, studentDTO);
        model.addAttribute(STUDENT_IOM_DTO, studentIomService.getAllForListing(id));
        model.addAttribute("subject", subjects);
        return "student/student_iom_info";
    }

    @Secured({"ROLE_ADMIN", "ROLE_MODER"})
    @RequestMapping(EDIT + STUDENT_IOM + "{studentId}" + ADD)
    public String studentIomCreate(@PathVariable("studentId") Long id, Model model) {
        StudentDTO studentDTO = getStudentDTO(id);

        model.addAttribute(STUDENT_IOM_DTO, new StudentIomDTO());
        model.addAttribute(STUDENT_DTO, studentDTO);
        model.addAttribute("yearPeriods", getStudyYears());
        model.addAttribute("subjects", subjectService.list());
        model.addAttribute("teachers", teacherService.getAllTeachers());
        model.addAttribute("space", " ");
        model.addAttribute(HEADER_NAME, HEADER_ADD);

        return "student/student_iom_edit";
    }

    @Secured({"ROLE_ADMIN", "ROLE_MODER"})
    @RequestMapping(EDIT + STUDENT_IOM + "{mode}" + "/" + "{studentIomId}")
    public String studentIomEdit(@PathVariable("mode") String mode, @PathVariable("studentIomId") Long studentIomId, Model model, HttpSession session) {

        StudentIomDTO studentIomDTO = studentIomService.byId(studentIomId);
        if (studentIomDTO == null) {
            throw new PageNotFoundException();
        }

        if (mode.equals("view")) {
            model.addAttribute("isWiewing", true);
            session.setAttribute("mySessionAttribute", "view");
            model.addAttribute(HEADER_NAME, HEADER_VIEW);
        }

        if (mode.equals("edit")) {
            model.addAttribute("isWiewing", false);
            session.setAttribute("mySessionAttribute", "edit");
            model.addAttribute(HEADER_NAME, HEADER_EDIT);
        }

        model.addAttribute(STUDENT_DTO, studentService.byId(studentIomDTO.getStudentId()));
        model.addAttribute(STUDENT_IOM_DTO, studentIomDTO);
        model.addAttribute("yearPeriods", getStudyYears());
        model.addAttribute("subjects", subjectService.list());
        model.addAttribute("teachers", teacherService.getAllTeachers());
        model.addAttribute("space", " ");
        model.addAttribute("iomRoute", studentIomRouteService.getAllForListing(studentIomId));
        model.addAttribute("iomDC", studentIomDCService.getAllForListing(studentIomId));

        return "student/student_iom_edit";
    }

    @Secured({"ROLE_ADMIN", "ROLE_MODER"})
    @PostMapping(value = EDIT + STUDENT_IOM + EDIT)
    public String studentIomUpdate(@ModelAttribute(STUDENT_IOM_DTO) StudentIomDTO studentIomDTO) {
        StudentIomDTO studentIomFromDB;
        if (studentIomDTO.getId() != null) {
            studentIomFromDB = studentIomService.byId(studentIomDTO.getId());
        } else {
            studentIomFromDB = new StudentIomDTO();
        }

        studentIomFromDB.setStudyYear(studentIomDTO.getStudyYear())
                .setFromImplPeriod(studentIomDTO.getFromImplPeriod())
                .setToImplPeriod(studentIomDTO.getToImplPeriod())
                .setSubjectId(studentIomDTO.getSubjectId())
                .setRouteTeacherId(studentIomDTO.getRouteTeacherId())
                .setDiagnosticCardTeacherId(studentIomDTO.getDiagnosticCardTeacherId())
                .setStudentId(studentIomDTO.getStudentId());

        studentIomService.saveOrUpdate(studentIomFromDB);

        return "redirect:" + STUDENTS + EDIT + STUDENT_IOM + SECOND_EDIT + studentIomFromDB.getId();
    }

    @Secured({"ROLE_ADMIN", "ROLE_MODER"})
    @RequestMapping(EDIT + STUDENT_IOM + DELETE + "{studentIomId}")
    public String studentIomDelete(@PathVariable("studentIomId") Long studentIomId, Model model) {
        StudentIomDTO studentIomDTO = studentIomService.byId(studentIomId);
        if (studentIomDTO == null) {
            throw new PageNotFoundException();
        }

        String url = STUDENTS + EDIT + STUDENT_IOM + studentIomService.byId(studentIomId).getStudentId();
        model.addAttribute(STUDENT_DTO, studentService.byId(studentIomDTO.getStudentId()));
        studentIomService.delete(studentIomId);

        return "redirect:" + url;
    }

    private List<YearPeriodDTO> getStudyYears() {
        return studyGroupService.findDistinctStudyYears().stream()
                .filter(x -> x.getPeriod() != null)
                .sorted(Comparator.nullsFirst(Comparator.comparing(YearPeriodDTO::getYearStart,
                        Comparator.nullsFirst(Comparator.naturalOrder()))).reversed())
                .collect(Collectors.toList());
    }
}
