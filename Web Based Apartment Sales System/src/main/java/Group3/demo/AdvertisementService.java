package com.group3.demo.service;

import com.group3.demo.dto.AdvertisementDTO;
import com.group3.demo.entity.Advertisement;
import com.group3.demo.repository.AdvertisementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdvertisementService {
    @Autowired
    private AdvertisementRepository repository;

    // Create campaign
    public AdvertisementDTO create(AdvertisementDTO dto, String createdBy) {
        Advertisement ad = mapToEntity(dto);
        ad.setCreatedBy(createdBy);  // Assume passed from logged-in user
        ad.setCreatedAt(LocalDateTime.now());
        ad.setUpdatedAt(LocalDateTime.now());
        return mapToDTO(repository.save(ad));
    }

    // Update campaign
    public AdvertisementDTO update(Long id, AdvertisementDTO dto) {
        Optional<Advertisement> optionalAd = repository.findById(id);
        if (optionalAd.isPresent()) {
            Advertisement ad = optionalAd.get();
            ad.setTitle(dto.getTitle());
            ad.setDescription(dto.getDescription());
            ad.setBannerImage(dto.getBannerImage());
            ad.setStartDate(dto.getStartDate());
            ad.setEndDate(dto.getEndDate());
            ad.setUpdatedAt(LocalDateTime.now());
            return mapToDTO(repository.save(ad));
        }
        return null;  // Or throw exception
    }

    // Delete campaign
    public void delete(Long id) {
        repository.deleteById(id);
    }

    // Get all for marketing exec
    public List<AdvertisementDTO> getAll() {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Get ongoing for customers
    public List<AdvertisementDTO> getOngoing() {
        LocalDate today = LocalDate.now();
        return repository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Increment view/click (call from controller)
    public void incrementView(Long id) {
        repository.findById(id).ifPresent(ad -> {
            ad.setViewCount(ad.getViewCount() + 1);
            repository.save(ad);
        });
    }

    public void incrementClick(Long id) {
        repository.findById(id).ifPresent(ad -> {
            ad.setClickCount(ad.getClickCount() + 1);
            repository.save(ad);
        });
    }

    // Mapping methods
    private Advertisement mapToEntity(AdvertisementDTO dto) {
        Advertisement ad = new Advertisement();
        ad.setId(dto.getId());
        ad.setTitle(dto.getTitle());
        ad.setDescription(dto.getDescription());
        ad.setBannerImage(dto.getBannerImage());
        ad.setStartDate(dto.getStartDate());
        ad.setEndDate(dto.getEndDate());
        ad.setCreatedBy(dto.getCreatedBy());
        ad.setClickCount(dto.getClickCount());
        ad.setViewCount(dto.getViewCount());
        return ad;
    }

    private AdvertisementDTO mapToDTO(Advertisement ad) {
        AdvertisementDTO dto = new AdvertisementDTO();
        dto.setId(ad.getId());
        dto.setTitle(ad.getTitle());
        dto.setDescription(ad.getDescription());
        dto.setBannerImage(ad.getBannerImage());
        dto.setStartDate(ad.getStartDate());
        dto.setEndDate(ad.getEndDate());
        dto.setCreatedBy(ad.getCreatedBy());
        dto.setClickCount(ad.getClickCount());
        dto.setViewCount(ad.getViewCount());
        return dto;
    }
}