package com.jborned.vverh.service;

import com.jborned.vverh.converter.IomRouteToIomRouteDtoConverter;
import com.jborned.vverh.db.model.StudentIomRoute;
import com.jborned.vverh.db.repository.studentiom.StudentIomRepository;
import com.jborned.vverh.db.repository.studentiom.StudentIomRouteRepository;
import com.jborned.vverh.service.dto.StudentIomRouteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class IomRouteServiceImpl implements StudentIomService<StudentIomRouteDTO> {

    private final StudentIomRepository studentIomRepository;
    private final StudentIomRouteRepository studentIomRouteRepository;
    private final IomRouteToIomRouteDtoConverter iomRouteToIomRouteDtoConverter;

    @Override
    public List<StudentIomRouteDTO> getAllForListing(Long id) {
        List<StudentIomRoute> routeList = studentIomRouteRepository.findByStudentIomIdOrderById(id);
        return routeList.stream()
                .map(iomRouteToIomRouteDtoConverter::convert)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        studentIomRouteRepository.delete(id);
    }

    @Override
    public StudentIomRouteDTO byId(Long id) {
        StudentIomRoute iomRoute = studentIomRouteRepository.findOne(id);
        if (iomRoute == null)
            return null;
        return iomRouteToIomRouteDtoConverter.convert(iomRoute);
    }

    @Override
    public StudentIomRouteDTO saveOrUpdate(StudentIomRouteDTO studentIomRouteDTO) {

        StudentIomRoute studentIomRoute = new StudentIomRoute();

        studentIomRoute.setId(studentIomRouteDTO.getId())
                .setTopic(studentIomRouteDTO.getTopic())
                .setHours(studentIomRouteDTO.getHours())
                .setCompletionDatePlan((studentIomRouteDTO.getCompletionDatePlan() != null && !studentIomRouteDTO.getCompletionDatePlan().equals("")) ? LocalDate.parse(studentIomRouteDTO.getCompletionDatePlan()) : null)
                .setCompletionDateFact((studentIomRouteDTO.getCompletionDateFact() != null && !studentIomRouteDTO.getCompletionDateFact().equals("")) ? LocalDate.parse(studentIomRouteDTO.getCompletionDateFact()) : null)
                .setStudentIom(studentIomRepository.findOne(studentIomRouteDTO.getStudentIomId()));

        studentIomRouteRepository.save(studentIomRoute);
        return studentIomRouteDTO.setId(studentIomRoute.getId());
    }
}
