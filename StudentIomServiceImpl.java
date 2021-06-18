package com.jborned.vverh.service;

import com.jborned.vverh.converter.StudentIomToStudentIomDtoConverter;
import com.jborned.vverh.db.model.StudentIom;
import com.jborned.vverh.db.repository.*;
import com.jborned.vverh.db.repository.student.StudentRepository;
import com.jborned.vverh.db.repository.studentiom.StudentIomRepository;
import com.jborned.vverh.service.dto.StudentIomDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentIomServiceImpl implements StudentIomService<StudentIomDTO> {

    private final StudentIomRepository studentIomRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final StudentIomToStudentIomDtoConverter studentIomToStudentIomDtoConverter;

    @Override
    public List<StudentIomDTO> getAllForListing(Long studentId) {
        List<StudentIom> studentIomList = studentIomRepository.findByStudentIdOrderById(studentId);

        return studentIomList.stream()
                .map(studentIomToStudentIomDtoConverter::convert)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        studentIomRepository.delete(id);
    }

    @Override
    public StudentIomDTO byId(Long id) {
        StudentIom studentIom = studentIomRepository.findOne(id);
        if (studentIom == null)
            return null;
        return studentIomToStudentIomDtoConverter.convert(studentIom);
    }

    @Override
    public StudentIomDTO saveOrUpdate(StudentIomDTO studentIomDTO) {

        StudentIom studentIom = new StudentIom();
        studentIom.setId(studentIomDTO.getId())
                .setStudyYear(studentIomDTO.getStudyYear())
                .setFromImplPeriod((studentIomDTO.getFromImplPeriod() != null && !studentIomDTO.getFromImplPeriod().equals("")) ? LocalDate.parse(studentIomDTO.getFromImplPeriod()) : null)
                .setToImplPeriod((studentIomDTO.getToImplPeriod() != null && !studentIomDTO.getToImplPeriod().equals("")) ? LocalDate.parse(studentIomDTO.getToImplPeriod()) : null)
                .setSubject(subjectRepository.findOne(studentIomDTO.getSubjectId()))
                .setTeacherForRoute(teacherRepository.findOne(studentIomDTO.getRouteTeacherId()))
                .setTeacherForDC(teacherRepository.findOne(studentIomDTO.getDiagnosticCardTeacherId()))
                .setStudent(studentRepository.findOne(studentIomDTO.getStudentId()));

        studentIomRepository.save(studentIom);
        return studentIomDTO.setId(studentIom.getId());
    }
}
