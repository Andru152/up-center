package com.jborned.vverh.service;

import com.jborned.vverh.converter.IomDiagnosticCardToIomDiagnosticCardDtoConverter;
import com.jborned.vverh.db.model.StudentIomDiagnosticCard;
import com.jborned.vverh.db.repository.studentiom.StudentIomDiagnosticCardRepository;
import com.jborned.vverh.db.repository.studentiom.StudentIomRepository;
import com.jborned.vverh.service.dto.StudentIomDiagnosticCardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class IomDiagnosticCardServiceImpl implements StudentIomService<StudentIomDiagnosticCardDTO> {

    private final StudentIomRepository studentIomRepository;
    private final StudentIomDiagnosticCardRepository studentIomDiagnosticCardRepository;
    private final IomDiagnosticCardToIomDiagnosticCardDtoConverter iomDiagnosticCardToIomDiagnosticCardDtoConverter;

    @Override
    public List<StudentIomDiagnosticCardDTO> getAllForListing(Long studentIomId) {
        List<StudentIomDiagnosticCard> diagnosticCardList = studentIomDiagnosticCardRepository.findByStudentIomIdOrderById(studentIomId);
        return diagnosticCardList.stream()
                .map(iomDiagnosticCardToIomDiagnosticCardDtoConverter::convert)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        studentIomDiagnosticCardRepository.delete(id);
    }

    @Override
    public StudentIomDiagnosticCardDTO byId(Long id) {
        StudentIomDiagnosticCard diagnosticCard = studentIomDiagnosticCardRepository.findOne(id);
        if (diagnosticCard == null)
            return null;
        return iomDiagnosticCardToIomDiagnosticCardDtoConverter.convert(diagnosticCard);
    }

    @Override
    public StudentIomDiagnosticCardDTO saveOrUpdate(StudentIomDiagnosticCardDTO studentIomDiagnosticCardDTO) {

        StudentIomDiagnosticCard studentIomDiagnosticCard = new StudentIomDiagnosticCard();

        studentIomDiagnosticCard.setId(studentIomDiagnosticCardDTO.getId())
                .setDate((studentIomDiagnosticCardDTO.getDate() != null && !studentIomDiagnosticCardDTO.getDate().equals("")) ? LocalDate.parse(studentIomDiagnosticCardDTO.getDate()) : null)
                .setMeasurementForm(studentIomDiagnosticCardDTO.getMeasurementForm())
                .setTaskFile(studentIomDiagnosticCardDTO.getTaskFile())
                .setTaskFileExtension(studentIomDiagnosticCardDTO.getTaskFileExtension())
                .setComment(studentIomDiagnosticCardDTO.getComment())
                .setCompletedTaskFile(studentIomDiagnosticCardDTO.getCompletedTaskFile())
                .setCompletedTaskFileExtension(studentIomDiagnosticCardDTO.getCompletedTaskFileExtension())
                .setStudentIom(studentIomRepository.findOne(studentIomDiagnosticCardDTO.getStudentIomId()));

        studentIomDiagnosticCardRepository.save(studentIomDiagnosticCard);
        return studentIomDiagnosticCardDTO.setId(studentIomDiagnosticCard.getId());
    }
}
