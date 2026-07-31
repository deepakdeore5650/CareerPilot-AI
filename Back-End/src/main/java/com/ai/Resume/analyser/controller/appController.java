package com.ai.Resume.analyser.controller;


import com.ai.Resume.analyser.model.aiMentorSaveRequest;
import com.ai.Resume.analyser.model.analysisSaveRequest;
import com.ai.Resume.analyser.model.profileUpdateRequest;
import com.ai.Resume.analyser.service.appService;
import org.apache.tika.exception.TikaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("resumeAnalyserCore/service/v1")
public class appController {

    @Autowired
    private appService appServices;

    @PostMapping("/extract")
    public ResponseEntity<?> extract(@RequestParam String roles, @RequestParam MultipartFile file) throws TikaException, IOException, InterruptedException {
        return appServices.extract(roles,file);
    }

    @GetMapping("/lastReport")
    public ResponseEntity<?> lastReport(){
        return appServices.lastReport();
    }

    @GetMapping("/analysis/history")
    public ResponseEntity<?> getAnalysisHistory(){
        return appServices.getAnalysisHistory();
    }

    @PostMapping("/analysis/save")
    public ResponseEntity<?> saveAnalysisHistory(@RequestBody analysisSaveRequest request){
        return appServices.saveAnalysisHistory(request);
    }

    @GetMapping("/analysis/{id}")
    public ResponseEntity<?> getAnalysisById(@PathVariable Long id){
        return appServices.getAnalysisById(id);
    }

    @GetMapping("/career/history")
    public ResponseEntity<?> getAiMentorHistory(){
        return appServices.getAiMentorHistory();
    }

    @GetMapping("/career/{id}")
    public ResponseEntity<?> getAiMentorById(@PathVariable Long id){
        return appServices.getAiMentorById(id);
    }

    @PostMapping("/career/save")
    public ResponseEntity<?> saveAiMentorHistory(@RequestBody aiMentorSaveRequest request){
        return appServices.saveAiMentorHistory(request);
    }

    @GetMapping("/career/suggestions")
    public ResponseEntity<?> getCareerSuggestions(){
        return appServices.generateCareerSuggestions();
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(){
        return appServices.getProfile();
    }

    @GetMapping("/profile/completion")
    public ResponseEntity<?> getProfileCompletion(){
        return appServices.getProfileCompletion();
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody profileUpdateRequest request){
        return appServices.updateProfile(request);
    }

    @PostMapping("/profile/photo")
    public ResponseEntity<?> uploadProfilePhoto(@RequestParam MultipartFile file, HttpServletRequest request) throws IOException {
        return appServices.uploadProfilePhoto(file, request);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(){
        return  appServices.logout();
    }

    @PostMapping("/deleteAccount")
    public ResponseEntity<?> deleteAccount(){
        return  appServices.deleteAccount();
    }

    @PostMapping("/isValid")
    public ResponseEntity<?> tokenValidation(){
        return appServices.tokenValidation();
    }

}
