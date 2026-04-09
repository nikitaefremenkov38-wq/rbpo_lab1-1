package com.example.demo.controller;

import com.example.demo.dto.ScheduleRequest;
import com.example.demo.entity.Attraction;
import com.example.demo.entity.Schedule;
import com.example.demo.repository.AttractionRepository;
import com.example.demo.repository.ScheduleRepository;
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
@RequestMapping("/schedules")
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;
    private final AttractionRepository attractionRepository;

    public ScheduleController(ScheduleRepository scheduleRepository, AttractionRepository attractionRepository) {
        this.scheduleRepository = scheduleRepository;
        this.attractionRepository = attractionRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Schedule create(@Valid @RequestBody ScheduleRequest request) {
        validateTimeRange(request.getStartTime().isBefore(request.getEndTime()));
        Attraction attraction = findAttraction(request.getAttractionId());

        Schedule schedule = new Schedule();
        schedule.setAttraction(attraction);
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());

        return scheduleRepository.save(schedule);
    }

    @GetMapping
    public List<Schedule> getAll() {
        return scheduleRepository.findAll();
    }

    @GetMapping("/{id}")
    public Schedule getById(@PathVariable Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found: " + id));
    }

    @PutMapping("/{id}")
    public Schedule update(@PathVariable Long id, @Valid @RequestBody ScheduleRequest request) {
        validateTimeRange(request.getStartTime().isBefore(request.getEndTime()));
        Attraction attraction = findAttraction(request.getAttractionId());

        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found: " + id));

        schedule.setAttraction(attraction);
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());

        return scheduleRepository.save(schedule);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found: " + id));
        scheduleRepository.delete(schedule);
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
