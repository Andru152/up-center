package com.jborned.vverh.web.controller;

import com.jborned.vverh.service.StudentIomService;
import com.jborned.vverh.service.StudentService;
import com.jborned.vverh.service.dto.StudentIomDTO;
import com.jborned.vverh.service.dto.StudentIomRouteDTO;
import com.jborned.vverh.web.exception.PageNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import static com.jborned.vverh.web.constant.Constants.Mapping.BaseController.*;

@Controller
@RequiredArgsConstructor
@RequestMapping(STUDENTS)
public class IomRouteController {

    private final String HEADER_EDIT = this.getClass().getSimpleName() + EDIT_KEY;
    private static final String STUDENT_IOM_DTO = "studentIom";
    private static final String IOM_ROUTE_DTO = "studentIomRoute";


    private final StudentService studentService;
    private final StudentIomService<StudentIomDTO> studentIomService;
    private final StudentIomService<StudentIomRouteDTO> studentIomRouteService;

    @Secured({"ROLE_ADMIN", "ROLE_MODER"})
    @RequestMapping(EDIT + STUDENT_IOM + SECOND_EDIT + ROUTE + "{studentIomId}" + ADD)
    public String routeCreate(@PathVariable("studentIomId") Long studentIomId, Model model) {
        StudentIomDTO studentIomDTO = studentIomService.byId(studentIomId);

        model.addAttribute(IOM_ROUTE_DTO, new StudentIomRouteDTO());
        model.addAttribute(STUDENT_IOM_DTO, studentIomDTO);
        model.addAttribute("student", studentService.byId(studentIomDTO.getStudentId()));
        model.addAttribute("headerName", HEADER_EDIT);

        return "student/student_iom_route_edit";
    }

    @Secured({"ROLE_ADMIN", "ROLE_MODER"})
    @RequestMapping(EDIT + STUDENT_IOM + SECOND_EDIT + ROUTE + SECOND_EDIT + "{iomRouteId}")
    public String routeEdit(@PathVariable("iomRouteId") Long routeId, Model model) {

        StudentIomRouteDTO iomRouteDTO = studentIomRouteService.byId(routeId);
        if (iomRouteDTO == null) {
            throw new PageNotFoundException();
        }
        StudentIomDTO studentIomDTO = studentIomService.byId(iomRouteDTO.getStudentIomId());

        model.addAttribute("student", studentService.byId(studentIomDTO.getStudentId()));
        model.addAttribute(IOM_ROUTE_DTO, iomRouteDTO);
        model.addAttribute(STUDENT_IOM_DTO, studentIomDTO);
        model.addAttribute("headerName", HEADER_EDIT);

        return "student/student_iom_route_edit";
    }

    @Secured({"ROLE_ADMIN", "ROLE_MODER"})
    @PostMapping(value = EDIT + STUDENT_IOM + SECOND_EDIT + ROUTE + SECOND_EDIT)
    public String routeUpdate(@ModelAttribute(IOM_ROUTE_DTO) StudentIomRouteDTO studentIomRouteDTO) {
        StudentIomRouteDTO studentIomRouteDTOFromDB;
        if (studentIomRouteDTO.getId() != null) {
            studentIomRouteDTOFromDB = studentIomRouteService.byId(studentIomRouteDTO.getId());
        } else {
            studentIomRouteDTOFromDB = new StudentIomRouteDTO();
        }

        studentIomRouteDTOFromDB.setTopic(studentIomRouteDTO.getTopic())
                .setHours(studentIomRouteDTO.getHours())
                .setCompletionDatePlan(studentIomRouteDTO.getCompletionDatePlan())
                .setCompletionDateFact(studentIomRouteDTO.getCompletionDateFact())
                .setStudentIomId(studentIomRouteDTO.getStudentIomId());

        studentIomRouteService.saveOrUpdate(studentIomRouteDTOFromDB);

        return "redirect:" + STUDENTS + EDIT + STUDENT_IOM + SECOND_EDIT + studentIomRouteDTO.getStudentIomId();
    }

    @Secured({"ROLE_ADMIN", "ROLE_MODER"})
    @RequestMapping(EDIT + STUDENT_IOM + SECOND_EDIT + ROUTE + DELETE + "{iomRouteId}")
    public String routeDelete(@PathVariable("iomRouteId") Long routeId, Model model) {
        StudentIomRouteDTO iomRouteDTO = studentIomRouteService.byId(routeId);
        if (iomRouteDTO == null) {
            throw new PageNotFoundException();
        }

        String url = STUDENTS + EDIT + STUDENT_IOM + SECOND_EDIT + studentIomRouteService.byId(routeId).getStudentIomId();
        model.addAttribute(STUDENT_IOM_DTO, studentIomService.byId(iomRouteDTO.getStudentIomId()));
        studentIomRouteService.delete(routeId);

        return "redirect:" + url;
    }
}
