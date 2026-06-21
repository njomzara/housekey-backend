package com.housekey.media.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.housekey.auth.domain.AuthenticatedUser;
import com.housekey.media.api.MediaDtos.MediaOrderUpdateRequest;
import com.housekey.media.api.MediaDtos.PropertyMediaResponse;
import com.housekey.media.application.PropertyMediaService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/properties/{propertyId}/media")
public class PropertyMediaController {

    private final PropertyMediaService mediaService;

    public PropertyMediaController(PropertyMediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping(path = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload one or more gallery images for a property")
    public List<PropertyMediaResponse> uploadImages(
            @PathVariable Long propertyId,
            @RequestParam(name = "files", required = false) List<MultipartFile> files,
            @RequestParam(name = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return mediaService.uploadGalleryImages(propertyId, combine(files, file), principal);
    }

    @PostMapping(path = "/floor-plans/{planId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload a floor-plan image for a property")
    public PropertyMediaResponse uploadFloorPlanImage(
            @PathVariable Long propertyId,
            @PathVariable Integer planId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return mediaService.uploadFloorPlanImage(propertyId, planId, file, principal);
    }

    @GetMapping
    @Operation(summary = "Get editable media metadata for a property")
    public List<PropertyMediaResponse> getMedia(
            @PathVariable Long propertyId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return mediaService.getPropertyMedia(propertyId, principal);
    }

    @PutMapping("/order")
    @Operation(summary = "Update property gallery media order")
    public List<PropertyMediaResponse> updateOrder(
            @PathVariable Long propertyId,
            @RequestBody MediaOrderUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return mediaService.updateGalleryOrder(propertyId, request, principal);
    }

    @PatchMapping("/{mediaId}")
    @Operation(summary = "Update media alt text or sort metadata")
    public PropertyMediaResponse updateMetadata(
            @PathVariable Long propertyId,
            @PathVariable Long mediaId,
            @RequestBody Map<String, Object> patch,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return mediaService.updateMetadata(propertyId, mediaId, patch, principal);
    }

    @DeleteMapping("/{mediaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft delete property media")
    public void deleteMedia(
            @PathVariable Long propertyId,
            @PathVariable Long mediaId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        mediaService.deleteMedia(propertyId, mediaId, principal);
    }

    private List<MultipartFile> combine(List<MultipartFile> files, MultipartFile file) {
        List<MultipartFile> combined = new ArrayList<>();
        if (files != null) {
            combined.addAll(files);
        }
        if (file != null) {
            combined.add(file);
        }
        return combined;
    }
}
