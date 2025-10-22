package Group3.demo.Controller;

import Group3.demo.Entity.Document;
import Group3.demo.Entity.enums.DocumentType;
import Group3.demo.Repository.DocumentRepository;
import Group3.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentRepository documentRepo;
    private final UserRepository userRepo;

    @PostMapping("/order/{orderId}/upload")
    public String uploadOrderDoc(@PathVariable("orderId") Integer orderId,
                                 @RequestParam("file") MultipartFile file,
                                 @RequestParam(name = "type", required = false) DocumentType docType,
                                 Authentication auth) throws IOException {
        String fileName = "order_" + orderId + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get("uploads/orders");
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, file.getBytes());

        var doc = new Group3.demo.Entity.Document();
        doc.setOwnerType(Group3.demo.Entity.enums.DocumentOwner.SALES_ORDER);
        doc.setOwnerId(orderId);
        doc.setDocType(docType != null ? docType : DocumentType.OTHER);
        doc.setFileName(fileName);
        doc.setFileUrl("/uploads/orders/" + fileName);
        if (auth != null) {
            userRepo.findByUsername(auth.getName()).ifPresent(doc::setUploadedBy);
        }
        doc.setUploadedAt(LocalDateTime.now());
        documentRepo.save(doc);

        return "redirect:/sales/orders";
    }

    @PostMapping("/invoice/{invoiceId}/upload")
    public String uploadInvoiceDoc(@PathVariable("invoiceId") Integer invoiceId,
                                   @RequestParam("file") MultipartFile file,
                                   @RequestParam(name = "type", required = false) DocumentType docType,
                                   Authentication auth) throws IOException {
        String fileName = "invoice_" + invoiceId + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get("uploads/invoices");
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, file.getBytes());

        var doc = new Group3.demo.Entity.Document();
        doc.setOwnerType(Group3.demo.Entity.enums.DocumentOwner.INVOICE);
        doc.setOwnerId(invoiceId);
        doc.setDocType(docType != null ? docType : DocumentType.OTHER);
        doc.setFileName(fileName);
        doc.setFileUrl("/uploads/invoices/" + fileName);
        if (auth != null) {
            userRepo.findByUsername(auth.getName()).ifPresent(doc::setUploadedBy);
        }
        doc.setUploadedAt(LocalDateTime.now());
        documentRepo.save(doc);

        return "redirect:/finance/invoices";
    }
}
