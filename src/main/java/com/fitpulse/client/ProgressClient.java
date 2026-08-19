package com.fitpulse.client;

import com.fitpulse.model.dto.progress.ProgressCreateRequest;
import com.fitpulse.model.dto.progress.ProgressResponse;
import com.fitpulse.model.dto.progress.ProgressUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "progress-service", url = "${progress.service.url}")
public interface ProgressClient {
    @PostMapping("/api/progress")
    ProgressResponse create(@RequestBody ProgressCreateRequest request);

    @PutMapping("/api/progress/{id}")
    ProgressResponse update(@PathVariable("id") UUID id, @RequestBody ProgressUpdateRequest request);

    @DeleteMapping("/api/progress/{id}")
    void delete(@PathVariable("id") UUID id);

    @GetMapping("/api/progress/user/{userId}")
    List<ProgressResponse> getByUserId(@PathVariable("userId") UUID userId);
}
