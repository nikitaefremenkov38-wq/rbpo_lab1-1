package com.example.demo.controller;

import com.example.demo.dto.AttractionRequest;
import com.example.demo.entity.Attraction;
import com.example.demo.repository.AttractionRepository;
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
@RequestMapping("/attractions")
public class AttractionController {

    private final AttractionRepository attractionRepository;

    public AttractionController(AttractionRepository attractionRepository) {
        this.attractionRepository = attractionRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Attraction create(@Valid @RequestBody AttractionRequest request) {
        Attraction attraction = new Attraction();
        attraction.setName(request.getName());
        attraction.setDescription(request.getDescription());
        attraction.setCapacity(request.getCapacity());
        return attractionRepository.save(attraction);
    }

    @GetMapping
    public List<Attraction> getAll() {
        return attractionRepository.findAll();
    }

    @GetMapping("/{id}")
    public Attraction getById(@PathVariable Long id) {
        return attractionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attraction not found: " + id));
    }

    @PutMapping("/{id}")
    public Attraction update(@PathVariable Long id, @Valid @RequestBody AttractionRequest request) {
        Attraction attraction = attractionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attraction not found: " + id));

        attraction.setName(request.getName());
        attraction.setDescription(request.getDescription());
        attraction.setCapacity(request.getCapacity());

        return attractionRepository.save(attraction);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Attraction attraction = attractionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attraction not found: " + id));
        attractionRepository.delete(attraction);
    }
}
