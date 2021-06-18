package com.jborned.vverh.web.controller;

import com.jborned.vverh.service.DocumentTypeService;
import com.jborned.vverh.service.StudentIomService;
import com.jborned.vverh.service.StudentService;
import com.jborned.vverh.service.dto.StudentIomDTO;
import com.jborned.vverh.service.dto.StudentIomDiagnosticCardDTO;
import com.jborned.vverh.web.exception.PageNotFoundException;
import liquibase.util.file.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;

import static com.jborned.vverh.web.constant.Constants.Mapping.BaseController.*;

@Controller
@RequestMapping(STUDENTS)
public class IomDiagnosticCardController extends AbstractDocumentController {

    public IomDiagnosticCardController(DocumentTypeService documentTypeService) {
        super(documentTypeService);
    }

    private final String HEADER_EDIT_DC = this.getClass().getSimpleName() + EDIT_KEY;
    private static final String STUDENT_IOM_DTO = "studentIom";
    private static final String IOM_DC_DTO = "studentIomDC";
    private static final String REDIRECT = "redirect:";

    @Autowired
    protected StudentService studentService;
    @Autowired
    protected StudentIomService<StudentIomDTO> studentIomService;
    @Autowired
    protected StudentIomService<StudentIomDiagnosticCardDTO> studentIomDCService;

    @GetMapping(value = EDIT + STUDENT_IOM + SECOND_EDIT + DC + DOWNLOAD + "{file}" + "/" + "{iomDCId}")
    public void downloadFileDC(@PathVariable("file") String file,
                               @PathVariable("iomDCId") long diagnosticCardId,
                               HttpServletResponse response) {
        StudentIomDiagnosticCardDTO diagnosticCardDTO = studentIomDCService.byId(diagnosticCardId);

        if (diagnosticCardDTO == null) {
            throw new PageNotFoundException();
        }

        if (file.equals("task_file")) {
            downloadFile(diagnosticCardDTO.getTaskFile(), response, "student_iom"
                    + diagnosticCardDTO.getStudentIomId() + "_task_file." + diagnosticCardDTO.getTaskFileExtension());
        } else if (file.equals("completed_task_file")) {
            downloadFile(diagnosticCardDTO.getCompletedTaskFile(), response, "student_iom"
                    + diagnosticCardDTO.getStudentIomId() + "_completed_task_file." + diagnosticCardDTO.getCompletedTaskFileExtension());
        }

    }

    @Secured({"ROLE_ADMIN", "ROLE_MODER"})
    @RequestMapping(EDIT + STUDENT_IOM + SECOND_EDIT + DC + "{studentIomId}" + ADD)
    public String diagnosticCardCreate(@PathVariable("studentIomId") Long studentIomId, Model model) {
        StudentIomDTO studentIomDTO = studentIomService.byId(studentIomId);

        model.addAttribute(IOM_DC_DTO, new StudentIomDiagnosticCardDTO());
        model.addAttribute(STUDENT_IOM_DTO, studentIomDTO);
        model.addAttribute("student", studentService.byId(studentIomDTO.getStudentId()));
        model.addAttribute("headerName", HEADER_EDIT_DC);

        return "student/student_iom_diagnostic_card_edit";
    }

    @Secured({"ROLE_ADMIN", "ROLE_MODER"})
    @RequestMapping(EDIT + STUDENT_IOM + SECOND_EDIT + DC + SECOND_EDIT + "{iomDCId}")
    public String diagnosticCardEdit(@PathVariable("iomDCId") Long diagnosticCardId, Model model) {

        StudentIomDiagnosticCardDTO diagnosticCardDTO = studentIomDCService.byId(diagnosticCardId);
        if (diagnosticCardDTO == null) {
            throw new PageNotFoundException();
        }
        StudentIomDTO studentIomDTO = studentIomService.byId(diagnosticCardDTO.getStudentIomId());

        model.addAttribute("student", studentService.byId(studentIomDTO.getStudentId()));
        model.addAttribute(IOM_DC_DTO, diagnosticCardDTO);
        model.addAttribute(STUDENT_IOM_DTO, studentIomDTO);
        model.addAttribute("headerName", HEADER_EDIT_DC);

        return "student/student_iom_diagnostic_card_edit";
    }

