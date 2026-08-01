package com.ai.Resume.analyser.service;


import com.ai.Resume.analyser.model.*;
import com.ai.Resume.analyser.repository.aiMentorHistoryRepo;
import com.ai.Resume.analyser.repository.analysisHistoryRepo;
import com.ai.Resume.analyser.repository.prevTable;
import com.ai.Resume.analyser.repository.usersTableRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class appService {

    @Value("${genKey}")
    private String genKey ;

    @Value("${application-id}")
    private String applicationId;

    @Value("${application-api-key}")
    private String applicationApiKey;

    @Value("${uploads.path}")
    private String uploadDirectory;

    @Autowired
    private prevTable previousTableRepo;

    @Autowired
    private analysisHistoryRepo analysisHistoryRepository;

    @Autowired
    private aiMentorHistoryRepo aiMentorHistoryRepository;

    @Autowired
    private usersTableRepo usersTableRepository;

    public ResponseEntity<?> extract(String roles, MultipartFile file) throws TikaException, IOException, InterruptedException {

        Tika tika = new Tika();
        ByteArrayInputStream inpfile = new ByteArrayInputStream(file.getBytes());
        String extracted = tika.parseToString(inpfile);

        String results=null;
        Client client =  Client.builder().apiKey(genKey).build();
        Content content= Content.builder().parts(Part.fromText(extracted), Part.fromText(  "You are now an advanced enterprise-grade ATS resume checker. Your task is to analyze the given resume strictly based on industry-level ATS standards and evaluate it for the specified roles. The evaluation should be moderate to strict (not lenient). A resume should only receive a score between 90 and 100 if it is nearly perfect across all aspects and the content is highly relevant to the specified roles. If any section content is irrelevant to the role, give zero points for that section.\n" +
                "\nBefore analyzing, ensure the roles and resume content match each other and that the resume content is actual content of a real resume (refer: 1. rules and instructions). If it is unrelated, simply treat it as irrelevant content and follow the instructions for irrelevant content. " +
                "Analyze this resume for roles: " + roles + "\n" +
                "Resume Content:\n"  +
                "\n" +
                "Rules and Instructions:\n" +
                "1. Evaluation Categories and Score Allocation (Total 100 points, conditional on role relevance):\n" +
                "- Contact Information (name, email, phone, LinkedIn/GitHub) – 15 points (always scored if present)\n" +
                "- Professional Summary / Objective – 10 points (only score if aligned with role)\n" +
                "- Skills (hard skills, tools, technologies) – 7 points (zero if skills not relevant to role)\n" +
                "- Education (degree, college, graduation year) – 10 points (score only if relevant for role)\n" +
                "- Achievements / Projects (relevant and measurable) – 15 points (zero if not relevant to role)\n" +
                "- Keywords / ATS readiness – 10 points (score only for role-relevant keywords)\n" +
                "- Formatting / Presentation – 5 points (always scored if well formatted)\n" +
                "- No grammatical or spelling mistakes (deduct 5 points if any) – 10 points\n" +
                "- Basic resume evaluation (must meet ATS parsing requirements) – 10 points (score only if structured properly for role content)\n" +
                "- Professional structure and proper layout – 5 points (always scored if proper layout)\n" +
                "- Skills matched with roles – 8 points (zero if skills do not match role)\n" +
                "\n" +
                "2. ATS Optimization Score (0-100):\n" +
                "- Score separately based on ATS parsing readiness, keyword usage, readability, section clarity, lack of graphics/tables, content relevance, and alignment with target role.\n" +
                "- If resume contains irrelevant content for the role, give 0 for the atsoptimizationscore.\n" +
                "\n" +
                "3. Scoring Philosophy:\n" +
                "- Be strict with scoring.\n" +
                "- A resume should only score 90–100 if nearly flawless and fully relevant to the role.\n" +
                "- If any section content is irrelevant to the role, assign zero points for that section.\n" +
                "- 50–89 → Resume is partially relevant but may lack keywords, formatting, or role alignment.\n" +
                "- Below 50 → Resume has significant relevance or ATS issues.\n" +
                "\n" +
                "4. Evaluation Criteria (industrial ATS rules, all relevance-dependent):\n" +
                "- Proper headings: Contact Information, Summary, Skills, Education, Experience, Projects, Achievements.\n" +
                "- Bullet points for readability.\n" +
                "- No images, graphics, or tables that disrupt ATS parsing.\n" +
                "- Chronological or functional structure.\n" +
                "- Action-oriented language in achievements.\n" +
                "- Only include role-relevant keywords; irrelevant keywords give zero points.\n" +
                "- Balanced hard skills (technical) and soft skills relevant to role.\n" +
                "- Professional formatting: consistent fonts, bold section titles, simple layout.\n" +
                "- Concise, measurable content; no long irrelevant descriptions.\n" +
                "- No spelling or grammar mistakes.\n" +
                "- Education and work history clearly structured with dates and relevant to role.\n" +
                "\n" +
                "5. Irrelevant content:\n" +
                "- If the resume is completely irrelevant to the role, return score and atsoptimizationscore as 0, and empty arrays for pros, cons, and suggestions.\n" +
                "\n" +
                "6. Output Format:\n" +
                "Return strict raw JSON only (alphanumeric only, no symbols, no commentary). Response structure:\n" +
                "{\n" +
                "  \"score\": number,\n" +
                "  \"atsoptimizationscore\": number,\n" +
                "  \"pros\": [array of strings](String length <275(chars)),\n" +
                "  \"cons\": [array of strings](String length <275(chars)),\n" +
                "  \"suggestions\": [array of strings](String length <275(chars))\n" +
                "}\n"

        )).build();
        while (true){
            try{
                GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash",content, GenerateContentConfig.builder().temperature(0.0f).build());
                results = response.text();
                break;
            } catch (Exception e) {
                Thread.sleep(1500);
                System.out.println(e);
            }
        }
        if ( results.startsWith("```")) {
            int firstBrace =  results.indexOf("{");
            int lastBrace =  results.lastIndexOf("}");
            if (firstBrace != -1 && lastBrace != -1) {
                results =  results.substring(firstBrace, lastBrace + 1);
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        resultsDto  resultsDto = objectMapper.readValue(results, resultsDto.class);
        if(resultsDto.getScore() !=0){
            String uname=SecurityContextHolder.getContext().getAuthentication().getName();
            previousTable processedData = new previousTable(uname,resultsDto.getScore(),resultsDto.getAtsoptimizationscore(),roles,resultsDto.getPros(),resultsDto.getCons(),resultsDto.getSuggestions());
            previousTableRepo.save(processedData);

            analysisHistory history = new analysisHistory();
            history.setEmail(uname);
            history.setRoles(roles);
            history.setScore(resultsDto.getScore());
            history.setAtsoptimizationscore(resultsDto.getAtsoptimizationscore());
            history.setPros(resultsDto.getPros());
            history.setCons(resultsDto.getCons());
            history.setSuggestions(resultsDto.getSuggestions());
            history.setResumeSummary(extracted != null ? (extracted.length() > 420 ? extracted.substring(0, 420) + "..." : extracted) : "");
            history.setResumeText(extracted);
            history.setFullReport(results);
            analysisHistoryRepository.save(history);

            usersTable usermod = usersTableRepository.findById(uname).orElse(null);
            if(usermod != null){
                usermod.setPreviousResults(true);
                usersTableRepository.save(usermod);
            }
            return  new ResponseEntity<>("Analysed successfully", HttpStatus.OK);
        }

        return  new ResponseEntity<>("Invalid document", HttpStatus.NOT_ACCEPTABLE);


    }

    public ResponseEntity<?> lastReport() {
        previousTable previousTable = previousTableRepo.findById(SecurityContextHolder.getContext().getAuthentication().getName()).orElse(null);
        if(previousTable != null){
            // Job from API
            RestTemplate restTemplate = new RestTemplate();
            List<Job> jobs;
            String url = "https://api.adzuna.com/v1/api/jobs/in/search/1?app_id="+applicationId+"&app_key="+applicationApiKey+"&what="+previousTable.getRoles()+"&where=tamilnadu&content-type=application/json";
            try{
                JobSearchResponse response = restTemplate.getForObject(url, JobSearchResponse.class);
                jobs = response.getResults();
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
                return new ResponseEntity<>("Job Fetch Failed",HttpStatus.NOT_FOUND);
            }
            resultsDto resultsDto = new resultsDto(previousTable.getScore(),previousTable.getAtsoptimizationscore(),previousTable.getPros(),previousTable.getCons(),previousTable.getSuggestions(),jobs);
            return  new ResponseEntity<>(resultsDto,HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>("No previous Analysis",HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getAnalysisHistory() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<analysisHistory> history = analysisHistoryRepository.findAllByEmailOrderByCreatedAtDesc(username);
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    public ResponseEntity<?> saveAnalysisHistory(analysisSaveRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        usersTable user = usersTableRepository.findById(username).orElse(null);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        analysisHistory history = new analysisHistory();
        history.setEmail(username);
        history.setRoles(request.getRoles());
        history.setScore(request.getScore());
        history.setAtsoptimizationscore(request.getAtsoptimizationscore());
        history.setPros(request.getPros());
        history.setCons(request.getCons());
        history.setSuggestions(request.getSuggestions());
        history.setResumeSummary(request.getResumeSummary());
        history.setResumeText(request.getResumeText());
        history.setFullReport(request.getFullReport());
        analysisHistoryRepository.save(history);

        user.setPreviousResults(true);
        usersTableRepository.save(user);
        return new ResponseEntity<>("Analysis saved", HttpStatus.CREATED);
    }

    public ResponseEntity<?> getAnalysisById(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<analysisHistory> maybeHistory = analysisHistoryRepository.findByIdAndEmail(id, username);
        if (maybeHistory.isEmpty()) {
            return new ResponseEntity<>("Analysis not found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(buildAnalysisDetailResponse(maybeHistory.get()), HttpStatus.OK);
    }

    public ResponseEntity<?> saveAiMentorHistory(aiMentorSaveRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        usersTable user = usersTableRepository.findById(username).orElse(null);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        aiMentorHistory report = new aiMentorHistory();
        report.setEmail(username);
        report.setTitle(request.getTitle());
        report.setSummary(request.getSummary());
        report.setRoadmap(request.getRoadmap());
        report.setCareerSuggestions(request.getCareerSuggestions());
        report.setProjectIdeas(request.getProjectIdeas());
        report.setSkillsToLearn(request.getSkillsToLearn());
        report.setImprovementAreas(request.getImprovementAreas());
        report.setRecommendedTechnologies(request.getRecommendedTechnologies());
        report.setInterviewPrep(request.getInterviewPrep());
        report.setMissingSkills(request.getMissingSkills());
        report.setResumeTips(request.getResumeTips());
        report.setFullReport(request.getFullReport());
        aiMentorHistoryRepository.save(report);
        return new ResponseEntity<>("AI mentor history saved", HttpStatus.CREATED);
    }

    public ResponseEntity<?> getAiMentorHistory() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<aiMentorHistory> history = aiMentorHistoryRepository.findAllByEmailOrderByCreatedAtDesc(username);
        List<aiMentorHistoryResponse> response = history.stream()
                .map(this::buildAiMentorHistoryResponse)
                .collect(Collectors.toList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getAiMentorById(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<aiMentorHistory> maybe = aiMentorHistoryRepository.findByIdAndEmail(id, username);
        if (maybe.isEmpty()) {
            return new ResponseEntity<>("AI mentor report not found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(buildAiMentorHistoryResponse(maybe.get()), HttpStatus.OK);
    }

    private aiMentorHistoryResponse buildAiMentorHistoryResponse(aiMentorHistory history) {
        aiMentorHistoryResponse response = new aiMentorHistoryResponse();
        response.setId(history.getId());
        response.setTitle(history.getTitle());
        response.setSummary(history.getSummary());
        response.setRoadmap(history.getRoadmap());
        response.setCareerSuggestions(history.getCareerSuggestions());
        response.setProjectIdeas(history.getProjectIdeas());
        response.setSkillsToLearn(history.getSkillsToLearn());
        response.setImprovementAreas(history.getImprovementAreas());
        response.setRecommendedTechnologies(history.getRecommendedTechnologies());
        response.setInterviewPrep(history.getInterviewPrep());
        response.setMissingSkills(history.getMissingSkills());
        response.setResumeTips(history.getResumeTips());
        response.setFullReport(history.getFullReport());
        response.setCreatedAt(history.getCreatedAt());
        return response;
    }

    private analysisHistoryDetailResponse buildAnalysisDetailResponse(analysisHistory item) {
        analysisHistoryDetailResponse response = new analysisHistoryDetailResponse();
        response.setId(item.getId());
        response.setRoles(item.getRoles());
        response.setScore(item.getScore());
        response.setAtsoptimizationscore(item.getAtsoptimizationscore());
        response.setPros(item.getPros());
        response.setCons(item.getCons());
        response.setSuggestions(item.getSuggestions());
        response.setResumeSummary(item.getResumeSummary());
        response.setResumeText(item.getResumeText());
        response.setFullReport(item.getFullReport());
        response.setCreatedAt(item.getCreatedAt());
        return response;
    }

    public ResponseEntity<?> getProfileCompletion() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        usersTable user = usersTableRepository.findById(username).orElse(null);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        Map<String, Integer> response = new HashMap<>();
        response.put("profileCompletion", calculateProfileCompletion(user));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> generateCareerSuggestions() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        usersTable user = usersTableRepository.findById(username).orElse(null);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        previousTable previousTable = previousTableRepo.findById(username).orElse(null);
        List<analysisHistory> history = analysisHistoryRepository.findAllByEmailOrderByCreatedAtDesc(username).stream().limit(3).collect(Collectors.toList());

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI career mentor for a software professional. Use the user profile data, career goals, skills, experience, resume analysis history, ATS score, and any previous recommendations to generate actionable career guidance. Respond with valid JSON only. Do not include any explanation outside the JSON object.\n");
        prompt.append("User profile:\n");
        prompt.append("- Name: ").append(user.getUsername() == null ? "N/A" : user.getUsername()).append("\n");
        prompt.append("- Degree: ").append(user.getDegree() == null ? "N/A" : user.getDegree()).append("\n");
        prompt.append("- College: ").append(user.getCollege() == null ? "N/A" : user.getCollege()).append("\n");
        prompt.append("- Career Goal: ").append(user.getCareerGoal() == null ? "N/A" : user.getCareerGoal()).append("\n");
        prompt.append("- Skills: ").append(user.getSkills() == null ? "N/A" : user.getSkills()).append("\n");
        prompt.append("- Experience: ").append(user.getExperience() == null ? "N/A" : user.getExperience()).append("\n");
        prompt.append("- Bio: ").append(user.getBio() == null ? "N/A" : user.getBio()).append("\n");
        prompt.append("- LinkedIn: ").append(user.getLinkedin() == null ? "N/A" : user.getLinkedin()).append("\n");
        prompt.append("- GitHub: ").append(user.getGithub() == null ? "N/A" : user.getGithub()).append("\n");
        prompt.append("- Profile completion: ").append(calculateProfileCompletion(user)).append("%\n");
        if (calculateProfileCompletion(user) < 100) {
            prompt.append("Note: The user has not completed their profile. Encourage profile completion to improve the quality of suggestions.\n");
        }

        if (previousTable != null) {
            prompt.append("Last resume analysis:\n");
            prompt.append("- ATS score: ").append(previousTable.getAtsoptimizationscore()).append("\n");
            prompt.append("- Overall score: ").append(previousTable.getScore()).append("\n");
            prompt.append("- Strengths: ").append(String.join(", ", previousTable.getPros() == null ? List.of() : previousTable.getPros())).append("\n");
            prompt.append("- Improvement areas: ").append(String.join(", ", previousTable.getCons() == null ? List.of() : previousTable.getCons())).append("\n");
            prompt.append("- Current resume suggestions: ").append(String.join(", ", previousTable.getSuggestions() == null ? List.of() : previousTable.getSuggestions())).append("\n");
        }

        if (!history.isEmpty()) {
            prompt.append("Analysis history summary:\n");
            for (int i = 0; i < history.size(); i++) {
                analysisHistory item = history.get(i);
                prompt.append("History ").append(i + 1).append(": roles=").append(item.getRoles()).append(", score=").append(item.getScore()).append(", ats=").append(item.getAtsoptimizationscore()).append("\n");
            }
        }

        prompt.append("\nInstructions:\n");
        prompt.append("- Provide career development suggestions, what projects to build next, what skills to learn next, a personalized roadmap, interview preparation guidance, missing skills, improvement areas, recommended technologies, portfolio improvement tips, and resume improvement suggestions.\n");
        prompt.append("- Use bullet-list arrays for suggestions and keep each item concise.\n");
        prompt.append("- Favor real guidance such as 'Learn Docker and AWS next', 'Build a microservices project', 'Improve React frontend architecture', 'Add authentication projects', 'Focus on backend optimization', 'Practice DSA for product companies'.\n");
        prompt.append("- Return only JSON with the following keys: careerSuggestions, projectIdeas, skillsToLearn, roadmap, interviewPrep, missingSkills, improvementAreas, recommendedTechnologies, resumeTips.\n");

        Client client = Client.builder().apiKey(genKey).build();
        Content content = Content.builder().parts(Part.fromText(prompt.toString())).build();
        String results = null;
        while (true) {
            try {
                GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", content, GenerateContentConfig.builder().temperature(0.45f).build());
                results = response.text();
                break;
            } catch (Exception e) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ignored) {
                }
            }
        }

        if (results != null && results.startsWith("```")) {
            int firstBrace = results.indexOf("{");
            int lastBrace = results.lastIndexOf("}");
            if (firstBrace != -1 && lastBrace != -1) {
                results = results.substring(firstBrace, lastBrace + 1);
            }
        }

        try {
            Map<String, Object> parsed = new ObjectMapper().readValue(results, Map.class);
            if (parsed == null) {
                return new ResponseEntity<>(Map.of("message", "Failed to parse AI response"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            aiMentorHistory report = new aiMentorHistory();
            report.setEmail(SecurityContextHolder.getContext().getAuthentication().getName());
            report.setTitle("AI Mentor guidance session");
            report.setSummary(buildMentorSummary(parsed));
            report.setRoadmap(asString(parsed.get("roadmap")));
            report.setCareerSuggestions(asList(parsed.get("careerSuggestions")));
            report.setProjectIdeas(asList(parsed.get("projectIdeas")));
            report.setSkillsToLearn(asList(parsed.get("skillsToLearn")));
            report.setImprovementAreas(asList(parsed.get("improvementAreas")));
            report.setRecommendedTechnologies(asList(parsed.get("recommendedTechnologies")));
            report.setInterviewPrep(asList(parsed.get("interviewPrep")));
            report.setMissingSkills(asList(parsed.get("missingSkills")));
            report.setResumeTips(asList(parsed.get("resumeTips")));
            report.setFullReport(results);
            aiMentorHistoryRepository.save(report);
            parsed.put("reportId", report.getId());
            parsed.put("profileCompletion", calculateProfileCompletion(user));
            parsed.put("atsScore", previousTable == null ? 0 : previousTable.getAtsoptimizationscore());
            return new ResponseEntity<>(parsed, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("careerSuggestions", List.of(results != null ? results : "Unable to generate AI suggestions at this time."));
            fallback.put("profileCompletion", calculateProfileCompletion(user));
            fallback.put("atsScore", previousTable == null ? 0 : previousTable.getAtsoptimizationscore());
            return new ResponseEntity<>(fallback, HttpStatus.OK);
        }
    }

    public ResponseEntity<?> logout() {
        HttpHeaders headers = new HttpHeaders();
        ResponseCookie cookie = ResponseCookie.from("entrypasstoken", "").httpOnly(true).secure(true).sameSite("None").maxAge(0).path("/").build();
        // ResponseCookie cookie = ResponseCookie.from("entrypasstoken","").httpOnly(true).secure(false).sameSite("Strict").maxAge(0).path("/").build();
        headers.add(HttpHeaders.SET_COOKIE,cookie.toString());
        return new ResponseEntity<>("Successfully loggedOut",headers,HttpStatus.OK);
    }
    public ResponseEntity<?> getProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        usersTable user = usersTableRepository.findById(username).orElse(null);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(buildProfileResponse(user), HttpStatus.OK);
    }

    public ResponseEntity<?> updateProfile(profileUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        usersTable user = usersTableRepository.findById(username).orElse(null);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getCollege() != null) user.setCollege(request.getCollege());
        if (request.getDegree() != null) user.setDegree(request.getDegree());
        if (request.getSkills() != null) user.setSkills(request.getSkills());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getLinkedin() != null) user.setLinkedin(request.getLinkedin());
        if (request.getGithub() != null) user.setGithub(request.getGithub());
        if (request.getCareerGoal() != null) user.setCareerGoal(request.getCareerGoal());

        usersTableRepository.save(user);
        return new ResponseEntity<>(buildProfileResponse(user), HttpStatus.OK);
    }

    public ResponseEntity<?> uploadProfilePhoto(MultipartFile file, HttpServletRequest request) throws IOException {
        if (file == null || file.isEmpty()) {
            return new ResponseEntity<>("No file provided", HttpStatus.BAD_REQUEST);
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        usersTable user = usersTableRepository.findById(username).orElse(null);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        Path uploadDir = Paths.get(uploadDirectory);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        String filename = username.replaceAll("[^a-zA-Z0-9._-]", "_") + "_" + System.currentTimeMillis() + extension;
        Path targetPath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String baseUrl = request.getScheme() + "://" + request.getServerName();
        if (request.getServerPort() != 80 && request.getServerPort() != 443) {
            baseUrl += ":" + request.getServerPort();
        }
        String photoUrl = baseUrl + "/uploads/" + filename;
        user.setProfilePhoto(photoUrl);
        usersTableRepository.save(user);

        return new ResponseEntity<>(buildProfileResponse(user), HttpStatus.OK);
    }

    private profileResponse buildProfileResponse(usersTable user) {
        profileResponse response = new profileResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setCollege(user.getCollege());
        response.setDegree(user.getDegree());
        response.setSkills(user.getSkills());
        response.setBio(user.getBio());
        response.setLinkedin(user.getLinkedin());
        response.setGithub(user.getGithub());
        response.setCareerGoal(user.getCareerGoal());
        response.setExperience(user.getExperience());
        response.setProfilePhoto(user.getProfilePhoto());
        response.setProfileCompletion(calculateProfileCompletion(user));
        return response;
    }

    private int calculateProfileCompletion(usersTable user) {
        int completed = 0;
        if (user.getUsername() != null && !user.getUsername().isBlank()) completed++;
        if (user.getPhone() != null && !user.getPhone().isBlank()) completed++;
        if (user.getCollege() != null && !user.getCollege().isBlank()) completed++;
        if (user.getDegree() != null && !user.getDegree().isBlank()) completed++;
        if (user.getSkills() != null && !user.getSkills().isBlank()) completed++;
        if (user.getBio() != null && !user.getBio().isBlank()) completed++;
        if (user.getLinkedin() != null && !user.getLinkedin().isBlank()) completed++;
        if (user.getGithub() != null && !user.getGithub().isBlank()) completed++;
        if (user.getCareerGoal() != null && !user.getCareerGoal().isBlank()) completed++;
        if (user.getExperience() != null && !user.getExperience().isBlank()) completed++;
        if (user.getProfilePhoto() != null && !user.getProfilePhoto().isBlank()) completed++;
        return Math.round((completed * 100f) / 11f);
    }

    private String asString(Object value) {
        return value == null ? "" : value.toString();
    }

    private List<String> asList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.toList());
        }
        if (value instanceof String text) {
            return List.of(text);
        }
        return List.of();
    }

    private String buildMentorSummary(Map<String, Object> parsed) {
        var pieces = asList(parsed.get("careerSuggestions"));
        if (!pieces.isEmpty()) {
            return pieces.stream().limit(3).collect(Collectors.joining(" "));
        }
        return asString(parsed.get("summary"));
    }

    public ResponseEntity<?> deleteAccount() {

        try{
            String uname=SecurityContextHolder.getContext().getAuthentication().getName();
            usersTableRepository.deleteById(uname);
            previousTableRepo.deleteById(uname);
            HttpHeaders headers = new HttpHeaders();
            ResponseCookie cookie = ResponseCookie.from("entrypasstoken", "").httpOnly(true).secure(true).sameSite("None").maxAge(0).path("/").build();
            // ResponseCookie cookie = ResponseCookie.from("entrypasstoken","").httpOnly(true).secure(false).sameSite("Strict").maxAge(0).path("/").build();
            headers.add(HttpHeaders.SET_COOKIE,cookie.toString());
            return new ResponseEntity<>("Account deleted successfully",headers,HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete",HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> tokenValidation() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        usersTable user = usersTableRepository.findById(name).orElse(null);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        loginResponse loginRes=new loginResponse(user.getUsername(), user.getPreviousResults(), user.getProfilePhoto());
        return new ResponseEntity<>(loginRes,HttpStatus.OK);
    }
}
