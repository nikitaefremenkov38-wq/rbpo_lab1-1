package com.example.demo.controller;

import com.example.demo.dto.MaintenanceRequest;
import com.example.demo.entity.Attraction;
import com.example.demo.entity.Maintenance;
import com.example.demo.repository.AttractionRepository;
import com.example.demo.repository.MaintenanceRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/maintenances")
public class MaintenanceController {

    private final MaintenanceRepository maintenanceRepository;
    private final AttractionRepository attractionRepository;

    public MaintenanceController(MaintenanceRepository maintenanceRepository, AttractionRepository attractionRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.attractionRepository = attractionRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Maintenance create(@Valid @RequestBody MaintenanceRequest request) {
        validateTimeRange(request.getStartTime().isBefore(request.getEndTime()));
        Attraction attraction = findAttraction(request.getAttractionId());

        Maintenance maintenance = new Maintenance();
        maintenance.setAttraction(attraction);
        maintenance.setStartTime(request.getStartTime());
        maintenance.setEndTime(request.getEndTime());
        maintenance.setReason(request.getReason());

        return maintenanceRepository.save(maintenance);
    }

    @GetMapping
    public List<Maintenance> getAll() {
        return maintenanceRepository.findAll();
    }

    @GetMapping("/{id}")
    public Maintenance getById(@PathVariable Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance not found: " + id));
    }

    @PutMapping("/{id}")
    public Maintenance update(@PathVariable Long id, @Valid @RequestBody MaintenanceRequest request) {
        validateTimeRange(request.getStartTime().isBefore(request.getEndTime()));
        Attraction attraction = findAttraction(request.getAttractionId());

        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance not found: " + id));

        maintenance.setAttraction(attraction);
        maintenance.setStartTime(request.getStartTime());
        maintenance.setEndTime(request.getEndTime());
        maintenance.setReason(request.getReason());

        return maintenanceRepository.save(maintenance);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance not found: " + id));
        maintenanceRepository.delete(maintenance);
    }

    private Attraction findAttraction(Long id) {
        return attractionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attraction not found: " + id));
    }

    private void validateTimeRange(boolean isValidRange) {
        if (!isValidRange) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startTime must be before endTime");
        }
    }
}
