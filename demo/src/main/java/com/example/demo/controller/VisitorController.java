package com.example.demo.controller;

import com.example.demo.dto.VisitorRequest;
import com.example.demo.entity.Visitor;
import com.example.demo.repository.VisitorRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/visitors")
public class VisitorController {

    private final VisitorRepository visitorRepository;

    public VisitorController(VisitorRepository visitorRepository) {
        this.visitorRepository = visitorRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Visitor create(@Valid @RequestBody VisitorRequest request) {
        Visitor visitor = new Visitor();
        visitor.setFullName(request.getFullName());
        visitor.setEmail(request.getEmail());

        try {
            return visitorRepository.save(visitor);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visitor email must be unique");
        }
    }

    @GetMapping
    public List<Visitor> getAll() {
        return visitorRepository.findAll();
    }

    @GetMapping("/{id}")
    public Visitor getById(@PathVariable Long id) {
        return visitorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visitor not found: " + id));
    }

    @PutMapping("/{id}")
    public Visitor update(@PathVariable Long id, @Valid @RequestBody VisitorRequest request) {
        Visitor visitor = visitorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visitor not found: " + id));

        visitor.setFullName(request.getFullName());
        visitor.setEmail(request.getEmail());

        try {
            return visitorRepository.save(visitor);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visitor email must be unique");
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Visitor visitor = visitorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visitor not found: " + id));
        visitorRepository.delete(visitor);
    }
}