    @Secured({"ROLE_ADMIN", "ROLE_MODER"})
    @PostMapping(value = EDIT + STUDENT_IOM + SECOND_EDIT + DC + SECOND_EDIT)
    public String diagnosticCardUpdate(@ModelAttribute(IOM_DC_DTO) StudentIomDiagnosticCardDTO studentIomDiagnosticCardDTO,
                                       Model model,
                                       @RequestParam(value = "task_file", required = false) MultipartFile taskFile,
                                       @RequestParam(value = "completed_task_file", required = false) MultipartFile completedTaskFile) {

        StudentIomDiagnosticCardDTO studentIomDiagnosticCardDTOFromDB = studentIomDiagnosticCardDTO.getId() != null ?
                studentIomDCService.byId(studentIomDiagnosticCardDTO.getId())
                : new StudentIomDiagnosticCardDTO();

        studentIomDiagnosticCardDTOFromDB.setDate(studentIomDiagnosticCardDTO.getDate())
                .setMeasurementForm(studentIomDiagnosticCardDTO.getMeasurementForm())
                .setComment(studentIomDiagnosticCardDTO.getComment())
                .setStudentIomId(studentIomDiagnosticCardDTO.getStudentIomId());

        if (taskFile != null && !taskFile.isEmpty()) {
            try {
                studentIomDiagnosticCardDTOFromDB.setTaskFile(taskFile.getBytes());
                studentIomDiagnosticCardDTOFromDB.setTaskFileExtension(FilenameUtils.getExtension(taskFile.getOriginalFilename()));
            } catch (IOException e) {
                model.addAttribute("timestamp", new Timestamp(System.currentTimeMillis()));
                model.addAttribute("path", STUDENTS + EDIT + STUDENT_IOM + SECOND_EDIT + DC + SECOND_EDIT);
                model.addAttribute("error", "Cannot read task file");
                model.addAttribute("status", 500);
                model.addAttribute("message", "No message available");
                model.addAttribute("exception", e);
                model.addAttribute("stacktrace", e.getStackTrace());
            }
        }

        if (completedTaskFile != null && !completedTaskFile.isEmpty()) {
            try {
                studentIomDiagnosticCardDTOFromDB.setCompletedTaskFile(completedTaskFile.getBytes());
                studentIomDiagnosticCardDTOFromDB.setCompletedTaskFileExtension(FilenameUtils.getExtension(completedTaskFile.getOriginalFilename()));
            } catch (IOException e) {
                model.addAttribute("timestamp", new Timestamp(System.currentTimeMillis()));
                model.addAttribute("path", STUDENTS + EDIT + STUDENT_IOM + SECOND_EDIT + DC + SECOND_EDIT);
                model.addAttribute("error", "Cannot read completed task file");
                model.addAttribute("status", 500);
                model.addAttribute("message", "No message available");
                model.addAttribute("exception", e);
                model.addAttribute("stacktrace", e.getStackTrace());
            }
        }

        studentIomDCService.saveOrUpdate(studentIomDiagnosticCardDTOFromDB);

        return REDIRECT + STUDENTS + EDIT + STUDENT_IOM + SECOND_EDIT + DC + SECOND_EDIT + studentIomDiagnosticCardDTOFromDB.getId();
    }

    @Secured({"ROLE_ADMIN", "ROLE_MODER"})
    @RequestMapping(EDIT + STUDENT_IOM + SECOND_EDIT + DC + DELETE + "{iomDCId}")
    public String diagnosticCardDelete(@PathVariable("iomDCId") Long diagnosticCardId, Model model) {
        StudentIomDiagnosticCardDTO iomDiagnosticCardDTO = studentIomDCService.byId(diagnosticCardId);

        if (iomDiagnosticCardDTO == null) {
            throw new PageNotFoundException();
        }

        String url = STUDENTS + EDIT + STUDENT_IOM + SECOND_EDIT + iomDiagnosticCardDTO.getStudentIomId();
        model.addAttribute(STUDENT_IOM_DTO, studentIomService.byId(iomDiagnosticCardDTO.getStudentIomId()));

        studentIomDCService.delete(diagnosticCardId);

        return REDIRECT + url;
    }

    @Secured({"ROLE_ADMIN", "ROLE_MODER"})
    @RequestMapping(EDIT + STUDENT_IOM + SECOND_EDIT + DC + "{file}" + DELETE + "{iomDCId}")
    public String diagnosticCardDeleteFile(@PathVariable("iomDCId") Long diagnosticCardId,
                                           @PathVariable("file") String file,
                                           Model model) {
        StudentIomDiagnosticCardDTO studentIomDiagnosticCardDTO = studentIomDCService.byId(diagnosticCardId);

        if (studentIomDiagnosticCardDTO == null) {
            throw new PageNotFoundException();
        }

        if (file.equals("task_file")) {
            studentIomDiagnosticCardDTO
                    .setTaskFile(null)
                    .setTaskFileExtension(null);
        } else if (file.equals("completed_task_file")) {
            studentIomDiagnosticCardDTO
                    .setCompletedTaskFile(null)
                    .setCompletedTaskFileExtension(null);
        }

        model.addAttribute(IOM_DC_DTO, studentIomDiagnosticCardDTO);

        studentIomDCService.saveOrUpdate(studentIomDiagnosticCardDTO);

        return REDIRECT + STUDENTS + EDIT + STUDENT_IOM + SECOND_EDIT + DC + SECOND_EDIT + studentIomDiagnosticCardDTO.getId();
    }
}
