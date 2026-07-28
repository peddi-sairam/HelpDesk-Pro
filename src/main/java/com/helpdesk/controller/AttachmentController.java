package com.helpdesk.controller;

import com.helpdesk.dto.ApiResponse;
import com.helpdesk.entity.Attachment;
import com.helpdesk.entity.Ticket;
import com.helpdesk.entity.TicketComment;
import com.helpdesk.repository.AttachmentRepository;
import com.helpdesk.repository.TicketCommentRepository;
import com.helpdesk.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final Path fileStorageLocation;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    public AttachmentController() {
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            
            // Create a default placeholder file if it doesn't exist for seeded tickets
            Path placeholderPath = this.fileStorageLocation.resolve("demo_placeholder.png");
            if (!Files.exists(placeholderPath)) {
                byte[] placeholderPng = new byte[]{
                    (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                    0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
                    (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00,
                    0x00, 0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
                    (byte) 0xAE, 0x42, 0x60, (byte) 0x82
                };
                Files.write(placeholderPath, placeholderPng);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Attachment>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("ticketId") Long ticketId,
            @RequestParam(value = "commentId", required = false) Long commentId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id " + ticketId));

        TicketComment comment = null;
        if (commentId != null) {
            comment = ticketCommentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found with id " + commentId));
        }

        // Normalize file name
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            originalFileName = "unnamed";
        }
        
        // Prevent path traversal
        String cleanedFileName = Paths.get(originalFileName).getFileName().toString();
        // Generate a unique file name to avoid overwrite conflict
        String uniqueFileName = UUID.randomUUID().toString() + "_" + cleanedFileName;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Attachment attachment = Attachment.builder()
                    .ticket(ticket)
                    .comment(comment)
                    .fileName(cleanedFileName)
                    .filePath(uniqueFileName)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();

            Attachment saved = attachmentRepository.save(attachment);
            return ResponseEntity.ok(new ApiResponse<>(true, "File uploaded successfully", saved));

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + cleanedFileName + ". Please try again!", ex);
        }
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<ApiResponse<List<Attachment>>> getAttachmentsForTicket(@PathVariable Long ticketId) {
        List<Attachment> attachments = attachmentRepository.findByTicketId(ticketId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Attachments fetched successfully", attachments));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found with id " + id));

        try {
            Path filePath = this.fileStorageLocation.resolve(attachment.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                String contentType = attachment.getFileType();
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("File not found " + attachment.getFileName());
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + attachment.getFileName(), ex);
        }
    }
}
