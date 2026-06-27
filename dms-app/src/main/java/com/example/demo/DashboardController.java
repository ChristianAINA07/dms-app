package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private DocumentVersionRepository versionRepository;

    @Autowired
    private ActivityLogRepository logRepository;

    @Autowired
    private UserRepository userRepository;

    private final String UPLOAD_DIR = "C:/dms_enterprise_storage/";

    /* Endpoint pour l'authentification des trois types d'utilisateurs */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        
        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email) && u.getPassword().equals(password))
                .findFirst();

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("userId", user.getId());
            response.put("username", user.getName());
            response.put("role", user.getRole().getName()); /* Renvoie ADMIN, MANAGER, ou EMPLOYE */
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("status", "ERROR", "message", "Identifiants invalides"));
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardData() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalDocuments", documentRepository.count());
        response.put("totalFolders", folderRepository.count());
        
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        response.put("documentsToday", versionRepository.countByUploadedAtAfter(startOfToday));
        
        long totalSizeBytes = versionRepository.findAll().stream().mapToLong(DocumentVersion::getFileSize).sum();
        response.put("storageUsed", String.format("%.2f Mo", totalSizeBytes / (1024.0 * 1024.0)));
        response.put("folders", folderRepository.findAll());
        response.put("recentLogs", logRepository.findByOrderByTimestampDesc());
        return response;
    }

    @GetMapping("/documents")
    public List<Map<String, Object>> getAllDocuments() {
        List<Document> documents = documentRepository.findAll();
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        for (Document doc : documents) {
            Map<String, Object> docMap = new HashMap<>();
            docMap.put("id", doc.getId());
            docMap.put("title", doc.getTitle());
            docMap.put("folderName", doc.getFolder().getName());
            docMap.put("createdAt", doc.getCreatedAt().toString());

            List<DocumentVersion> versions = versionRepository.findAll().stream()
                .filter(v -> v.getDocument().getId().equals(doc.getId()))
                .sorted((v1, v2) -> v2.getVersionNumber().compareTo(v1.getVersionNumber()))
                .toList();

            if (!versions.isEmpty()) {
                DocumentVersion latest = versions.get(0);
                docMap.put("version", "V" + latest.getVersionNumber());
                docMap.put("size", String.format("%.2f Ko", latest.getFileSize() / 1024.0));
                docMap.put("latestVersionId", latest.getId());
            }
            result.add(docMap);
        }
        return result;
    }

    @PostMapping("/folders")
    public Folder createFolder(@RequestBody Folder folder) {
        folder.setName(folder.getName().trim());
        Folder savedFolder = folderRepository.save(folder);
        ActivityLog log = new ActivityLog();
        log.setAction("CREATION");
        log.setDetails("Création de la structure de classification : " + savedFolder.getName());
        User systemUser = new User(); systemUser.setId(1L); log.setUser(systemUser);
        logRepository.save(log);
        return savedFolder;
    }

    @PostMapping("/documents/upload")
    public Map<String, Object> uploadDocument(@RequestParam("file") MultipartFile file,
                                              @RequestParam("title") String title,
                                              @RequestParam("folderId") Long folderId,
                                              @RequestParam("userId") Long userId) throws IOException {
        
        Map<String, Object> response = new HashMap<>();
        String cleanTitle = title.trim();
        
        List<Document> allDocs = documentRepository.findAll();
        Document document = null;

        for (Document d : allDocs) {
            if (d.getTitle().trim().equalsIgnoreCase(cleanTitle) && d.getFolder().getId().equals(folderId)) {
                document = d;
                break;
            }
        }

        int nextVersionNumber = 1;
        String actionLog = "UPLOAD";
        String logDetails = "";

        if (document != null) {
            final Long docId = document.getId();
            nextVersionNumber = (int) versionRepository.findAll().stream().filter(v -> v.getDocument().getId().equals(docId)).count() + 1;
            actionLog = "VERSION_V" + nextVersionNumber;
            logDetails = "Nouvelle version " + nextVersionNumber + " ajoutée pour le document '" + document.getTitle() + "'";
        } else {
            document = new Document();
            document.setTitle(cleanTitle);
            document.setFolder(folderRepository.findById(folderId).orElseThrow());
            documentRepository.save(document);
            logDetails = "Importation du document '" + cleanTitle + "' (V1)";
        }

        String finalPath = UPLOAD_DIR + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        file.transferTo(new File(finalPath));

        DocumentVersion version = new DocumentVersion();
        version.setDocument(document);
        version.setVersionNumber(nextVersionNumber);
        version.setFilePath(finalPath);
        version.setMimeType(file.getContentType());
        version.setFileSize(file.getSize());
        
        User operator = userRepository.findById(userId).orElseThrow();
        version.setUploadedBy(operator);
        versionRepository.save(version);

        ActivityLog log = new ActivityLog();
        log.setAction(actionLog);
        log.setDetails(logDetails + " par " + operator.getName());
        log.setUser(operator);
        logRepository.save(log);

        response.put("status", "SUCCESS");
        return response;
    }

    /* NOUVELLE FONCTIONNALITÉ : ENDPOINT DE SUPPRESSION STRICTE (ACCÈS ADMIN) */
    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Map<String, Object>> deleteDocument(@PathVariable Long id, @RequestParam Long userId) {
        Document doc = documentRepository.findById(id).orElseThrow();
        User operator = userRepository.findById(userId).orElseThrow();
        
        /* Sécurité backend subsidiaire au cas où le frontend est contourné */
        if (!operator.getRole().getName().equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("status", "FORBIDDEN"));
        }

        /* Inscription de la suppression dans le registre d'audit avant effacement */
        ActivityLog log = new ActivityLog();
        log.setAction("SUPPRESSION");
        log.setDetails("Suppression définitive du document '" + doc.getTitle() + "' par " + operator.getName());
        log.setUser(operator);
        logRepository.save(log);

        documentRepository.delete(doc);
        return ResponseEntity.ok(Map.of("status", "SUCCESS"));
    }

    @GetMapping("/documents/download/{versionId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long versionId) {
        try {
            DocumentVersion version = versionRepository.findById(versionId).orElseThrow();
            Path path = Paths.get(version.getFilePath());
            Resource resource = new UrlResource(path.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(version.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
